# 人狼RPG v3
人狼RPGは [WhiteTails](https://www.youtube.com/watch?v=ZRJOh_9EIFY&list=PL5aADROd9wP_ivTTE5bG3XlCrEyWJZz1o) 様の企画であり、
このプラグインはその企画を再現・一部改変したものになります。
基本的なルールは [WhiteTails](https://www.youtube.com/watch?v=ZRJOh_9EIFY&list=PL5aADROd9wP_ivTTE5bG3XlCrEyWJZz1o) 様及び、そのコラボしている方々の動画を見て学んでください。
以下では、このプラグイン固有の機能について記載します。
万が一YouTubeなどの公的な場でこのプラグインを用いた企画を行う際には、WhiteTails様公式の [ガイドライン](https://whitetails.jp/guideline/) に従い利用してください。
---
## バージョン
バージョンはSpigot-1.18.1です

## 役職

|   役職   | 説明                                  |
|:------:|:------------------------------------|
| 人狼(w)  | 人狼です                                |
| 共犯者(m) | 人狼側ですが、占われると村人と出ます                  |
| 狼付き(l) | 村人側ですが、占われると人狼と出ます                  |
| 吸血鬼(v) | 第三陣営で、決着がついた時に生きていれば単独勝利となります       |
|   村人   | 村人です                                |
| パン屋(b) | 生きていると毎朝全員にパンが配布されます                |
| 狩人(h)  | 弓を打つとエメラルドが1つもらえます                  |
| 騎士(k)  | 騎士の加護を使用した相手が襲撃されると通知とエメラルドを1つもらえます |

## コマンド

|             コマンド              |          権限          | 説明                            |
|:-----------------------------:|:--------------------:|:------------------------------|
|         /jinro start          | jinro.command.start  | ゲームの募集を開始します                  |
|         /jinro cancel         | jinro.command.cancel | ゲームを強制終了します                   |
|          /jinro join          |          なし          | 募集されてるゲームに参加します               |
| /jinro spawn <combat/support> | jinro.command.spawn  | 村人を自分の位置に召喚します                |
|     /jinro give <アイテムID>      |  jinro.command.give  | ゲームのアイテムを入手します                |
|     /jinro sign <素材> [名前]     |  jinro.command.sign  | 占いや騎士の加護に使うための看板をアイテムとして入手します |

## ゲーム設定
ゲーム設定はconfig.ymlで行えます

|                    key                     |   型   | 説明                                      |
|:------------------------------------------:|:-----:|:----------------------------------------|
|       items.<アイテムの種類>.<アイテムID>.price       |  整数   | ショップでの値段を決めます(0以下の場合はショップで販売されなくなります)   |
| items.<アイテムの種類>.<アイテムID>.custom_model_data |  自然数  | ゲームアイテムのCustomModelDataを決めます            |
|      numbers.skeleton_per_population       | 非負小数  | プレイヤー1人あたりにスポーンするスケルトンの数を決めます           |
|            numbers.emerald_odds            | 0 ~ 1 | スケルトンがエメラルドを落とす確率を決めます                  |
|              numbers.time_day              |  自然数  | 昼の長さを秒数で決めます                            |
|             numbers.time_night             |  自然数  | 夜の長さを秒数で決めます                            |
|           numbers.time_first_day           |  自然数  | 最初の昼の長さを秒数で決めます                         |
|             numbers.max_player             |  自然数  | プレイヤーの最大人数を決めます                         |
|                numbers.jobs                |  自然数  | 村人以外の役職の人数を決めます、文字は上記の役職の()内に書かれているものです |

## エリア設定
ゲームを行うにはワールドごとにエリア設定が必要です。
エリア設定ファイルはプラグインのデータフォルダのarea/<ワールド名>.ymlです。
エリアはすなわちスケルトンが湧く範囲です。
サンプルとして、world_setting_sample.ymlがデータフォルダに作成されます。
エリアは縦方向の円柱(cylinder)と直方体(box)の組み合わせで作成することができます。
※ (free)はエリアのパーツごとに自由な文字列を定めてください

| key               |       型        |                  説明                  |
|:------------------|:--------------:|:------------------------------------:|
| area.(free).shape | cylinder / box |            追加するエリアの形を定めます            |
| area.(free).type  |   add / pile   | pileの時はエリアが重なった場合、そこにスケルトンが沸きやすくなります |

### 直方体(area.(free).shape: box)の場合に追加で必要な要素
| key               |  型  |       説明       |
|:------------------|:---:|:--------------:|
| area.(free).min.x | 整数  | 直方体の中で最も小さいX座標 |
| area.(free).min.y | 整数  | 直方体の中で最も小さいY座標 |
| area.(free).min.z | 整数  | 直方体の中で最も小さいZ座標 |
| area.(free).max.x | 整数  | 直方体の中で最も大きいX座標 |
| area.(free).max.y | 整数  | 直方体の中で最も大きいY座標 |
| area.(free).max.z | 整数  | 直方体の中で最も大きいZ座標 |

### 円柱(area.(free).shape: cylinder)の場合に追加で必要な要素
| key                  |  型  |      説明      |
|:---------------------|:---:|:------------:|
| area.(free).center.x | 整数  | 円柱の底面の中心のX座標 |
| area.(free).center.y | 整数  | 円柱の底面の中心のY座標 |
| area.(free).center.z | 整数  | 円柱の底面の中心のZ座標 |
| area.(free).radius   | 整数  |   円柱の底面の半径   |
| area.(free).height   | 自然数 |    円柱の高さ     |

## アイテム一覧
| アイテムID                 | 種別      | 使える役職 | 使える時間         | 名前        | 説明                               |
|:-----------------------|:--------|:------|:--------------|:----------|:---------------------------------|
| bow                    | combat  | 誰でも   | いつでも          | 弓         | 一撃必殺の弓です、一回で壊れます                 |
| arrow                  | combat  | 誰でも   | いつでも          | 矢         | 弓とセットで使います                       |
| cooked_beef            | combat  | 誰でも   | いつでも          | ステーキ      | 5枚セットの牛肉です                       |
| skeleton_killer        | combat  | 誰でも   | いつでも          | スケ狩り剣     | スケルトンを30回ワンパンできる剣です              |
| stan_grenade           | combat  | 誰でも   | いつでも          | スタングレネード  | 当たると盲目+身動き不可能になる雪玉です             |
| werewolf_axe           | combat  | 人狼    | いつでも(各昼一回のみ)  | 人狼の斧      | ワンパンの斧です                         |
| invisible_potion       | combat  | 誰でも   | いつでも          | 透明化のポーション | 20秒間透明になるポーションです                 |
| trident_of_rancor      | combat  | 誰でも   | いつでも          | 怨念の槍      | 2回当たると死亡するトライデント、当てるまで再利用可能      |
| speed_potion           | support | 誰でも   | いつでも          | 俊敏のポーション  | 試合中永続の俊敏のポーション                   |
| heart_of_fortuneteller | support | 誰でも   | 夜のみ           | 占い師の心     | 買うとインベントリから消え、占い可能回数が増えます        |
| pray_of_knight         | support | 人狼以外  | 夜のみ         　 | 騎士の祈り     | 使った夜のみ有効な不死のトーテムみたいなの            |
| providence_of_knight   | support | 誰でも   | いつでも          | 騎士の加護     | 看板にクリックすることでその人に騎士の祈りと同様の効果を付与する |
| eye_of_mad             | support | 共犯者   | いつでも          | 共犯者の目     | 人狼一人の名前をランダムで知ることができる            |
| sacred_cross           | support | 誰でも   | いつでも          | 聖なる十字架    | 吸血鬼を一撃で倒すことができる                  |
| eye_of_providence      | support | 誰でも   | いつでも          | プロビデンスの目  | 30秒間の発光エフェクトを使用者以外の生存者に付与します     |
| talisman               | support | 誰でも   | 夜のみ           | 天啓の呪符     | 使用した夜に占われると通知がくる                 |
| ash_of_medium          | support | 誰でも   | いつでも          | 霊媒師の遺灰    | 死んだ全てのプレイヤーの名前がわかる               |
| blunt                  | other   | 誰でも   | いつでも          | 鈍器        | ゲーム開始時にもらえる木の棒です                 |
| bread                  | other   | 誰でも   | いつでも          | パン        | パン屋さんが生きてる時に朝になるたびにもらえるパンです      |

## その他の仕様
- 試合中にログアウトした場合、その旨が全員に通知され、一定時間が経過するまで再びログインしていない場合、死亡したとみなし処理をします。
- 役職の希望を提出する際に、人数が不確定である以上、全ての役職が候補として表示されますが、使われない役職を選択した場合は効果がありません。
- ゲームを進行するうえで必要な設定ファイルが足りていなかった場合などは、ゲームが開始されず、その旨が表示されます。
- ゲーム中に死亡し観戦モード(Spectator)になっても生きているプレイヤー視点のプレイヤーリストの名前は暗くなりません。