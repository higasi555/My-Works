# Image-Search
## 概要
選択した画像に類似した画像を、様々なエンコード方式で比較し検索できるシステムです。

## 各種ファイル
### レポート、スライドのpdf
レポートは、以下からご覧になれます。
```aiignore
2025-2_Image-Search_Report.pdf
```

## 実行に際して
### 実行エントリポイント
まず、次のディレクトリへ移動します。
```aiignore
/imsearch/cgi-bin
```
次に、cgiオプションありで、httpサーバーをお好きなポートで動かします。
```aiignore
python -m http.server 8001 --cgi
```
最後に、ローカルでアクセスすると、image-searchのページが表示されます。
```aiignore
http://127.0.0.1:8001
```
なお、htmlのエントリポイントは以下のファイルとなっています。
```aiignore
/imsearch/index.html
```

### 必要環境
pythonで記載されたcgiを動かすために、次のものが必要となります。
```aiignore
cgi
numpy<2
```

# Image-Search

## Overview
This is a system that allows you to search for images similar to a selected one by comparing them using various encoding methods.

## Files

### Report and Slides (PDF)
The report can be found at the following location:
```aiignore
2025-2_Image-Search_Report.pdf
```

## Running the System

### Execution Entry Point
First, move to the following directory:
```aiignore
/imsearch/cgi-bin
```

Next, start an HTTP server on your preferred port with the CGI option enabled:
```aiignore
python -m http.server 8001 --cgi
```

Finally, access the following URL locally to display the image-search page:
```aiignore
http://127.0.0.1:8001
```

The entry point for the HTML is the following file:
```aiignore
/imsearch/index.html
```

### Required Environment
To run the CGI scripts written in Python, the following are required:

```aiignore
cgi
numpy<2
```
