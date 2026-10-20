package com.nebula;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import com.nebula.data.*;
import com.nebula.model.*;
import com.nebula.system.*;
import com.nebula.util.NumberFormatter;
public class GameSystemsTest {
 private MiningSystem session(){MiningSystem m=new MiningSystem(new PlayerData(),new Random(42));m.start();return m;}
 private void complete(MiningSystem m){String v=m.typing.target();int n=m.typing.entered().length();for(int i=n;i<v.length();i++)m.input(v.charAt(i));}
 @Test public void everyWordVariantCompletes(){assertTrue(WordRepository.WORDS.length>=30);for(TypingWord w:WordRepository.WORDS)for(String v:w.variants){TypingSystem t=new TypingSystem();t.setWord(w);for(int i=0;i<v.length();i++){TypingSystem.Verdict result=t.input(v.charAt(i));assertEquals(w.japanese+" / "+v,i==v.length()-1?TypingSystem.Verdict.COMPLETE:TypingSystem.Verdict.CORRECT,result);}}}
 @Test public void uppercaseAccepted(){TypingSystem t=new TypingSystem();t.setWord(new TypingWord("ほし","hoshi","hosi"));assertEquals(TypingSystem.Verdict.CORRECT,t.input('H'));assertEquals("h",t.entered());}
 @Test public void wrongInputDoesNotAdvance(){TypingSystem t=new TypingSystem();t.setWord(new TypingWord("つき","tsuki","tuki"));t.input('t');assertEquals(TypingSystem.Verdict.WRONG,t.input('z'));assertEquals("t",t.entered());t.input('u');t.input('k');assertEquals(TypingSystem.Verdict.COMPLETE,t.input('i'));}
 @Test public void smallKanaSokuonAndN(){for(String v:new String[]{"robotto","roboxtuto","roboltuto","chikyuu","tikyuu","shinnya","shin'ya","sinnya"}){TypingSystem t=new TypingSystem();t.setWord(new TypingWord("test",v));for(char c:v.toCharArray())assertNotEquals(TypingSystem.Verdict.WRONG,t.input(c));assertEquals(v,t.entered());}}
 @Test public void imeIsGuidedAndNotCounted(){MiningSystem m=session();assertEquals(TypingSystem.Verdict.IME,m.input('あ'));assertEquals(0,m.hits+m.misses);assertEquals("",m.typing.entered());}
 @Test public void completedWordAdvancesAndMistakeResetsCombo(){MiningSystem m=session();TypingWord previous=m.typing.word();complete(m);assertEquals(1,m.words);assertEquals(1,m.combo);assertNotSame(previous,m.typing.word());m.input('#');assertEquals(0,m.combo);assertEquals(1,m.maxCombo);assertEquals(1,m.misses);}
 @Test public void sixtySecondsFinishAndInputLocked(){MiningSystem m=session();m.tick(59.999);assertEquals(GameState.MINING,m.state);m.tick(.001);assertEquals(GameState.RESULT,m.state);assertEquals(TypingSystem.Verdict.IGNORED,m.input('a'));assertEquals(60,m.elapsed,0);}
 @Test public void pauseDoesNotConsumeMiningTime(){MiningSystem m=session();m.tick(10);m.pause();m.tick(100);m.input('x');assertEquals(10,m.elapsed,0);assertEquals(0,m.misses);m.resume();m.tick(50);assertEquals(GameState.RESULT,m.state);}
 @Test public void comboThresholds(){int[] c={0,2,3,4,5,9,10,100};double[] expected={1,1,1.2,1.2,1.5,1.5,2,2};for(int i=0;i<c.length;i++)assertEquals(expected[i],GameBalance.comboMultiplier(c[i]),0);}
 @Test public void characterAndCompletionRewards(){MiningSystem m=session();int length=m.typing.target().length();complete(m);assertEquals(length+6,m.earned,.0001);assertEquals(m.earned,m.player.ore,.0001);}
 @Test public void meteorWindowsAndRewards(){MiningSystem m=session();m.tick(20);assertTrue(m.meteor());int len=m.typing.target().length();complete(m);assertEquals(2*(len+6),m.earned,.001);m.tick(5);assertFalse(m.meteor());m.tick(15);assertTrue(m.meteor());m.tick(5);assertFalse(m.meteor());m.tick(15);assertFalse(m.meteor());}
 @Test public void laserAndPlanetStackWithMeteor(){MiningSystem m=session();m.player.laser=2;m.player.planet=Planet.MARS;m.tick(20);int len=m.typing.target().length();complete(m);assertEquals((len+6)*1.4*1.6*2,m.earned,.0001);}
 @Test public void purchasesCostAndCompound(){PlayerData p=new PlayerData();assertFalse(EconomySystem.buyRobot(p));p.ore=500;assertTrue(EconomySystem.buyRobot(p));assertEquals(400,p.ore,0);assertEquals(160,EconomySystem.robotCost(p),0);assertTrue(EconomySystem.buyRobot(p));assertEquals(240,p.ore,0);assertEquals(256,EconomySystem.robotCost(p),0);assertFalse(EconomySystem.buyRobot(p));assertEquals(2,p.robots);assertTrue(EconomySystem.buyLaser(p));assertEquals(90,p.ore,0);assertEquals(1.2,p.laserMultiplier(),.0001);}
 @Test public void maximumLevelDoesNotSpend(){PlayerData p=new PlayerData();p.ore=1e15;p.robots=p.laser=60;assertFalse(EconomySystem.buyRobot(p));assertFalse(EconomySystem.buyLaser(p));assertEquals(1e15,p.ore,0);}
 @Test public void automationFractionalAndCapped(){PlayerData p=new PlayerData();p.robots=3;assertEquals(1.5,EconomySystem.produce(p,.5),0);assertEquals(1.5,p.ore,0);EconomySystem.produce(p,-10);EconomySystem.produce(p,Double.NaN);assertEquals(1.5,p.ore,0);EconomySystem.produce(p,1e12);assertEquals(43201.5,p.ore,0);}
 @Test public void rarePityOnEleventhCompletion(){PlayerData p=new PlayerData();for(int i=0;i<10;i++)assertFalse(MiningSystem.rareDrop(p,.99));assertEquals(0,p.rare);assertTrue(MiningSystem.rareDrop(p,.99));assertEquals(1,p.rare);assertEquals(0,p.rareMisses);assertTrue(MiningSystem.rareDrop(p,.14));assertEquals(2,p.rare);}
 @Test public void marsRequiresBothAndOnlyChargesOnce(){PlayerData p=new PlayerData();p.ore=700;assertFalse(EconomySystem.unlockMars(p));p.rare=4;p.ore=699;assertFalse(EconomySystem.unlockMars(p));p.ore=700;assertTrue(EconomySystem.unlockMars(p));assertEquals(0,p.ore,0);assertEquals(0,p.rare);assertEquals(Planet.MARS,p.planet);assertFalse(EconomySystem.unlockMars(p));}
 @Test public void offlineFutureAndFourHourCap(){PlayerData p=new PlayerData();p.robots=2;p.savedAt=100000;assertEquals(0,EconomySystem.offline(p,99999),0);assertEquals(20,EconomySystem.offline(p,110000),0);assertEquals(28800,EconomySystem.offline(p,1000000000),0);}
 @Test public void resourceOverflowAndFormatting(){PlayerData p=new PlayerData();p.addOre(Double.POSITIVE_INFINITY);assertEquals(1e15,p.ore,0);p.addOre(Double.NaN);assertEquals(1e15,p.ore,0);assertEquals("1,250",NumberFormatter.number(1250));assertEquals("1.5万",NumberFormatter.number(15000));assertEquals("0",NumberFormatter.number(Double.NaN));}
 @Test public void decimalRoundsCarry(){assertEquals("1.2",NumberFormatter.decimal(1.199999999));assertEquals("2.0",NumberFormatter.decimal(1.99999999));}
 @Test public void resultMetricsAndBest(){MiningSystem m=session();complete(m);m.input('#');int hits=m.hits;m.tick(60);assertEquals(hits*100.0/(hits+1),m.accuracy(),.001);assertEquals(hits,m.kpm(),.001);assertTrue(m.newBest);assertEquals(1,m.player.bestWords);}
}
