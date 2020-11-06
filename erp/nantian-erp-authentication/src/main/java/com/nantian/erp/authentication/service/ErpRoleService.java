package com.nantian.erp.authentication.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nantian.erp.authentication.data.dao.ErpRoleMapper;
import com.nantian.erp.authentication.data.dao.ErpSysButtonMapper;
import com.nantian.erp.authentication.data.dao.ErpSysMenuMapper;
import com.nantian.erp.authentication.data.dao.ErpSysPrivilegeMapper;
import com.nantian.erp.authentication.data.dao.ErpSysRecordMapper;
import com.nantian.erp.authentication.data.dao.ErpSysUserMapper;
import com.nantian.erp.authentication.data.dao.ErpUrlPrivilegeMapper;
import com.nantian.erp.authentication.data.dao.ErpUrlRelativeMapper;
import com.nantian.erp.authentication.data.dao.ErpUserRoleMapper;
import com.nantian.erp.authentication.data.model.ErpRole;
import com.nantian.erp.authentication.data.model.ErpSysButton;
import com.nantian.erp.authentication.data.model.ErpSysMenu;
import com.nantian.erp.authentication.data.model.ErpSysRecord;
import com.nantian.erp.authentication.data.model.ErpSysUrl;
import com.nantian.erp.authentication.data.vo.ErpSysPrivilegeVo;
import com.nantian.erp.authentication.data.vo.ErpSysUserVo;
import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.HttpClientUtil;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;

/** 
 * Description: 角色信息管理service接口
 * @author caoxiubin
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月8日                   caoxiubin         1.0      
 * 2018年11月23日                 yumingxian        1.0  
 * </pre>
 */
@Service
@Transactional(readOnly=true)
public class ErpRoleService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	RestTemplate restTemplate;
	
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisTemplate redisTemplate; 
	
	@Autowired
	private ErpRoleMapper erpRoleMapper;
	
	@Autowired
	private ErpSysPrivilegeMapper erpSysPrivilegeMapper;
	
	@Autowired
	private ErpSysMenuMapper erpSysMenuMapper;
	
	@Autowired
	private ErpSysButtonMapper erpSysButtonMapper;
	
	@Autowired
	private ErpUrlRelativeMapper erpUrlRelativeMapper;
	
	@Autowired
	private ErpUserRoleMapper erpUserRoleMapper;
	
	@Autowired
	private ErpSysUserService erpSysUserService;
	
	@Autowired
	private ErpUrlPrivilegeMapper erpUrlPrivilegeMapper;
	
	@Autowired
	private StringRedisTemplate stringRredisTemplate;
	
	@Autowired
	private ErpSysRecordMapper erpSysRecordMapper;
	
	@Autowired
	private ErpSysUserMapper erpSysUserMapper;
	/*
	 * 从配置文件中获取Email相关属性
	 */
	@Value("${email.service.host}")
	private  String emailServiceHost;//邮件服务的IP地址和端口号
	@Value("${environment.type}")
	private  String environmentType;//环境类型（根据该标识，决定邮件的发送人、抄送人、收件人）
	@Value("${test.email.frommail}")
	private String testEmailFrommail;//测试环境发件人
	@Value("${test.email.bcc}")
	private String testEmailBcc;//测试环境抄送人
	@Value("${test.email.tomail}")
	private String testEmailTomail;//测试环境收件人
	
	/**
	 * Description: 根据当前登陆用户加载菜单信息
	 * 				可能存在一个用户多个角色的情况
	 * @param  token        
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月8日 下午14:32:53
	 */
	public RestResponse menuTreeLoad(HttpServletRequest request){
		
				//最终返回结果
				List<ErpSysMenu> returnList = new ArrayList<>();
				try {
					returnList = new ArrayList<>();
					String token = request.getHeader("token");
					//用户名
					ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
					//根据角色ID查询菜单信息
					List<Integer> roles = erpUser.getRoles();
					List<ErpSysMenu> menuNodeList = null;
					for (int i = 0; i < roles.size(); i++) {
						
						/*
						 * 登陆用户可能存在一个或多个角色
						 * 当有多个角色时 去重所包括的菜单按钮权限信息
						 */
						menuNodeList = this.erpSysMenuMapper.findAllMenuByRoleId(roles.get(i));
						if(i == 0) {
							//num为0时,表示初始化菜单按钮资源库
							for (ErpSysMenu erpSysMenu : menuNodeList) {
								erpSysMenu.setType("1");
								if (1 == erpSysMenu.getIsButton()) {
										Map<String, Object> param=new HashMap<String, Object>();
										param.put("menuNo", erpSysMenu.getMenuNo());
										param.put("roleId", roles.get(i));
							        	List<ErpSysButton> buttonList=erpSysButtonMapper.getButtonByMenuNoRole(param);
							        	//给按钮添加节点类型
							        	for(ErpSysButton erpSysButton : buttonList) {
							        		erpSysButton.setType("2");
							        	}
							        	erpSysMenu.setButtonListLogin(buttonList);
							    }
								returnList.add(erpSysMenu);
							}
						}else {
							/*
							 * 权限去重-添加2个或第n个角色-权限信息
							 * 判断menuNodeList里面的每个元素和returnList里面的元素对比 
							 * 相同则不添加，不相同往returnList添加元素
							 */
							for (int j = 0; j < menuNodeList.size(); j++) {
								ErpSysMenu erpSysMenu = menuNodeList.get(j);
								//定义当前菜单是否与库中的菜单有重复 0：没有重复 1：有重复
								int flag_menu=0;
								for (int k = 0; k < returnList.size();k++) {
									ErpSysMenu erpSysMenu2=returnList.get(k);
									if(erpSysMenu.getMenuNo().trim().equals(erpSysMenu2.getMenuNo().trim())) {
										//当前菜单与菜单库中的进行比较，相同则将flag_menu状态置为1
										flag_menu=1;
										/*
										 * 将重复的菜单中的按钮添加到按钮库中，并重置当前菜单按钮资源库
										 */
										if (1 == erpSysMenu.getIsButton()) {
											//查询当前菜单的按钮权限
											Map<String, Object> param=new HashMap<String, Object>();
											param.put("menuNo", erpSysMenu.getMenuNo());
											param.put("roleId", roles.get(i));
								        	List<ErpSysButton> buttonList=erpSysButtonMapper.getButtonByMenuNoRole(param);
								        	//获取当前菜单的菜单库的按钮权限
								        	List<ErpSysButton> buttonListku=erpSysMenu2.getButtonListLogin();
											//遍历当前菜单的按钮权限添加到按钮
											for (ErpSysButton erpSysButton : buttonList) {
												//定义当前菜单的按钮是否与库中的菜单有重复 0：没有重复 1：有重复
												int flag_button=0;
												for (ErpSysButton erpSysButtonku : buttonListku) {
													if( erpSysButton.getBtnNo()==erpSysButtonku.getBtnNo()) {
														flag_button=1;
														break;
													}
												}
												//如果当前菜单的按钮在按钮库中不存在，则添加到按钮库中
												if(flag_button==0) {
													erpSysButton.setType("2");
													buttonListku.add(erpSysButton);
												}
											}
								        	//将整合后的按钮库更新到当前按钮库中
								        	erpSysMenu2.setButtonListLogin(buttonListku);
								        	returnList.set(k, erpSysMenu2);
								        }
										break;
									}
								}
								//如果没有重复，则将当前菜单添加到菜单库中
								if(flag_menu==0) {
									erpSysMenu.setType("1");
									if (1 == erpSysMenu.getIsButton()) {
										Map<String, Object> param=new HashMap<String, Object>();
										param.put("menuNo", erpSysMenu.getMenuNo());
										param.put("roleId", roles.get(i));
							        	List<ErpSysButton> buttonList=erpSysButtonMapper.getButtonByMenuNoRole(param);
							        	//给按钮添加节点类型
							        	for(ErpSysButton erpSysButton : buttonList) {
							        		erpSysButton.setType("2");
							        	}
							        	erpSysMenu.setButtonListLogin(buttonList);
							        }
									returnList.add(erpSysMenu);
								}
								
								
								
							}
						}
					}
				} catch (Exception e) {
					logger.error("根据当前登陆用户加载菜单信息出现错误："+ e.getMessage(),e);
				}
				logger.info("最终菜单组装返回结果："+ returnList.size());
				return RestUtils.returnSuccess(returnList);
	}

	/**
	 * Description: 角色信息-查询角色列表信息
	 * @param null     
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月9日 下午18:32:53
	 */
	public RestResponse findAllRole(String token, String roleType){
		List<Map<String,Object>> roleList = null;
		try {
			if(roleType.equals("baseRole")){
				roleList = this.erpRoleMapper.findAllBaseRole();
				
				for (Map<String,Object> baseRole : roleList){
					List<Map<String, Object>> roleAuthlist = new ArrayList<>();
					List<Map<String, Object>> userAuthlist = new ArrayList<>();
					
					List<Map<String,Object>> authList = this.erpRoleMapper.findAuthListByRoleId((Integer) baseRole.get("roleId"));
					for (Map<String,Object> auth : authList){
						//判断那是角色还是用户
						if (auth.get("authType").equals(1)){
							//角色
							Map<String, Object> roleMap =  new HashMap<>();
							ErpRole role =  this.erpRoleMapper.findRoleInfoByRoleId((Integer) auth.get("authId"));
							roleMap.put("id", role.getRoleId());
							roleMap.put("name", role.getName());
							roleAuthlist.add(roleMap);
						}
						else if (auth.get("authType").equals(2)){
							//用户
							Map<String, Object> userMap =  new HashMap<>();
							ErpSysUserVo user = this.erpSysUserMapper.findUserInfoByUserId((Integer) auth.get("authId"));
							if(user==null) {
								continue;
							}
							userMap.put("id", user.getId());
							userMap.put("name", user.getUsername());
							userAuthlist.add(userMap);
						}
						else{
							logger.info("基础角色授权类型错误："+ auth);
							continue;
						}
					}
					baseRole.put("roleAuthlist",roleAuthlist);
					baseRole.put("userAuthlist",userAuthlist);				
				}
			}else if(roleType.equals("childRole")){
				ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
				roleList = this.erpRoleMapper.findAllChildRole(erpUser.getUserId());
			}
			else{
				return RestUtils.returnSuccess(roleList);
			}

			//查询角色对应的权限信息
			for (Map<String, Object> erpRole : roleList) {
				List<ErpSysPrivilegeVo> privilegeList = 
						this.erpSysPrivilegeMapper.findPrivilegeByRoleId((Integer) erpRole.get("roleId"));
				String[] menus = new String[privilegeList.size()];
				for (int i = 0; i < menus.length; i++) {
					if(privilegeList.get(i).getPrivilegeAccess().intValue()==2) {
						//如果访问类型为菜单，则根据菜单id，获取菜单编号
						ErpSysMenu erpSysMenu=erpSysMenuMapper.findMenuById(privilegeList.get(i).getPrivilegeAccessValue());
						if (erpSysMenu == null){
							continue;
						}
						menus[i] =erpSysMenu.getMenuNo();
					}else {
						menus[i] = String.valueOf(privilegeList.get(i).getPrivilegeAccessValue());
					}
				}
				erpRole.put("menus", menus);
			}
		} catch (Exception e) {
			logger.error("角色信息-查询角色列表信息出现错误 方法 findAllRole ："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(roleList);
	}
	
	/**
	 * Description: 角色信息-新增角色信息
	 * @param  erpRole  角色对象 带有该角色下的特权信息     
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月11日 上午11:32:53
	 */
	@Transactional
	public RestResponse insertRole (String token, ErpRole erpRole){
		
		String str = null;	
		try {
			/*
			 * 新增角色表本身信息
			 */
			this.erpRoleMapper.insertRole(erpRole);
			
			if (erpRole.getRoleType().equals(1)){
				str = "新增基础角色,";
			}else{
				str = "新增子角色,";
			}
			
			str = str + "角色名：" + erpRole.getName() + ",权限包括:\r\n";
			/*
			 * 新增特权菜单信息，并记录角色变化
			 */
			String[] menus = erpRole.getMenus();
			
			String menuStr = "菜单：";
			String buttonStr = "按钮：";
			
			for (int i = 0; i < menus.length; i++) {
				addRoleRelative (menus[i], erpRole.getRoleId());
				
				if((menus[i] +"").contains("m")) {
					//根据menuNo查询menuName
					ErpSysMenu erpSysMenu=erpSysMenuMapper.checkMenuNo(menus[i]);
					menuStr = menuStr + erpSysMenu.getMenuName() + "、";
				}else {
					
					ErpSysButton button = erpSysButtonMapper.findButtonInfoById(Integer.valueOf(menus[i]));
					buttonStr = buttonStr + button.getBtnName() + "、";
				}
			}
			
			String strSql = str + menuStr + "\r\n" + buttonStr;
			
			//插入权限修改记录
			ErpUser user = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			
			ErpSysRecord record = new ErpSysRecord();
			record.setOpType(1);//角色修改
			record.setOpId(erpRole.getRoleId());//角色id
			record.setProcessor(user.getUsername());
			record.setTime(ExDateUtils.getCurrentStringDateTime());				
			record.setOpRecord(strSql);			
			this.erpSysRecordMapper.insertRecord(record);
			
			//向总裁发送邮件
			String subject = "集成集团运营管理平台权限变更通知";//主题
			
			String frommail = user.getUsername();//发件人
			if(!frommail.contains("@")) {
				frommail += "@nantian.com.cn";
			}
			
			String tomail = "heli@nantian.com.cn";//主送人
			
			StringBuilder text = new StringBuilder();
			text.append("<span style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; widows: 1;\">&nbsp; &nbsp;</span><br/>\r\n" + 
					"</p >\r\n" + 
					"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
					"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp;集成集团运营管理平台权限变更：</span>\r\n" + 
					"</div>\r\n" + 
					"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
					"    <span style=\"background-color: rgba(0, 0, 0, 0);\"><br/></span>\r\n" + 
					"</div>\r\n" + 
					"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
					"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp; 变更人："+frommail+"</span>\r\n" + 
					"</div>\r\n" + 
					"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
					"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp; 变更时间："+ExDateUtils.getCurrentStringDateTime()+"</span>\r\n" + 
					"</div>\r\n" + 
					"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
					"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp; 变更内容："+str+"</span>\r\n" + 
					"</div>\r\n" + 
					"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
					"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; "+menuStr+"</span>\r\n" + 
					"</div>\r\n" + 
					"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
					"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; "+buttonStr+"</span>\r\n" + 
					"</div>");
			
			this.sendEmail(frommail,"",subject,text.toString(),tomail);
			
			return RestUtils.returnSuccess("新增成功 ！");
		} catch (Exception e) {

			logger.error("角色信息-新增角色信息出现错误  方法 insertRole："+ e.getMessage(),e);
			return RestUtils.returnSuccess("新增失败 ！");
		}
	}
	
	@SuppressWarnings("unchecked")
	public void sendEmail(String frommail,String bcc,String subject,String text,String tomail) {
    	logger.info("进入sendEmail方法，参数是：frommail={},bcc={},subject={},tomail={}",
    			frommail,bcc,subject,tomail);
        try {
        	if("test".equals(environmentType)) {
				frommail = "zhangqian@nantian.com.cn";
				bcc = "";
				tomail = "zhangqian@nantian.com.cn";
				logger.info("测试环境......发件人：{},抄送人：{},收件人：{}",frommail,bcc,tomail);
			}
			logger.info("实际环境：{}",environmentType);
			
			//调用邮件管理工程，将发邮件送必要的参数传递过去，并发送无附件的邮件
			String emailUrl = emailServiceHost+"/nantian-erp/email/send/withoutAttachment";
			Map<String,String> emailParams = new HashMap<>();
			emailParams.put("frommail", frommail);
			emailParams.put("bcc", bcc);
			emailParams.put("subject", subject);
			emailParams.put("text", text);
			emailParams.put("tomail", tomail);
			Map<String,String> headers = null;
			Map<String, String> emailResponse = HttpClientUtil.executePostMethodWithParas(emailUrl, JSON.toJSONString(emailParams), headers, "application/json", 30000);
			String code = emailResponse.get("code");//响应码
			String result = emailResponse.get("result");//响应结果
			logger.info("code={},result={}",code,result);
			if(!"200".equals(code)) {
				logger.error("邮件工程响应失败！");
			}
			Map<String,Object> emailResultMap = (Map<String,Object>) JSON.parse(result);
			String statusOfResult = String.valueOf(emailResultMap.get("status"));//方法调用的返回码
			if(!"200".equals(statusOfResult)) {
				logger.error("邮件发送失败！");
			}
        } catch (Exception e) {
            logger.error("sendEmail异常："+e.getMessage(),e);
        }
        return;
    }
	
	/**
	 * Description: 角色信息-修改角色信息
	 * @param  erpRole    角色对象 带有该角色下的特权信息  
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月9日 下午18:32:53
	 */
	@Transactional
	public RestResponse updateRole (String token, ErpRole erpRole){
		
		String str = null;
		try {
			/*
			 * 修改已有角色信息
			 */
			this.erpRoleMapper.updateRole(erpRole);
			
			if (erpRole.getRoleType().equals(1)){
				str = "修改基础角色,";
			}else{
				str = "修改子角色,";
			}
			str = str + "角色名：" + erpRole.getName() + ",权限修改:\r\n";
			
			if ((1 ==erpRole.getRoleType()) && (0 == erpRole.getChildRoleRight())){
				//查询子角色
				List<ErpRole> childRoleList = this.erpRoleMapper.findAllChildRoleByFatherId(erpRole.getRoleId());
				//删除子角色下的关联
				for (ErpRole child : childRoleList){
					deleteRole(token, child.getRoleId());
				}
			}

			String[] menus = erpRole.getMenus();
			List<String> addList = new ArrayList<>();
			addList.addAll(Arrays.asList(menus));
			
			//查询角色的当前菜单
			List<String> menuList = findMenuByRole(erpRole.getRoleId());
			List<String> deleteList = new ArrayList<>();
			deleteList.addAll(menuList);
			
			for (int i=0; i<menus.length; i++){
				for (String menuId : menuList){
					if (menus[i].equals(menuId)){
						// 移除相同的剩下要新增的
						addList.remove(menus[i]);

						// 移除相同的剩下要删除的
						deleteList.remove(menuId);
					}
				}
			}
			ErpSysPrivilegeVo erpSysPrivilegeVo = new ErpSysPrivilegeVo();
			
			String menuStrAdd = "增加菜单：";
			String buttonStrAdd = "增加按钮：";
			String menuStrDel = "删除菜单：";
			String buttonStrDel = "删除按钮：";
			
			//新增部分
			if (!addList.isEmpty()) {
				for (String addMenu : addList) {					
					addRoleRelative (addMenu, erpRole.getRoleId());
					
					if((addMenu +"").contains("m")) {
						//根据menuNo查询menuName
						ErpSysMenu erpSysMenu=erpSysMenuMapper.checkMenuNo(addMenu);
						menuStrAdd = menuStrAdd + erpSysMenu.getMenuName() + "、";
					}else {
						
						ErpSysButton button = erpSysButtonMapper.findButtonInfoById(Integer.valueOf(addMenu));
						buttonStrAdd = buttonStrAdd + button.getBtnName() + "、";
					}
				}
			}
			
			//删除部分
			if(!deleteList.isEmpty()) {
				for (String delMenu : deleteList) {
					if ((1 == erpRole.getRoleType()) && (1 == erpRole.getChildRoleRight())){
						//查询子角色
						List<ErpRole> childRoleList = this.erpRoleMapper.findAllChildRoleByFatherId(erpRole.getRoleId());
						//删除子角色下的关联
						for (ErpRole child : childRoleList){
							deleteRoleRelative(delMenu, child.getRoleId());
						}
					}
					//删除角色关联
					deleteRoleRelative(delMenu, erpRole.getRoleId());					

					if((delMenu +"").contains("m")) {
						//根据menuNo查询menuName
						ErpSysMenu erpSysMenu=erpSysMenuMapper.checkMenuNo(delMenu);
						menuStrDel = menuStrDel + erpSysMenu.getMenuName() + "、";
					}else {
						
						ErpSysButton button = erpSysButtonMapper.findButtonInfoById(Integer.valueOf(delMenu));
						buttonStrDel = buttonStrDel + button.getBtnName() + "、";
					}					
				}
			}
			
			String strSql = str + menuStrAdd + "\r\n" + buttonStrAdd + "\r\n" + menuStrDel + "\r\n" + buttonStrDel;
			
			//插入权限修改记录
			ErpUser user = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			
			ErpSysRecord record = new ErpSysRecord();
			record.setOpType(1);//角色修改
			record.setOpId(erpRole.getRoleId());//角色id
			record.setProcessor(user.getUsername());
			record.setTime(ExDateUtils.getCurrentStringDateTime());				
			record.setOpRecord(strSql);			
			this.erpSysRecordMapper.insertRecord(record);
			
			if (erpRole.getRoleType().equals(1)){
				//向总裁发送邮件
				String subject = "集成集团运营管理平台权限变更通知";//主题
				
				String frommail = user.getUsername();//发件人
				if(!frommail.contains("@")) {
					frommail += "@nantian.com.cn";
				}
				
				String tomail = "heli@nantian.com.cn";//主送人
				
				StringBuilder text = new StringBuilder();
				text.append("<span style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; widows: 1;\">&nbsp; &nbsp;</span><br/>\r\n" + 
						"</p >\r\n" + 
						"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
						"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp;集成集团运营管理平台权限变更：</span>\r\n" + 
						"</div>\r\n" + 
						"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
						"    <span style=\"background-color: rgba(0, 0, 0, 0);\"><br/></span>\r\n" + 
						"</div>\r\n" + 
						"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
						"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp; 变更人："+frommail+"</span>\r\n" + 
						"</div>\r\n" + 
						"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
						"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp; 变更时间："+ExDateUtils.getCurrentStringDateTime()+"</span>\r\n" + 
						"</div>\r\n" + 
						"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
						"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp; 变更内容："+str+"</span>\r\n" + 
						"</div>\r\n" + 
						"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
						"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; "+menuStrAdd+"</span>\r\n" + 
						"</div>\r\n" + 
						"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
						"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; "+buttonStrAdd+"</span>\r\n" + 
						"</div>\r\n" + "<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
						"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; "+menuStrDel+"</span>\r\n" + 
						"</div>\r\n" + 
						"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
						"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; "+buttonStrDel+"</span>\r\n" + 
						"</div>");
				
				this.sendEmail(frommail,"",subject,text.toString(),tomail);
			}
			
			return RestUtils.returnSuccess("修改成功 ！");
		} catch (Exception e) {
			logger.error("角色信息-修改角色信息出现错误  方法 updateRole："+ e.getMessage(),e);
			return RestUtils.returnSuccess("修改失败 ！");
		}
	}
	
	/**
	 * Description: 角色信息-增加角色的关联
	 * @param  erpRole    角色对象 带有该角色下的特权信息  
	 * @return RestResponse             
	 * @Author zhangqian
	 * @Create Date: 2019年2月14日 下午18:32:53
	 */
	@Transactional
	public void addRoleRelative (String menu, Integer roleId){
		
		try {
			Integer relativeType = 0;
			Integer relativId = 0;
			ErpSysPrivilegeVo erpSysPrivilegeVo = new ErpSysPrivilegeVo();
			
			erpSysPrivilegeVo.setPrivilegeMaster(1);//特权主类型(1：Role 2：User)
			erpSysPrivilegeVo.setPrivilegeValue(roleId);//特权主编号(userID、roleID)
			if((menu +"").contains("m")) {
				erpSysPrivilegeVo.setPrivilegeAccess(2);//特权访问类型(1:Url 或2:Menu或3:Button)
				//根据menuNo查询menuId
				ErpSysMenu erpSysMenu=erpSysMenuMapper.checkMenuNo(String.valueOf(menu));
				erpSysPrivilegeVo.setPrivilegeAccessValue(erpSysMenu.getMenuID());//特权访问值(1:UrlID 或2:MenuId或3:ButtonId)
				relativeType = 1;
				relativId = erpSysMenu.getMenuID();
			}else {
				erpSysPrivilegeVo.setPrivilegeAccess(3);//特权访问类型(1:Url 或2:Menu或3:Button)
				erpSysPrivilegeVo.setPrivilegeAccessValue(Integer.valueOf(menu));//特权访问值(1:UrlID 或2:MenuId或3:ButtonId)
				relativeType = 2;
				relativId = Integer.valueOf(menu);
			}
			
			
			erpSysPrivilegeVo.setPrivilegeOperation(1);//特权操作类型(1：是否显示 2：是否可以访问 )
			erpSysPrivilegeVo.setPrivilegeOperationValue(1);//特权操作值(0:不显示/不可以访问/不可以点击,1:显示/可以点击/可以访问)
			
			this.erpSysPrivilegeMapper.insertPrivilege(erpSysPrivilegeVo);
			
			//通过menu查找到关联的url，增加角色与url的关联
			List<Map<String,Object>> relativeList =  erpUrlRelativeMapper.selectUrlRelativeByRelativeId(relativeType, relativId);
			for (Map<String,Object> relative : relativeList){			
				ErpSysPrivilegeVo findPrivilegeByRoleUrl = erpSysPrivilegeMapper.findPrivilegeByRoleUrl(roleId, (Integer) relative.get("urlId"));
				logger.info("findPrivilegeByRoleUrl" + findPrivilegeByRoleUrl);
				
				if (findPrivilegeByRoleUrl != null) {
					erpSysPrivilegeMapper.addRelativeNumByPrivilegeId(findPrivilegeByRoleUrl.getPrivilegeID());
				} else {
					erpSysPrivilegeMapper.insertPrivilegeByRoleUrl(roleId, (Integer) relative.get("urlId"));
					
					//写入redis
					ErpSysUrl sysUrl = this.erpUrlPrivilegeMapper.getUrlData((Integer) relative.get("urlId"));
					String urlPath = sysUrl.getUrlPath().trim();
					String urlKey = (roleId.toString() + "_" + urlPath).trim();
					
					stringRredisTemplate.opsForValue().set(urlKey, urlPath);
				}
			}			
		} catch (Exception e) {
			logger.error("角色信息-修改角色信息出现错误  方法 updateRole："+ e.getMessage(),e);
		}
		return;
	}
	
	/**
	 * Description: 角色信息-删除角色的关联
	 * @param  erpRole    角色对象 带有该角色下的特权信息  
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月9日 下午18:32:53
	 */
	@Transactional
	public void deleteRoleRelative (String menu, Integer roleId){
		
		try {
			Integer relativeType = 0;
			Integer relativId = 0;
			ErpSysPrivilegeVo erpSysPrivilegeVo = new ErpSysPrivilegeVo();
			
			erpSysPrivilegeVo.setPrivilegeMaster(1);//特权主类型(1：Role 2：User)
			erpSysPrivilegeVo.setPrivilegeValue(roleId);//特权主编号(userID、roleID)
			if((menu +"").contains("m")) {
				erpSysPrivilegeVo.setPrivilegeAccess(2);//特权访问类型(1:Url 或2:Menu或3:Button)
				//根据menuNo查询menuId
				ErpSysMenu erpSysMenu=erpSysMenuMapper.checkMenuNo(String.valueOf(menu));
				erpSysPrivilegeVo.setPrivilegeAccessValue(erpSysMenu.getMenuID());//特权访问值(1:UrlID 或2:MenuId或3:ButtonId)
				relativeType = 1;
				relativId = erpSysMenu.getMenuID();
			}else {
				erpSysPrivilegeVo.setPrivilegeAccess(3);//特权访问类型(1:Url 或2:Menu或3:Button)
				erpSysPrivilegeVo.setPrivilegeAccessValue(Integer.valueOf(menu));//特权访问值(1:UrlID 或2:MenuId或3:ButtonId)
				relativeType = 2;
				relativId = Integer.valueOf(menu);
			}
			//删除角色与menu的关联
			this.erpSysPrivilegeMapper.deletePrivilege(erpSysPrivilegeVo);
			
			//通过menu查找到关联的url，删除角色与url的关联
			List<Map<String,Object>> relativeList =  erpUrlRelativeMapper.selectUrlRelativeByRelativeId(relativeType, relativId);
			for (Map<String,Object> relative : relativeList){			
				ErpSysPrivilegeVo findPrivilegeByRoleUrl = erpSysPrivilegeMapper.findPrivilegeByRoleUrl(roleId, (Integer) relative.get("urlId"));
				logger.info("findPrivilegeByRoleUrl" + findPrivilegeByRoleUrl);
				if (findPrivilegeByRoleUrl != null) {
					if (findPrivilegeByRoleUrl.getRelativeNum().equals(1)){
						erpSysPrivilegeMapper.deletePrivilegeByPrivilegeId(findPrivilegeByRoleUrl.getPrivilegeID());
						
						//删除redis
						ErpSysUrl sysUrl = this.erpUrlPrivilegeMapper.getUrlData((Integer) relative.get("urlId"));
						String urlPath = sysUrl.getUrlPath().trim();
						String urlKey = (roleId.toString() + "_" + urlPath).trim();
						
						stringRredisTemplate.delete(urlKey);
					}
					else{
						erpSysPrivilegeMapper.decRelativeNumByPrivilegeId(findPrivilegeByRoleUrl.getPrivilegeID());
					}
				}
			}			
		} catch (Exception e) {
			logger.error("角色信息-修改角色信息出现错误  方法 updateRole："+ e.getMessage(),e);
		}
		return;
	}
	
	/**
	 * Description: 角色信息-删除角色信息
	 * @param  erpRole  角色对象 带有该角色下的菜单信息     
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月11日 上午11:32:53
	 */
	@Transactional
	public RestResponse deleteRole (String token, Integer roleId){
		
		String str = null;
		try {
			/*
			 * 删除角色表本身信息
			 */
			ErpRole roleInfo = this.erpRoleMapper.findRoleInfoByRoleId(roleId);
			
			str = "删除角色：" + roleInfo.getName();
			
			this.erpRoleMapper.deleteRole(roleId);
			
			//删除角色授权
			Map<String, Object> param = new HashMap<>();
			param.put("baseRoleId", roleId);
			this.erpRoleMapper.deleteRoleAuth(param);
			
			/*
			 * 删除特权信息
			 */
			this.erpSysPrivilegeMapper.deletePrivilegeByRoleId(roleId);
			
			//删除角色与用户的关联
			this.erpUserRoleMapper.deleteUserRoleByRoleId(roleId);
			
			//查询是否含有子角色
			List<ErpRole> erpRoleList = this.erpRoleMapper.findAllChildRoleByFatherId(roleId);
			for (ErpRole erpRole : erpRoleList){
				deleteRole(token, erpRole.getRoleId());
			}
			
			//插入权限修改记录
			ErpUser user = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			
			ErpSysRecord record = new ErpSysRecord();
			record.setOpType(1);//角色修改
			record.setOpId(roleId);//角色id
			record.setProcessor(user.getUsername());
			record.setTime(ExDateUtils.getCurrentStringDateTime());				
			record.setOpRecord("删除角色："+ roleInfo.getName());			
			this.erpSysRecordMapper.insertRecord(record);
			
			//向总裁发送邮件
			String subject = "集成集团运营管理平台权限变更通知";//主题
			
			String frommail = user.getUsername();//发件人
			if(!frommail.contains("@")) {
				frommail += "@nantian.com.cn";
			}
			
			String tomail = "heli@nantian.com.cn";//主送人
			
			StringBuilder text = new StringBuilder();
			text.append("<span style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; widows: 1;\">&nbsp; &nbsp;</span><br/>\r\n" + 
					"</p >\r\n" + 
					"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
					"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp;集成集团运营管理平台权限变更：</span>\r\n" + 
					"</div>\r\n" + 
					"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
					"    <span style=\"background-color: rgba(0, 0, 0, 0);\"><br/></span>\r\n" + 
					"</div>\r\n" + 
					"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
					"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp; 变更人："+frommail+"</span>\r\n" + 
					"</div>\r\n" + 
					"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
					"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp; 变更时间："+ExDateUtils.getCurrentStringDateTime()+"</span>\r\n" + 
					"</div>\r\n" + 
					"<div style=\"font-family: &quot;Microsoft YaHei UI&quot;; font-size: 14px; font-variant-numeric: normal; font-variant-east-asian: normal; line-height: 21px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);\">\r\n" + 
					"    <span style=\"background-color: rgba(0, 0, 0, 0);\">&nbsp; &nbsp; 变更内容："+str+"</span>\r\n" + 
					"</div>");
			
			this.sendEmail(frommail,"",subject,text.toString(),tomail);
			
			str = "删除成功 ！";
		} catch (Exception e) {
			str = "删除失败 ！";
			logger.error("角色信息-删除角色信息出现错误  方法 deleteRole："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(str);
	}
	
	/**
	 * Description: 角色管理-获取所有的菜单信息树结构
	 * @param  token        
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月9日 下午14:32:53
	 */
	public RestResponse getAllMenus(Boolean findAll, Integer roleId){
		
		// 最后的结果
		List<ErpSysMenu> parentMenuList = new ArrayList<ErpSysMenu>();
		try {
			//根据角色ID查询菜单信息
			List<ErpSysMenu> resultList = this.erpSysMenuMapper.findAllMenu();
		    //先找到所有的一级菜单
		    for (int i = 0; i < resultList.size(); i++) {
		    	ErpSysMenu erpSysMenu = resultList.get(i);
		    	if(null != erpSysMenu) {        	
		    		//一级菜单没有MenuparentNo 为 0 
			        if (1 == erpSysMenu.getIsLeaf() && "0".equals(erpSysMenu.getMenuparentNo())) {
			        	parentMenuList.add(erpSysMenu);
			        }
			        
		    	}
		    }
		    // 为一级菜单设置子菜单，getChild是递归调用的
		    List<ErpSysMenu> childrenList = null;
		    for (ErpSysMenu erpSysMenu : parentMenuList) {
		    	
		    	childrenList = prepareResultParam(erpSysMenu.getMenuNo(), resultList, findAll, roleId);
		    	
	        	erpSysMenu.setChildrenList(childrenList);
			}
		    //给菜单添加节点类型
		    for (ErpSysMenu erpSysMenu : parentMenuList) {
		    	 erpSysMenu.setType("1");
		    }
		    
		} catch (Exception e) {
			logger.error("根据当前登陆用户加载菜单信息出现错误："+ e.getMessage(),e);
		}
		logger.info("最终菜单组装返回结果："+JSONObject.toJSONString(parentMenuList));
		return RestUtils.returnSuccess(parentMenuList);
	}
	
	/**
	 * Description: 组装菜单结果
	 * @param  resultList        
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月8日 下午14:32:53
	 */
	private List<ErpSysMenu> prepareResultParam(String MenuNo,List<ErpSysMenu> resultList, Boolean findAll, Integer roleId) {
//		logger.info("菜单父级编号 ： " + MenuNo);		
		List<ErpSysMenu> childListList = null;
		try {
			// 子菜单
			childListList = new ArrayList<>();
		    for (ErpSysMenu erpSysMenu : resultList) {
		    	if(null != erpSysMenu) {
		    		 // 遍历所有节点，将父菜单编号与传过来的编号比较
		            if (erpSysMenu.getMenuparentNo().equals(MenuNo)) {
		            	childListList.add(erpSysMenu);
		            }
		    	}
		    }
		    // 把子菜单的子菜单再循环一遍
		    for (ErpSysMenu erpSysMenu : childListList) {
		    	List<ErpSysButton> buttonList=erpSysButtonMapper.getAllButtonByMenuNo(erpSysMenu.getMenuNo());
	        	//给按钮添加节点类型
	        	for(ErpSysButton erpSysButton : buttonList) {
	        		erpSysButton.setType("2");
	        	}
	        	erpSysMenu.setButtonListLogin(buttonList);
	        	
		    	// 还有子菜单
		    	List<ErpSysMenu> childrenList = null;
		        if (1 == erpSysMenu.getIsLeaf()) {
		            // 递归
		        	
		        	childrenList = prepareResultParam(erpSysMenu.getMenuNo(), resultList, findAll, roleId);
		        	erpSysMenu.setChildrenList(childrenList);
		        }
		    } // 递归退出条件
		    if (childListList.size() == 0) {
		        return null;
		    }
		} catch (Exception e) {
			logger.error("组装菜单结果出现错误 方法 prepareResultParam："+ e.getMessage(),e);
		}
		return childListList;
	}
	
	/**
	 * Description: 角色管理-判断输入角色名或代号是否已存在
	 * @param  UrlPath       
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月11日 上午11:32:53
	 */
	public RestResponse checkNameOrKeyword (String token, Boolean isChild, String paramName,int isName){
		
		boolean flag = false;
		try {
			Map<String, Object> param = new HashMap<>();
			
			if (isChild == true){
				ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
				param.put("childRoleOwner", erpUser.getUserId());
			}			
			
			if(1 == isName) {
				param.put("name", paramName);
				ErpRole erpRole = this.erpRoleMapper.checkName(param);
				if(null == erpRole) {
					flag = true;
				}
			}else {
				param.put("keyword", paramName);
				ErpRole erpRole = this.erpRoleMapper.checkKeyword(param);
				if(null == erpRole) {
					flag = true;
				}
			}
		} catch (Exception e) {
			logger.error("角色管理-判断输入角色名或代号是否已存在出现错误  方法 checkNameOrKeyword："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(flag);
	}

	/**
	 * Description: 角色信息-查询角色列表信息
	 * @param null     
	 * @return RestResponse             
	 * @Author houhuirong
	 * @Create Date: 2018年10月15日 下午17:00:00
	 */
	public RestResponse findAllBaseRole() {
		logger.info("查询所有基础权限");
		
		List<Map<String,Object>> baseRoleList=null;
		try{
			baseRoleList=this.erpRoleMapper.findAllBaseRole();
			
			for (Map<String,Object> baseRole : baseRoleList){
				List<Map<String, Object>> roleAuthlist = new ArrayList<>();
				List<Map<String, Object>> userAuthlist = new ArrayList<>();
				
				List<Map<String,Object>> authList = this.erpRoleMapper.findAuthListByRoleId((Integer) baseRole.get("roleId"));
				for (Map<String,Object> auth : authList){
					//判断那是角色还是用户
					if (auth.get("authType").equals(1)){
						//角色
						Map<String, Object> roleMap =  new HashMap<>();
						ErpRole role =  this.erpRoleMapper.findRoleInfoByRoleId((Integer) auth.get("authId"));
						roleMap.put("id", role.getRoleId());
						roleMap.put("name", role.getName());
						roleAuthlist.add(roleMap);
					}
					else if (auth.get("authType").equals(2)){
						//用户
						Map<String, Object> userMap =  new HashMap<>();
						ErpSysUserVo user = this.erpSysUserMapper.findUserInfoByUserId((Integer) auth.get("authId"));
						userMap.put("id", user.getId());
						userMap.put("name", user.getUsername());
						userAuthlist.add(userMap);
					}
					else{
						logger.info("基础角色授权类型错误："+ auth);
						continue;
					}
				}
				baseRole.put("roleAuthlist",roleAuthlist);
				baseRole.put("userAuthlist",userAuthlist);				
			}
			
		}catch(Exception e){
			logger.info("角色信息-查询所有基础角色列表信息出现错误 方法 findAllBaseRole ："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(baseRoleList);
	}

	public RestResponse findRoleInfoByRoleId(Integer roleId) {
		logger.info("findRoleInfoByRoleId方法：根据roleId查询角色信息 参数"+roleId);		
		
		ErpRole erpRole=new ErpRole();
		try{
			erpRole=this.erpRoleMapper.findRoleInfoByRoleId(roleId);
		}catch(Exception e){
			logger.error("角色信息-查询所有基础角色列表信息出现错误 方法 findAllBaseRole ："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(erpRole);
	}
	
	public RestResponse findChildRole(Integer roleId) {
		logger.info("findChildRole方法：根据roleId查询角色信息 参数"+roleId);		
		List<Map<String, Object>> returnlist = new ArrayList<>();
		try{
			List<ErpRole> erpRoleList = this.erpRoleMapper.findAllChildRoleByFatherId(roleId);
			for (ErpRole erpRole : erpRoleList){
				Map<String, Object> resultMap =  new HashMap<>();
				resultMap.put("name", erpRole.getName());
				resultMap.put("keyword", erpRole.getKeyword());
				
				String employeeName = "";//员工姓名（初始化为空字符串）
				
				//跨工程查询员工姓名
				RestResponse response = this.erpSysUserService.findEmployeeNameByHrProject(erpRole.getChildRoleOwner());
				if("200".equals(response.getStatus())) {
					employeeName = response.getData().toString();
					resultMap.put("childRoleOwnerName", employeeName);
				}
				
				returnlist.add(resultMap);
			}		
			
		}catch(Exception e){
			logger.error("角色信息-查询子角色出现错误 方法 findChildRole ："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(returnlist);
	}
	
	/**
	 * Description: 角色信息-查询角色的菜单列表
	 * @param null     
	 * @return RestResponse             
	 * @Author zhangqian
	 * @Create Date: 2019年2月12日 下午18:32:53
	 */
	public List<String> findMenuByRole(Integer roleId){
		List<String> menus = new ArrayList<>();
		try {
			//查询角色对应的权限信息
			List<ErpSysPrivilegeVo> privilegeList = 
					this.erpSysPrivilegeMapper.findPrivilegeByRoleId(roleId);
			for (ErpSysPrivilegeVo privilege : privilegeList) {
				if(privilege.getPrivilegeAccess().intValue()==2) {
					//如果访问类型为菜单，则根据菜单id，获取菜单编号
					ErpSysMenu erpSysMenu=erpSysMenuMapper.findMenuById(privilege.getPrivilegeAccessValue());
					if (erpSysMenu == null){
						continue;
					}
					menus.add(erpSysMenu.getMenuNo());
				}else {
					menus.add(String.valueOf(privilege.getPrivilegeAccessValue()));
				}
			}			
		} catch (Exception e) {
			logger.error("角色信息-查询角色列表信息出现错误 方法 findAllRole ："+ e.getMessage(),e);
		}
		return menus;
	}
	
	/**
	 * @author songzixuan
	 * @date 2019-3-29
	 * @description 根据employeeId查询员工所具有的角色
	 */
	public RestResponse findRoleListByEmpId(Integer employeeId){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		try {
			list = erpRoleMapper.findRoleListByEmpId(employeeId);
		} catch (Exception e) {
			logger.error("根据employeeId查询员工所具有的角色出现错误 方法 findRoleListByEmpId ："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(list);
	}
	
	/**
	 * Description: 角色信息-修改角色授权
	 * @param  authMap   角色id、角色授权列表、用户授权列表
	 * @return RestResponse             
	 * @Author zhangqian
	 * @Create Date: 2019年5月27日 下午18:32:53
	 */
	@Transactional
	public RestResponse updateRoleAuth (String token, Map<String, Object> authMap){
		try {
			Integer roleId = (Integer) authMap.get("roleId");
			List<Integer> roleAuthlist = (List<Integer>) authMap.get("roleAuthlist");
			List<Integer> userAuthlist = (List<Integer>) authMap.get("userAuthlist");
			
			//查询角色当前的授权
			List<Map<String,Object>> authList = this.erpRoleMapper.findAuthListByRoleId(roleId);
			
			List<Integer> roleList = new ArrayList<>();
			List<Integer> userList = new ArrayList<>();
			for (Map<String,Object> auth : authList){
				if (auth.get("authType").equals(1)){
					roleList.add((Integer) auth.get("authId"));
				}
				else if (auth.get("authType").equals(2)){
					userList.add((Integer) auth.get("authId"));
				}
			}
			
			//处理角色授权更新
			updateRoleAuthMapper(1,roleId,roleAuthlist,roleList);
			//处理用户授权更新
			updateRoleAuthMapper(2,roleId,userAuthlist,userList);
			
			return RestUtils.returnSuccess("修改成功 ！");
		} catch (Exception e) {
			logger.error("角色信息-修改角色信息出现错误  方法 updateRole："+ e.getMessage(),e);
			return RestUtils.returnSuccess("修改失败 ！");
		}
	}
	
	public void updateRoleAuthMapper(Integer type, Integer roleId, List<Integer> newList, List<Integer> oldList){
		logger.info("updateRoleAuth开始执行："+ type,roleId,newList,oldList);
		Map<String,Object> param = new HashMap<>();
		try {
			param.put("authType", type);
			param.put("baseRoleId", roleId);
			
			List<Integer> roleAuthAddList = new ArrayList<>();
			roleAuthAddList.addAll(newList);
			
			List<Integer> roleAuthdeleteList = new ArrayList<>();
			roleAuthdeleteList.addAll(oldList);
			
			for (Integer roleAuth : newList){
				for (Integer role : oldList){
					if (role.equals(roleAuth)){
						// 移除相同的剩下要新增的
						roleAuthAddList.remove(role);
	
						// 移除相同的剩下要删除的
						roleAuthdeleteList.remove(role);
					}
					else{
						
					}
				}
			}
			
			for (Integer id : roleAuthAddList){
				param.put("authId", id);
				param.put("baseRoleId", roleId);
				this.erpRoleMapper.insertRoleAuth(param);
			}
			
			for (Integer id : roleAuthdeleteList){
				param.put("authId", id);
				this.erpRoleMapper.deleteRoleAuth(param);
			}
		} catch (Exception e) {
			logger.error("updateRoleAuth异常："+ e.getMessage(),e);
		}
		return;
	}
	
	/**
	 * Description: 角色管理-查询授权的基础角色
	 * @param null     
	 * @return RestResponse             
	 * @Author zhangqian
	 * @Create Date: 2019年5月27日
	 */
	public RestResponse findBaseRoleByAuth(String token) {
		logger.info("findBaseRoleByAuth开始执行");
		List<Map<String,Object>> basRoleList = new ArrayList<>();
		try{
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			
			Map<String,Object> param = new HashMap<>();
			
			param.put("authType", 2);
			param.put("authId", erpUser.getId());
			
			basRoleList.addAll(this.erpRoleMapper.findRoleByAuth(param));
			
			for (Integer role : erpUser.getRoles()){
				param.put("authType", 1);
				param.put("authId", role);
				
				List<Map<String,Object>> roleList = this.erpRoleMapper.findRoleByAuth(param);
				List<Map<String,Object>> addList = new ArrayList<>();
				
				//去重
				Boolean flag = false;
				for (Map<String,Object> role1 : roleList){
					for (Map<String,Object> role2 : basRoleList){
						if (role1.get("baseRoleId").equals(role2.get("baseRoleId"))){
							flag = true;
							break;
						}
					}
					if (flag){
						flag = false;
					}
					else {
						addList.add(role1);
					}				
				}
				basRoleList.addAll(addList);
			}
		}catch(Exception e){
			logger.error("方法 findBaseRoleByAuth 发生异常："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(basRoleList);
	}

	/**
	 * 获取总裁、经管、HR及子角色ID、副总裁及子角色ID、一级部门经理角色及子角色ID、二级部门经理角色及子角色ID
	 * @param token
	 * @return
	 */
	public RestResponse findRoleIdsAndChildRoleIds(String token) {
		logger.info("findRoleIdsAndChildRoleIds");
		Map<String,Object> roleIdsMap = new HashMap<>();
		try{

			List<Integer> ceoRoleIdList = new ArrayList<>();
			//总裁
			ceoRoleIdList.add(8);
			//获取总裁及子角色ID
			List<Integer> ceoAndChildRoleIdList = erpRoleMapper.findAllChildRoleByFatherIds(ceoRoleIdList);
			roleIdsMap.put("ceoAndChildRoleIdList",ceoAndChildRoleIdList);

			List<Integer> ceoManageHrRoleIdList = new ArrayList<>();
			//总裁
			ceoManageHrRoleIdList.add(8);
			//经管
			ceoManageHrRoleIdList.add(7);
			//HR
			ceoManageHrRoleIdList.add(1);
			//获取总裁、经管、HR及子角色ID
			List<Integer> ceoManageHrAndChildRoleIdList = erpRoleMapper.findAllChildRoleByFatherIds(ceoManageHrRoleIdList);
			roleIdsMap.put("ceoManageHrAndChildRoleIdList",ceoManageHrAndChildRoleIdList);



			List<Integer> deputyRoleIdList = new ArrayList<>();
			//副总裁
			deputyRoleIdList.add(9);
			//获取副总裁及子角色ID
			List<Integer> deputyAndChildRoleIdList = erpRoleMapper.findAllChildRoleByFatherIds(deputyRoleIdList);
			roleIdsMap.put("deputyAndChildRoleIdList",deputyAndChildRoleIdList);

			List<Integer> firstDepartmentManageRoleIdList = new ArrayList<>();
			//一级部门经理角色
			firstDepartmentManageRoleIdList.add(2);
			//获取一级部门经理角色及子角色ID
			List<Integer> firstDepartmentManageAndChildRoleIdList = erpRoleMapper.findAllChildRoleByFatherIds(firstDepartmentManageRoleIdList);
			roleIdsMap.put("firstDepartmentManageAndChildRoleIdList",firstDepartmentManageAndChildRoleIdList);

			List<Integer> secondDepartmentManageRoleIdList = new ArrayList<>();
			//二级部门经理角色
			secondDepartmentManageRoleIdList.add(5);
			//获取二级部门经理角色及子角色ID
			List<Integer> secondDepartmentManageAndChildRoleIdList = erpRoleMapper.findAllChildRoleByFatherIds(secondDepartmentManageRoleIdList);
			roleIdsMap.put("secondDepartmentManageAndChildRoleIdList",secondDepartmentManageAndChildRoleIdList);
		}catch(Exception e){
			logger.error("方法 findRoleIdsAndChildRoleIds 发生异常："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(roleIdsMap);
	}
}

