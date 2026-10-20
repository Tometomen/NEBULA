package com.nebula.lwjgl3;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.nebula.NebulaMinerGame;
public class DesktopLauncher {
 public static void main(String[] args) {
  Lwjgl3ApplicationConfiguration c = new Lwjgl3ApplicationConfiguration();
  c.setPreferencesConfig(System.getProperty("nebula.preferences",System.getProperty("user.home")+"/.nebula-miner/"),com.badlogic.gdx.Files.FileType.Absolute);
  c.setTitle("NEBULA MINER"); c.setWindowedMode(1280,720); c.setForegroundFPS(60); c.useVsync(true);
  new Lwjgl3Application(new NebulaMinerGame(),c);
 }
}
