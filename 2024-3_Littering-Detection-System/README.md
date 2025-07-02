# Littering-Detection-System
## 概要
国立雲林科技大学IRISセンター（台湾）での国際インターンシップの成果です。
監視カメラ映像から、ゴミのポイ捨てを検知し、ポイ捨てされたゴミに矩形を描くシステムを構築しました。

## 各種ファイル
### レポート、スライドのpdf
レポート、スライドは、以下からご覧になれます。
```aiignore
2024-3_Littering-Detection-System_Report.pdf
2024-3_Littering-Detection-System_Slide.pdf
```

### デモ動画
実際にゴミを検出している様子を、`demo.mp4`でご覧になれます。

## 実行に際して
### 実行エントリポイント
まず、次のディレクトリへ移動します。
```aiignore
/PythonProject
```
そして、main関数を実行します
```aiignore
/PythonProject/main.py
```

### 必要環境
pythonで記載された`main.py`の動作に際し、次のものとバージョンで、動作確認が取れています。
また、YOLOv8をGPU処理で行うためには、別途ultralyticsのインストラクションに従い、GPU処理に対応させる必要があります。
```aiignore
filterpy==1.4.5
imutils==0.5.4
lapx==0.5.11
matplotlib==3.9.2
numpy==2.1.3
opencv_python==4.10.0.84
pandas==2.2.3
scipy==1.14.1
skimage==0.0
ultralytics==8.3.28
```

