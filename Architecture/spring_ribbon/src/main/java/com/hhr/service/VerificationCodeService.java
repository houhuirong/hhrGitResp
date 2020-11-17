package com.hhr.service;

/**
 * @Auther: hhr
 * @Date: 2020/11/11 - 11 - 11 - 11:17
 * @Description: com.hhr.service
 * @version: 1.0
 */
public interface VerificationCodeService {
    /**
     * 获取验证码
     * @param phoneNumber
     * @return
     */
    String getCode(String phoneNumber);
}
