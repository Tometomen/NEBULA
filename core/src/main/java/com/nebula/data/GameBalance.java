package com.nebula.data;
public final class GameBalance {
 private GameBalance() {}
 public static final double SESSION_SECONDS=60, EVENT_INTERVAL=20, EVENT_SECONDS=5;
 public static final double KEY_ORE=1, WORD_ORE=6, ROBOT_COST=100, ROBOT_GROWTH=1.6, ROBOT_RATE=1;
 public static final double LASER_COST=150, LASER_GROWTH=1.8, LASER_GAIN=.2;
 public static final double MARS_COST=700, RARE_CHANCE=.15, MAX_RESOURCE=1e15, OFFLINE_CAP=14400;
 public static final int MARS_RARE=4, RARE_PITY=10, MAX_UPGRADE=60;
 public static final int[] COMBO_THRESHOLDS={0,3,5,10};
 public static final double[] COMBO_MULTIPLIERS={1,1.2,1.5,2};
 public static double comboMultiplier(int combo) { double v=1; for(int i=0;i<COMBO_THRESHOLDS.length;i++) if(combo>=COMBO_THRESHOLDS[i])v=COMBO_MULTIPLIERS[i]; return v; }
 public static double safe(double v) { return Double.isNaN(v)||v<0 ? 0 : Math.min(v,MAX_RESOURCE); }
}
