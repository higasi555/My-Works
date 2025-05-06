from pathlib import Path

import cv2
import numpy as np

from PIL import Image
import torch
import torchvision.models as models
import torch.nn as nn
import glob

class Encode:
    # pathを受け取って、3Nx1の特徴行列を出す関数
    @staticmethod
    def hist(type: str, path2img: str, num_bin: int) -> np.ndarray:
        # 画像読み込み
        img = cv2.imread(path2img, cv2.IMREAD_COLOR)
        if img is None:
            raise FileNotFoundError(f"can't read {path2img}")

        match type:
            case "hist_rgb":
                img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
            case "hist_hsv":
                img = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
            case "hist_luv":
                img = cv2.cvtColor(img, cv2.COLOR_BGR2LUV)

        # rgbへ分解
        r = img[:, :, 0]
        g = img[:, :, 1]
        b = img[:, :, 2]

        # hist用
        hist_r = np.zeros((num_bin,), dtype=np.int64)
        hist_g = np.zeros((num_bin,), dtype=np.int64)
        hist_b = np.zeros((num_bin,), dtype=np.int64)

        bin_interval = 256 // num_bin

        for i in range(0, 256):
            # 画素値がiと一致するところはTrue(1)、それ以外は0
            map_r = (r == i)
            map_g = (g == i)
            map_b = (b == i)
            # np.sumで1の部分を一気にカウント
            sum_r = np.sum(map_r)
            sum_g = np.sum(map_g)
            sum_b = np.sum(map_b)
            # hist配列に代入
            hist_r[i // bin_interval] += sum_r
            hist_g[i // bin_interval] += sum_g
            hist_b[i // bin_interval] += sum_b

        # histを合成
        output = np.concatenate([hist_r, hist_g, hist_b], axis=0)

        return output

    @staticmethod
    def hist_partition(type: str, path2img: str, num_bin: int, grid: tuple[int,int] = (2,2)) -> np.ndarray:
        # 画像読み込み
        img = cv2.imread(path2img, cv2.IMREAD_COLOR)
        if img is None:
            raise FileNotFoundError(f"can't read {path2img}")

        match type:
            case t if t.startswith("hist_rgb"):
                img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
            case t if t.startswith("hist_hsv"):
                img = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
            case t if t.startswith("hist_luv"):
                img = cv2.cvtColor(img, cv2.COLOR_BGR2LUV)

        # bit数取得
        img_num_bit = np.iinfo(img.dtype).max

        H, W, _ = img.shape
        gy, gx = grid
        h_step = H // gy
        w_step = W // gx

        # ここでリストにためて、最後に一度結合する
        feats = []

        # 2) サブ領域ごとにヒスト計算
        for iy in range(gy):
            for ix in range(gx):
                sub_img = img[iy*h_step:(iy+1)*h_step, ix*w_step:(ix+1)*w_step]

                # rgbへ分解
                r = sub_img[:, :, 0]
                g = sub_img[:, :, 1]
                b = sub_img[:, :, 2]

                # hist用
                hist_r = np.zeros((num_bin,), dtype=np.int64)
                hist_g = np.zeros((num_bin,), dtype=np.int64)
                hist_b = np.zeros((num_bin,), dtype=np.int64)

                # bin_interval = 256 // num_bin
                bin_interval = (img_num_bit + 1) // num_bin

                for i in range(0, img_num_bit + 1):
                    # 画素値がiと一致するところはTrue(1)、それ以外は0
                    map_r = (r == i)
                    map_g = (g == i)
                    map_b = (b == i)
                    # np.sumで1の部分を一気にカウント
                    sum_r = np.sum(map_r)
                    sum_g = np.sum(map_g)
                    sum_b = np.sum(map_b)
                    # hist配列に代入
                    hist_r[i // bin_interval] += sum_r
                    hist_g[i // bin_interval] += sum_g
                    hist_b[i // bin_interval] += sum_b

                # histを合成
                feats.append(np.concatenate([hist_r, hist_g, hist_b], axis=0))

        feature_martix = np.concatenate(feats, axis=0).astype(np.float32)
        s = feature_martix.sum()
        if s > 0:
            feature_martix /= s
        return feature_martix

    @staticmethod
    def hist_partition_new(type: str, path2img: str, num_bin: int, grid: tuple[int,int]) -> np.ndarray:
        img = cv2.imread(path2img, cv2.IMREAD_COLOR)
        if img is None:
            raise FileNotFoundError(f"can't read {path2img}")

        # カラースペース変換
        match type:
            case t if t.startswith("hist_rgb"):
                img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
            case t if t.startswith("hist_hsv"):
                img = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
            case t if t.startswith("hist_luv"):
                img = cv2.cvtColor(img, cv2.COLOR_BGR2LUV)

        H, W, _ = img.shape
        gy, gx = grid
        h_step = H // gy
        w_step = W // gx

        feats: list[np.ndarray] = []
        for iy in range(gy):
            for ix in range(gx):
                # sub = img[iy*h_step:(iy+1)*h_step, ix*w_step:(ix+1)*w_step]
                y0 = iy*h_step;  y1 = (H if iy==gy-1 else (iy+1)*h_step)
                x0 = ix*w_step;  x1 = (W if ix==gx-1 else (ix+1)*w_step)
                sub = img[y0:y1, x0:x1]

                # 各チャンネル毎にヒストグラム抽出
                # H は [0,180)、S,V,L,U は [0,256) でヒストを切る
                if type.startswith("hist_hsv"):
                    h_r = cv2.calcHist([sub], [0], None, [num_bin], [0, 180]).flatten()  # Hue
                    h_g = cv2.calcHist([sub], [1], None, [num_bin], [0, 256]).flatten()
                    h_b = cv2.calcHist([sub], [2], None, [num_bin], [0, 256]).flatten()
                else:
                    # RGB / LUV はすべて [0,256)
                    h_r = cv2.calcHist([sub], [0], None, [num_bin], [0, 256]).flatten()
                    h_g = cv2.calcHist([sub], [1], None, [num_bin], [0, 256]).flatten()
                    h_b = cv2.calcHist([sub], [2], None, [num_bin], [0, 256]).flatten()

                # チャンネルごとL1正規化
                # h_r /= (h_r.sum() + 1e-9)
                # h_g /= (h_g.sum() + 1e-9)
                # h_b /= (h_b.sum() + 1e-9)


                vec = np.concatenate([h_r, h_g, h_b]).astype(np.float32)

                # 全体でL1正規化
                vec /= (vec.sum() + 1e-9)

                feats.append(vec)

        return np.concatenate(feats, axis=0)


def do_encode_hist_partition(method, num_bin, grid):
    # 特徴量行列（最終的に、3NxM）
    features_list = []

    i = 0
    while True:
        path2img = Path(f"../img/{i+1}.jpg")
        if not path2img.exists():
            print(f"{path2img} doesnt exist")
            break

        # 各種Encode
        # vec = Encode.hist(METHOD, str(path2img), num_bin) # 3Nx1
        # vec = Encode.hist_partition(method, str(path2img), num_bin, grid)
        vec = Encode.hist_partition_new(method, str(path2img), num_bin, grid)

        # feature_matrix = np.concatenate([feature_matrix, vec.reshape(-1, 1)], axis=1)
        features_list.append(vec)

        print(f"Encode {i+1}.jpg finished")
        i = i + 1

    feature_matrix = np.stack(features_list, axis=1)

    np.save(f"../features/features_{method}.npy", feature_matrix)
    print(f"Saved features_{method}.npy ({feature_matrix.shape})")


def do_encode_dcnn(method):
    device = 'cuda' if torch.cuda.is_available() else 'cpu'

    weights=models.VGG16_Weights.DEFAULT
    vgg16 = models.vgg16(weights=weights,progress=True)
    vgg16fc7 = torch.nn.Sequential(
        vgg16.features,
        vgg16.avgpool,
        nn.Flatten(),
        *list(vgg16.classifier.children())[:-3]  # 最後の3つのlayer(relu,dropout,fc1000)を削除
    )
    vgg16fc7 = vgg16fc7.to(device)
    vgg16fc7.eval()

    # imglist = sorted(glob.glob("../img/*.jpg"))
    imglist = []
    idx = 0
    while True:
        fname = f"../img/{idx+1}.jpg"
        if not Path(fname).exists():
            break
        imglist.append(fname)
        idx += 1

    in_size=224
    imgs = np.empty((0,in_size,in_size,3), dtype=np.float32)
    for i,img_path in enumerate(imglist):
        if i%100==0:
            print("reading {}th image".format(i))
        x = np.array(Image.open(img_path).resize((in_size,in_size)), dtype=np.float32)
        x = np.expand_dims(x, axis=0)
        imgs = np.vstack((imgs,x))
    mean=np.array([0.485, 0.456, 0.406], dtype=np.float32)
    std=np.array([0.229, 0.224, 0.225], dtype=np.float32)
    imgs=(imgs/255.0-mean)/std
    imgs=imgs.transpose(0,3,1,2)  # HWC -> CHW
    img=torch.from_numpy(imgs)
    print(imgs.shape)

    with torch.no_grad():
        fc=vgg16fc7(img.to(device)).cpu().numpy()
    print(fc.shape)     # shapeの表示

    feature_matrix = fc.T

    np.save(f"../features/features_{method}.npy", feature_matrix)
    print(f"Saved features_{method}.npy ({feature_matrix.shape})")


if __name__ == "__main__":
    do_encode_hist_partition("hist_rgb_256", 256, (1, 1))
    do_encode_hist_partition("hist_rgb_64", 64, (1, 1))
    do_encode_hist_partition("hist_rgb_64_2x2", 64, (2, 2))
    do_encode_hist_partition("hist_rgb_64_3x3", 64, (3, 3))

    do_encode_hist_partition("hist_hsv_256", 256, (1, 1))
    do_encode_hist_partition("hist_hsv_64", 64, (1, 1))
    do_encode_hist_partition("hist_hsv_64_2x2", 64, (2, 2))
    do_encode_hist_partition("hist_hsv_64_3x3", 64, (3, 3))

    do_encode_hist_partition("hist_luv_256", 256, (1, 1))
    do_encode_hist_partition("hist_luv_64", 64, (1, 1))
    do_encode_hist_partition("hist_luv_64_2x2", 64, (2, 2))
    do_encode_hist_partition("hist_luv_64_3x3", 64, (3, 3))

    do_encode_dcnn("vgg16_fc7")
