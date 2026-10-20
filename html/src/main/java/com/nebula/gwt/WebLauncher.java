package com.nebula.gwt;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.*;
import com.nebula.NebulaMinerGame;
public class WebLauncher extends GwtApplication {
 public GwtApplicationConfiguration getConfig() { GwtApplicationConfiguration c = new GwtApplicationConfiguration(true); c.padHorizontal=0; c.padVertical=0; return c; }
 public ApplicationListener createApplicationListener() { NebulaMinerGame game=new NebulaMinerGame(); game.browserReducedMotion=reduced();
 game.observer=new NebulaMinerGame.Observer(){public void update(NebulaMinerGame g){
  String target=g.mining.typing.word()==null?"":g.mining.typing.target();
  String word=g.mining.typing.word()==null?"":g.mining.typing.word().japanese;
  publish(g.mining.state.name(),word,target,g.notice,g.player.ore,g.player.rare,g.player.robots,g.player.laser,g.player.planet.name(),g.mining.elapsed,g.mining.words,g.mining.combo,g.mining.misses,g.player.mute,g.player.reducedMotion);
 }}; hook(game); return game; }
 private static native void publish(String state,String word,String target,String notice,double ore,int rare,int robots,int laser,String planet,double elapsed,int words,int combo,int misses,boolean mute,boolean reduced) /*-{
  $wnd.nebulaSnapshot=Object.freeze({state:state,word:word,target:target,ore:ore,rare:rare,robots:robots,laser:laser,planet:planet,elapsed:elapsed,words:words,combo:combo,misses:misses,mute:mute,reduced:reduced});
  var el=$doc.getElementById('game-status');var text=state+' '+word+' '+notice;
  if(el && el.textContent!==text)el.textContent=text;
 }-*/;
 private static native boolean reduced() /*-{ return !!($wnd.matchMedia && $wnd.matchMedia('(prefers-reduced-motion: reduce)').matches); }-*/;
 private static native void hook(NebulaMinerGame game) /*-{
  var pause=$entry(function(){ if(game.@com.nebula.NebulaMinerGame::mining) game.@com.nebula.NebulaMinerGame::pause()(); });
  var ime=$entry(function(){ game.@com.nebula.NebulaMinerGame::notify(Ljava/lang/String;)("英数字入力に切り替えてください（IMEをオフ）"); });
  $doc.addEventListener('compositionstart',ime);
  $doc.addEventListener('keydown',function(e){if(e.isComposing || e.keyCode===229)ime();});
  $doc.addEventListener('visibilitychange',function(){if($doc.hidden)pause();});
  $wnd.addEventListener('blur',pause);
  $wnd.addEventListener('pagehide',pause);
  var loading=$doc.getElementById('loading');if(loading)loading.style.display='none';
 }-*/;
}

