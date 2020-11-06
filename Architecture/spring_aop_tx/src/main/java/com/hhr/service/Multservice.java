package com.hhr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Auther: hhr
 * @Date: 2020/11/6 - 11 - 06 - 13:58
 * @Description: com.hhr.service
 * @version: 1.0
 */
@Service
public class Multservice {
    @Autowired
    private BookService bookService;
    @Transactional
    public void mult(){
        bookService.buyBook();
        bookService.updatePrice();
    }
}
