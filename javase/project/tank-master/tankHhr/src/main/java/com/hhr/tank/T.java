package com.hhr.tank;

/**
 * @Auther: hhr
 * @Date: 2020/8/27 - 08 - 27 - 15:18
 * @Description: com.hhr.tank
 * @version: 1.0
 */
public class T {
    public static void main(String[] args) throws InterruptedException {
        TankFrame tf=new TankFrame();

        for (int i=0;i<5;i++){
            tf.tanks.add(new Tank(50+i*60,50+i*60,Dir.DOWN,tf));
        }
        while (true){
            Thread.sleep(50);
            tf.repaint();
        }
    }
}
