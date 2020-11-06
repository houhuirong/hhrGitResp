package com.nantian.erp.authentication.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.nantian.erp.authentication.data.dao.ErpSysButtonMapper;
import com.nantian.erp.authentication.data.dao.ErpSysMenuMapper;
import com.nantian.erp.authentication.data.dao.ErpSysPrivilegeMapper;
import com.nantian.erp.authentication.data.dao.ErpUrlPrivilegeMapper;
import com.nantian.erp.authentication.data.dao.ErpUrlRelativeMapper;
import com.nantian.erp.authentication.data.model.ErpSysButton;
import com.nantian.erp.authentication.data.model.ErpSysMenu;
import com.nantian.erp.authentication.data.model.ErpSysUrl;
import com.nantian.erp.authentication.data.vo.ErpSysPrivilegeVo;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;

/** 
 * Description: 树形结构-按钮资源管理接口
 * @author caoxiubin
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月8日                   gaoxiaodong         1.0        
 * </pre>
 */

@Service
public class ErpSysButtonService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ErpSysMenuMapper erpSysMenuMapper;
	
	@Autowired
	private ErpSysButtonMapper erpSysButtonMapper;
	
	@Autowired
	private ErpUrlRelativeMapper erpUrlRelativeMapper;
	
	@Autowired
	private ErpSysPrivilegeMapper erpSysPrivilegeMapper;
	
	@Autowired
	private ErpSysMenuService erpSysMenuService;
	
	@Autowired
	private ErpUrlPrivilegeMapper erpUrlPrivilegeMapper;
	
	@Autowired
	private StringRedisTemplate stringRredisTemplate;

	
	/**
	 * Description: 添加菜单资源
	 * 
	 * @param token
	 * @return RestResponse
	 * @Author gaoxiaodong
	 * @Create Date: 2018年10月8日 下午14:32:53
	 * @updateUser caoxiubin
	 * @updateTime 2018年10月18日 下午13:21:11
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	public RestResponse addButton(Map<String, Object> param) {
		String str = null;
		try {
			/*
			 * 新增按钮自身信息
			 */
			ErpSysButton erpSysButton = new ErpSysButton();
			erpSysButton.setBtnName(String.valueOf(param.get("BtnName")));
			erpSysButton.setBtnNo(Integer.valueOf(String.valueOf(param.get("BtnNo"))));
			erpSysButton.setMenuNo(String.valueOf(param.get("MenuNo")));
			this.erpSysButtonMapper.addButton(erpSysButton);
			/*
			 * 更新下上级菜单isButtonf状态
			 */
			ErpSysMenu erpSysMenuTemp = new ErpSysMenu();
			erpSysMenuTemp.setIsButton(1);;
			erpSysMenuTemp.setMenuNo(String.valueOf(param.get("MenuNo")));
			this.erpSysMenuMapper.updateIsButtonByMenuId(erpSysMenuTemp);
			
			//接收前端传过来的
			List<Integer> urlList = (List<Integer>) param.get("urlList");
			for (Integer url: urlList){
				//删除url与men的关联关系
				erpSysMenuService.deleteRelativeWithUrlAndMenu(url, (Integer) param.get("MenuId"));
				
				//新增button与url的关联关系
				addRelativeWithUrlAndButton(url, erpSysButton.getBtnID());
			}
			
			str = "创建成功！";
		} catch (Exception e) {
				str = "创建失败！";
				logger.info("addButton方法出现异常：" + e.getMessage(),e);
		}
		return RestUtils.returnSuccessWithString(str);
	}

	/**
	 * Description: 修改button资源
	 * 
	 * @param token
	 * @return RestResponse
	 * @Author gaoxiaodong
	 * @updateUser cadoxiubin
	 * @Create Date: 2018年10月8日 下午14:32:53
	 * @updateUser caoxiubin
	 * @updateTime 2018年10月18日 下午13:21:11
	 */
	@Transactional
	public RestResponse updateButton(Map<String, Object> param) {
		logger.info("updateButton方法正在执行：参数：erpSysButtonVo");
		String str = null;
		/*
		 * 目前只做名字修改
		 */
		ErpSysButton erpSysButton = new ErpSysButton();
		erpSysButton.setBtnName(String.valueOf(param.get("BtnName")));
		erpSysButton.setBtnNo(Integer.valueOf(String.valueOf(param.get("BtnNo"))));
		erpSysButton.setBtnID(Integer.valueOf(String.valueOf(param.get("BtnID"))));
		List<Integer> urlList = (List<Integer>) param.get("urlList");
		try {
			this.erpSysButtonMapper.updateButton(erpSysButton);
			
			List<Integer> addList = new ArrayList<Integer>();
			addList.addAll(urlList);
			//查询menu与url的现有关联
			List<Map<String,Object>> relativeList =  erpUrlRelativeMapper.selectUrlRelativeByRelativeId(2, erpSysButton.getBtnID());
			List<Map<String,Object>> deleteList =  new ArrayList<>();
			deleteList.addAll(relativeList);
			for (int i = 0; i<relativeList.size(); i++){
				for (Integer urlId : urlList) {
					if (urlId.equals(relativeList.get(i).get("urlId"))){
						// 移除相同的剩下要新增的
						addList.remove(urlId);

						// 移除相同的剩下要删除的
						deleteList.remove(relativeList.get(i));
					}
				}
			}
			
			if (!addList.isEmpty()){
				for (Integer url: addList){
					//增加url和menu的关联
					addRelativeWithUrlAndButton(url, erpSysButton.getBtnID());
				}
			}
			
			if (!deleteList.isEmpty()){
				for (Map<String,Object> map : deleteList){
					//删除url和menu的关联
					deleteRelativeWithUrlAndButton((Integer) map.get("urlId"), erpSysButton.getBtnID());			
				}
			}
			
			str = "修改成功！";
		} catch (Exception e) {
			str = "修改失败！";
			logger.error("updateButton方法出现异常：" + e.getMessage(),e);
		}
		return RestUtils.returnSuccessWithString(str);
	}
	
	/**
	 * Description: 删除button节点
	 * 
	 * @param token
	 * @return RestResponse
	 * @Author gaoxiaodong
	 * @Create Date: 2018年10月8日 下午14:32:53
	 * @updateUser caoxiubin
	 * @updateTime 2018年10月18日 下午13:21:11
	 */
	@Transactional
	public RestResponse delButton(Map<String, Object> param) {
		logger.info("delButton方法正在执行，参数：menuIdList<Integer>");
		String str = null;
		try {
			Integer BtnID = Integer.valueOf(String.valueOf(param.get("BtnID")));//按钮ID
			Integer BtnNo = Integer.valueOf(String.valueOf(param.get("BtnNo")));//按钮编号
			String MenuNo = String.valueOf(param.get("MenuNo"));//菜单编号
			ErpSysMenu erpSysMenu = erpSysMenuMapper.checkMenuNo(MenuNo);
			Integer MenuID = erpSysMenu.getMenuID();//菜单ID
			
			if ((Boolean)param.get("MenuDelete") != true){
				/*
				 * 判断该子菜单下的按钮个数
				 * 如果只有一个按钮 则将关联的菜单变为叶子节点
				 */
				int count = this.erpSysButtonMapper.countNum(MenuNo);
				if(1 == count) {
					/*
					 * 更新下上级菜单isButtonf状态
					 */
					ErpSysMenu erpSysMenuTemp = new ErpSysMenu();
					erpSysMenuTemp.setIsButton(0);;
					erpSysMenuTemp.setMenuNo(MenuNo);
					this.erpSysMenuMapper.updateIsButtonByMenuId(erpSysMenuTemp);
				}
			}
			this.erpSysButtonMapper.delButton(BtnNo);
			
			//删除button与url的关联
			List<Map<String,Object>> relativeList =  erpUrlRelativeMapper.selectUrlRelativeByRelativeId(2, BtnID);
			if (!relativeList.isEmpty()){
				//删除url和button的关联
				for (Map<String,Object> map : relativeList){
					deleteRelativeWithUrlAndButton((Integer) map.get("urlId"), BtnID);
					
					if ((Boolean)param.get("MenuDelete") != true){
						//增加url和menu的关联
						//addRelativeWithUrlAndButton((Integer) map.get("urlId"), BtnID);
						erpSysMenuService.addRelativeWithUrlAndMenu((Integer) map.get("urlId"), MenuID);
					}
					
				}
			}
			
			//删除button与角色的所有关联
			erpSysPrivilegeMapper.deletePrivilegeByMenuId(3,BtnID);
			
			str = "删除成功";
		} catch (Exception e) {
			str = "删除失败";
			logger.error("delButton方法出现异常：" + e.getMessage(),e);
		}
		return RestUtils.returnSuccessWithString(str);
	}
	
	/**
	 * Description: button编号去重
	 * 
	 * @param erpSysMenu
	 * @return RestResponse
	 * @Author gaoxiaodong
	 * @Create Date: 2018年10月8日 下午14:32:53
	 * @updateUser caoxiubin
	 * @updateTime 2018年10月18日 下午13:21:11
	 */
	@Transactional
	public RestResponse checkButtonNo(String buttonNo) {
		logger.info("checkButtonNo方法正在执行，参数：menuIdList<Integer>");
		boolean flag = false;
		try {
			ErpSysButton erpSysButton = this.erpSysButtonMapper.checkButtonNo(buttonNo);
			if(null == erpSysButton) {
				flag = true;
			}
		} catch (Exception e) {
			logger.error("checkButtonNo方法出现异常：" + e.getMessage(),e);
		}
		return RestUtils.returnSuccess(flag);
	}

	/**
	 * Description: 查询按钮权限信息（供其他工程调用）
	 * 
	 * @param token
	 * @return RestResponse
	 * @Author gaoxiaodong
	 * @Create Date: 2018年10月8日 下午14:32:53
	 */
	public RestResponse findButtonPrivilege(Map<String,Object> paramsMap) {
		/*String menuNo = (String)paramsMap.get("menuNo");
		@SuppressWarnings("unchecked")
		List<Integer> roleIds = (List<Integer>)paramsMap.get("roleIds");*/
		List<Map<String,Object>> erpSysButtonList = this.erpSysButtonMapper.findButtonPrivilege(paramsMap);
		return RestUtils.returnSuccess(erpSysButtonList);
	}
	

	public void addRelativeWithUrlAndButton(Integer urlID, Integer btnId){
		logger.info("addRelativeWithUrlAndButton   urlID:" + urlID + "  btnId:" + btnId);
		try{
			//查询menu关联的角色
			List<ErpSysPrivilegeVo> priviList = erpSysPrivilegeMapper.findRolePrivilegeByMenusId(3,btnId);
			
			if (priviList != null){
				for(ErpSysPrivilegeVo privilege : priviList){
					//增加角色和url的关联关系				
					ErpSysPrivilegeVo findPrivilegeByRoleUrl = erpSysPrivilegeMapper
							.findPrivilegeByRoleUrl(privilege.getPrivilegeValue(), urlID);
					logger.info("findPrivilegeByRoleUrl" + findPrivilegeByRoleUrl);
					if (findPrivilegeByRoleUrl != null) {
						erpSysPrivilegeMapper.addRelativeNumByPrivilegeId(findPrivilegeByRoleUrl.getPrivilegeID());
					} else {
						erpSysPrivilegeMapper.insertPrivilegeByRoleUrl(privilege.getPrivilegeValue(), urlID);
						
						//写入redis
						ErpSysUrl sysUrl = this.erpUrlPrivilegeMapper.getUrlData(urlID);
						String urlPath = sysUrl.getUrlPath().trim();
						String urlKey = (privilege.getPrivilegeValue().toString() + "_" + urlPath).trim();
						
						stringRredisTemplate.opsForValue().set(urlKey, urlPath);
					}
				}				
			}
			
			//增加url与menu的关联
			Map<String, Object> urlRelative = new HashMap<>();
			urlRelative.put("relativeType", 2);
			urlRelative.put("relativeId", btnId);
			urlRelative.put("urlId", urlID);
			
			erpUrlRelativeMapper.insertUrlRelative(urlRelative);			
		}
		catch (Exception e) {
			logger.info("addRelativeWithUrlAndButton失败：" + e.getMessage(), e);
		}
	}
	
	public void deleteRelativeWithUrlAndButton(Integer urlID, Integer btnId){
		logger.info("deleteRelativeWithUrlAndButton   urlID:" + urlID + "  btnId:" + btnId);
		try{
			//查询menu关联的角色
			List<ErpSysPrivilegeVo> priviList = erpSysPrivilegeMapper.findRolePrivilegeByMenusId(3,btnId);
			
			if (priviList != null){
				for(ErpSysPrivilegeVo privilege : priviList){
					//删除角色和url的关联关系				
					ErpSysPrivilegeVo findPrivilegeByRoleUrl = erpSysPrivilegeMapper
							.findPrivilegeByRoleUrl(privilege.getPrivilegeValue(), urlID);
					logger.info("findPrivilegeByRoleUrl" + findPrivilegeByRoleUrl);
					if (findPrivilegeByRoleUrl != null) {
						if (findPrivilegeByRoleUrl.getRelativeNum().equals(1)){
							erpSysPrivilegeMapper.deletePrivilegeByPrivilegeId(findPrivilegeByRoleUrl.getPrivilegeID());
							
							//删除redis
							ErpSysUrl sysUrl = this.erpUrlPrivilegeMapper.getUrlData(urlID);
							String urlPath = sysUrl.getUrlPath().trim();
							String urlKey = (privilege.getPrivilegeValue().toString() + "_" + urlPath).trim();
							
							stringRredisTemplate.delete(urlKey);
						}
						else{
							erpSysPrivilegeMapper.decRelativeNumByPrivilegeId(findPrivilegeByRoleUrl.getPrivilegeID());
						}
					}
				}
			}
			
			//删除url与button的关联
			Map<String, Object> urlRelative = new HashMap<>();
			urlRelative.put("relativeType", 2);
			urlRelative.put("relativeId", btnId);
			urlRelative.put("urlId", urlID);
			
			erpUrlRelativeMapper.deleteUrlRelative(urlRelative);
		}
		catch (Exception e) {
			logger.info("deleteRelativeWithUrlAndButton失败：" + e.getMessage(), e);
		}
	}
	
}
