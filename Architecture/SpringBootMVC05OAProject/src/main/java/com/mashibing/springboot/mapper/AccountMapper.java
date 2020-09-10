package com.mashibing.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashibing.springboot.entity.Account;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AccountMapper继承基类
 */
@Repository
public interface AccountMapper extends BaseMapper<Account> {
}