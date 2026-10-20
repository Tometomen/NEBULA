package com.nebula.model;
import com.nebula.data.GameBalance;
public class PlayerData {
 public double ore; public int rare,robots,laser,bestWords,bestCombo,rareMisses;
 public boolean mars,mute,reducedMotion; public float volume=.5f; public long savedAt;
 public Planet planet=Planet.MOON;
 public double production(){return robots*GameBalance.ROBOT_RATE;}
 public double laserMultiplier(){return 1+laser*GameBalance.LASER_GAIN;}
 public void addOre(double amount){if(amount>0)ore=GameBalance.safe(ore+amount);}
}
