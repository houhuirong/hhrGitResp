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
    private static final int SPEED=10;
    private boolean moving=false;

    public Tank(int x,int y,Dir dir){
        super();
        this.x=x;
        this.y=y;
        this.dir=dir;
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
        System.out.println("paint");
        g.fillRect(x,y,50,50);
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
}
