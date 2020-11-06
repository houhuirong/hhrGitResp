/**
 * Copyright (c) 2015, China Construction Bank Co., Ltd. All rights reserved.
 * 南天软件版权所有.
 *
 * 审核人：
 */
package com.nantian.erp.common.base.exception;

import com.nantian.erp.common.constants.ExceptionConstants;

/**
 * 公用运行时异常
 * <p>
 * 
 * @author nantian.co
 * @version 1.0 2015年9月7日
 * @see
 */
public class BizException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5940086202654528275L;

	private String exCode = ExceptionConstants.EX_INNER_ERROR;// 异常代码
	private String exMessage = ExceptionConstants.EX_INNER_ERROR_MSG;// 异常信息

	public BizException() {
		super();
	}

	public BizException(String exCode, String exMessage) {
		super(exMessage);
		this.exCode = exCode;
		this.exMessage = exMessage;
	}

	public BizException(String message) {
		super(message);
		this.setExMessage(message);
	}

	public BizException(String message, Throwable cause) {
		super(message, cause);
		this.setExMessage(message);
	}

	public BizException(String exCode, String exMessage,
			Throwable cause) {
		super(exMessage, cause);
		this.setExCode(exCode);
		this.setExMessage(exMessage);

	}

	public BizException(Throwable cause) {
		super(cause);
	}

	public String getExCode() {
		return exCode;
	}

	public void setExCode(String exCode) {
		this.exCode = exCode;
	}

	public String getExMessage() {
		return exMessage;
	}

	public void setExMessage(String exMessage) {
		this.exMessage = exMessage;
	}

}
