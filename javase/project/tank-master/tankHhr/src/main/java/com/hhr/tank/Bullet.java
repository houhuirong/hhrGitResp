package com.hhr.tank;

import com.sun.javafx.scene.traversal.WeightedClosestCorner;

import java.awt.*;

/**
 * @Auther: hhr
 * @Date: 2020/8/27 - 08 - 27 - 18:29
 * @Description: com.hhr.tank
 * @version: 1.0
 */
public class Bullet {
    private static final int SPEED=2;
    private static final int WIDTH=30,HEIGHT=30;
    private int x,y;
    private Dir dir;
    public boolean live=true;
    TankFrame tf=null;
    public Bullet(int x,int y,Dir dir,TankFrame tf){
        this.x=x;
        this.y=y;
        this.dir=dir;
        this.tf=tf;
    }
    public void paint(Graphics g){
        if(!live){
            tf.bulletS.remove(this);
        }
        Color c=g.getColor();
        g.setColor(Color.RED);
        g.fillRect(x,y,WIDTH,HEIGHT);
        g.setColor(c);
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
        if(x<0||y<0||x>TankFrame.Game_HEIGHT||y>TankFrame.GAME_WIGTH)live=false;
    }
}
