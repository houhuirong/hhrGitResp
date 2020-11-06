package com.nantian.erp.email.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.email.util.WordToHtmlUtil;
import com.nantian.erp.email.util.WordToPDFUtil;

/** 
 * Description: 文件格式转换service
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年01月18日      		ZhangYuWei          1.0       
 * </pre>
 */
@Service
public class ConvertFormatService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private WordToPDFUtil wordToPDFUtil;
	@Autowired
	private WordToHtmlUtil wordToHtmlUtil;
	
	/**
	 * Description: word转pdf
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月18日 下午12:30:42
	 */
	public RestResponse wordToPdfDemo() {
		logger.info("wordToPdfDemo方法开始执行，无参数");
		try {
			String inPath = "D:\\1.doc";
			String outPath = "D:\\1.pdf";
			
			File inFile = new File(inPath);
			InputStream inStream = new FileInputStream(inFile);
			
			File outFile = new File(outPath);
			outFile.getParentFile().mkdirs();
			outFile.createNewFile();
			OutputStream outStream = new FileOutputStream(outFile);
			
			if(inPath.endsWith("doc")){
				wordToPDFUtil.convertDoc(inStream, outStream);
			}else if (inPath.endsWith("docx")){
				wordToPDFUtil.convertDocx(inStream, outStream);
			}else {
				logger.info("该文件不是word格式！");
			}
			inStream.close();
			outStream.close();
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("wordToPdfDemo方法发生异常："+e.getMessage(),e);
			return RestUtils.returnFailure("转换pdf过程中发生异常！");
		}
	}
	
	/**
	 * Description: word转html
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月21日 下午16:28:38
	 */
	public RestResponse wordToHtmlDemo() {
		logger.info("wordToHtmlDemo方法开始执行，无参数");
		try {
			String inPath = "D:\\3.docx";
			String outPath = "D:\\3.html";
			
			File inFile = new File(inPath);
			InputStream inStream = new FileInputStream(inFile);
			
			if(inPath.endsWith("doc")){
				wordToHtmlUtil.doc2Html(inStream, outPath);
			}else if (inPath.endsWith("docx")){
				wordToHtmlUtil.docx2Html(inStream, outPath);
			}else {
				logger.info("该文件不是word格式！");
			}
			inStream.close();
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("wordToHtmlDemo方法发生异常："+e.getMessage(),e);
			return RestUtils.returnFailure("转换html过程中发生异常！");
		}
	}
	
	/**
	 * Description: word转pdf
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月18日 下午13:56:01
	 */
	public RestResponse wordToPdf(Map<String,String> params) {
		logger.info("wordToPdf方法开始执行，参数是：params="+params);
		try {
			String inPath = params.get("wordPathAndName"); //word完整路径+文件名
			String outPath = params.get("pdfPathAndName"); //pdf完整路径+文件名
			
			File inFile = new File(inPath);
			InputStream inStream = new FileInputStream(inFile);
			
			File outFile = new File(outPath);
			outFile.getParentFile().mkdirs();
			outFile.createNewFile();
			OutputStream outStream = new FileOutputStream(outFile);
			
			if(inPath.endsWith("doc")){
				wordToPDFUtil.convertDoc(inStream, outStream);
				logger.info("doc格式的word文件已经转换为pdf文件！");
			}else if (inPath.endsWith("docx")){
				wordToPDFUtil.convertDocx(inStream, outStream);
				logger.info("docx格式的word文件已经转换为pdf文件！");
			}else {
				logger.info("该文件不是word格式！");
			}
			inStream.close();
			outStream.close();
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("wordToPdf发生异常："+e.getMessage(),e);
			return RestUtils.returnFailure("转换pdf过程中发生异常！");
		}
	}
	
	/**
	 * Description: word转html
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月21日 下午17:10:02
	 */
	public RestResponse wordToHtml(Map<String,String> params) {
		logger.info("wordToHtml方法开始执行，参数是：params="+params);
		try {
			String inPath = params.get("wordPathAndName"); //word完整路径+文件名
			String outPath = params.get("htmlPathAndName"); //html完整路径+文件名
			
			File inFile = new File(inPath);
			InputStream inStream = new FileInputStream(inFile);
			
			if(inPath.endsWith("doc")){
				wordToHtmlUtil.doc2Html(inStream, outPath);
			}else if (inPath.endsWith("docx")){
				wordToHtmlUtil.docx2Html(inStream, outPath);
			}else {
				logger.info("该文件不是word格式！");
			}
			inStream.close();
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("wordToHtml发生异常："+e.getMessage(),e);
			return RestUtils.returnFailure("转换html过程中发生异常！");
		}
	}
	
}
