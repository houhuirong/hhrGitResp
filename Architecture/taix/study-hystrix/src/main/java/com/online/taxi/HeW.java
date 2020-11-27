package com.online.taxi;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * @Auther: hhr
 * @Date: 2020/11/27 - 11 - 27 - 9:56
 * @Description: com.online.taxi
 * @version: 1.0
 */
public class HeW extends HystrixCommand {
    protected HeW(HystrixCommandGroupKey group) {
        super(group);
    }

    @Override
    protected Object run() throws Exception {
        return null;
    }

    public static void main(String[] args) {
        System.out.println("ceshi");
    }
}
