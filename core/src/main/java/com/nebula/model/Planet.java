package com.nebula.model;
public enum Planet {
 MOON("月面採掘基地", "LUNA / SECTOR 01", 1), MARS("火星採掘エリア", "MARS / SECTOR 02", 1.6);
 public final String title,sector; public final double multiplier;
 Planet(String title,String sector,double multiplier){this.title=title;this.sector=sector;this.multiplier=multiplier;}
}
