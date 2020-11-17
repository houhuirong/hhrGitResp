package com.online.taxi.passenger.feign.request;

import lombok.Data;
/**
 * @author yueyi2019
 */
@Data
public class CodeVerifyRequest {

	private int identity;

	private String phoneNumber;
	
	private String code;
}