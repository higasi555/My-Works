import cgi
import cgitb
import json
import os
from pathlib import Path

import numpy as np

# デバッグ ON
cgitb.enable()

# メソッドごとの .npy ファイル名だけを保持
METHODS = {
    "hist_rgb_256":  "features_hist_rgb_256.npy",
    "hist_rgb_64":  "features_hist_rgb_64.npy",
    "hist_rgb_64_2x2":  "features_hist_rgb_64_2x2.npy",
    "hist_rgb_64_3x3":  "features_hist_rgb_64_3x3.npy",
    "hist_hsv_256":  "features_hist_hsv_256.npy",
    "hist_hsv_64":  "features_hist_hsv_64.npy",
    "hist_hsv_64_2x2":  "features_hist_hsv_64_2x2.npy",
    "hist_hsv_64_3x3":  "features_hist_hsv_64_3x3.npy",
    "hist_luv_256":  "features_hist_luv_256.npy",
    "hist_luv_64":  "features_hist_luv_64.npy",
    "hist_luv_64_2x2":  "features_hist_luv_64_2x2.npy",
    "hist_luv_64_3x3":  "features_hist_luv_64_3x3.npy",
    "vgg16_fc7":  "features_vgg16_fc7.npy",
}

# 保存済み.npyを読み込んで、特徴量行列を返す関数
def load_feature_db(method: str) -> np.ndarray:
    if method not in METHODS:
        raise ValueError(f"Unsupported method: {method}")

    base = Path(__file__).resolve().parent.parent
    feat_path = base / "features" / METHODS[method]
    if not feat_path.exists():
        raise FileNotFoundError(f"{feat_path} がありません")

    features = np.load(feat_path, mmap_mode="r")
    return features


# 以下類似度計算関数
# L2
def calc_euclidean_distance(query_vec: np.ndarray, db: np.ndarray) -> np.ndarray:
    diff = db - query_vec[:, None]
    sims = np.linalg.norm(diff, axis=0)
    return sims

# L1
def calc_hist_intersection(query_vec: np.ndarray, db: np.ndarray) -> np.ndarray:
    return np.minimum(db, query_vec[:, None]).sum(axis=0)
    # numer  = np.minimum(db, query_vec[:,None]).sum(axis=0)
    # denom  = query_vec.sum() + 1e-9
    # return numer / denom

# チャンネル別平均intersection
def calc_channel_intersection(query_vec: np.ndarray, db: np.ndarray, bins: int) -> np.ndarray:
    sims = []
    for c in range(3):
        q_c = query_vec[c*bins:(c+1)*bins]
        d_c = db[c*bins:(c+1)*bins]
        numer = np.minimum(q_c[:, None], d_c).sum(axis=0)
        denom = q_c.sum() + 1e-9
        sims.append(numer/denom)
    return np.mean(sims, axis=0)


# 以下ソート関数
def sort_indices_by_desc(sims: np.ndarray) -> np.ndarray:
    return np.argsort(sims)[::-1]

def sort_indices_by_distance(sims: np.ndarray) -> np.ndarray:
    return np.argsort(sims)  # 小さいものが先


# 結果をjson化する関数
def build_results(sorted_idx: np.ndarray, sims: np.ndarray) -> list[dict]:
    results = []
    for i in sorted_idx:
        results.append({
            "index":       int(i),
            "similarity":  float(sims[i]),
            "path":        f"../img/{i+1}.jpg"
        })
    return results


# encode方法、dist方法、indexを受け取って、結果が格納されたjsonを返す関数
def main(method, index, dist_method):
    try:
        # DBやらの読み込み
        db = load_feature_db(method)
        D, M = db.shape
        if not (0 <= index < M):
            raise IndexError(f"index は 0～{M-1} の間で指定してください")

        query_vec = db[:, index]

        # 距離計算
        if method.startswith("hist_"):
            if dist_method == "intersect":
                bins = db.shape[0] // 3
                sims = calc_channel_intersection(query_vec, db, bins)
                sorted_idx = sort_indices_by_desc(sims)
            elif dist_method == "euclid":
                sims = calc_euclidean_distance(query_vec, db)
                sorted_idx = sort_indices_by_distance(sims)
            else:
                raise ValueError(f"Unsupported dist_method: {dist_method}")
        else:
            sims = calc_euclidean_distance(query_vec, db)
            sorted_idx = sort_indices_by_distance(sims)

        # レスポンスの組み立て
        result = build_results(sorted_idx, sims)
        print(json.dumps(result, ensure_ascii=False))

    except Exception as e:
        # エラー時もJSONで返す
        print(json.dumps({"error": str(e)}, ensure_ascii=False))


if __name__ == "__main__":
    # CGI ヘッダ
    print("Content-Type: application/json; charset=utf-8")
    print()
    form = cgi.FieldStorage()
    method = form.getfirst("method", "hist_rgb_256")
    try:
        index = int(form.getfirst("index", "0"))
    except ValueError:
        index = 0

    dist_method = form.getfirst("dist_method", "intersect")

    # デバッグ用
    # method = "hist_rgb"
    # index = 1

    main(method, index, dist_method)