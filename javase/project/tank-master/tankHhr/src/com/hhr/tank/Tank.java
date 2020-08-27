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

    public Dir getDir() {
        return dir;
    }

    public void setDir(Dir dir) {
        this.dir = dir;
    }

    private Dir dir;
    private static final int SPEED=10;
    public Tank(int x,int y,Dir dir){
        super();
        this.x=x;
        this.y=y;
        this.dir=dir;
    }
    public void paint(Graphics g){
        System.out.println("paint");
        g.fillRect(x,y,50,50);
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
