package com.hhr.tank;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Auther: hhr
 * @Date: 2020/8/27 - 08 - 27 - 15:47
 * @Description: com.hhr.tank
 * @version: 1.0
 */
public class TankFrame extends Frame {

    Tank myTank=new Tank(200,200,Dir.UP,this);
    Bullet bullet=new Bullet(300,300,Dir.DOWN,this);
    static final int GAME_WIGTH=800,Game_HEIGHT=600;
    List<Bullet> bulletS=new ArrayList<Bullet>();
    List<Tank> tanks=new ArrayList<Tank>();

    public TankFrame(){
        setSize(GAME_WIGTH,Game_HEIGHT);
        setResizable(false);
        setTitle("tank war");
        setVisible(true);
        this.addKeyListener(new MyKeyListener());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
    Image offScreenImage = null;

    @Override
    public void update(Graphics g) {
        if (offScreenImage == null) {
            offScreenImage = this.createImage(GAME_WIGTH, Game_HEIGHT);
        }
        Graphics gOffScreen = offScreenImage.getGraphics();
        Color c = gOffScreen.getColor();
        gOffScreen.setColor(Color.BLACK);
        gOffScreen.fillRect(0, 0, GAME_WIGTH, Game_HEIGHT);
        gOffScreen.setColor(c);
        paint(gOffScreen);
        g.drawImage(offScreenImage, 0, 0, null);
    }

    @Override
    public void paint(Graphics g) {
        Color color=g.getColor();
        g.setColor(Color.WHITE);
        g.drawString("子弹数量:"+bulletS.size(),10,60);
        g.setColor(color);
       myTank.paint(g);
        //bullet.paint(g);
/*       for (Bullet b:bulletS){
           b.paint(g);
       }*/
        for (int i=0;i<bulletS.size();i++){
            bulletS.get(i).paint(g);
        }
        for (Iterator<Bullet> it=bulletS.iterator();it.hasNext();){
            Bullet bb=it.next();
            if (!bb.live) it.remove();
        }

        for (int i=0;i<tanks.size();i++){
            tanks.get(i).paint(g);
        }
    }



    class MyKeyListener extends KeyAdapter{
        boolean bL = false;
        boolean bU = false;
        boolean bR = false;
        boolean bD = false;
        @Override
        public void keyPressed(KeyEvent e) {
            int key=e.getKeyCode();
            switch (key) {
                case KeyEvent.VK_LEFT:
                    bL = true;
                    break;
                case KeyEvent.VK_UP:
                    bU = true;
                    break;
                case KeyEvent.VK_RIGHT:
                    bR = true;
                    break;
                case KeyEvent.VK_DOWN:
                    bD = true;
                    break;
                default:
                    break;
            }
            setMainTankDir();
        }

        private void setMainTankDir() {
            if(!bD && !bR && !bL &&! bU) myTank.setMoving(false);
            else myTank.setMoving(true);
            if(bD) myTank.setDir(Dir.DOWN);
            if(bL) myTank.setDir(Dir.LEFT);
            if(bR) myTank.setDir(Dir.RIGHT);
            if(bU) myTank.setDir(Dir.UP);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int key=e.getKeyCode();

            switch (key) {
                case KeyEvent.VK_LEFT:
                    bL = false;
                    break;
                case KeyEvent.VK_UP:
                    bU = false;
                    break;
                case KeyEvent.VK_RIGHT:
                    bR = false;
                    break;
                case KeyEvent.VK_DOWN:
                    bD = false;
                    break;
                case KeyEvent.VK_CONTROL:
                    myTank.fire();
                    break;
                default:
                    break;
            }
            setMainTankDir();
        }
    }
}
