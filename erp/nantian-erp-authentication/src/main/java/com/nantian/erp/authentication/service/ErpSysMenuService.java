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
import com.nantian.erp.authentication.constants.DicConstants;
import com.nantian.erp.authentication.data.dao.ErpSysButtonMapper;
import com.nantian.erp.authentication.data.dao.ErpSysMenuMapper;
import com.nantian.erp.authentication.data.dao.ErpSysPrivilegeMapper;
import com.nantian.erp.authentication.data.dao.ErpUrlPrivilegeMapper;
import com.nantian.erp.authentication.data.dao.ErpUrlRelativeMapper;
import com.nantian.erp.authentication.data.model.ErpSysButton;
import com.nantian.erp.authentication.data.model.ErpSysMenu;
import com.nantian.erp.authentication.data.model.ErpSysUrl;
import com.nantian.erp.authentication.data.vo.ErpSysBntVo;
import com.nantian.erp.authentication.data.vo.ErpSysPrivilegeVo;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;

/**
 * Description: 菜单资源管理接口
 * 
 * @author caoxiubin
 * @version 1.0
 * 
 *          <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月8日                   gaoxiaodong         1.0
 *          </pre>
 */

@Service
public class ErpSysMenuService {

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
	private ErpSysButtonService erpSysButtonService;
	
	@Autowired
	private ErpUrlPrivilegeMapper erpUrlPrivilegeMapper;
	
	@Autowired
	private StringRedisTemplate stringRredisTemplate;

	/**
	 * Description: 查找菜单资源
	 * 
	 * @param token
	 * @return RestResponse
	 * @Author gaoxiaodong
	 * @updateUser caoxiubin 
	 * @Create Date: 2018年10月8日 下午14:32:53
	 * @updateUser caoxiubin 
	 * @updateTime 2018年10月17日 上午09:32:53
	 */
	public List<Object> findAllMenu() {
		logger.info("findAllMenuAndBtn方法执行，无参数");
		// 返回web层一个list数组
		List<Object> list = null;
		try {
			list = new ArrayList<>();
			// 菜单层级数据
			List<ErpSysMenu> resultList = erpSysMenuMapper.findAllMenuAndBtn();
			for (ErpSysMenu erpSysMenu : resultList) {
				// 根据menuNo查找ErpSysButton,并平铺到菜单列表
				List<ErpSysButton>  buttonList = this.erpSysButtonMapper.getAllButtonByMenuNo(erpSysMenu.getMenuNo());
				/*
				 * 将按钮处理成前端需要的格式-菜单格式
				 */
				ErpSysBntVo erpSysButtonVo = null;
				for (ErpSysButton erpSysButton : buttonList) {
					erpSysButtonVo = new ErpSysBntVo();
					erpSysButtonVo.setMenuNo(String.valueOf(erpSysButton.getBtnID()));
					erpSysButtonVo.setMenuparentNo(erpSysButton.getMenuNo());
					erpSysButtonVo.setBtnNo(erpSysButton.getBtnNo());
					erpSysButtonVo.setMenuName(erpSysButton.getBtnName());
					erpSysButtonVo.setBtnClass(erpSysButton.getBtnClass());
					erpSysButtonVo.setBtnIcon(erpSysButton.getBtnIcon());
					erpSysButtonVo.setBtnScript(erpSysButton.getBtnScript());
					erpSysButtonVo.setInitStatus(erpSysButton.getInitStatus());
					erpSysButtonVo.setType(DicConstants.BUTTON_TYPE);					

					//查找button关联的url列表				
					List<Map<String,Object>> relativeList =  erpUrlRelativeMapper.selectUrlRelativeByRelativeId(2, erpSysButton.getBtnID());
					Integer[] urlList = new Integer[relativeList.size()];
					for (int i = 0; i<relativeList.size(); i++){
						Integer urlId = (Integer) relativeList.get(i).get("urlId");
						urlList[i] = urlId;
					}
					erpSysButtonVo.setUrlList(urlList);
					
					list.add(erpSysButtonVo);
				}
				erpSysMenu.setType(DicConstants.MENU_TYPE);
				
				//查找menu关联的url列表				
				List<Map<String,Object>> relativeList =  erpUrlRelativeMapper.selectUrlRelativeByRelativeId(1, erpSysMenu.getMenuID());
				Integer[] urlList = new Integer[relativeList.size()];
				for (int i = 0; i<relativeList.size(); i++){
					Integer urlId = (Integer) relativeList.get(i).get("urlId");
					urlList[i] = urlId;
				}
				erpSysMenu.setUrlList(urlList);
				
				list.add(erpSysMenu);
			}
		} catch (Exception e) {
			logger.error("findAllMenuAndButAndType方法出现异常：" + e.getMessage());
		}
		return list;
	}
	
	/**
	 * Description: 添加菜单资源
	 * 
	 * @param token
	 * @return RestResponse
	 * @Author gaoxiaodong
	 * @Create Date: 2018年10月8日 下午14:32:53
	 */
	@Transactional
	public RestResponse addMenu(ErpSysMenu erpSysMenu) {
		String str = null;
		try {
			/*
			 * 新增菜单自身信息
			 */
			this.erpSysMenuMapper.addMenu(erpSysMenu);
			/*
			 * 不管在哪级菜单下新增菜单 更新下上级菜单isleaf状态
			 */
			ErpSysMenu erpSysMenuTemp = new ErpSysMenu();
			erpSysMenuTemp.setMenuNo(erpSysMenu.getMenuparentNo());
			erpSysMenuTemp.setIsLeaf(1);
			this.erpSysMenuMapper.updateIsLeafByMenuId(erpSysMenuTemp);
			
			//新增menu和url的关联关系
			Integer[] urlList = erpSysMenu.getUrlList();
			for (Integer url: urlList){
				addRelativeWithUrlAndMenu(url, erpSysMenu.getMenuID());
			}			
			str = "创建成功！";
		} catch (Exception e) {
				str = "创建失败！";
				logger.error("addMenu方法出现异常：" + e.getMessage(),e);
		}
		return RestUtils.returnSuccessWithString(str);
	}

	/**
	 * Description: 修改菜单资源
	 * 
	 * @param token
	 * @return RestResponse
	 * @Author gaoxiaodong
	 * @Create Date: 2018年10月8日 下午14:32:53
	 */
	@Transactional
	public RestResponse updateMenu(Map<String, Object> param) {
		logger.info("updateMenuAndBut方法正在执行：参数：ErpSysMenu");
		// 获取修改数据，添加到ErpSysMenu中
		ErpSysMenu erpSysMenu = new ErpSysMenu();
		erpSysMenu.setMenuID(Integer.valueOf(String.valueOf(param.get("MenuID"))));
		erpSysMenu.setMenuNo(String.valueOf(param.get("MenuNo")));
		erpSysMenu.setMenuparentNo(String.valueOf(param.get("MenuparentNo")));
		erpSysMenu.setMenuUrl(String.valueOf(param.get("MenuUrl")));
		erpSysMenu.setMenuOrder(Integer.valueOf(String.valueOf(param.get("MenuOrder"))));
		erpSysMenu.setMenuName(String.valueOf(param.get("MenuName")));
		List<Integer> urlList = (List<Integer>) param.get("urlList");
		String str = null;
		try {
			this.erpSysMenuMapper.updateMenu(erpSysMenu);
			
			List<Integer> addList = new ArrayList<Integer>();
			addList.addAll(urlList);
			
			//查询menu与url的现有关联
			List<Map<String,Object>> relativeList =  erpUrlRelativeMapper.selectUrlRelativeByRelativeId(1, erpSysMenu.getMenuID());
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
				//增加url和menu的关联
				for (Integer url: addList){
					addRelativeWithUrlAndMenu(url, erpSysMenu.getMenuID());
				}
			}
			
			if (!deleteList.isEmpty()){
				//删除url和menu的关联
				for (Map<String,Object> map : deleteList){
					deleteRelativeWithUrlAndMenu((Integer) map.get("urlId"), erpSysMenu.getMenuID());
				}
			}
			
			
			str = "修改成功！";
		} catch (Exception e) {
			str = "修改失败！";
			logger.error("updateMenuAndBut方法出现异常：" + e.getMessage(),e);
		}
		return RestUtils.returnSuccessWithString(str);
	}
	
	public void addRelativeWithUrlAndMenu(Integer urlID, Integer menuId){
		logger.info("addRelativeWithUrlAndMenu   urlID:" + urlID + "  menuId:" + menuId);
		try{
			//查询menu关联的角色
			List<ErpSysPrivilegeVo> priviList = erpSysPrivilegeMapper.findRolePrivilegeByMenusId(2,menuId);
			
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
			urlRelative.put("relativeType", 1);
			urlRelative.put("relativeId", menuId);
			urlRelative.put("urlId", urlID);
			
			erpUrlRelativeMapper.insertUrlRelative(urlRelative);			
		}
		catch (Exception e) {
			logger.info("addRelativeWithUrlAndMenu失败：" + e.getMessage(), e);
		}
	}
	
	public void deleteRelativeWithUrlAndMenu(Integer urlID, Integer menuId){
		logger.info("deleteRelativeWithUrlAndMenu   urlID:" + urlID + "  menuId:" + menuId);
		try{
			//查询menu关联的角色
			List<ErpSysPrivilegeVo> priviList = erpSysPrivilegeMapper.findRolePrivilegeByMenusId(2,menuId);
			
			if (priviList != null){
				for(ErpSysPrivilegeVo privilege : priviList){
					//增加角色和url的关联关系				
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
			
			//删除url与menu的关联
			Map<String, Object> urlRelative = new HashMap<>();
			urlRelative.put("relativeType", 1);
			urlRelative.put("relativeId", menuId);
			urlRelative.put("urlId", urlID);
			
			erpUrlRelativeMapper.deleteUrlRelative(urlRelative);
		}
		catch (Exception e) {
			logger.info("deleteRelativeWithUrlAndMenu失败：" + e.getMessage(), e);
		}
	}
	
	/**
	 * Description: 删除菜单资源
	 * 
	 * @param delMenu
	 * @return String
	 * @Author zhangqian
	 * @Create Date: 2019年1月22日 下午14:32:53
	 */
	@Transactional
	public String delMenu(ErpSysMenu erpSysMenu) {
		logger.info("delMenuRec方法正在执行，参数：menuIdList<Integer>");
		String str = null;
		RestResponse ret = null;
		try {
			//删除本级菜单的按钮
			if (1 == erpSysMenu.getIsButton()){
				// 根据menuNo查找ErpSysButton,获取叶子 节点下的btn
				List<ErpSysButton> erpSysButtonList = erpSysButtonMapper.getAllButtonByMenuNo(erpSysMenu.getMenuNo());
				
				if (erpSysButtonList != null){
					for (ErpSysButton erpSysButton : erpSysButtonList){
						//删除按钮
						Map<String, Object> param = new HashMap<>();
						param.put("MenuNo", erpSysMenu.getMenuNo());
						param.put("BtnNo", erpSysButton.getBtnNo());
						param.put("MenuDelete", true);
						param.put("MenuId", erpSysMenu.getMenuID());
						erpSysButtonService.delButton(param);					
					}
				}
			}
				
			//查询该菜单的上级菜单的子菜单个数
			Integer count = this.erpSysMenuMapper.countNum(erpSysMenu.getMenuparentNo());
			if(1 == count) {
				/*
				 * 更新上级菜单isleaf状态
				 */
				ErpSysMenu erpSysMenuTemp = new ErpSysMenu();
				erpSysMenuTemp.setIsLeaf(0);
				erpSysMenuTemp.setMenuNo(erpSysMenu.getMenuparentNo());
				this.erpSysMenuMapper.updateIsLeafByMenuId(erpSysMenuTemp);
			}
			
			/*
			 * 删除末级菜单时
			 */
			if(0 == erpSysMenu.getIsLeaf()) {
				this.erpSysMenuMapper.delMenu(erpSysMenu.getMenuID());
				str = "删除成功";
			}else {
				//查找下一级菜单
				List<ErpSysMenu> leafMenuList = this.erpSysMenuMapper.findLeafMenu(erpSysMenu.getMenuNo());
				for(int i = 0; i < leafMenuList.size(); i++){
					ErpSysMenu leafMenu = leafMenuList.get(i);
					str = delMenu(leafMenu);
					logger.info("删除菜单：" + str, leafMenu.getMenuID());
				}

				this.erpSysMenuMapper.delMenu(erpSysMenu.getMenuID());
				str = "删除成功";
			}
			
			//删除与menu关联的url
			List<Map<String,Object>> relativeList =  erpUrlRelativeMapper.selectUrlRelativeByRelativeId(1, erpSysMenu.getMenuID());
			if (!relativeList.isEmpty()){
				//删除url和menu的关联
				for (Map<String,Object> map : relativeList){
					deleteRelativeWithUrlAndMenu((Integer) map.get("urlId"), erpSysMenu.getMenuID());
				}
			}
			
			//删除menu与角色的所有关联
			erpSysPrivilegeMapper.deletePrivilegeByMenuId(2, erpSysMenu.getMenuID());
			
		} catch (Exception e) {
			str = "删除失败";
			logger.error("delMenuRec方法出现异常：" + e.getMessage(),e);
		}
		return str;
	}
	
	/**
	 * Description: 菜单编号去重
	 * 
	 * @param erpSysMenu
	 * @return RestResponse
	 * @Author gaoxiaodong
	 * @Create Date: 2018年10月8日 下午14:32:53
	 */
	@Transactional
	public RestResponse checkMenuNo(String menuNo) {
		logger.info("checkMenuNo方法正在执行，参数：menuIdList<Integer>");
		boolean flag = false;
		try {
			ErpSysMenu erpSysMenu = this.erpSysMenuMapper.checkMenuNo(menuNo);
			if(null == erpSysMenu) {
				flag = true;
			}
		} catch (Exception e) {
			logger.error("checkMenuNo方法出现异常：" + e.getMessage(),e);
		}
		return RestUtils.returnSuccess(flag);
	}

}
