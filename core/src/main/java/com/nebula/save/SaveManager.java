package com.nebula.save;
import com.badlogic.gdx.Preferences;
import com.nebula.model.*;
import com.nebula.data.GameBalance;
public class SaveManager {
 public static final int VERSION=1; private final Preferences prefs;
 public SaveManager(Preferences prefs){this.prefs=prefs;}
 public boolean exists(){return prefs.contains("version");}
 private String read(String k,String fallback){try{return prefs.getString(k,fallback);}catch(Exception e){return fallback;}}
 private double number(String k,double fallback){try{double v=Double.parseDouble(read(k,""));return Double.isNaN(v)||Double.isInfinite(v)?fallback:v;}catch(Exception e){return fallback;}}
 private int count(String k,int cap){return (int)Math.max(0,Math.min(cap,number(k,0)));}
 public PlayerData load(){
  PlayerData p=new PlayerData();if(!exists())return p;
  p.ore=GameBalance.safe(number("ore",0));p.rare=count("rare",1000000000);p.robots=count("robots",GameBalance.MAX_UPGRADE);p.laser=count("laser",GameBalance.MAX_UPGRADE);
  p.bestWords=count("bestWords",1000000);p.bestCombo=count("bestCombo",1000000);p.rareMisses=count("rareMisses",GameBalance.RARE_PITY);
  p.mars="true".equals(read("mars","false"));p.planet=p.mars&&"MARS".equals(read("planet","MOON"))?Planet.MARS:Planet.MOON;
  p.mute="true".equals(read("mute","false"));p.reducedMotion="true".equals(read("reducedMotion","false"));p.volume=(float)Math.max(0,Math.min(1,number("volume",.5)));
  // Version 1 fields are optional. Missing/invalid older fields use defaults; unknown fields are preserved.
  p.savedAt=(long)Math.max(0,Math.min(9e15,number("savedAt",0)));return p;
 }
 public void save(PlayerData p,long now){
  p.savedAt=now;
  prefs.putString("version",Integer.toString(VERSION));prefs.putString("ore",Double.toString(p.ore));prefs.putString("rare",Integer.toString(p.rare));
  prefs.putString("robots",Integer.toString(p.robots));prefs.putString("laser",Integer.toString(p.laser));prefs.putString("mars",Boolean.toString(p.mars));prefs.putString("planet",p.planet.name());
  prefs.putString("bestWords",Integer.toString(p.bestWords));prefs.putString("bestCombo",Integer.toString(p.bestCombo));prefs.putString("rareMisses",Integer.toString(p.rareMisses));
  prefs.putString("mute",Boolean.toString(p.mute));prefs.putString("reducedMotion",Boolean.toString(p.reducedMotion));prefs.putString("volume",Float.toString(p.volume));prefs.putString("savedAt",Long.toString(now));prefs.flush();
 }
 public void reset(){prefs.clear();prefs.flush();}
}
