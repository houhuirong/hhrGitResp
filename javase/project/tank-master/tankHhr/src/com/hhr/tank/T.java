package com.hhr.tank;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @Auther: hhr
 * @Date: 2020/8/27 - 08 - 27 - 15:18
 * @Description: com.hhr.tank
 * @version: 1.0
 */
public class T {
    public static void main(String[] args) throws InterruptedException {
        TankFrame tf=new TankFrame();
        while (true){
            Thread.sleep(50);
            tf.repaint();
        }
    }
}
