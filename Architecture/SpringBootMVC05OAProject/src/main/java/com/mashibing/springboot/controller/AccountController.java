package com.mashibing.springboot.controller;

import javax.servlet.http.HttpServletRequest;

import com.github.pagehelper.PageInfo;
import com.mashibing.springboot.RespStat;
import com.mashibing.springboot.entity.Account;
import com.mashibing.springboot.entity.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mashibing.springboot.service.AccountService;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


/**
 * 用户账户相关
 * @author Administrator
 *
 */

@Controller
@RequestMapping("/account")
public class AccountController {

	
	@Autowired
	AccountService accountSrv;
	@Autowired
	Config config;

/**
 * 改进：密码是用户名加密码后再加密
 * 判断两次输入密码是否一致
 * */
	@RequestMapping("regist")
	@ResponseBody
	public RespStat regist(Account account){
		System.out.println(account.getLoginName()+"-------------"+account.getPassword());
		return accountSrv.insertAccount(account);
	}
	@RequestMapping("updatePassword")
	@ResponseBody
	public RespStat updatePassword(Account account){
		return accountSrv.updatePassword(account);
	}

	@RequestMapping("login")
	public String login(Model model) {
		model.addAttribute("config",config);
		return "account/login";
	}
	

	/**
	 * 用户登录异步校验
	 * @param loginName
	 * @param password
	 * @return success 成功
	 */
	@RequestMapping("validataAccount")
	@ResponseBody
	public String validataAccount(String loginName,String password,HttpServletRequest request) {
		
		System.out.println("loginName:" + loginName);
		System.out.println("password:" + password);
		
		// 1. 直接返回是否登录成功的结果
		// 2. 返回 Account对象，对象是空的 ，在controller里做业务逻辑
		// 在公司里 统一写法
		
	
		//让service返回对象，如果登录成功 把用户的对象 
		Account account = accountSrv.findByLoginNameAndPassword(loginName, password);
		
		if (account == null) {
			return "登录失败";
		}else {
			// 登录成功
			// 写到Session里
			// 在不同的controller 或者前端页面上 都能使用 
			// 当前登录用户的Account对象
			
			request.getSession().setAttribute("account", account);
			return "success";
		}
	}
	
	
	@RequestMapping("/logOut")
	public String logOut(HttpServletRequest request) {
		
		request.getSession().removeAttribute("account");
		return "index";
	}
	@RequestMapping("/list")
	public String list(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "5" ) int pageSize, Model model) {
		
		PageInfo<Account> page = accountSrv.findByPage(pageNum,pageSize);
		model.addAttribute("page", page);
		return "/account/list";
	}

	@RequestMapping("/deleteById")
	@ResponseBody
	public RespStat deleteById(Integer id){
		return accountSrv.deleteById(id);
	}

	@RequestMapping("/profile")
	public String profile(){
		try{
			File path=new File(ResourceUtils.getURL("classpath:").getPath());
			File upload = new File(path.getAbsolutePath(), "static/upload/");
			System.out.println(upload.getAbsolutePath());
		}catch(Exception e){
			e.printStackTrace();
		}
		return "account/profile";
	}

	@RequestMapping("/fileUploadController")
	@ResponseBody
	public String fileUpload(MultipartFile filename,String password){
		System.out.println("password"+password);
		System.out.println("file"+filename.getOriginalFilename());
		try{
		File path=new File(ResourceUtils.getURL("classpath:").getPath());
		File upload=new File(path.getAbsolutePath(),"static/upload");
			System.out.println("upload"+upload);
			filename.transferTo(new File(upload+"/"+filename.getOriginalFilename()));
		}catch(Exception e){
		e.printStackTrace();
		}
		return "account/profile";
	}
}
