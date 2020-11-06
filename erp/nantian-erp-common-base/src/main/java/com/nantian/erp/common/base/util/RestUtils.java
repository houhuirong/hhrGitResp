/**
 * Copyright (c) 2015, China Construction Bank Co., Ltd. All rights reserved.
 * 南天软件版权所有.
 *
 * 审核人：
 */
package com.nantian.erp.common.base.util;

import com.nantian.erp.common.rest.RestResponse;

/**
 * Rest处理工具类
 * 
 * <p>
 * 
 * @author nantian.co
 * @version 1.0 2015年6月24日
 * @see
 */
public class RestUtils {

	public static RestResponse returnSuccess() {

		RestResponse restResponse = new RestResponse();
		restResponse.setStatus(RestResponse.SUCCESS);

		return restResponse;
	}

	public static RestResponse returnSuccess(String message) {

		RestResponse restResponse = new RestResponse();
		restResponse.setStatus(RestResponse.SUCCESS);
		restResponse.setMsg(message);

		return restResponse;
	}
	
	
	public static RestResponse returnSuccessWithString(String data) {

		RestResponse restResponse = new RestResponse();
		restResponse.setStatus(RestResponse.SUCCESS);
		restResponse.setData(data);

		return restResponse;
	}

	public static RestResponse returnSuccess(Object data) {

		RestResponse restResponse = new RestResponse();
		restResponse.setStatus(RestResponse.SUCCESS);
		restResponse.setData(data);

		return restResponse;
	}

	public static RestResponse returnSuccess(Object data, String message) {

		RestResponse restResponse = new RestResponse();
		restResponse.setStatus(RestResponse.SUCCESS);
		restResponse.setData(data);
		restResponse.setMsg(message);

		return restResponse;
	}

	public static RestResponse returnFailure(Object data) {

		RestResponse restResponse = new RestResponse();
		restResponse.setStatus(RestResponse.FAIL);
		restResponse.setData(data);

		return restResponse;
	}

	public static RestResponse returnFailure(Object data, String message) {

		RestResponse restResponse = new RestResponse();
		restResponse.setStatus(RestResponse.FAIL);
		restResponse.setData(data);
		restResponse.setMsg(message);

		return restResponse;
	}

	public static RestResponse returnFailure(String message) {

		RestResponse restResponse = new RestResponse();
		restResponse.setStatus(RestResponse.FAIL);
		restResponse.setMsg(message);

		return restResponse;
	}
	
	public static RestResponse returnTips(String message) {

		RestResponse restResponse = new RestResponse();
		restResponse.setStatus(RestResponse.TIPS);
		restResponse.setMsg(message);

		return restResponse;
	}

}
