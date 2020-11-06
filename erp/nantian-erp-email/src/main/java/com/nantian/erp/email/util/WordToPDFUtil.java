package com.nantian.erp.email.util;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
 
import org.apache.commons.io.IOUtils;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.docx4j.Docx4J;
import org.docx4j.convert.in.Doc;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.RFonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;

import fr.opensagres.xdocreport.itext.extension.font.ITextFontRegistry;

/** 
 * Description: word格式文件转换为pdf格式文件工具类
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
@Component
public class WordToPDFUtil {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Description: doc转换pdf
	 * 
	 * @param inStream
	 * @param outStream
	 * @return
	 */
	public void convertDoc(InputStream inStream,OutputStream outStream) throws Exception {
		InputStream iStream = inStream;
		WordprocessingMLPackage wordMLPackage = Doc.convert(iStream);
		Docx4J.toPDF(wordMLPackage, outStream);
		logger.info("doc格式的word文件已经转换为pdf文件！");
	}
	
	/**
	 * Description: doc转换pdf（支持中文）
	 * 注意：该方法目前没有测试通过，暂时不建议使用
	 * 
	 * @param inStream
	 * @param outStream
	 * @return
	 */
	public void convertDocSupportChinese(InputStream inStream,OutputStream outStream) throws Exception {
		InputStream iStream = inStream;
		try {
			WordprocessingMLPackage wordMLPackage = Doc.convert(iStream);
			Mapper fontMapper = new IdentityPlusMapper();
			String fontFamily = "SimSun";
			 
			Resource fileRource = new ClassPathResource("simsun.ttc");
			String path =  fileRource.getFile().getAbsolutePath();
			URL fontUrl = new URL("file:"+path);
			PhysicalFonts.addPhysicalFont(fontUrl);
			 
			PhysicalFont simsunFont = PhysicalFonts.get(fontFamily);
			fontMapper.put(fontFamily, simsunFont);
			
			RFonts rfonts = Context.getWmlObjectFactory().createRFonts(); // 设置文件默认字体
			rfonts.setAsciiTheme(null);
			rfonts.setAscii(fontFamily);
			wordMLPackage.getMainDocumentPart().getPropertyResolver().getDocumentDefaultRPr().setRFonts(rfonts);
			wordMLPackage.setFontMapper(fontMapper);
			FOSettings foSettings = Docx4J.createFOSettings();
			foSettings.setWmlPackage(wordMLPackage);
			Docx4J.toFO(foSettings, outStream, Docx4J.FLAG_EXPORT_PREFER_XSL);
			logger.info("doc格式的word文件已经转换为pdf文件！");
		} catch (Exception ex) {
			logger.error("convertDocSupportChinese发生异常："+ex.getMessage(),ex);
		} finally {
			IOUtils.closeQuietly(outStream);
		}
	}
	
	/**
	 * Description: docx转换pdf
	 * 
	 * @param inStream
	 * @param outStream
	 * @return
	 */
	public void convertDocx(InputStream inStream,OutputStream outStream) throws Exception {
	    XWPFDocument document = new XWPFDocument(inStream);
	    PdfOptions options = PdfOptions.create();
	    PdfConverter.getInstance().convert(document, outStream, options);
	    logger.info("docx格式的word文件已经转换为pdf文件！");
	}
 
	/**
	 * Description: docx转换pdf（支持中文）
	 * 注意：该方法目前没有测试通过，暂时不建议使用
	 * 
	 * @param inStream
	 * @param outStream
	 * @return
	 */
	public void convertDocxSupportChinese(InputStream inStream,OutputStream outStream) throws Exception {
		PdfOptions options = PdfOptions.create();
		XWPFDocument document = new XWPFDocument(inStream);
		//支持中文字体
		options.fontProvider(new ITextFontRegistry() {
		public Font getFont(String familyName, String encoding, float size, int style, Color color) {
			try {
				Resource fileRource = new ClassPathResource("simsun.ttc");
				String path =  fileRource.getFile().getAbsolutePath();
				 
				BaseFont bfChinese = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
				Font fontChinese = new Font(bfChinese, size, style, color);
				if (familyName != null)
					fontChinese.setFamily(familyName);
				return fontChinese;
			} catch (Throwable e) {
				logger.error("convertDocxSupportChinese发生异常："+e.getMessage(),e);
				return ITextFontRegistry.getRegistry().getFont(familyName, encoding, size, style, color);
			}
		}
		});
		PdfConverter.getInstance().convert(document, outStream, options);
		logger.info("docx格式的word文件已经转换为pdf文件！");
	}

}
