package com.nantian.erp.authentication.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.nantian.erp.authentication.data.dao.ErpUrlPrivilegeMapper;
import com.nantian.erp.authentication.data.model.ErpSysPrivilege;
import com.nantian.erp.authentication.data.model.ErpSysUrl;


/**
 *
 * Description: 服务启动时，将url同步到Redis中(Service)
 *
 * @author gaoxiaodong
 * @version 1.0
 * 
 *          <pre>
* Modification History: 
* Date                  Author           Version     
* ------------------------------------------------
* 2018年10月09日      		gaoxiaodong       1.0
 * 
 *          </pre>
 */
@Service
public class ErpUrlPrivilegeService {

	@Autowired
	private ErpUrlPrivilegeMapper erpUrlPrivilegeMapper;
	@Autowired
	private StringRedisTemplate stringRredisTemplate;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Description：服务启动，查询数据库Sys_privilege表中的urlId
	 * 
	 * @author gaoxiaodong
	 * @version 1.0
	 * @create 2018年10月9日
	 */
	public List<ErpSysPrivilege> urlIdByPrivilegeAccess() {
		logger.info("seceltAccessType方法开始执行，传递参数：无");
		List<ErpSysPrivilege> sysList = null;
		try {
			sysList = this.erpUrlPrivilegeMapper.urlIdByPrivilegeAccess();
		} catch (Exception e) {
			logger.error("urlIdByPrivilegeAccess方法出现异常：" + e.getMessage(),e);
		}
		return sysList;
	}

	/**
	 * Description：通多urlId获取Sys_url表中的urlPath，同步到redis中
	 * 
	 * @author gaoxiaodong
	 * @version 1.0
	 * @create 2018年10月9日
	 */
	public void SaveUrlForRedis() {
		logger.info("SaveUrlForRedis方法开始执行，传递参数：无");
		try {
			List<ErpSysPrivilege> sysList = this.urlIdByPrivilegeAccess();
			logger.info("sysList长度："+ sysList.size());
			String urlPath = null;
			String urlKey = null;
			for (ErpSysPrivilege erpSysPrivilege : sysList) {
				Integer urlID = erpSysPrivilege.getPrivilegeAccessValue();
				Integer roleId = erpSysPrivilege.getPrivilegeValue();
				if (urlID != null) {
					ErpSysUrl sysUrl = this.erpUrlPrivilegeMapper.getUrlData(urlID);
					if (sysUrl != null) {
						urlPath = sysUrl.getUrlPath().trim();
						urlKey = (roleId.toString() + "_" + urlPath).trim();
						// 判断key与value是否存在，如果不存在就直接存储
						if (stringRredisTemplate.opsForValue().setIfAbsent(urlKey, urlPath) == false) {
							stringRredisTemplate.opsForValue().set(urlKey, urlPath);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("SaveUrlForRedis方法出现异常：" + e.getMessage(),e);
		}
	}
}
