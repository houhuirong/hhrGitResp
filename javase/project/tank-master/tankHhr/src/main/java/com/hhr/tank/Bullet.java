package com.hhr.tank;

import java.awt.*;

/**
 * @Auther: hhr
 * @Date: 2020/8/27 - 08 - 27 - 18:29
 * @Description: com.hhr.tank
 * @version: 1.0
 */
public class Bullet {
    private static final int SPEED=2;
    public static int WIDTH=ResourceMgr.bulletD.getWidth();
    public static int HEIGHT=ResourceMgr.bulletD.getHeight();
    private int x,y;
    private Dir dir;
    public boolean living =true;
    TankFrame tf=null;
    public Bullet(int x,int y,Dir dir,TankFrame tf){
        this.x=x;
        this.y=y;
        this.dir=dir;
        this.tf=tf;
    }
    public void paint(Graphics g){
        if(!living){
            tf.bulletS.remove(this);
        }
        switch (dir){
            case LEFT:
                g.drawImage(ResourceMgr.bulletL,x,y,null);
                break;
            case RIGHT:
                g.drawImage(ResourceMgr.bulletR,x,y,null);
                break;
            case UP:
                g.drawImage(ResourceMgr.bulletU,x,y,null);
                break;
            case DOWN:
                g.drawImage(ResourceMgr.bulletD,x,y,null);
                break;
        }
        move();
    }

    private void move() {
        switch (dir) {
            case UP:
                y+=SPEED;
                break;
            case DOWN:
                y-=SPEED;
                break;
            case LEFT:
                x-=SPEED;
                break;
            case RIGHT:
                x+=SPEED;
                break;
            default:break;
        }
        if(x<0||y<0||x>TankFrame.Game_HEIGHT||y>TankFrame.GAME_WIGTH) living =false;
    }

    public void collideWith(Tank tank) {
        Rectangle rect=new Rectangle(this.x,this.y,WIDTH,HEIGHT);
        Rectangle rect2=new Rectangle(tank.getX(),tank.getY(),tank.WIDTH,tank.HEIGHT);
        if (rect.intersects(rect2)){
            tank.die();
            this.die();
        }

    }

    private void die() {
        living=false;
    }
}
