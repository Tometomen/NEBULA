package com.nebula.util;
public final class NumberFormatter {
 private NumberFormatter(){}
 public static String number(double value){
  if(Double.isNaN(value)||value<0)value=0;
  if(value>=1e12)return decimal(value/1e12)+"兆";
  if(value>=1e8)return decimal(value/1e8)+"億";
  if(value>=1e4)return decimal(value/1e4)+"万";
  String s=Long.toString((long)value);String out="";for(int i=0;i<s.length();i++){if(i>0&&(s.length()-i)%3==0)out+=",";out+=s.charAt(i);}return out;
 }
 public static String decimal(double v){long tenths=Math.round(v*10);return Long.toString(tenths/10)+"."+(tenths%10);}
}
