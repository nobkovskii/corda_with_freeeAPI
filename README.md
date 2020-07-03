<p align="center">
  <img src="https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png" alt="Corda" width="500">
</p>

# freeeAPI x Corda

Qiita夏祭り2020参加企画です。

▼Qiita夏祭り
https://qiita.com/summer-festival

▼「会計」「勤怠」をハックしよう！freee API のTips募集
https://qiita.com/official-events/3f740f71bbdcb59d9959

# 環境

* 想定ユーザ
   * Windows10

freeeAPIが叩けるように、アクセストークンを取得しておいてください。

# 使い方

1. クローンします
1. ビルドします
    1. `gradlew.bat deployNodes`
1. ノードを起動します
    1. `build\nodes\runnodes.bat`
1. フロント起動
    1. `gradlew.bat runTemplateServer`
