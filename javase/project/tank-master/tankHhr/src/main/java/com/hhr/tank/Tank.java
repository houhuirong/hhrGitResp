package com.hhr.tank;

import java.awt.*;

/**
 * @Auther: hhr
 * @Date: 2020/8/27 - 08 - 27 - 17:50
 * @Description: com.hhr.tank
 * @version: 1.0
 */
public class Tank {
    private int x,y;
    private Dir dir;
    public static int WIDTH=ResourceMgr.tankD.getWidth();
    public static int HEIGHT=ResourceMgr.tankD.getHeight();
    private static final int SPEED=5;
    private boolean moving=false;
    private boolean living=true;
    private TankFrame tf=null;

    public Tank(int x,int y,Dir dir,TankFrame tf){
        super();
        this.x=x;
        this.y=y;
        this.dir=dir;
        this.tf=tf;
    }
    public Dir getDir() {
        return dir;
    }

    public void setDir(Dir dir) {
        this.dir = dir;
    }
    public boolean isMoving() {
        return moving;
    }
    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public void paint(Graphics g){
        if(!living) tf.tanks.remove(this);
        switch (dir){
            case LEFT:
                g.drawImage(ResourceMgr.tankL,x,y,null);
                break;
            case RIGHT:
                g.drawImage(ResourceMgr.tankR,x,y,null);
                break;
            case UP:
                g.drawImage(ResourceMgr.tankU,x,y,null);
                break;
            case DOWN:
                g.drawImage(ResourceMgr.tankD,x,y,null);
                break;
        }
        move();
    }
    private void move(){
        if (!moving) return;
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

    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void fire() {
        int bx=this.x+Tank.WIDTH/2-Bullet.WIDTH/2;
        int by=this.y+Tank.HEIGHT/2-Bullet.HEIGHT/2;
        tf.bulletS.add(new Bullet(bx, by, this.dir,this.tf));
    }

    public void die() {
        living=false;
    }
}
