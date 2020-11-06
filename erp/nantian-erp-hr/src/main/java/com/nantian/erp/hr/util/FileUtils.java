package com.nantian.erp.hr.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.jcraft.jsch.SftpException;

import sun.misc.BASE64Encoder;

/** 
 * Description: 文件相关工具类
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年03月05日      		ZhangYuWei          1.0       
 * </pre>
 */
@Component
@PropertySource(value= {"classpath:config/sftp.properties","file:${spring.profiles.path}/config/sftp.properties"},ignoreResourceNotFound = true)
public class FileUtils {
	/* 打印日志 */
	private final Logger logger = LoggerFactory.getLogger(FileUtils.class);
	/* 从配置文件中获取SFTP相关属性  */
    @Value("${sftp.username}")
    private String username;//用户名
    @Value("${sftp.password}")
    private String password;//密码
    @Value("${sftp.host}")
    private String host;//主机
    @Value("${sftp.port}")
    private int port;//端口
    @Value("${sftp.basePath}")
    private String basePath;//服务器基本路径
	
	/**
	 * 下载文件时，针对不同浏览器，进行附件名的编码
	 * 
	 * @param filename 下载文件名
	 * @param agent 客户端浏览器
	 * @return 编码后的下载附件名
	 * @throws UnsupportedEncodingException 
	 */
	public String encodeDownloadFilename(String filename, String agent) throws UnsupportedEncodingException {
		if (agent.contains("Firefox")) { // 火狐浏览器
			filename = "=?UTF-8?B?"
					+ new BASE64Encoder().encode(filename.getBytes("utf-8"))
					+ "?=";
			filename = filename.replaceAll("\r\n", "");
		} else { // IE及其他浏览器
			filename = URLEncoder.encode(filename, "utf-8");
			filename = filename.replace("+"," ");
		}
		return filename;
	}
	
	
	/**
	 * Description: 压缩多个文件
	 *
	 * @param srcFiles 多个源文件
	 * @param zipFile 一个目标文件
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年03月04日 10:37:25
	 */
	public void zipFiles(List<File> srcFiles, File zipFile) throws Exception {
		logger.info("进入zipFiles方法，参数是：srcFiles="+srcFiles+",zipFile="+zipFile);
    	// 创建 FileOutputStream 对象
    	FileOutputStream fileOutputStream = null;
    	// 创建 ZipOutputStream
    	ZipOutputStream zipOutputStream = null;
    	// 创建 FileInputStream 对象
    	FileInputStream fileInputStream = null;

    	// 判断文件夹是否存在，如果不存在则创建
		if (!zipFile.getParentFile().exists()){
			zipFile.getParentFile().mkdirs();  
        }
		// 判断压缩后的文件是否存在，如果不存在则创建
    	if (!zipFile.exists()) {
    		zipFile.createNewFile();
    	}
    	
		// 实例化 FileOutputStream 对象
		fileOutputStream = new FileOutputStream(zipFile);
		// 实例化 ZipOutputStream 对象
		zipOutputStream = new ZipOutputStream(fileOutputStream);
		// 创建 ZipEntry 对象
		ZipEntry zipEntry = null;
		// 遍历源文件数组
		for (File srcFile : srcFiles) {
			// 将源文件数组中的当前文件读入 FileInputStream 流中
			fileInputStream = new FileInputStream(srcFile);
			// 实例化 ZipEntry 对象，源文件数组中的当前文件
			zipEntry = new ZipEntry(srcFile.getName());
			zipOutputStream.putNextEntry(zipEntry);
			// 该变量记录每次真正读的字节个数
			int len;
			// 定义每次读取的字节数组
			byte[] buffer = new byte[1024];
			while ((len = fileInputStream.read(buffer)) > 0) {
				zipOutputStream.write(buffer, 0, len);
			}
		}
		zipOutputStream.closeEntry();
		zipOutputStream.close();
		fileInputStream.close();
		fileOutputStream.close();
    }
	
	/**
	 * Description: 下载文件到用户本地电脑（待下载的文件与工程在同一台服务器上）
	 * 
	 * @param filePathAndName 文件路径+文件名
	 * @param contentType 响应头内容类型
	 * @throws Exception 
	 * @Author ZhangYuWei
	 * @Create Date: 2019年03月01日 11:06:45
	 */
	public void downloadFileToComputer(String filePathAndName,String contentType) throws Exception {
		logger.info("进入downloadFileToComputer方法，参数是：filePathAndName="+filePathAndName+",contentType="+contentType);
		String filePath = filePathAndName.substring(0,filePathAndName.lastIndexOf("/"));
		String fileName = filePathAndName.substring(filePathAndName.lastIndexOf("/")+1);
		logger.info("文件路径："+filePath+"，文件名："+fileName);
        /*
		 * 响应给前端，准备下载文件
		 */
        File file = new File(filePathAndName);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		
		HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
		//response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName,"UTF-8"));
		response.setContentType(contentType);
		response.setCharacterEncoding("UTF-8");
		response.setHeader("filename",  new String(fileName.getBytes(),"utf-8"));
		OutputStream out = response.getOutputStream();
		out.write(data);
		out.flush();
		out.close();
	}
	
	/**
	 * Description: 根据目录删除其下所有文件（单层目录删除，不支持多级目录递归，避免重要文件的误删）
	 * 
	 * @param tempFilePath 文件路径
	 * @Author ZhangYuWei
	 * @Create Date: 2019年03月05日 15:48:23
	 */
	public void deleteFileByPath(String tempFilePath) throws Exception {
		logger.info("进入deleteFileByPath方法，参数是：tempFilePath="+tempFilePath);
		logger.info("即将删除该文件夹下的全部文件："+tempFilePath);
		File filePath = new File(tempFilePath);
		if(filePath.exists() && filePath.isDirectory()) {
			String[] files = filePath.list();
			for (String file : files) {
				String deletingFile = tempFilePath+"/"+file;
				boolean deleteSuccess = new File(deletingFile).delete();
				logger.info(deletingFile+" 是否删除成功："+deleteSuccess);
			}
		}
	}
	
	/* 以下接口是跨服务器的文件操作，需要用到SFTP工具  */
	
	/**
	 * Description: 通过SFTP上传文件
	 * 文件完整路径=文件基本路径（属性文件中获取）+文件业务路径（属性文件中获取）+日期路径（动态生成）+文件名（增加时间戳）
	 * 
	 * @param file 需要上传的文件
	 * @param filePath 上传的文件需要保存的位置【文件业务路径（属性文件中获取）+日期路径（动态生成）】
	 * @return 文件名
	 * @Author ZhangYuWei
	 * @Create Date: 2019年03月06日 18:15:20
	 */
	public Map<String,Object> uploadFileBySFTP(MultipartFile file,String filePath) {
		logger.info("进入uploadFileBySFTP方法，参数是：filePath="+filePath);
		Map<String,Object> resultMap = new HashMap<>();//自定义返回结果集
		try {
			if(file==null) {
				logger.warn("上传的文件为空！");
				resultMap.put("isSuccess", false);
				resultMap.put("data", "上传的文件为空！");
				return resultMap;
			}
			
			/* 文件的名称  */
			String originalFileName = file.getOriginalFilename();
			int indexOfSuffix = originalFileName.lastIndexOf(".");
			String fileName = originalFileName.substring(0, indexOfSuffix);
			String fileSuffix = originalFileName.substring(indexOfSuffix+1);
			//上传的文件限定格式
//			if(!"doc".equals(fileSuffix) && !"docx".equals(fileSuffix)) {
//				logger.warn("文件格式不支持！导致文件上传失败！");
//				resultMap.put("isSuccess", false);
//				resultMap.put("data", "文件格式不支持！导致文件上传失败！");
//				return resultMap;
//			}
			//文件名增加时间戳，避免重名文件
			String fileNameOnly = fileName+String.valueOf(System.currentTimeMillis())+"."+fileSuffix;
			
			/* 文件的输入流  */
			InputStream inputStream = file.getInputStream();
			
			/* 通过SFTP上传文件到文件服务器  */
			SFTPUtil sftp = new SFTPUtil(username,password,host,port);
			sftp.login();//连接SFTP服务器
			sftp.upload(basePath,filePath,fileNameOnly,inputStream);
			sftp.logout();//关闭SFTP服务器连接
			
			resultMap.put("isSuccess", true);
			resultMap.put("data", fileNameOnly);
			return resultMap;
		} catch (Exception e) {
			logger.error("uploadFileBySFTP方法出现异常：{}",e.getMessage(),e);
			resultMap.put("isSuccess", false);
			resultMap.put("data", "uploadFileBySFTP方法出现异常："+e.getMessage());
			return resultMap;
		}
	}
	
	/**
	 * Description: 通过SFTP上传文件
	 * 文件完整路径=文件基本路径（属性文件中获取）+文件业务路径（属性文件中获取）+日期路径（动态生成）+文件名（增加时间戳）
	 * 
	 * @param fileForUpload 需要上传的文件的路径
	 * @param fileNameForUpload 需要上传的文件的名字
	 * @param filePath 上传的文件需要保存的位置【文件业务路径（属性文件中获取）+日期路径（动态生成）】
	 * @return 文件名
	 * @Author ZhangYuWei
	 * @Create Date: 2019年05月09日 14:12:53
	 */
	public Map<String,Object> uploadFileBySFTP(String filePathForUpload,String fileNameForUpload,String filePath) {
		logger.info("进入uploadFileBySFTP方法，参数是：filePathForUpload={},fileNameForUpload={},filePath={}",
				filePathForUpload,fileNameForUpload,filePath);
		Map<String,Object> resultMap = new HashMap<>();//自定义返回结果集
		try {
			if(filePathForUpload==null || fileNameForUpload==null) {
				logger.warn("上传的文件为空！");
				resultMap.put("isSuccess", false);
				resultMap.put("data", "上传的文件为空！");
				return resultMap;
			}
			
			/* 文件的名称  */
			int indexOfSuffix = fileNameForUpload.lastIndexOf(".");
			String fileName = fileNameForUpload.substring(0, indexOfSuffix);
			String fileSuffix = fileNameForUpload.substring(indexOfSuffix+1);
			//上传的文件限定格式
//			if(!"doc".equals(fileSuffix) && !"docx".equals(fileSuffix)) {
//				logger.warn("文件格式不支持！导致文件上传失败！");
//				resultMap.put("isSuccess", false);
//				resultMap.put("data", "文件格式不支持！导致文件上传失败！");
//				return resultMap;
//			}
			//文件名增加时间戳，避免重名文件
			String fileNameOnly = fileName+String.valueOf(System.currentTimeMillis())+"."+fileSuffix;
			
			/* 文件的输入流  */
			File file = new File(filePathForUpload+"/"+fileNameForUpload);
			InputStream inputStream = new FileInputStream(file);
			
			/* 通过SFTP上传文件到文件服务器  */
			SFTPUtil sftp = new SFTPUtil(username,password,host,port);
			sftp.login();//连接SFTP服务器
			sftp.upload(basePath,filePath,fileNameOnly,inputStream);
			sftp.logout();//关闭SFTP服务器连接
			
			resultMap.put("isSuccess", true);
			resultMap.put("data", fileNameOnly);
			return resultMap;
		} catch (Exception e) {
			logger.error("uploadFileBySFTP方法出现异常：{}",e.getMessage(),e);
			resultMap.put("isSuccess", false);
			resultMap.put("data", "uploadFileBySFTP方法出现异常："+e.getMessage());
			return resultMap;
		}
	}
	
	/**
	 * Description: 通过SFTP删除文件
	 * 
	 * @param filePathAndName 文件路径+文件名称
	 * @Author ZhangYuWei
	 * @Create Date: 2019年03月07日 10:36:02
	 */
	public boolean deleteFileBySFTP(String filePathAndName) {
		logger.info("进入deleteFileBySFTP方法，参数是：filePathAndName="+filePathAndName);
		try {
			/* 将文件完整目录拆分成文件路径、文件名称  */
			int indexOfFileName = filePathAndName.lastIndexOf('/');
			String filePath = filePathAndName.substring(0, indexOfFileName);
			String fileName = filePathAndName.substring(indexOfFileName+1);
			
			/* 通过SFTP删除文件服务器上的文件  */
			SFTPUtil sftp = new SFTPUtil(username,password,host,port);
			sftp.login();//连接SFTP服务器
			sftp.delete(basePath+filePath, fileName);
			sftp.logout();//关闭SFTP服务器的连接
			return true;
		} catch (Exception e) {
			logger.error("deleteFileBySFTP方法出现异常：",e.getMessage(),e);
			return false;
		}
	}
	
	/**
	 * Description: 通过SFTP下载文件（待下载的文件与工程不在同一台服务器上）
	 * 
	 * @param filePathAndName 文件路径+文件名称
	 * @Author ZhangYuWei
	 * @Create Date: 2019年03月07日 10:52:52
	 */
	public boolean downloadFileBySFTP(String filePathAndName,String contentType) {
		logger.info("进入downloadFileBySFTP方法，参数是：filePathAndName="+filePathAndName+",contentType="+contentType);
	    try {
	    	/* 将文件完整目录拆分成文件路径、文件名称  */
			int indexOfFileName = filePathAndName.lastIndexOf('/');
			String filePath = filePathAndName.substring(0, indexOfFileName);
			String fileName = filePathAndName.substring(indexOfFileName+1);
			
			/* 获取文件服务器的文件比特数组  */
			SFTPUtil sftp = new SFTPUtil(username,password,host,port);
			sftp.login();//连接SFTP服务器
			byte[] data = sftp.download(basePath+filePath, fileName);
			sftp.logout();//关闭SFTP服务器连接
			
			/* 响应给前端，准备下载文件  */
			HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
			response.setContentType(contentType);
			response.setCharacterEncoding("UTF-8");
			response.setHeader("filename", new String(fileName.getBytes(),"utf-8"));
			OutputStream out = response.getOutputStream();
			out.write(data);
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			logger.error("downloadFileBySFTP方法出现异常：",e.getMessage(),e);
			return false;
		}
	}
	
	public void uploadFile(InputStream inputStream,String filePath,String fileNameOnly) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		try {
			while ((len = inputStream.read(buffer)) > -1) {
				baos.write(buffer, 0, len);
			}
			baos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		InputStream stream1 = new ByteArrayInputStream(baos.toByteArray());
		InputStream stream2 = new ByteArrayInputStream(baos.toByteArray());
		BufferedInputStream inBuf = null;
		// 数据库存入地址
		try {
			inBuf = new BufferedInputStream(stream1);
			/* 通过SFTP上传文件到文件服务器  */
			SFTPUtil sftp = new SFTPUtil(username,password,host,port);
			sftp.login();//连接SFTP服务器
			sftp.upload(basePath,filePath,fileNameOnly,inBuf);
			sftp.logout();//关闭SFTP服务器连接
		} finally {
			try {
				if (stream1 != null)
					stream1.close();
				if (stream2 != null)
					stream2.close();
				if (stream2 != null)
					inputStream.close();
				if (inBuf != null)
					inBuf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
