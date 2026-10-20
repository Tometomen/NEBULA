package com.nebula.system;
import com.nebula.model.*;
import com.nebula.data.GameBalance;
public final class EconomySystem {
 private EconomySystem(){}
 public static double robotCost(PlayerData p){return Math.ceil(GameBalance.ROBOT_COST*Math.pow(GameBalance.ROBOT_GROWTH,p.robots)-1e-7);}
 public static double laserCost(PlayerData p){return Math.ceil(GameBalance.LASER_COST*Math.pow(GameBalance.LASER_GROWTH,p.laser)-1e-7);}
 public static boolean buyRobot(PlayerData p){double cost=robotCost(p);if(p.robots>=GameBalance.MAX_UPGRADE||p.ore<cost)return false;p.ore-=cost;p.robots++;return true;}
 public static boolean buyLaser(PlayerData p){double cost=laserCost(p);if(p.laser>=GameBalance.MAX_UPGRADE||p.ore<cost)return false;p.ore-=cost;p.laser++;return true;}
 public static boolean unlockMars(PlayerData p){if(p.mars||p.ore<GameBalance.MARS_COST||p.rare<GameBalance.MARS_RARE)return false;p.ore-=GameBalance.MARS_COST;p.rare-=GameBalance.MARS_RARE;p.mars=true;p.planet=Planet.MARS;return true;}
 public static double produce(PlayerData p,double seconds){double before=p.ore;if(!Double.isNaN(seconds)&&seconds>0)p.addOre(p.production()*Math.min(seconds,GameBalance.OFFLINE_CAP));return p.ore-before;}
 public static double offline(PlayerData p,long now){return p.savedAt>0 ? produce(p,Math.max(0,((double)now-p.savedAt)/1000)) : 0;}
}
