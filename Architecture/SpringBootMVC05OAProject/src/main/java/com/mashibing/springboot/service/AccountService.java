package com.mashibing.springboot.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.Page;
import com.mashibing.springboot.mapper.AccountExample;
import com.mashibing.springboot.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mashibing.springboot.entity.Account;

@Service
public class AccountService {

	@Autowired
	AccountMapper accMapper;

	public Account findByLoginNameAndPassword(String loginName, String password) {

		AccountExample example = new AccountExample();
		example.createCriteria()
				.andLoginNameEqualTo(loginName)
				.andPasswordEqualTo(password);
		Map<String, Object> map = new HashMap<>();
		map.put("login_name", loginName);
		map.put("password", password);

		// password
		// 1. 没有
		// 2. 有一条
		// 3. 好几条 X

		List<Account> list = accMapper.selectByMap(map);
		return list.size() == 0 ? null : list.get(0);
		/*Account account=new Account();
		account.setLoginName(loginName);
		account.setPassword(password);
		account.setAge(18);
		return account;*/
	}

	public List<Account> findAll() {

		AccountExample example = new AccountExample();
		return null;
	}

	public PageInfo<Account> findByPage(int pageNum, int pageSize) {

		PageHelper.startPage(pageNum, pageSize);

		Map<String, Object> map = new HashMap<>();
		/*return accMapper.selectByMap(map);*/
		PageInfo<Account> pageInfo = new PageInfo<Account>(accMapper.selectByMap(map), 5);
		return pageInfo;
	}
}