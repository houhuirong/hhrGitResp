package com.nantian.erp.hr.util;

import freemarker.template.Configuration;  
import freemarker.template.Template;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;  
 
/** 
 * Description: word相关工具类
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年02月27日      		ZhangYuWei          1.0       
 * </pre>
 */
@Component
public class WordUtil {
	
	private final Logger logger = LoggerFactory.getLogger(WordUtil.class);
  
    /** 
     * add by ZhangYuWei 生成文件
     * @param dataMap word中需要展示的动态数据，用map集合来保存 
     * @param templateName word模板名称，例如：test.ftl 
     * @param filePath 文件生成的目标路径，例如：D:/wordFile/ 
     * @param fileName 生成的文件名称，例如：test.doc 
     */  
    @SuppressWarnings("deprecation")
	public void createFileByTemplate(Map<String,Object> dataMap,String templateName,String filePath,String fileName){ 
    	//dataMap中含有图片的比特数组，数据量比较大，就不打印了
    	logger.info("进入createFileByTemplate方法，参数是templateName="+templateName+",filePath="+filePath+",fileName="+fileName);
        try {  
            //创建配置实例  
            Configuration configuration = new Configuration();  
  
            //设置编码  
            configuration.setDefaultEncoding("UTF-8");  
  
            //ftl模板文件  
            configuration.setClassForTemplateLoading(WordUtil.class,"/template");  
  
            //获取模板  
            Template template = configuration.getTemplate(templateName);  
  
            //输出文件  
            File outFile = new File(filePath+File.separator+fileName);  
  
            //如果输出目标文件夹不存在，则创建  
            if (!outFile.getParentFile().exists()){  
                outFile.getParentFile().mkdirs();  
            }  
  
            //将模板和数据模型合并生成文件  
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile),"UTF-8"));  
  
            //生成文件  
            template.process(dataMap, out);  
  
            //关闭流  
            out.flush();  
            out.close();  
        } catch (Exception e) {
            logger.error("createFileByTemplate方法出现异常："+e.getMessage(),e); 
        }  
    }  
    
    /**
     * add by ZhangYuWei 图片数据转Base64字节码 字符串
     * imgFile是上面存到本地的图片路径
     */
	public String getImageStr(String imgFile){
	    try {
	    	InputStream in=null;
	 	    byte[] data=null;
	        in=new FileInputStream(imgFile);
	        data=new byte[in.available()];
	        in.read(data);
	        in.close();
	        BASE64Encoder encoder = new BASE64Encoder();
		    return encoder.encode(data);
	    } catch (Exception e) {
	    	 logger.error("getImageStr方法出现异常："+e.getMessage()); 
	    	 return "";
	    }
	   
	}
  
}