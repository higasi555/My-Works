import cv2
from ultralytics import YOLO
import imutils
import numpy as np
from sort_v2 import Sort
import pandas as pd
import glob

# Requires related to YOLOv8
# cuda12.1
# cudnn8.9.7
# torch==2.3.1 torchvision==0.18.1 torchaudio==2.3.1

# Requires related to SORT
# need to set sort_v2.py on the same directory as this project root

# The function of merging rectangles based on the distance between two objs
# input: list of rects
# output: list of rects
def merge_rectangles(rects, threshold = 50):
    # if there are no rectangles, return empty list
    if not rects:
        return []

    # sort rectangles based on x coordinate
    rects.sort(key=lambda x: x[0])
    merged = [rects[0]]

    for current in rects[1:]:
        previous = merged[-1]
        # calculate distance
        distance = np.sqrt((current[0] - previous[0])**2 + (current[1] - previous[1])**2)
        if distance <= threshold:
            # merging
            new_x = min(previous[0], current[0])
            new_y = min(previous[1], current[1])
            new_w = max(previous[0] + previous[2], current[0] + current[2]) - new_x
            new_h = max(previous[1] + previous[3], current[1] + current[3]) - new_y
            merged[-1] = (new_x, new_y, new_w, new_h)
        else:
            merged.append(current)

    return merged

# The function of getting contour from mask
# input: mask
# output: list of contours
def get_contour(input_mask):
    # get contours
    contours = cv2.findContours(input_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)[0]

    # expand the contours
    expanded_contours = []
    for contour in contours:
        # exchange to float32
        contour_f = contour.astype(np.float32)
        center = contour_f.mean(axis=0)
        expanded_contour = (contour_f - center) * 1.1 + center
        # re-exchange to int32
        expanded_contours.append(expanded_contour.astype(np.int32))

    return expanded_contours


#### Start ####
# ↓ init
# ↓ YOLOv8 segmentation
# ↓ Frame smoothing
# ↓ MOG2 D-Frame
# ↓ Process contours
# ↓ Process bounding boxes
# ↓ Judge whether the box is inside the segmentation or not
# ↓ Process bounding boxes
# ↓ Draw bounding boxes
#### End ####

def main(input_path):
    cap = cv2.VideoCapture(input_path)

    wait_secs = int(1000 / cap.get(cv2.CAP_PROP_FPS))

    # init models
    # D-frame model
    model = cv2.createBackgroundSubtractorMOG2(history = 5, varThreshold = 16, detectShadows = False) # shadowはfalseの方が良さそう？
    # YOLOv8
    yolo = YOLO("yolov8x-seg.pt")
    # yolo = YOLO("tire_v1.pt")
    # init SORT tracker
    tracker_sort = Sort()

    # init var
    prev_bboxes_count = 0

    # start reading frame
    ret, frame = cap.read()

    # avg_frame is a smoothed frame
    avg_frame = np.float32(frame)
    # トラックIDごとの過去の中心座標を保存する辞書
    track_centers = {}

    # 前のフレームのセグメンテーションを保存する配列
    previous_masks = []
    # ゴミのある可能性のもののトラック情報を保存する配列（後でCSVへ書き出す用）
    poss_trash = []
    # 移動物体のBBOX中心座標を保存するリスト（各フレームごと）
    track_centers_history = {}
    vehicle_centers_history = {}

    # ゴミと判定されたトラックのデータを保存するデータフレーム
    trash_tracks_df = pd.DataFrame()

    frame_num = -1
    # 前のフレームと重複していないかの確認用
    prev_frame = None
    prev_trash_tracks = None

    # process of every 1 frame
    while True:
        ret, frame = cap.read()
        if not ret:
            break

        frame_num = frame_num + 1

        if prev_frame is None:
            prev_frame = frame.copy()
        else:
            # フレーム間の差分を計算
            diff_frame = cv2.absdiff(prev_frame, frame)
            # 差分をグレースケールに変換
            gray_diff = cv2.cvtColor(diff_frame, cv2.COLOR_BGR2GRAY)
            # 差分の平均値を計算
            mean_diff = np.mean(gray_diff)
            # 差分の平均値が閾値以下なら、同一フレームとみなす
            threshold = 0.1  # この値は調整が必要かも
            if mean_diff < threshold:
                print(f"フレーム {frame_num} は前のフレームとほぼ同じです。処理をスキップします。")
                # prev_frameを更新せずに続行（データを保持）
                # 前のフレームのデータが存在する場合、それを現在のフレーム番号で追加
                if prev_trash_tracks is not None:
                    for data in prev_trash_tracks:
                        new_data = data.copy()
                        new_data['frame_num'] = frame_num
                        poss_trash.append(new_data)
                continue
            else:
                # prev_frameを更新
                prev_frame = frame.copy()

        # Detection of YOLOv8
        results = yolo.track(frame, persist=True)
        annotated_frame = results[0].plot()

        # 現在のフレームのセグメンテーションマスクを抽出
        current_masks = []
        for result in results:
            if result.masks is not None:
                for mask_data in result.masks:
                    mask_contours = mask_data.xy[0]
                    current_masks.append(mask_contours)

        # 前のフレームのマスクリストを更新（最大2フレーム分保持）
        previous_masks.append(current_masks)
        if len(previous_masks) > 10:
            previous_masks.pop(0)

        # 車両のトラッキング結果を処理
        for result in results:
            boxes = result.boxes
            ids = result.boxes.id  # トラッキングIDを取得
            for i, box in enumerate(boxes):
                cls_id = int(box.cls[0])
                if cls_id == 2 or cls_id == 7:  # 2: car, 7: truck
                    x1, y1, x2, y2 = box.xyxy[0].cpu().numpy()
                    track_id = int(ids[i]) if ids is not None else None
                    if track_id is not None:
                        center_x = (x1 + x2) / 2
                        center_y = (y1 + y2) / 2

                        # vehicle_centers_historyにtrack_idが存在しない場合、新しくリストを作成
                        if track_id not in vehicle_centers_history:
                            vehicle_centers_history[track_id] = []

                        # 車両が移動しているかどうかを判定
                        if len(vehicle_centers_history[track_id]) >= 1:
                            _, prev_center_x, prev_center_y, _ = vehicle_centers_history[track_id][-1]
                            dist = np.sqrt((center_x - prev_center_x) ** 2 + (center_y - prev_center_y) ** 2)
                            moving_flag = dist > 2.0
                        else:
                            moving_flag = False  # この車両の初回フレーム

                        vehicle_centers_history[track_id].append((frame_num, center_x, center_y, moving_flag))

        # Temporal Pixel Smoothing
        cv2.accumulateWeighted(frame, avg_frame, alpha=0.4)
        # get smoothed frame
        frame = cv2.convertScaleAbs(avg_frame)

        # BGRからGRAYへ変換
        newcolor_frame = frame.copy()
        newcolor_frame = cv2.cvtColor(newcolor_frame, cv2.COLOR_BGR2GRAY)
        frame = newcolor_frame

        # get D-Frame
        mask = model.apply(frame)
        # 時間テロップの誤検出防止
        # フレームの高さと幅を取得
        height, width = mask.shape[:2]
        # 上部15%の高さを計算
        top_height = int(height * 0.15)
        # マスクの上部15%を0に設定
        mask[0:top_height, :] = 0

        # get contours
        contours = get_contour(mask)
        # draw contours
        cv2.drawContours(frame, contours, -1, (0, 255, 0), 2)

        # remove small contours
        contours = list(filter(lambda x: cv2.contourArea(x) > 27, contours))

        # draw contours
        cv2.drawContours(frame, contours, -1, (255, 0, 0), 2)

        # get bounding boxes
        bboxes = list(map(lambda x: cv2.boundingRect(x), contours))

        # merging rectangles 6 times
        bboxes_merged = merge_rectangles(bboxes)
        for i in range(5):
            bboxes_merged = merge_rectangles(bboxes_merged)

        bboxes_merged_processed = []


        # When the number of rectangles increased suddenly,
        # skip the process as there are noise
        current_bboxes_count = len(bboxes_merged)
        if current_bboxes_count == 0:
            current_bboxes_count = 1
        if current_bboxes_count > prev_bboxes_count * 4.0:
            print("Possible noise detected. Skipping this frame.")
            prev_bboxes_count = current_bboxes_count
            continue
        prev_bboxes_count = current_bboxes_count


        ary_det_originIdx = []

        # draw bounding boxes
        for idx, (x, y, w, h) in enumerate(bboxes_merged):
            # center of bbox
            center_x = x + w / 2
            center_y = y + h / 2
            center_point = np.array([center_x, center_y])
            # print((center_x, center_y))

            # calculate whether the bounding box is inside the polygon that was detected by YOLOv8 or not
            draw_box = True

            # 現在と過去2フレームのマスクをすべて結合
            all_masks = []
            for masks in previous_masks:
                all_masks.extend(masks)

            # BBOXの中心がいずれかのマスク内にあるかチェック
            for mask_contours in all_masks:
                if cv2.pointPolygonTest(mask_contours, (center_x, center_y), False) >= 0:
                    # print(f"bounding box ({x}, {y}, {w}, {h}) is inside the polygon")
                    draw_box = False

            # draw bounding boxes
            if draw_box == True:
                bboxes_merged_processed.append([x + w/2 - 180, y + h/2 - 180, x + w/2 + 180, y + h/2 + 180, 0.8, idx])
                ary_det_originIdx.append((idx, x, y, w, h))
                # とりあえず80-0.8でうまくいったけど、大きい物体に対応不可能

        # draw bounding boxes to
        for x, y, w, h in bboxes:
            cv2.rectangle(frame, (x, y), (x + w, y + h), (255, 0, 0), 2)


        # 180のバージョン
        ary_det = bboxes_merged_processed

        # 各トラックに対応する車両の移動フラグを保存する辞書
        track_vehicle_moving_flags = {}

        # バウンディングボックスが存在しない場合の処理
        if len(ary_det) > 0:
            ary_det = np.array(ary_det)
        else:
            # バウンディングボックスがない場合、空の(0,5)配列を作成（データ型をfloat32に指定）
            ary_det = np.empty((0, 6), dtype=np.float32)
        # トラッカーの更新
        ary_tracks = tracker_sort.update(ary_det)
        ary_tracks = tracker_sort.update(ary_det)
        # トラッキング結果の描画と移動ベクトルの計算
        if len(ary_tracks) > 0:
            for ary_track in ary_tracks:
                x1, y1, x2, y2, track_id, det_idx = ary_track.astype(np.float32)
                det_idx = int(det_idx)
                center_x = int((x1 + x2) / 2)
                center_y = int((y1 + y2) / 2)
                current_center = (center_x, center_y)

                # 最も近い車両を見つける
                min_distance = float('inf')
                closest_vehicle_id = None
                vehicle_moving_flag = False

                for vehicle_id, centers in vehicle_centers_history.items():
                    if len(centers) > 0:
                        # 最新の位置と移動フラグを取得
                        _, veh_center_x, veh_center_y, moving_flag = centers[-1]
                        distance = np.sqrt((center_x - veh_center_x) ** 2 + (center_y - veh_center_y) ** 2)
                        if distance < min_distance:
                            min_distance = distance
                            closest_vehicle_id = vehicle_id
                            vehicle_moving_flag = moving_flag

                # このトラックに対する移動フラグを保存
                if closest_vehicle_id is not None:
                    track_vehicle_moving_flags[track_id] = vehicle_moving_flag
                else:
                    track_vehicle_moving_flags[track_id] = True  # 車両が見つからない場合は移動していると仮定（ノイズ扱い）


                # 元の検出結果を取得
                for idx, orig_x, orig_y, orig_w, orig_h in ary_det_originIdx:
                    if idx == det_idx:
                        if track_vehicle_moving_flags[track_id] == False:
                            # 元のバウンディングボックスを描画
                            cv2.rectangle(annotated_frame, (int(orig_x), int(orig_y)), (int(orig_x + orig_w), int(orig_y + orig_h)), (0, 255, 0), 2)
                        break  # 一致する検出結果が見つかったらループを抜ける

                # 過去の中心座標を取得
                if track_id in track_centers_history:
                    previous_center = track_centers_history[track_id][-1]

                    # 一番近くの車が移動中であれば、移動ベクトルは無効化
                    if track_vehicle_moving_flags[track_id] == False:
                        # 移動ベクトルの描画
                        cv2.arrowedLine(annotated_frame, (previous_center[1], previous_center[2]), current_center, (255, 0, 0), 2, tipLength=0.5)
                        # IDの描画
                        cv2.putText(annotated_frame, f'ID: {int(track_id)}', (int(x1) + 150, int(y1) + 150),
                                    cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 0, 0), 2)

                        # CSVに格納
                        # trash_tracks_df.loc[frame_num, track_id] = f'({center_x}, {center_y})'
                        poss_trash.append({
                            'frame_num': frame_num,
                            'track_id': int(track_id),
                            'coordinate': f'({center_x}, {center_y})'
                        })

                else:
                    # 初回は移動ベクトルを描画しない
                    pass

                # 現在の中心座標を保存
                # track_centers_historyにtrack_idがまだ存在しない場合、新しくリストを作成
                if track_id not in track_centers_history:
                    track_centers_history[track_id] = []
                track_centers_history[track_id].append((frame_num, center_x, center_y))



            # 存在しなくなったトラックIDを削除
            current_track_ids = set(ary_tracks[:, 4].astype(int))
            lost_track_ids = set(track_centers.keys()) - current_track_ids
            for track_id in lost_track_ids:
                del track_centers[track_id]


        prev_trash_tracks = [data for data in poss_trash if data['frame_num'] == frame_num]


        cv2.imshow("Frame_YOLOv8_&_D-Frame", imutils.resize(annotated_frame, height = 900))
        cv2.waitKey(wait_secs)

        if cv2.waitKey(wait_secs) & 0xFF == ord('q'):
            break


    cap.release()
    cv2.destroyAllWindows()

    # The following is for checking the results through outputting the csv
    #
    # # Generate DataFrame from list
    # trash_tracks_df = pd.DataFrame(poss_trash)
    # # convert DataFrame
    # pivot_df = trash_tracks_df.pivot(index='frame_num', columns='track_id', values='coordinate')
    #
    # # pivot_df.columns.name = None  # 'track_id'の名前を削除
    # pivot_df.reset_index(inplace=True)  # フレーム番号を列に戻す
    # # pivot_df.to_csv("result_track/" + input_path + '_result.csv', index=False)
    # # # CSVファイルに保存
    # # trash_tracks_df.to_csv("result_track/" + input_path + '_result.csv')

if __name__ == '__main__':
    main("test22_modified.mp4")

    # # 処理したいディレクトリのパスを指定
    # directory_path = "testdata_origin/"
    # # ディレクトリ内の全ての.mp4ファイルを取得
    # mp4_files = glob.glob(directory_path + "*.mp4")
    # # 取得したファイル一覧をループで回す
    # for mp4_file in mp4_files:
    #     main(mp4_file)