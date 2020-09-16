package com.mashibing.springboot.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mashibing.springboot.entity.Account;
import com.mashibing.springboot.mapper.AccountExample;
import com.mashibing.springboot.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RoleService {
	@Autowired
	AccountMapper accMapper;
	public PageInfo<Account> findByPage(int pageNum, int pageSize) {

		PageHelper.startPage(pageNum, pageSize);
        AccountExample example = new AccountExample();
		PageInfo<Account> pageInfo = new PageInfo<Account>(accMapper.selectByExample(example), 5);
		return pageInfo;
	}
	}
