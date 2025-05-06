# Reverse-AI-Using=Evolutionary-Computing
## 概要
進化計算を用いて進化させた遺伝子を用いてプレイする、オセロAIを作成しました。

## 各種ファイル
### レポート、スライドのpdf
レポートは、以下からご覧になれます。
```aiignore
2025-5_Reverse-AI-Using-Evolutionary-Computing_Report.pdf
```

## 実行に際して
### 実行エントリポイント
コンパイル済みの次の実行ファイルを動かすことで、CUI上でゲームをプレイすることができます。
```aiignore
Battle_with_AI.exe
```

### 必要環境
・Windows11<br>
・intel製CPU

### プレイ方法
・コンパイル済のBattle_with_AI.exeを実行します<br>
・まず、先攻（1）か後攻（2）を入力し、Enterを押します<br>
・先攻は◯、後攻は✕ですので、置きたい座標を指定します<br>
　例えば、<br>
　0 1 2 3 4 5 6 7<br>
　0 - - - - - - - -<br>
　1 - - - - - - - -<br>
　2 - - - - - - - -<br>
　3 - - - o x - - -<br>
　4 - - - x o - - -<br>
　5 - - - - - - - -<br>
　6 - - - - - - - -<br>
　7 - - - - - - - -<br>
　のとき、(2,4)に置きたい場合は、「2 4」と入力し、Enterを押します<br>
・すると、AIが自動で手を打ち、次の盤面へと移り変わりますので、しばらくお待ち下さい