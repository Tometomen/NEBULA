package com.nebula.ui;
import java.util.Random;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.nebula.NebulaMinerGame;
import com.nebula.model.*;

/** Procedural 2D art, no external image assets or runtime font generation. */
public class WorldRenderer {
 private final NebulaMinerGame game;private final ShapeRenderer shapes=new ShapeRenderer();
 private final float[] stars=new float[300];private float time;
 private static final Color CYAN=Color.valueOf("79e9e8");
 public WorldRenderer(NebulaMinerGame game){this.game=game;Random r=new Random(2086);for(int i=0;i<stars.length;i+=3){stars[i]=r.nextInt(1280);stars[i+1]=r.nextInt(720);stars[i+2]=.5f+r.nextFloat()*1.5f;}}
 private void color(String hex){shapes.setColor(Color.valueOf(hex));}
 public void render(float dt){
  if(!game.player.reducedMotion)time+=dt;
  game.view.stage.getViewport().apply();shapes.setProjectionMatrix(game.view.stage.getCamera().combined);
  Gdx.gl.glEnable(GL20.GL_BLEND);Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
  shapes.begin(ShapeRenderer.ShapeType.Filled);
  // Broad, translucent nebula clouds keep the center spacious.
  shapes.setColor(.17f,.12f,.34f,.055f);for(int i=0;i<8;i++)shapes.ellipse(470+i*45,280+i*16,600-i*35,350-i*15,60);
  for(int i=0;i<stars.length;i+=3){shapes.setColor(.65f,.78f,.94f,.25f+stars[i+2]*.18f);shapes.circle(stars[i],stars[i+1],stars[i+2],6);}
  GameState s=game.mining.state;boolean mining=s==GameState.MINING;
  if(s==GameState.TITLE){planet(1015,420,208);ship(965,158,.65f);}
  else if(mining){planet(1140,455,74);surface();asteroid(835,384,73);ship(276,354,1);
   if(game.hitFlash>0){float thickness=2+Math.min(5,game.mining.combo*.3f)+game.player.laser*.14f;
    shapes.setColor(.3f,.8f,1,.09f);shapes.rectLine(365,378,803,393,thickness*6);
    shapes.setColor(game.mining.meteor()?Color.valueOf("c2a9ff"):CYAN);shapes.rectLine(365,378,803,393,thickness);
    shapes.setColor(Color.WHITE);shapes.rectLine(365,378,803,393,1.5f);
    shapes.circle(803,393,8,16);
   }
   if(game.breakFlash>0&&!game.player.reducedMotion){shapes.setColor(1,.78f,.4f,game.breakFlash);for(int i=0;i<10;i++){float a=i*36;float d=80+(1-game.breakFlash/.4f)*48;shapes.circle(835+MathUtils.cosDeg(a)*d,384+MathUtils.sinDeg(a)*d,3,6);}}
   if(game.mining.meteor()){shapes.setColor(.75f,.6f,1,.55f);for(int i=0;i<7;i++){float x=(time*100+i*197)%1500;float y=510+i%3*30;shapes.rectLine(x,y,x-40,y+16,2);}}
   drones(480,325);
  } else if(s==GameState.BASE){planet(405,255,67);ship(310,253,.55f);drones(400,260);
   if(game.breakFlash>0&&!game.player.reducedMotion){shapes.setColor(.5f,.8f,1,Math.min(.6f,game.breakFlash));for(int i=0;i<6;i++){float a=i*60;float d=90+(1-game.breakFlash)*45;float x=405+MathUtils.cosDeg(a)*d,y=255+MathUtils.sinDeg(a)*d;shapes.rectLine(x,y,x+MathUtils.cosDeg(a)*25,y+MathUtils.sinDeg(a)*25,2);}}
  }
  else {planet(1140,390,145);}
  // Quiet frame: one top hairline and bottom horizon.
  color("233449");shapes.rect(40,656,1200,1);shapes.rect(40,58,1200,1);
  if(mining){shapes.setColor(.12f,.2f,.27f,1);shapes.rect(48,535,1184,3);shapes.setColor(CYAN);shapes.rect(48,535,(float)(1184*(1-game.mining.elapsed/60)),3);}
  shapes.end();Gdx.gl.glDisable(GL20.GL_BLEND);
 }
 private void planet(float x,float y,float r){
  boolean mars=game.player.planet==Planet.MARS;
  shapes.setColor(mars?new Color(.8f,.3f,.18f,.025f):new Color(.3f,.55f,.9f,.025f));
  for(int i=5;i>0;i--)shapes.circle(x,y,r+i*8,70);
  color(mars?"4c2a2c":"1b2e48");shapes.circle(x,y,r,80);
  color(mars?"6d3930":"2e4764");shapes.circle(x-r*.15f,y+r*.12f,r*.86f,80);
  color(mars?"572c2c":"253b56");shapes.circle(x+r*.20f,y-r*.04f,r*.78f,70);
  color(mars?"4b282a":"20334c");shapes.circle(x-r*.36f,y+r*.25f,r*.18f,32);shapes.circle(x-r*.38f,y-r*.28f,r*.1f,24);
  color(mars?"8a4936":"3b5671");shapes.circle(x-r*.37f,y+r*.28f,r*.13f,32);
  shapes.setColor(.75f,.9f,1,.13f);for(int i=0;i<40;i++){float a=55+i*3;float rr=r+21;shapes.rectLine(x+MathUtils.cosDeg(a)*rr,y+MathUtils.sinDeg(a)*rr,x+MathUtils.cosDeg(a+1)*rr,y+MathUtils.sinDeg(a+1)*rr,1);}
 }
 private void surface(){color(game.player.planet==Planet.MARS?"251b26":"111e30");shapes.triangle(0,290,220,299,0,264);shapes.triangle(220,299,460,285,0,264);shapes.rect(0,263,460,16);color("34475a");shapes.rect(160,284,220,3);}
 private void ship(float x,float y,float z){
  color("0f1a2c");shapes.rect(x-70*z,y-32*z,160*z,65*z);
  color("30465e");shapes.triangle(x-86*z,y-16*z,x-60*z,y+36*z,x-26*z,y-16*z);shapes.rect(x-45*z,y-16*z,105*z,56*z);
  color("52667d");shapes.rect(x-34*z,y+15*z,60*z,35*z);color("12263a");shapes.rect(x-25*z,y+22*z,42*z,19*z);
  shapes.setColor(CYAN);shapes.rect(x-22*z,y+25*z,34*z,3*z);shapes.rect(x-51*z,y+4*z,6*z,17*z);
  color("8196a7");shapes.rect(x+28*z,y+17*z,78*z,12*z);color("38546a");shapes.rect(x+25*z,y+11*z,47*z,24*z);
  shapes.setColor(CYAN);shapes.rect(x+98*z,y+16*z,8*z,14*z);
  color("385069");shapes.rect(x-46*z,y-32*z,15*z,19*z);shapes.rect(x+35*z,y-32*z,15*z,19*z);
  shapes.setColor(.5f,.85f,1,.1f);shapes.ellipse(x-75*z,y-43*z,155*z,12*z,24);
 }
 private void asteroid(float x,float y,float r){
  float drift=game.player.reducedMotion?0:MathUtils.sin(time*.6f)*3;y+=drift;
  shapes.setColor(.25f,.47f,.58f,.05f);for(int i=3;i>0;i--)shapes.circle(x,y,r+i*8,8);
  for(int i=0;i<8;i++){float a=i*45+15,b=a+45;float ra=r*(i%2==0?1:.87f),rb=r*(i%2==0?.87f:1);color(i%3==0?"587789":i%3==1?"344c63":"263b54");shapes.triangle(x,y,x+MathUtils.cosDeg(a)*ra,y+MathUtils.sinDeg(a)*ra,x+MathUtils.cosDeg(b)*rb,y+MathUtils.sinDeg(b)*rb);}
  color("1e3249");shapes.circle(x+21,y+14,17,6);shapes.circle(x-25,y-27,10,6);
  shapes.setColor(game.mining.meteor()?Color.valueOf("bca1f0"):CYAN);shapes.triangle(x-29,y+24,x-18,y+40,x-15,y+15);shapes.triangle(x+12,y-8,x+20,y+5,x+27,y-16);
 }
 private void drones(float x,float y){int count=Math.min(game.player.robots,8);for(int i=0;i<count;i++){float dx=x+i%4*52,dy=y+i/4*39+(game.player.reducedMotion?0:MathUtils.sin(time*1.5f+i)*5);color("52667e");shapes.rect(dx,dy,20,11);shapes.setColor(CYAN);shapes.rect(dx+7,dy+3,8,4);color("2a4056");shapes.rect(dx-6,dy+8,30,3);}}
 public void dispose(){shapes.dispose();}
}
