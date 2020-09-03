package com.mashibing.service;

import com.mashibing.bean.TblUserRecord;
import com.mashibing.mapper.TblUserRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Auther: hhr
 * @Date: 2020/9/2 - 09 - 02 - 18:36
 * @Description: com.mashibing.service
 * @version: 1.0
 */
@Service
public class LoginService {
    @Autowired
    private TblUserRecordMapper tblUserRecordMapper;

    public TblUserRecord login(String username,String password){
        return tblUserRecordMapper.login(username, password);
    }
}
