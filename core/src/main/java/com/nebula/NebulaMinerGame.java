package com.nebula;
import java.util.Random;
import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.nebula.model.*;
import com.nebula.save.SaveManager;
import com.nebula.system.*;
import com.nebula.ui.*;
import com.nebula.util.NumberFormatter;

/** Coordinates lifecycle, input, and presentation. Domain systems do not depend on rendering. */
public class NebulaMinerGame extends ApplicationAdapter {
 public PlayerData player; public MiningSystem mining; public GameView view;
 private SaveManager saves; private WorldRenderer world; private Sound hitSound,completeSound;
 private long lastFrame,lastSave; private GameState settingsReturn=GameState.TITLE;
 public String notice=""; private double noticeTime; public float hitFlash,errorFlash,breakFlash;
 public boolean hadSave,resetConfirm,help; private boolean lastMeteor;
 public interface Observer { void update(NebulaMinerGame game); }
 public Observer observer;
 public boolean browserReducedMotion;
 public void create(){
  saves=new SaveManager(Gdx.app.getPreferences("nebula-miner-v1"));hadSave=saves.exists();player=saves.load();
  if(!hadSave)player.reducedMotion=browserReducedMotion;
  double offline=EconomySystem.offline(player,System.currentTimeMillis());
  mining=new MiningSystem(player,new Random());world=new WorldRenderer(this);view=new GameView(this);
  hitSound=Gdx.audio.newSound(Gdx.files.internal("hit.wav"));completeSound=Gdx.audio.newSound(Gdx.files.internal("complete.wav"));
  lastFrame=lastSave=System.currentTimeMillis();
  if(offline>0)notify("おかえりなさい！ 放置生産 +"+NumberFormatter.number(offline)+" 鉱石（最大4時間）");
  Gdx.input.setInputProcessor(new InputMultiplexer(view.stage,new InputAdapter(){
   public boolean keyTyped(char c){
    if(mining.state!=GameState.MINING)return false;
    process(c);return true;
   }
   public boolean keyDown(int key){
    if(key==Input.Keys.ESCAPE){if(mining.state==GameState.MINING)pauseMining();else if(mining.state==GameState.PAUSED)resumeMining();return true;}
    return false;
   }
  }));
  save();
 }
 private void process(char c){
  // Charge real elapsed time before input so a key arriving after the deadline cannot earn rewards.
  advanceClock();
  int rareBefore=player.rare;TypingSystem.Verdict v=mining.input(c);
  if(v==TypingSystem.Verdict.IME){notify("英数字入力に切り替えてください（IMEをオフ）");return;}
  if(v==TypingSystem.Verdict.WRONG){errorFlash=.22f;notify("MISS · コンボが途切れました。入力位置はそのままです");}
  if(v==TypingSystem.Verdict.CORRECT||v==TypingSystem.Verdict.COMPLETE){hitFlash=.10f;if(!player.mute)hitSound.play(player.volume*.14f);}
  if(v==TypingSystem.Verdict.COMPLETE){breakFlash=.4f;if(!player.mute)completeSound.play(player.volume*.25f);
   if(player.rare>rareBefore)notify("レア鉱石 +1 · 新たな資源を発見！");
   else notify("採掘成功！ "+mining.combo+" COMBO · 次の鉱石へ");
  }
 }
 private double advanceClock(){
  long now=System.currentTimeMillis();double dt=Math.max(0,(now-lastFrame)/1000.0);lastFrame=now;
  EconomySystem.produce(player,dt);mining.tick(dt);return dt;
 }
 public void render(){
  GameState before=mining.state;double raw=advanceClock();float dt=(float)Math.min(raw,.1);
  hitFlash=Math.max(0,hitFlash-dt);errorFlash=Math.max(0,errorFlash-dt);breakFlash=Math.max(0,breakFlash-dt);
  if(noticeTime>0){noticeTime-=dt;if(noticeTime<=0)notice="";}
  boolean event=mining.meteor();if(mining.state==GameState.MINING&&event!=lastMeteor)notify(event?"流星群到来！ 5秒間、タイピング報酬2倍":"流星群が通過しました · 通常採掘へ");lastMeteor=event;
  if(before!=GameState.RESULT&&mining.state==GameState.RESULT)save();
  if(System.currentTimeMillis()-lastSave>=10000)save();
  Gdx.gl.glClearColor(.025f,.045f,.09f,1);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
  world.render(dt);view.render(dt);if(observer!=null)observer.update(this);
 }
 public void notify(String message){notice=message;noticeTime=4;}
 public void go(GameState s){mining.state=s;help=false;resetConfirm=false;view.stage.setKeyboardFocus(null);}
 public void ready(){go(GameState.READY);}
 public void start(){advanceClock();lastMeteor=false;mining.start();view.stage.setKeyboardFocus(null);}
 public void pauseMining(){advanceClock();mining.pause();save();}
 public void resumeMining(){advanceClock();mining.resume();view.stage.setKeyboardFocus(null);}
 public void settings(){settingsReturn=mining.state;go(GameState.SETTINGS);}
 public void closeSettings(){save();go(settingsReturn);}
 public void buyRobot(){advanceClock();if(EconomySystem.buyRobot(player)){notify("採掘ドローンを配備しました · 自動生産 +1 /秒");save();}else notify(player.robots>=60?"最大台数です":"鉱石があと "+NumberFormatter.number(EconomySystem.robotCost(player)-player.ore)+" 必要です");}
 public void buyLaser(){if(EconomySystem.buyLaser(player)){notify("レーザー出力を強化しました");save();}else notify(player.laser>=60?"最大レベルです":"鉱石があと "+NumberFormatter.number(EconomySystem.laserCost(player)-player.ore)+" 必要です");}
 public void travel(){
  if(player.mars){player.planet=player.planet==Planet.MOON?Planet.MARS:Planet.MOON;notify(player.planet.title+"へ移動しました");save();}
  else if(EconomySystem.unlockMars(player)){breakFlash=1;notify("航路開通！ 火星へ到着 · タイピング報酬1.6倍");save();}
  else notify("解放まで 鉱石 "+NumberFormatter.number(Math.max(0,700-player.ore))+" / レア鉱石 "+Math.max(0,4-player.rare)+" が必要です");
 }
 public void reset(){if(!resetConfirm){resetConfirm=true;view.rebuild();return;}saves.reset();player=new PlayerData();mining=new MiningSystem(player,new Random());hadSave=false;resetConfirm=false;notice="";lastFrame=System.currentTimeMillis();view.rebuild();}
 public void save(){try{saves.save(player,System.currentTimeMillis());lastSave=System.currentTimeMillis();}catch(Exception e){notify("保存できません。ブラウザの保存設定を確認してください");}}
 public void resize(int w,int h){if(view!=null)view.resize(w,h);}
 public void pause(){if(mining!=null){pauseMining();}}
 public void resume(){/* Explicit resume button prevents accidental timer/keyboard activation. */}
 public void dispose(){if(saves!=null)save();if(view!=null)view.dispose();if(world!=null)world.dispose();if(hitSound!=null)hitSound.dispose();if(completeSound!=null)completeSound.dispose();}
}
