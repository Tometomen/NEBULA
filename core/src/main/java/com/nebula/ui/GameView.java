package com.nebula.ui;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.nebula.*;
import com.nebula.model.*;
import com.nebula.data.*;
import com.nebula.system.*;
import com.nebula.util.NumberFormatter;

/** Fixed logical 16:9 layout letterboxes on resize; Scene2D owns all mouse controls. */
public class GameView {
 public final Stage stage=new Stage(new FitViewport(1280,720));
 private final NebulaMinerGame game;private final Skin skin=new Skin();private final BitmapFont font;
 private GameState shown;private Label resources,status,toast,word,input,progress,stats,robotInfo,laserInfo,planetInfo,settingsInfo;
 private TextButton robotButton,laserButton,planetButton;
 private static final Color WHITE=Color.valueOf("e9f3ff"),MUTED=Color.valueOf("a0b4d2"),CYAN=Color.valueOf("79e9e8"),AMBER=Color.valueOf("ffce87");
 public GameView(NebulaMinerGame game){
  this.game=game;font=new BitmapFont(Gdx.files.internal("ui.fnt"));font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);font.getData().markupEnabled=true;
  skin.add("default-font",font);Pixmap p=new Pixmap(1,1,Pixmap.Format.RGBA8888);p.setColor(Color.WHITE);p.fill();skin.add("pixel",new Texture(p));p.dispose();
  skin.add("default",new Label.LabelStyle(font,WHITE));
  TextButton.TextButtonStyle s=new TextButton.TextButtonStyle();s.font=font;s.fontColor=WHITE;s.up=skin.newDrawable("pixel",Color.valueOf("173044"));s.over=skin.newDrawable("pixel",Color.valueOf("24536a"));s.down=skin.newDrawable("pixel",Color.valueOf("37737f"));s.disabled=skin.newDrawable("pixel",Color.valueOf("111c2d"));s.disabledFontColor=MUTED;skin.add("default",s);
  rebuild();
 }
 private Label label(String text,float x,float y,float w,float h,float scale,Color color){Label l=new Label(text,skin);l.setFontScale(scale*.5f);l.setColor(color);l.setBounds(x,y,w,h);stage.addActor(l);return l;}
 private Label centered(String text,float x,float y,float w,float h,float scale,Color color){Label l=label(text,x,y,w,h,scale,color);l.setAlignment(Align.center);return l;}
 private TextButton button(String text,float x,float y,float w,float h,final Runnable action){TextButton b=new TextButton(text,skin);b.getLabel().setFontScale(.34f);b.setBounds(x,y,w,h);b.addListener(new ChangeListener(){public void changed(ChangeEvent e,Actor a){stage.setKeyboardFocus(null);action.run();}});stage.addActor(b);return b;}
 private void title(String section){label(section,56,597,1160,52,1.12f,WHITE);}
 public void rebuild(){
  stage.clear();shown=game.mining.state;resources=status=word=input=progress=stats=robotInfo=laserInfo=planetInfo=settingsInfo=null;robotButton=laserButton=planetButton=null;
  label("N / M     NEBULA MINER",40,667,520,30,.62f,CYAN);
  label("ORBITAL MINING DIVISION   /   2086",790,667,450,30,.43f,MUTED).setAlignment(Align.right);
  toast=centered("",40,18,1200,35,.60f,AMBER);
  switch(shown){
  case TITLE:
   label("宇宙は、あなたの一打で動き出す。",72,533,700,40,.75f,CYAN);
   label("NEBULA",66,417,760,105,2.8f,WHITE);label("M I N E R",72,331,720,85,1.85f,WHITE);
   label("打ち込め。掘り出せ。まだ見ぬ惑星へ。",76,280,700,40,.69f,MUTED);
   button("採掘を開始  →",76,187,290,65,new Runnable(){public void run(){game.ready();}});
   TextButton cont=button("つづきから · 基地",380,187,260,65,new Runnable(){public void run(){game.go(GameState.BASE);}});cont.setDisabled(!game.hadSave);
   label(game.hadSave?"保存された基地を再開できます":"初めての航海です · まずは60秒の採掘へ",76,145,690,30,.5f,MUTED);
   button("操作方法",76,76,170,44,new Runnable(){public void run(){game.help=!game.help;rebuild();}});
   button("設定",258,76,130,44,new Runnable(){public void run(){game.settings();}});
   if(game.help){label("英数字入力で表示のローマ字をタイプ。正解で採掘、ミスでコンボ解除。\nEscで一時停止。終了後は基地で設備購入。保存はこのブラウザのみ。",420,64,820,72,.51f,WHITE);}
   break;
  case READY:
   title("搭乗チェック / READY");
   centered("60秒間、星を掘り出そう。",120,440,1040,70,1.3f,WHITE);
   centered("日本語の問題をローマ字で入力 · shi / si などに対応",120,366,1040,50,.72f,CYAN);
   centered("IMEをオフにして英数字入力へ。ミスしても、その文字から続けられます。\n20秒ごとに流星群！ コンボをつなげて資源を増やそう。",120,260,1040,85,.65f,MUTED);
   button("準備完了 · 採掘開始",430,144,420,68,new Runnable(){public void run(){game.start();}});
   button("基地へ",530,78,220,45,new Runnable(){public void run(){game.go(GameState.BASE);}});break;
  case MINING:
   resources=label("",48,604,1180,44,.65f,WHITE);
   status=label("",48,553,1180,40,.65f,CYAN);
   centered("TYPE TO EXTRACT",300,250,680,25,.45f,MUTED);
   word=centered("",200,177,880,70,1.5f,WHITE);
   input=centered("",200,119,880,52,1.05f,CYAN);
   progress=centered("",200,73,880,36,.53f,MUTED);
   button("一時停止 / Esc",1030,555,200,44,new Runnable(){public void run(){game.pauseMining();}});break;
  case PAUSED:
   title("一時停止 / PAUSED");
   centered("採掘タイマーを停止しています",160,410,960,70,1.2f,WHITE);
   centered("タブを離れたため、または Esc で一時停止しました。\n自動生産は継続します。英数字入力とフォーカスを確認してください。",150,302,980,90,.65f,MUTED);
   button("採掘を再開",430,190,420,64,new Runnable(){public void run(){game.resumeMining();}});
   button("設定",430,127,200,45,new Runnable(){public void run(){game.settings();}});
   button("採掘を中断 · 基地へ",650,127,260,45,new Runnable(){public void run(){game.save();game.go(GameState.BASE);}});break;
  case RESULT:
   title("採掘レポート / MISSION COMPLETE");
   centered(game.mining.newBest?"自己ベスト更新！":"おつかれさま、オペレーター。",160,490,960,60,1.1f,AMBER);
   stats=centered("",180,233,920,235,.83f,WHITE);
   button("もう一度採掘",320,123,300,62,new Runnable(){public void run(){game.ready();}});
   button("基地へ戻る →",650,123,300,62,new Runnable(){public void run(){game.hadSave=true;game.go(GameState.BASE);}});break;
  case BASE:
   title("採掘基地 / OPERATIONS");
   resources=label("",56,542,1160,45,.67f,AMBER);
   label("AUTOMATION ONLINE",64,467,530,35,.54f,CYAN);
   status=label("",64,410,550,50,.84f,WHITE);
   label("タイピングで資源を集め、ドローンで生産を自動化。\n次の航路：火星 · 鉱石700 ＋ レア鉱石4",64,330,550,70,.57f,MUTED);
   button("採掘を開始  →",64,147,410,68,new Runnable(){public void run(){game.ready();}});
   button("設定",64,80,190,43,new Runnable(){public void run(){game.settings();}});
   button("タイトル",270,80,204,43,new Runnable(){public void run(){game.go(GameState.TITLE);}});
   robotInfo=label("",672,418,550,95,.60f,WHITE);
   robotButton=button("",672,365,540,44,new Runnable(){public void run(){game.buyRobot();}});
   laserInfo=label("",672,260,550,90,.60f,WHITE);
   laserButton=button("",672,207,540,44,new Runnable(){public void run(){game.buyLaser();}});
   planetInfo=label("",672,114,550,80,.58f,WHITE);
   planetButton=button("",672,65,540,44,new Runnable(){public void run(){game.travel();}});break;
  case SETTINGS:
   title("設定 / FLIGHT PREFERENCES");
   settingsInfo=centered("",150,457,980,65,.75f,WHITE);
   button("ミュート切替",210,372,260,56,new Runnable(){public void run(){game.player.mute=!game.player.mute;game.save();}});
   button("音量 −",490,372,180,56,new Runnable(){public void run(){game.player.volume=Math.max(0,game.player.volume-.1f);game.save();}});
   button("音量 ＋",690,372,180,56,new Runnable(){public void run(){game.player.volume=Math.min(1,game.player.volume+.1f);game.save();}});
   button("演出軽減を切替",210,297,660,56,new Runnable(){public void run(){game.player.reducedMotion=!game.player.reducedMotion;game.save();}});
   centered("このブラウザに自動保存します。保存データを削除すると進捗を失います。\nクラウド同期はありません。PCの物理キーボードでプレイしてください。",150,200,980,70,.56f,MUTED);
   button(game.resetConfirm?"本当に全データを削除する":"セーブデータをリセット",210,120,420,50,new Runnable(){public void run(){game.reset();}});
   button(game.resetConfirm?"キャンセルして戻る":"戻る",660,120,270,50,new Runnable(){public void run(){game.closeSettings();}});break;
  }
 }
 public void render(float dt){
  if(shown!=game.mining.state)rebuild();PlayerData p=game.player;MiningSystem m=game.mining;
  if(resources!=null)resources.setText("鉱石  "+NumberFormatter.number(p.ore)+"     レア  "+p.rare+"     自動生産  "+NumberFormatter.decimal(p.production())+" /秒     "+p.planet.title);
  if(shown==GameState.MINING){
   status.setText("残り "+(int)Math.ceil(Math.max(0,60-m.elapsed))+" 秒     "+m.combo+" COMBO   ×"+NumberFormatter.decimal(GameBalance.comboMultiplier(m.combo))+(m.meteor()?"     流星群 ×2 · 残り "+(int)Math.ceil(m.eventRemaining())+"秒":"     次の流星群 "+(int)Math.ceil(20-m.elapsed%20)+"秒"));
   word.setText(m.typing.word().japanese);
   String target=m.typing.target();int n=m.typing.entered().length();input.setText("[#79e9e8]"+target.substring(0,n)+"[][#"+(game.errorFlash>0?"ff9d9d":"e9f3ff")+"]"+target.substring(n)+"[]");
   progress.setText("入力 "+n+" / "+target.length()+"     採掘 +"+NumberFormatter.number(m.earned)+"     レア +"+m.rareEarned+"     半角英数字入力 / Escで一時停止");
  }
  if(shown==GameState.RESULT)stats.setText("完成した単語   "+m.words+"     正確率   "+NumberFormatter.decimal(m.accuracy())+"%\n\n最大コンボ   "+m.maxCombo+"     入力速度   "+NumberFormatter.number(m.kpm())+" KPM\n\n獲得鉱石   +"+NumberFormatter.number(m.earned)+"     レア鉱石   +"+m.rareEarned);
  if(shown==GameState.BASE){
   status.setText(p.robots+"台の採掘ドローンが稼働中");
   robotInfo.setText("01  採掘ドローン · "+p.robots+"台\n現在 "+NumberFormatter.decimal(p.production())+" /秒 → 購入後 "+NumberFormatter.decimal(p.production()+1)+" /秒");
   laserInfo.setText("02  レーザー出力 · Lv."+p.laser+"\nタイピング報酬 ×"+NumberFormatter.decimal(p.laserMultiplier())+" → ×"+NumberFormatter.decimal(p.laserMultiplier()+.2));
   robotButton.setText(p.robots>=60?"最大台数に到達":buyText("配備",EconomySystem.robotCost(p),p.ore));
   laserButton.setText(p.laser>=60?"最大レベルに到達":buyText("強化",EconomySystem.laserCost(p),p.ore));
   robotButton.setDisabled(p.robots>=60||p.ore<EconomySystem.robotCost(p));laserButton.setDisabled(p.laser>=60||p.ore<EconomySystem.laserCost(p));
   planetInfo.setText(p.mars?"03  火星航路 · 解放済み\n火星のタイピング報酬 ×1.6 / 月面 ×1.0":"03  火星航路 · 未解放（報酬 ×1.6）\n必要：鉱石 "+NumberFormatter.number(Math.min(700,p.ore))+" / 700  · レア "+Math.min(4,p.rare)+" / 4");
   planetButton.setText(p.mars?(p.planet==Planet.MOON?"火星へ移動 →":"月面へ移動 →"):(p.ore>=700&&p.rare>=4?"火星航路を解放 →":"不足：鉱石 "+NumberFormatter.number(Math.max(0,700-p.ore))+" / レア "+Math.max(0,4-p.rare)));
   planetButton.setDisabled(!p.mars&&(p.ore<700||p.rare<4));
  }
  if(settingsInfo!=null)settingsInfo.setText("音量 "+Math.round(p.volume*100)+"%   /   "+(p.mute?"ミュート ON":"サウンド ON")+"   /   演出軽減 "+(p.reducedMotion?"ON":"OFF"));
  toast.setText(game.notice);stage.act(dt);stage.draw();
 }
 private String buyText(String verb,double cost,double ore){return ore>=cost?verb+" · 鉱石 "+NumberFormatter.number(cost):"費用 "+NumberFormatter.number(cost)+" · あと "+NumberFormatter.number(cost-ore)+" 必要";}
 public void resize(int w,int h){stage.getViewport().update(w,h,true);}
 public void dispose(){stage.dispose();skin.dispose();}
}
