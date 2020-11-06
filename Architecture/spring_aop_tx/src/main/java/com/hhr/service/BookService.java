package com.hhr.service;

import com.hhr.dao.BookDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Auther: hhr
 * @Date: 2020/11/5 - 11 - 05 - 15:47
 * @Description: com.hhr.service
 * @version: 1.0
 */
@Service
public class BookService {
    @Autowired
    private BookDao bookDao;

    @Transactional(propagation = Propagation.NESTED)
    public void buyBook(){
        bookDao.getPrice(1);
        bookDao.updateBalance("zhangsan",100);
        bookDao.updateStock(1);
        int i=1/0;
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public void updatePrice(){
        bookDao.updatePrice(1);
    }
}
