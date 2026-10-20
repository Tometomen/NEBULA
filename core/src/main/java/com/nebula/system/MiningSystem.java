package com.nebula.system;
import java.util.Random;
import com.nebula.data.*;
import com.nebula.model.*;
public class MiningSystem {
 public final TypingSystem typing=new TypingSystem();
 public final PlayerData player; private final Random random;
 public GameState state=GameState.TITLE;
 public double elapsed,earned; public int words,hits,misses,combo,maxCombo,rareEarned;
 public boolean newBest; private int lastIndex=-1;
 public MiningSystem(PlayerData p,Random r){player=p;random=r;}
 public void ready(){state=GameState.READY;}
 public void start(){elapsed=0;earned=0;words=hits=misses=combo=maxCombo=rareEarned=0;newBest=false;state=GameState.MINING;nextWord();}
 private void nextWord(){int limit=words<3?10:WordRepository.WORDS.length;int i;do{i=random.nextInt(limit);}while(i==lastIndex);lastIndex=i;typing.setWord(WordRepository.WORDS[i]);}
 public boolean meteor(){return elapsed>=GameBalance.EVENT_INTERVAL && elapsed<GameBalance.SESSION_SECONDS && elapsed%GameBalance.EVENT_INTERVAL<GameBalance.EVENT_SECONDS;}
 public double eventRemaining(){return meteor()?GameBalance.EVENT_SECONDS-elapsed%GameBalance.EVENT_INTERVAL:0;}
 public double multiplier(){return player.laserMultiplier()*player.planet.multiplier*(meteor()?2:1);}
 private void award(double amount){double before=player.ore;player.addOre(amount);earned+=player.ore-before;}
 public TypingSystem.Verdict input(char ch){
  if(state!=GameState.MINING)return TypingSystem.Verdict.IGNORED;
  TypingSystem.Verdict result=typing.input(ch);
  if(result==TypingSystem.Verdict.WRONG){misses++;combo=0;}
  if(result==TypingSystem.Verdict.CORRECT||result==TypingSystem.Verdict.COMPLETE){hits++;award(GameBalance.KEY_ORE*multiplier());}
  if(result==TypingSystem.Verdict.COMPLETE){words++;combo++;maxCombo=Math.max(maxCombo,combo);award(GameBalance.WORD_ORE*GameBalance.comboMultiplier(combo)*multiplier());
   if(rareDrop(player,random.nextDouble()))rareEarned++;
   nextWord();
  }
  return result;
 }
 public static boolean rareDrop(PlayerData p,double roll){if(p.rareMisses>=GameBalance.RARE_PITY||roll<GameBalance.RARE_CHANCE){p.rare=Math.min(1000000000,p.rare+1);p.rareMisses=0;return true;}p.rareMisses++;return false;}
 public void tick(double dt){if(state!=GameState.MINING||dt<=0||Double.isNaN(dt))return;elapsed=Math.min(GameBalance.SESSION_SECONDS,elapsed+dt);if(elapsed>=GameBalance.SESSION_SECONDS)finish();}
 private void finish(){state=GameState.RESULT;newBest=words>player.bestWords;player.bestWords=Math.max(player.bestWords,words);player.bestCombo=Math.max(player.bestCombo,maxCombo);}
 public void pause(){if(state==GameState.MINING)state=GameState.PAUSED;}
 public void resume(){if(state==GameState.PAUSED)state=GameState.MINING;}
 public double accuracy(){return hits+misses==0?100:100.0*hits/(hits+misses);}
 public double kpm(){return elapsed>0?hits*60/elapsed:0;}
}
