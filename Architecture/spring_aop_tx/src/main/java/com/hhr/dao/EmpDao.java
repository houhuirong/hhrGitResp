package com.hhr.dao;

import com.hhr.entity.Emp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @Auther: hhr
 * @Date: 2020/11/5 - 11 - 05 - 15:07
 * @Description: com.hhr.dao
 * @version: 1.0
 */
@Repository
public class EmpDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public void save(Emp emp){
        String sql="insert into emp(empno,ename) values(?,?)";
        int update = jdbcTemplate.update(sql, emp.getEmpno(), emp.getEname());
        System.out.println(update);
    }
}
