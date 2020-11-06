package com.nantian.erp.salary.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AesUtils {
	
	private static Logger logger = LoggerFactory.getLogger(AesUtils.class);
	
	private static String password = "2018nt-erpsalary";
	
	/**
     * AES加密字符串
     * 
     * @param content
     *            需要被加密的字符串
     * @param password
     *            加密需要的密码
     * @return 密文
     */
    public static String encrypt(String content) {
    	String encryptedData = null;
        try {
        	if(content!=null && !"".equals(content)) {
        		KeyGenerator kgen = KeyGenerator.getInstance("AES");// 创建AES的Key生产者
        		// 使用这种初始化方法可以特定种子来生成密钥，这样加密后的密文是唯一固定的。
                //kgen.init(128, new SecureRandom(password.getBytes("utf-8")));// 利用用户密码作为随机数初始化出
                //加密没关系，SecureRandom是生成安全随机数序列，password.getBytes()是种子，只要种子相同，序列就一样，所以解密只要有password就行
                SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
                secureRandom.setSeed(password.getBytes());
                kgen.init(128,secureRandom);
                
                SecretKey secretKey = kgen.generateKey();// 根据用户密码，生成一个密钥
                byte[] enCodeFormat = secretKey.getEncoded();// 返回基本编码格式的密钥，如果此密钥不支持编码，则返回
                SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");// 转换为AES专用密钥
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// 创建密码器
                byte[] byteContent = content.getBytes();
                cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化为加密模式的密码器
                byte[] result = cipher.doFinal(byteContent);// 加密
                encryptedData = parseByte2HexStr(result);//将二进制密文转换为十六进制密文
        	}
        } catch (NoSuchPaddingException e) {
            logger.error("NoSuchPaddingException："+e.getMessage(),e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("NoSuchAlgorithmException："+e.getMessage(),e);
        } catch (InvalidKeyException e) {
            logger.error("InvalidKeyException："+e.getMessage(),e);
        } catch (IllegalBlockSizeException e) {
            logger.error("IllegalBlockSizeException："+e.getMessage(),e);
        } catch (BadPaddingException e) {
            logger.error("BadPaddingException："+e.getMessage(),e);
        }
        return encryptedData;
    }
    
    /**
     * 解密AES加密过的字符串
     * 
     * @param content
     *            AES加密过过的内容
     * @param password
     *            加密时的密码
     * @return 明文
     */
    public static String decrypt(String content) {
    	String decryptedData = null;
        try {
        	if(content!=null && !"".equals(content)) {
        		KeyGenerator kgen = KeyGenerator.getInstance("AES");// 创建AES的Key生产者
                //kgen.init(128, new SecureRandom(password.getBytes("utf-8")));
            	
            	SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
                secureRandom.setSeed(password.getBytes());
                kgen.init(128,secureRandom);
                
                SecretKey secretKey = kgen.generateKey();// 根据用户密码，生成一个密钥
                byte[] enCodeFormat = secretKey.getEncoded();// 返回基本编码格式的密钥
                SecretKey key = new SecretKeySpec(enCodeFormat, "AES");// 转换为AES专用密钥
                //Cipher cipher = Cipher.getInstance("AES");// 创建密码器
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// 创建密码器
                cipher.init(Cipher.DECRYPT_MODE, key);// 初始化为解密模式的密码器
                byte[] descryptData = parseHexStr2Byte(content);//将十六进制密文转换为二进制密文
                byte[] result = cipher.doFinal(descryptData);
                decryptedData = new String(result); // 明文
        	}
        } catch (NoSuchAlgorithmException e) {
            logger.error("NoSuchAlgorithmException："+e.getMessage(),e);
        } catch (NoSuchPaddingException e) {
            logger.error("NoSuchPaddingException："+e.getMessage(),e);
        } catch (InvalidKeyException e) {
            logger.error("InvalidKeyException："+e.getMessage(),e);
        } catch (IllegalBlockSizeException e) {
            logger.error("IllegalBlockSizeException："+e.getMessage(),e);
        } catch (BadPaddingException e) {
            logger.error("BadPaddingException："+e.getMessage(),e);
        }
        return decryptedData;
    }
    
    /**将二进制转换成16进制
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < buf.length; i++) {
                    String hex = Integer.toHexString(buf[i] & 0xFF);
                    if (hex.length() == 1) {
                            hex = '0' + hex;
                    }
                    sb.append(hex.toUpperCase());
            }
            return sb.toString();
    }
    
    /**将16进制转换为二进制
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
            if (hexStr.length() < 1)
                    return null;
            byte[] result = new byte[hexStr.length()/2];
            for (int i = 0;i< hexStr.length()/2; i++) {
                    int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
                    int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
                    result[i] = (byte) (high * 16 + low);
            }
            return result;
    }
    

    public static void main(String[] args) {
        String content = "0.0";
        System.out.println("加密之前：" + content);

        // 加密
        String encrypt = AesUtils.encrypt(content);
        System.out.println("加密后的内容：" + encrypt);
        
        // 解密
        String decrypt = AesUtils.decrypt(encrypt);
        System.out.println("解密后的内容：" + decrypt);        
    }

}
