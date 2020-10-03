package com.mashibing.springboot.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageInfo;
import com.mashibing.springboot.RespStat;
import com.mashibing.springboot.entity.Account;
import com.mashibing.springboot.entity.Config;
import com.mashibing.springboot.service.AccountService;


/**
 * 用户账户相关
 * @author Administrator
 *
 */

@Controller
@RequestMapping("/account")
public class AccountController {

	@Autowired
	FastFileStorageClient fc;
	@Autowired
	AccountService accountSrv;
	
	@Autowired
	Config config;
	
	
	@RequestMapping("login")
	public String login(Model model) {
		
		model.addAttribute("config", config);
		return "account/login";
	}
	

	/**
	 * 用户登录异步校验
	 * @param loginName
	 * @param password
	 * @return success 成功
	 */
	
	
	/**
	 *  /Login 1. 如果首次打开（没有任何参数），展示静态的HTML
	 *         2. 如果有post请求，验证账号密码是否正确
	 * @param loginName
	 * @param password
	 * @param request
	 * @return
	 */
	@RequestMapping("validataAccount")
	@ResponseBody
	public String validataAccount(String loginName,String password,HttpServletRequest request) {
		
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
	public String list(@RequestParam(defaultValue = "1") int pageNum,@RequestParam(defaultValue = "5" ) int pageSize,Model model) {
		
		PageInfo<Account>page = accountSrv.findByPage(pageNum,pageSize);
		
		model.addAttribute("page", page);
		return "/account/list";
	}
	
	@RequestMapping("/deleteById")
	@ResponseBody
	public RespStat deleteById(int id) {
		// 标记一下 是否删除成功？  status
		RespStat stat = accountSrv.deleteById(id);
		
		return stat;
	}
	
	
	// FastDFS
	
	
	
	@RequestMapping("/profile")
	public String profile () {
		return "account/profile";
	}
	
	
	/**
	 * 中文字符
	 * @param filename
	 * @param password
	 * @return
	 */
	@RequestMapping("/fileUploadController")
	public String fileUpload (MultipartFile filename,String password,HttpServletRequest request) {
		
		
		Account account = (Account)request.getSession().getAttribute("account");

		try {
			// 元数据
			Set<MetaData> metaDataSet = new HashSet<MetaData>();
			metaDataSet.add(new MetaData("Author", "yimingge"));
			metaDataSet.add(new MetaData("CreateDate", "2016-01-05"));
		// 当前项目的路径
//		File path = new File(ResourceUtils.getURL("classpath:").getPath());
//        File upload = new File(path.getAbsolutePath(), "static/uploads/");
        
        
        // 指定系统存放文件的目录
        
        // 文件转存
		// 文件重名
       // filename.transferTo(new File("c:/dev/uploads/"+filename.getOriginalFilename()));
			try {
				StorePath uploadFile = null;
				//uploadFile = fc.uploadFile(filename.getInputStream(), filename.getSize(), FilenameUtils.getExtension(filename.getOriginalFilename()), metaDataSet);
				StorePath storePath = fc.uploadImageAndCrtThumbImage(filename.getInputStream(), filename.getSize(), FilenameUtils.getExtension(filename.getOriginalFilename()), null);
				System.out.println("yuan 路径："+storePath.getFullPath());
				account.setPassword(password);
				//account.setLocation(uploadFile.getPath());

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



        account.setPassword(password);
        account.setLocation(filename.getOriginalFilename());
        
        accountSrv.update(account);
        
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "account/profile";
	}

	@RequestMapping("/down")
	@ResponseBody
	public ResponseEntity<byte[]> down(HttpServletResponse resp) {

		DownloadByteArray cb = new DownloadByteArray();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", "aaa.xx");
		byte[] bs = fc.downloadFile("group1", "M00/00/00/wKiWDV0vAb-AcOaYABf1Yhcsfws9181.xx", cb);

		return new ResponseEntity<>(bs,headers, HttpStatus.OK);
	}
}
