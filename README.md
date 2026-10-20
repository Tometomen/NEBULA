# NEBULA MINER

**打ち込め。掘り出せ。まだ見ぬ惑星へ。**

日本語の問題をローマ字で入力して宇宙の鉱石を採掘する、PC向けのタイピング × インクリメンタルゲームです。60秒の採掘で資源を集め、ドローンを配備し、自動生産ラインを育てて火星を目指します。ログイン・広告・バックエンドは不要です。

## 動作環境・技術

- PC / 物理キーボードとマウス / WebGL対応のChrome・Edgeを想定
- JDK 17以上（検証環境: Eclipse Temurin 21.0.6+7）
- Gradle Wrapper **8.12.1**（公式SHA-256検証付き）
- Java **8**互換の共通コード / libGDX **1.13.1**
- LWJGL3（libGDXバックエンドの固定推移依存）/ GWT **2.11.0**
- Scene2D UI / ShapeRenderer / JUnit **4.13.2**
- HTMLはGWTコンパイラをGradleのJavaExecで直接起動します。構成、GWT定義、Wrapperは公式 **gdx-liftoff** のテンプレートを参考にしています。追加のGWT Gradleプラグインは不要です。

初回ビルドには `services.gradle.org`、GitHubのGradle配布先、`repo.maven.apache.org` へのHTTPS接続が必要です。Wrapperと依存はバージョン固定です。フォントと効果音は同梱済みで、実行時に外部サービスに接続しません。

## 起動

```sh
# リポジトリ直下で実行（Windowsでは gradlew.bat）
./gradlew core:test
./gradlew lwjgl3:run
./gradlew html:dist

# Web版をローカル配信。file:// で直接開かないでください。
python3 -m http.server 8000 --directory html/build/dist
```

自分のPCではブラウザでHTTPサーバーのポート8000を開きます。Web配布物は `html/build/dist/` です。リポジトリ名によるサブパス配信にも対応する相対URLを使用しています。macOSのデスクトップ実行は `-XstartOnFirstThread` を自動設定します。

このCodexクラウド環境では、導入済みJDK・HTTPSプロキシ・システム証明書を有効にするヘルパーを利用できます。

```sh
./tools/cloud-gradle core:test html:dist
./tools/cloud-gradle lwjgl3:run   # GUIディスプレイが必要
```

`cloud-gradle` は認証情報を保存せず、証明書検証も無効化しません。一般の開発PCでは通常の `./gradlew` を使用してください。クラウド環境の再構築は `tools/setup-cloud.sh` で行えます。

## 遊び方

1. 「採掘を開始」→説明を確認し「準備完了」で60秒の採掘を開始。
2. IMEをオフにして、表示された日本語のローマ字を入力。大文字も使えます。
3. 正しい一打で鉱石1個、単語完成で6個＋コンボ倍率。ミスしても入力位置は戻りませんがコンボが切れます。
4. 20秒・40秒から5秒間は流星群。タイピング報酬が2倍になります。
5. リザルト後に基地へ。ドローン（初期100、価格1.6倍）で1台につき毎秒1個を自動生産。レーザー（初期150、価格1.8倍）は1レベルにつきタイピング報酬+20%。
6. 鉱石700＋レア鉱石4で火星を解放。火星のタイピング報酬は1.6倍。解放後は月面と往復可能です。

- `Esc` または画面のボタンで一時停止・再開。タブ非表示やウィンドウのフォーカス喪失時にも停止し、復帰時は明示的に再開します。
- `shi/si`、`chi/ti`、`tsu/tu`、`chu/tyu`相当の候補に対応。問題別の候補辞書方式です。
- **語末の「ん」は `nn` または表示された `n'` で確定します。**「しんや」は `shinnya` / `shin'ya` / `shinya` 等、「ろぼっと」は `robotto` / `roboxtuto` / `roboltuto`。全ての日本語ローマ字変換方式を網羅するものではありません。
- 正解候補に応じて残りの表示が変わります。無効なキーは問題を進めず通知します。全角文字入力時はIME切替を案内します。
- レア鉱石は単語完成時15%。10問続けて出なければ11問目に保証。ロボットはレア鉱石を生産しません。
- 設定で音量・ミュート・演出軽減を変更できます。初回はブラウザの `prefers-reduced-motion` も反映。タッチ端末にはPC推奨を表示します。

## 保存仕様

libGDX Preferencesを使用します。Webは同じブラウザ・同じオリジンのローカル保存、デスクトップのGradle実行は `.local/preferences/`（Git管理外）です。Webとデスクトップのセーブは共有されません。デスクトップ配布実行の既定はホーム下 `.nebula-miner/`、`-Dnebula.preferences=/任意のパス` で変更可能です。

所持鉱石・レア鉱石、設備、惑星、設定、ベスト、レア保証の状態、最終時刻とバージョンを保存します。購入・移動・採掘終了・設定変更・一時停止と10秒ごとに保存、`flush()` を実行します。再開時は自動生産を最大4時間分付与し、時計が逆戻りした場合は0とします。タイピングタイマーと生産の時計は分離しています。

欠損フィールドは既定値、不正数値は補正します。バージョン1の追加フィールドを省略できる構造です。将来の非互換スキーマに対する移行は追加実装が必要です。リセットは確認を挟みます。保存領域が無効な場合は通知し、そのセッションではプレイを継続します。ブラウザ保存を削除すると進捗が失われます。クラウド同期はありません。

## 構成

```text
core/src/main/java/com/nebula/
  NebulaMinerGame.java      ライフサイクル、入力、画面遷移
  data/                    バランス定数、36問のローマ字候補
  model/                   プレイヤー、惑星、状態、問題
  system/                  タイピング、採掘、購入・生産
  save/                    Preferencesの保存・復元
  ui/                      Scene2D画面、図形による宇宙・船・鉱石
  util/                    日本語数値表記
core/src/test/             判定、経済、タイマー、保存のJUnitテスト
lwjgl3/                    デスクトップエントリーポイント
html/                      GWTエントリーポイント、Webページ、配布ビルド
assets/                    日本語BitmapFont、合成WAV効果音
licenses/                  同梱素材・参考テンプレートのライセンス
tools/                    アセット生成、クラウド補助、ブラウザテスト
```

## テスト

```sh
./gradlew core:test html:dist lwjgl3:classes
# 上のローカルHTTPサーバーを起動した状態で別端末から:
npm install --no-save --package-lock=false playwright
node tools/browser-smoke.cjs
```

ブラウザスモークはPlaywrightとChromiumを使用し、**実時間60秒**のキーボード採掘、ミス・コンボ、停止・再開、注入したblurイベントによるフォーカス喪失処理、リザルト、設備購入、自動生産、火星、設定、ページ再読み込みによる保存、リサイズを検証します。環境に応じて `CHROMIUM`（実行ファイル）と `GAME_URL` を指定できます。実OSのタブ切替は自動操作環境がフォーカスを固定するため別途手動確認が必要です。放置上限・破損保存のブラウザ検証は専用の保存データを使います。結果画像はGit管理外の `artifacts/` に出力。`window.nebulaSnapshot` は現在の表示状態を読み取る診断用スナップショットで、ゲーム状態を変更するAPIは提供しません。Webページには状態・問題・通知の読み上げ用テキストも同期します。

JUnitは、全候補の完成、誤入力・IME、促音・小さい仮名・ん、60秒終了、一時停止、コンボ境界、流星群、報酬倍率、購入・価格成長・上限、生産、レア保証、火星条件、放置上限、不正数値、保存往復・欠損・破損・削除を検証します。検証結果と実施範囲は `VALIDATION.md` を参照してください。

## GitHub Pages公開

1. `./gradlew core:test html:dist` を実行。
2. `html/build/dist/` の**中身全体**をPagesの公開対象へ配置。`index.html`、`nebula/`、`assets/` を同じ階層に保ちます。
3. リポジトリ Settings → Pages で公開ブランチを設定、または同梱の `.github/workflows/pages.yml` を手動実行してGitHub Actionsから配布します。Actionsを使う場合、PagesのSourceを **GitHub Actions** に設定してください。
4. 公開URLで日本語表示、採掘、保存を確認します。公開オリジンが変わるとローカルのセーブは引き継がれません。

同梱ワークフローは手動実行です。開発作業からリポジトリへのpushやPages公開は自動では行いません。

## 制約と今後

- マウス＋物理キーボード前提。ゲームUIは16:9の論理座標をFitViewportで縮尺調整し、余白を付けます。極端に小さい画面では文字が小さくなります。
- 効果音はユーザー操作後のブラウザ音声許可に依存します。クラウドの仮想画面では実機音声の聴覚確認はできません。
- タイピングは問題別候補式。辞書は36問です。汎用かな変換器、IME compositionの完全サポート、スクリーンリーダーでの全UI操作は今後の課題です。
- 資源上限は10^15、設備は各60段階。オフライン時間はローカル時計に基づき、改変対策は行いません。
- 概ね数セッションで最初のドローン、5〜10分で火星を目指す設定ですが、タイピング速度により変わります。通常の人間のプレイによる長期バランス評価は未実施です。
- 次の拡張案: 木星の衛星、設備の相乗効果、航路ごとの問題辞書、日替わり採掘ミッション、任意のセーブ書き出し。

## アセットと参考資料

- 宇宙、基地、鉱石、ドローン: 独自のShapeRenderer描画。効果音は `tools/generate-assets.py` による合成。
- 日本語フォント: **Noto Sans CJK JP Regular**、SIL Open Font License 1.1。同梱BitmapFontはPillowで事前生成。`licenses/OFL.txt` 参照。[出典](https://github.com/notofonts/noto-cjk)。再生成時のみPillow 12.3.0とNoto Sans CJK Regularのインストールが必要です。文字を追加した場合は `python3 tools/generate-assets.py` を実行してください。
- [gdx-liftoff](https://github.com/libgdx/gdx-liftoff): MITライセンス、`licenses/GDX-LIFTOFF-LICENSE.txt`。テンプレートとGradle Wrapperを参照・利用。GradleはApache-2.0、libGDXはApache-2.0、JUnitはEPL-1.0です。
- [Game UI Frontend](https://github.com/openai/plugins/blob/main/plugins/game-studio/skills/game-ui-frontend/SKILL.md) と [Emil Kowalskiの資料](https://github.com/emilkowalski/skills) を参照。画面中央のプレイ領域、明確な入力フィードバック、抑えた演出を優先しました。
- 指定されたゲームアクセシビリティサイトおよびKenneyはクラウドのネットワーク制限で参照不可でした。Kenney素材は使用していません。
