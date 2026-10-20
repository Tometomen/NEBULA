package com.nebula;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import com.badlogic.gdx.Preferences;
import com.nebula.model.*;
import com.nebula.save.SaveManager;
public class SaveManagerTest {
 public static class MemoryPreferences implements Preferences {
  final Map<String,Object> map=new HashMap<String,Object>();int flushes;
  public Preferences putBoolean(String k,boolean v){map.put(k,v);return this;}public Preferences putInteger(String k,int v){map.put(k,v);return this;}public Preferences putLong(String k,long v){map.put(k,v);return this;}public Preferences putFloat(String k,float v){map.put(k,v);return this;}public Preferences putString(String k,String v){map.put(k,v);return this;}public Preferences put(Map<String,?> v){map.putAll(v);return this;}
  public boolean getBoolean(String k){return getBoolean(k,false);}public boolean getBoolean(String k,boolean d){return map.containsKey(k)?(Boolean)map.get(k):d;}
  public int getInteger(String k){return getInteger(k,0);}public int getInteger(String k,int d){return map.containsKey(k)?(Integer)map.get(k):d;}
  public long getLong(String k){return getLong(k,0);}public long getLong(String k,long d){return map.containsKey(k)?(Long)map.get(k):d;}
  public float getFloat(String k){return getFloat(k,0);}public float getFloat(String k,float d){return map.containsKey(k)?(Float)map.get(k):d;}
  public String getString(String k){return getString(k,"");}public String getString(String k,String d){return map.containsKey(k)?(String)map.get(k):d;}
  public Map<String,?> get(){return map;}public boolean contains(String k){return map.containsKey(k);}public void clear(){map.clear();}public void remove(String k){map.remove(k);}public void flush(){flushes++;}
 }
 @Test public void roundTripAllProgressAndSettings(){MemoryPreferences p=new MemoryPreferences();SaveManager s=new SaveManager(p);PlayerData d=new PlayerData();d.ore=1234.5;d.rare=8;d.robots=3;d.laser=2;d.mars=true;d.planet=Planet.MARS;d.mute=true;d.reducedMotion=true;d.volume=.3f;d.bestWords=24;d.bestCombo=12;d.rareMisses=7;s.save(d,123456);PlayerData r=s.load();assertTrue(s.exists());assertEquals(1234.5,r.ore,0);assertEquals(8,r.rare);assertEquals(3,r.robots);assertEquals(2,r.laser);assertEquals(Planet.MARS,r.planet);assertTrue(r.mars&&r.mute&&r.reducedMotion);assertEquals(.3f,r.volume,0);assertEquals(24,r.bestWords);assertEquals(12,r.bestCombo);assertEquals(7,r.rareMisses);assertEquals(123456,r.savedAt);assertEquals(1,p.flushes);}
 @Test public void absentAndLegacyFieldsUseDefaults(){MemoryPreferences p=new MemoryPreferences();SaveManager s=new SaveManager(p);assertFalse(s.exists());assertEquals(0,s.load().ore,0);p.putString("version","0");p.putString("ore","12.5");assertEquals(12.5,s.load().ore,0);assertEquals(Planet.MOON,s.load().planet);assertEquals(.5f,s.load().volume,0);}
 @Test public void corruptedValuesAreSanitized(){MemoryPreferences p=new MemoryPreferences();p.putString("version","1");p.putString("ore","NaN");p.putString("robots","Infinity");p.putString("rare","-99");p.putString("laser","999999");p.putInteger("volume",123);p.putString("planet","BROKEN");PlayerData d=new SaveManager(p).load();assertEquals(0,d.ore,0);assertEquals(0,d.robots);assertEquals(0,d.rare);assertEquals(60,d.laser);assertEquals(.5f,d.volume,0);assertEquals(Planet.MOON,d.planet);}
 @Test public void resetDeletesAndFlushes(){MemoryPreferences p=new MemoryPreferences();SaveManager s=new SaveManager(p);s.save(new PlayerData(),10);s.reset();assertFalse(s.exists());assertTrue(p.map.isEmpty());assertEquals(2,p.flushes);}
}
