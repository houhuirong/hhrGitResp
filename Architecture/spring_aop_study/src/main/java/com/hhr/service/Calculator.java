package com.hhr.service;

import org.springframework.stereotype.Service;

/**
 * @Auther: hhr
 * @Date: 2020/10/29 - 10 - 29 - 14:04
 * @Description: com.hhr.service
 * @version: 1.0
 */
public interface Calculator {
    public Integer add(Integer i,Integer j) throws NoSuchMethodException;
    public Integer sub(Integer i,Integer j) throws NoSuchMethodException;
    public Integer mul(Integer i,Integer j) throws NoSuchMethodException;
    public Integer div(Integer i,Integer j) throws NoSuchMethodException;
}
