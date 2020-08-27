package com.hhr.tank;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.KeyStore;

/**
 * @Auther: hhr
 * @Date: 2020/8/27 - 08 - 27 - 15:47
 * @Description: com.hhr.tank
 * @version: 1.0
 */
public class TankFrame extends Frame {
    int x=200;
    int y=200;
    Dir dir=Dir.LEFT;
    final int SPEED=10;
    public TankFrame(){
        setSize(800,600);
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

    @Override
    public void paint(Graphics g) {
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
            //x+=20;
            //repaint();
        }

        private void setMainTankDir() {
            if(bD) dir=Dir.DOWN;
            if(bL) dir=Dir.LEFT;
            if (bR) dir=Dir.RIGHT;
            if(bU) dir=Dir.UP;
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


                default:
                    break;
            }
            setMainTankDir();
        }
    }
}
