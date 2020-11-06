package com.nantian.erp.common.base.util;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.BASE64Encoder;

/** 
*
* Description: Rsa加密解密
*
* @author yumingxian
* @version 1.0
* <pre>
* Modification History: 
* Date                  Author           Version     
* ------------------------------------------------
* 2018年10月19日         yumingxian       1.0        
* </pre>
*/
public class RsaUtils {
	private static Logger logger = LoggerFactory.getLogger(RsaUtils.class);
	
	private static String cerPath = "D:/mypublickey.cer";		//证书文件路径
	private static String storePath = "D:/mykeystore.keystore";	//证书库文件路径
	private static String alias = "mykey";		//证书别名
	private static String storePw = "ymxsy2008";	//证书库密码
	private static String keyPw = "ymxwy2008";	//证书密码
	
	static{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
	
	/**
	 * rsa加密
	 * @param data
	 * @return
	 */
	public static String encryptDataRsa(String data) {
		String encryptData = null;
		try {
			
			////从证书获取的公钥
			String strpk = getPublicKey(cerPath);
			//从证书获取的私钥
			String strprivk =getPrivateKey(storePath, alias, storePw, keyPw);
//			
			X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(Base64.decodeBase64(strpk.getBytes()));
		    PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(strprivk.getBytes()));

		    KeyFactory keyf = KeyFactory.getInstance("RSA", "BC");
		    
		    PublicKey pubKey = keyf.generatePublic(pubX509);
			
//		    logger.info("加密前字符串=" + data);
			if (pubKey != null && (data != null && !data.equals(""))) {
				encryptData = encryptData(data, pubKey);
//				logger.info("加密后字符串=" + encryptData);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encryptData;
	}
	
	/**
	 * Rsa解谜
	 * @param data
	 * @return
	 */
	public static String decryptDataRsa(String data) {
		String descryptData = null;
		try {
			
			////从证书获取的公钥
			String strpk = getPublicKey(cerPath);
			//从证书获取的私钥
			String strprivk =getPrivateKey(storePath, alias, storePw, keyPw);
//			
		    PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(strprivk.getBytes()));

		    KeyFactory keyf = KeyFactory.getInstance("RSA", "BC");
		    
		    PrivateKey privKey = keyf.generatePrivate(priPKCS8);
			
//		    logger.info("解密前字符串：data=" + data);
			if (privKey != null && (data != null && !data.equals(""))) {
				descryptData = decryptData(data, privKey);
//				logger.info("解密后字符串：descryptData=" + descryptData);
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return  descryptData;
	}
	
	
	private static String getPublicKey(String cerPath) throws Exception {
		CertificateFactory certificatefactory = CertificateFactory.getInstance("X.509");
		FileInputStream fis = new FileInputStream(cerPath);
		X509Certificate Cert = (X509Certificate) certificatefactory.generateCertificate(fis);
		PublicKey pk = Cert.getPublicKey();
		String publicKey = new BASE64Encoder().encode(pk.getEncoded());
		return publicKey;
	}
 
	private static String getPrivateKey(String storePath, String alias, String storePw, String keyPw) throws Exception {
		FileInputStream is = new FileInputStream(storePath);
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(is, storePw.toCharArray());
		is.close();
		PrivateKey key = (PrivateKey) ks.getKey(alias, keyPw.toCharArray());
		String privateKey = new BASE64Encoder().encode(key.getEncoded());
		return privateKey;
	}
	
	
	/**
	 * luoguohui
	 * 2015-12-26
	 * RSA加密
	 */
	private static String encryptData(String data, PublicKey publicKey) {
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] dataToEncrypt = data.getBytes("utf-8");
			byte[] encryptedData = cipher.doFinal(dataToEncrypt);
			String encryptString = Base64.encodeBase64String(encryptedData);
			return encryptString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * luoguohui
	 * 2015-12-26
	 * RSA解密
	 */
	private static String decryptData(String data, PrivateKey privateKey) {
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] descryptData = Base64.decodeBase64(data);
			byte[] descryptedData = cipher.doFinal(descryptData);
			String srcData = new String(descryptedData, "utf-8");
			return srcData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
//	public static void main(String args[]) {
////		testRsa();
//		String data = "luoguohui阿斯顿发生大法20145";
//		System.out.println(data);
//		System.out.println(encryptDataRsa(data));
//		System.out.println(decryptDataRsa(encryptDataRsa(data)));
//		
//	}

}
