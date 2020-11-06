package com.nantian.erp.authentication.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.nantian.erp.authentication.data.dao.ErpSysButtonMapper;
import com.nantian.erp.authentication.data.dao.ErpSysMenuMapper;
import com.nantian.erp.authentication.data.dao.ErpSysPrivilegeMapper;
import com.nantian.erp.authentication.data.dao.ErpSysUrlMapper;
import com.nantian.erp.authentication.data.dao.ErpUrlPrivilegeMapper;
import com.nantian.erp.authentication.data.dao.ErpUrlRelativeMapper;
import com.nantian.erp.authentication.data.model.ErpSysUrl;
import com.nantian.erp.authentication.data.vo.ErpSysModulVo;
import com.nantian.erp.authentication.data.vo.ErpSysPrivilegeVo;
import com.nantian.erp.authentication.data.vo.ErpSysUrlVo;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;

/**
 * Description: 模块-url管理 service 接口
 * 
 * @author caoxiubin
 * @version 1.0
 * 
 *          <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月8日                   caoxiubin         1.0
 *          </pre>
 */
@Service
public class ErpSysUrlService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private ErpSysUrlMapper erpUrlMapper;

	@Autowired
	private ErpSysMenuMapper erpSysMenuMapper;

	@Autowired
	private ErpSysButtonMapper erpSysButtonMapper;

	@Autowired
	private ErpSysPrivilegeMapper erpSysPrivilegeMapper;

	@Autowired
	private ErpUrlRelativeMapper erpUrlRelativeMapper;
	
	@Autowired
	private ErpUrlPrivilegeMapper erpUrlPrivilegeMapper;
	
	@Autowired
	private StringRedisTemplate stringRredisTemplate;
	/**
	 * Description: 模块-url-查询列表信息
	 * 
	 * @param null
	 * @return RestResponse
	 * @Author caoxiubin
	 * @Create Date: 2018年10月9日 下午18:32:53
	 */
	public RestResponse findAllUrlToModul() {

		List<ErpSysModulVo> modulList = null;
		try {
			modulList = this.erpUrlMapper.findAllModul();
			for (ErpSysModulVo erpSysModulVo : modulList) {
				// 查找所有url
				List<ErpSysUrlVo> urlList = this.erpUrlMapper.findAllUrlToModulId(erpSysModulVo.getModulId());
				for (ErpSysUrlVo erpSysUrlVo : urlList) {				
					//查询URL关联的所有菜单信息
					List<Map<String, Object>> urlRelativeByUrlId = erpUrlRelativeMapper
							.selectUrlRelativeByUrlId(erpSysUrlVo.getUrlID());
					String[] menus = new String[urlRelativeByUrlId.size()];
					for (int i=0; i<urlRelativeByUrlId.size(); i++){
						if (urlRelativeByUrlId.get(i).get("relativeType").equals(1)){
							menus[i] = String.valueOf(urlRelativeByUrlId.get(i).get("MenuNo"));
						}
						else if (urlRelativeByUrlId.get(i).get("relativeType").equals(2)){
							menus[i] = String.valueOf(urlRelativeByUrlId.get(i).get("relativeId"));
						}
						else{
							logger.info("url关联查询菜单，关联类型错误" + urlRelativeByUrlId.get(i).get("relativeType"));
						}
						
					}
					erpSysUrlVo.setMenus(menus);			
				}
				erpSysModulVo.setUrlList(urlList);
			}

		} catch (Exception e) {
			logger.error("查询模块url列表信息出现错误 方法 findAllUrlToModul ：" + e.getMessage(), e);
		}
		return RestUtils.returnSuccess(modulList);
	}

	/**
	 * Description: 模块-url-新增url信息
	 * 
	 * @param erpSysUrl url对象
	 * @return RestResponse
	 * @Author caoxiubin
	 * @Create Date: 2018年10月11日 上午11:32:53
	 */
	@Transactional(rollbackFor = Exception.class)
	public RestResponse insertUrl(ErpSysUrlVo erpSysUrlVo, HttpServletRequest request) {

		String str = null;
		try {
			/*
			 * 新增url本身信息
			 */
			this.erpUrlMapper.insertUrl(erpSysUrlVo);
			Integer urlID = erpSysUrlVo.getUrlID();
			String[] menus = erpSysUrlVo.getMenus();
			for (int i = 0; i < menus.length; i++) {
				Map<String, Object> urlRelative = new HashMap<>();
				
				//查询menu和button的id，并格式化数据
				if((menus[i]+"").contains("m")) {
					urlRelative.put("relativeType", 1);
					Map<String, Object> findMenuIdByNoMap = erpSysMenuMapper.findMenuIdByNo(menus[i]);
					urlRelative.put("relativeId", findMenuIdByNoMap.get("MenuID"));
				}else {
					urlRelative.put("relativeType", 2);
					Map<String, Object> findButtonIdByNoMap = erpSysButtonMapper.findButtonIdByNo(menus[i]);
					urlRelative.put("relativeId", findButtonIdByNoMap.get("BtnID"));
				}
				addUrlWithMenu(urlID, urlRelative);
			}
			str = "新增成功 ！";
		} catch (Exception e) {
			str = "新增失败！";
			logger.error("insertUrl方法出现异常：" + e.getMessage(), e);
		}
		return RestUtils.returnSuccess(str);
	}

	private void updateRelativeNum(Integer urlID, Set<Integer> roleSet) {
		for (Integer roleId : roleSet) {
			logger.info("updateRelativeNum   roleId:" + roleId + "  urlID:" + urlID);
			ErpSysPrivilegeVo findPrivilegeByRoleUrl = erpSysPrivilegeMapper
					.findPrivilegeByRoleUrl(roleId, urlID);
			logger.info("findPrivilegeByRoleUrl" + findPrivilegeByRoleUrl);
			if (findPrivilegeByRoleUrl != null) {
				erpSysPrivilegeMapper.addRelativeNumByPrivilegeId(findPrivilegeByRoleUrl.getPrivilegeID());
			} else {
				erpSysPrivilegeMapper.insertPrivilegeByRoleUrl(roleId, urlID);
				
				//写入redis
				ErpSysUrl sysUrl = this.erpUrlPrivilegeMapper.getUrlData(urlID);
				String urlPath = sysUrl.getUrlPath().trim();
				String urlKey = (roleId.toString() + "_" + urlPath).trim();
				
				stringRredisTemplate.opsForValue().set(urlKey, urlPath);

			}
		}
	}

	private void deleteRelativeNum(Integer urlID, Set<Integer> roleSet) {
		for (Integer roleId : roleSet) {
			logger.info("deleteRelativeNum  roleId:" + roleId + "  urlID:" + urlID);
			ErpSysPrivilegeVo findPrivilegeByRoleUrl = erpSysPrivilegeMapper
					.findPrivilegeByRoleUrl(roleId, urlID);
			logger.info("findPrivilegeByRoleUrl" + findPrivilegeByRoleUrl);
			if (findPrivilegeByRoleUrl != null) {
				if (findPrivilegeByRoleUrl.getRelativeNum().equals(1)){
					erpSysPrivilegeMapper.deletePrivilegeByPrivilegeId(findPrivilegeByRoleUrl.getPrivilegeID());
					
					//删除redis
					ErpSysUrl sysUrl = this.erpUrlPrivilegeMapper.getUrlData(urlID);
					String urlPath = sysUrl.getUrlPath().trim();
					String urlKey = (roleId.toString() + "_" + urlPath).trim();
					
					stringRredisTemplate.delete(urlKey);
				}
				else{
					erpSysPrivilegeMapper.addRelativeNumByPrivilegeId(findPrivilegeByRoleUrl.getPrivilegeID());
				}
			}
		}
	}
	
	@Transactional
	private String addUrlWithMenu(Integer urlID, Map<String, Object> urlRelative){
		String str = null;
		try {
			Set<Integer> roleSet = new HashSet<>();
			
			List<ErpSysPrivilegeVo> rolePrivilege = null;
			
			//通过menu和button查询关联的角色
			urlRelative.put("urlId", urlID);
			erpUrlRelativeMapper.insertUrlRelative(urlRelative);
			
			Integer privilegeAccess = (Integer) urlRelative.get("relativeType") + 1;
			
			rolePrivilege = erpSysPrivilegeMapper.findRolePrivilegeByMenusId(privilegeAccess, urlRelative.get("relativeId"));
			
			for (ErpSysPrivilegeVo erpSysPrivilegeVo : rolePrivilege) {
				Integer roleId = erpSysPrivilegeVo.getPrivilegeValue();
				roleSet.add(roleId);
			}							

			updateRelativeNum(urlID, roleSet);
			str = "新增成功 ！";
		} catch (Exception e) {
			str = "新增失败！";
			logger.error("addUrlWithMenu方法出现异常：" + e.getMessage(), e);
		}
		return str;
	}
	
	@Transactional
	private String deleteUrlWithMenu(Integer urlID, Map<String, Object> urlRelative){
		String str = null;
		try {
			Set<Integer> roleSet = new HashSet<>();
			
			List<ErpSysPrivilegeVo> rolePrivilege = null;			
			
			Integer privilegeAccess = (Integer) urlRelative.get("relativeType") + 1;
			
			rolePrivilege = erpSysPrivilegeMapper.findRolePrivilegeByMenusId(privilegeAccess, urlRelative.get("relativeId"));
			
			for (ErpSysPrivilegeVo erpSysPrivilegeVo : rolePrivilege) {
				Integer roleId = erpSysPrivilegeVo.getPrivilegeValue();
				roleSet.add(roleId);
			}							
			
			//通过menu和button查询关联的角色
			deleteRelativeNum(urlID, roleSet);			

			//删除url与菜单的关联
			urlRelative.put("urlId", urlID);
			erpUrlRelativeMapper.deleteUrlRelative(urlRelative);
			
			str = "新增成功 ！";
		} catch (Exception e) {
			str = "新增失败！";
			logger.error("deleteUrlWithMenu方法出现异常：" + e.getMessage(), e);
		}
		return str;
	}
	
	@Transactional
	public RestResponse updateUrl(ErpSysUrlVo erpSysUrlVo) {
		String str = null;
		try {
			ErpSysUrlVo oldUrlInfo = this.erpUrlMapper.getUrlInfoById(erpSysUrlVo.getUrlID());
			if (!oldUrlInfo.getUrlPath().equals(erpSysUrlVo.getUrlPath())){
				//url路径变化，更新redis缓存
				List<ErpSysPrivilegeVo> privilegeList = erpSysPrivilegeMapper.findPrivilegeByUrlID(erpSysUrlVo.getUrlID());
				for (ErpSysPrivilegeVo privilege:privilegeList){
					//删除redis
					String urlPath = oldUrlInfo.getUrlPath().trim();
					String urlKey = (privilege.getPrivilegeValue().toString() + "_" + urlPath).trim();
					stringRredisTemplate.delete(urlKey);
					
					//写入redis
					String urlPath2 = erpSysUrlVo.getUrlPath().trim();
					String urlKey2 = (privilege.getPrivilegeValue().toString() + "_" + urlPath2).trim();
					stringRredisTemplate.opsForValue().set(urlKey2, urlPath2);
				}				
			}
			this.erpUrlMapper.updateUrl(erpSysUrlVo);
			String[] menus = erpSysUrlVo.getMenus();
			List<Map<String, Object>> menuBtnLi = new ArrayList<>();
			List<Map<String, Object>> addList = new ArrayList<>();
			
			//通过menu、button与url现有的关联关系
			for (int i = 0; i < menus.length; i++) {
				Map<String, Object> map = new HashMap<>();
				
				if((menus[i]+"").contains("m")) {
					Map<String, Object> findMenuIdByNo = erpSysMenuMapper.findMenuIdByNo(menus[i]);
					map.put("relativeId", findMenuIdByNo.get("MenuID"));
					map.put("relativeType", 1);
				}else {
					map.put("relativeId", menus[i]);
					map.put("relativeType", 2);
				}
				menuBtnLi.add(map);
				addList.add(map);
			}
		
			List<Map<String, Object>> urlRelativeByUrlId = erpUrlRelativeMapper
					.selectUrlRelativeByUrlId(erpSysUrlVo.getUrlID());
			List<Map<String, Object>> deleteList = new ArrayList<>();
			if (!urlRelativeByUrlId.isEmpty()) {				
				deleteList.addAll(urlRelativeByUrlId);
				for (Map<String, Object> map : urlRelativeByUrlId) {
					for (Map<String, Object> menuBtnMap : menuBtnLi) {
						if (map.get("relativeType").equals(menuBtnMap.get("relativeType"))
								&& map.get("relativeId").equals(menuBtnMap.get("relativeId"))) {
							// 移除相同的剩下要新增的
							addList.remove(menuBtnMap);

							// 移除相同的剩下要删除的
							deleteList.remove(map);
						}
					}
				}
			}
			
		   //新增部分
			if (!addList.isEmpty()) {
				for (Map<String, Object> map : addList) {
					str = addUrlWithMenu(erpSysUrlVo.getUrlID(), map);
				}
			}
			
			//删除部分
			if(!deleteList.isEmpty()) {
				for (Map<String, Object> map : deleteList) {
					
					str = deleteUrlWithMenu(erpSysUrlVo.getUrlID(), map);
				}
			}
			
		} catch (Exception e) {
			str = "修改失败！";
			logger.error("updateUrl方法出现异常：" + e.getMessage(), e);
		}
		return RestUtils.returnSuccess(str);
	}

	
	@Transactional
	public RestResponse deleteUrl(Integer urlID) {

		String str = null;
		try {
			
			erpSysPrivilegeMapper.deletePrivilegeByUrlID(urlID);
			
			this.erpUrlMapper.deleteUrl(urlID);
			
			erpUrlRelativeMapper.deleteUrlRelativeByID(urlID);
			
			str = "删除成功 ！";
		} catch (Exception e) {
			str = "删除失败 ！";
			logger.error("url信息-删除url信息出现错误  方法 deleteUrl：" + e.getMessage(), e);
		}
		return RestUtils.returnSuccess(str);
	}

	/**
	 * Description: url信息-检验url路径是否重复
	 * 
	 * @param UrlPath
	 * @return RestResponse
	 * @Author caoxiubin
	 * @Create Date: 2018年10月11日 上午11:32:53
	 */
	public RestResponse checkUrlPath(String UrlPath) {

		boolean flag = false;
		try {
			ErpSysUrlVo erpSysUrlVo = this.erpUrlMapper.checkUrlPath(UrlPath);
			if (null == erpSysUrlVo) {
				flag = true;
			}
		} catch (Exception e) {
			logger.error("url信息-检验url路径是否重复出现错误  方法 checkUrlPath：" + e.getMessage(), e);
		}
		return RestUtils.returnSuccess(flag);
	}

}
