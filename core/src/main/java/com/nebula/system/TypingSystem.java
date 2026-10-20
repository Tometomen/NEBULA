package com.nebula.system;
import com.nebula.model.TypingWord;
public class TypingSystem {
 public enum Verdict { CORRECT, COMPLETE, WRONG, IME, IGNORED }
 private TypingWord word; private String entered="";
 public void setWord(TypingWord next){word=next;entered="";}
 public TypingWord word(){return word;} public String entered(){return entered;}
 public String target(){for(String v:word.variants)if(v.startsWith(entered))return v;return word.variants[0];}
 public Verdict input(char ch){
  if(ch>127)return Verdict.IME;
  ch=Character.toLowerCase(ch);
  if(ch<' '||ch==127)return Verdict.IGNORED;
  String trial=entered+ch;
  for(String v:word.variants) if(v.equals(trial)){entered=trial;return Verdict.COMPLETE;}
  for(String v:word.variants) if(v.startsWith(trial)){entered=trial;return Verdict.CORRECT;}
  return Verdict.WRONG;
 }
}
