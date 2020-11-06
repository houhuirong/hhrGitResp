/**
 * 
 */
package com.nantian.erp.common.base.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nantian.erp.common.base.exception.BizException;
import com.nantian.erp.common.constants.ExceptionConstants;

/**
 * <pre>
 * Title:配置信息读取工具类
 * Description: 用于获取相关配置信息
 * </pre>
 * 
 * @author nantian.co
 * @version 1.00.00
 * 
 *          <pre>
 * 修改记录
 *    修改后版本:     修改人：  修改日期:     修改内容:
 * </pre>
 */
public class PropertiesUtils {

	private static Logger log = LoggerFactory.getLogger(PropertiesUtils.class);

	private static Configuration config = null;

	static {

		try {
			config = new PropertiesConfiguration("application.properties");

		} catch (ConfigurationException e) {
			String message = "读取全局配置文件application.properties失败";
			log.error(message, e);
			throw new BizException(ExceptionConstants.EX_CONFIG_FILE_INIT_ERROR,message, e);
		}
	}

	public static String getString(String key) {

		String value = config.getString(key);
		log.debug("读取属性文件key:" + key + "value:" + value);

		return value;

	}

	public static String[] getStringArray(String key) {

		String[] valueArray = config.getStringArray(key);

		return valueArray;

	}

	public static int getInt(String key) {

		int value = -1;

		value = config.getInt(key);

		log.debug("读取属性文件key:" + key + "value:" + value);

		return value;

	}
}
