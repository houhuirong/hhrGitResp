package com.nantian.erp.email.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.core.FileURIResolver;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

/** 
 * Description: word格式文件转换为html格式文件工具类
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年01月21日      		ZhangYuWei          1.0       
 * </pre>
 */
@Component
public class WordToHtmlUtil {
	private final static String  tempPathForResumePicture = "/usr/local/nantian/temp/resume-picture/";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * doc转换为html
	 * 
	 * @param  fileName docx文件路径（如果你的是存到文件服务器的，直接用路径，如果是直接将文件流存到数据库的，转为InputStream）
	 * @param  outPutFile html输出文件路径
	 * @throws TransformerException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public void doc2Html(InputStream fileName, String outPutFile) {
		try {
			long startTime = System.currentTimeMillis();
			HWPFDocument wordDocument = new HWPFDocument(fileName);
			//HWPFDocument wordDocument = new HWPFDocument(new FileInputStream(fileName));
			WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
					DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
			wordToHtmlConverter.setPicturesManager(new PicturesManager() {
				public String savePicture(byte[] content, PictureType pictureType, String suggestedName,
						float widthInches, float heightInches) {
					return "tempPathForDocPicture" + suggestedName;
				}
			});
			wordToHtmlConverter.processDocument(wordDocument);
			// 保存图片
			List<Picture> pics = wordDocument.getPicturesTable().getAllPictures();
			if (pics != null) {
				for (int i = 0; i < pics.size(); i++) {
					Picture pic = (Picture) pics.get(i);
					System.out.println();
					try {
						pic.writeImageContent(
								new FileOutputStream(tempPathForResumePicture + pic.suggestFullFileName()));
					} catch (FileNotFoundException e) {
						logger.error("doc2Html中保存图片的方法发生异常：" + e.getMessage(), e);
					}
				}
			}
			Document htmlDocument = wordToHtmlConverter.getDocument();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DOMSource domSource = new DOMSource(htmlDocument);
			StreamResult streamResult = new StreamResult(out);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer serializer = tf.newTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.setOutputProperty(OutputKeys.METHOD, "html");
			serializer.transform(domSource, streamResult);
			out.close();
			writeFile(new String(out.toByteArray()), outPutFile);
			logger.info("Generate " + outPutFile + " with " + (System.currentTimeMillis() - startTime) + " ms.");
			logger.info("doc格式的word文件已经转换为html文件！");
		} catch (Exception e) {
			logger.error("doc2Html方法出现异常："+e.getMessage(),e);
		}
	}

	/**
	 * 写文件
	 * 
	 * @param content
	 * @param path
	 */
	public void writeFile(String content, String path) {
		FileOutputStream fos = null;
		BufferedWriter bw = null;
		try {
			File file = new File(path);
			fos = new FileOutputStream(file);
			bw = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
			bw.write(content);
		} catch (FileNotFoundException fnfe) {
			logger.error("writeFile方法出现异常："+fnfe.getMessage(),fnfe);
		} catch (IOException ioe) {
			logger.error("writeFile方法出现异常："+ioe.getMessage(),ioe);
		} finally {
			try {
				if (bw != null)
				bw.close();
				if (fos != null)
				fos.close();
			} catch (IOException ie) {
				logger.error("writeFile方法出现异常："+ie.getMessage(),ie);
			}
		}
	}

	/**
	 * docx格式word转换为html
	 * 
	 * @param fileName docx文件路径（如果你的是存到文件服务器的，直接用路径，如果是直接将文件流存到数据库的，转为InputStream ）
	 * @param outPutFile html输出文件路径
	 * @throws TransformerException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public void docx2Html(InputStream fileName, String outPutFile) {
		try {
			String fileOutName = outPutFile;
			long startTime = System.currentTimeMillis();
			//XWPFDocument document = new XWPFDocument(new FileInputStream(fileName));
			XWPFDocument document = new XWPFDocument(fileName);
			XHTMLOptions options = XHTMLOptions.create().indent(4);
			// 导出图片
			File imageFolder = new File(tempPathForResumePicture);
			options.setExtractor(new FileImageExtractor(imageFolder));
			// URI resolver
			options.URIResolver(new FileURIResolver(imageFolder));
			File outFile = new File(fileOutName);
			outFile.getParentFile().mkdirs();
			OutputStream out = new FileOutputStream(outFile);
			XHTMLConverter.getInstance().convert(document, out, options);
			logger.info("Generate " + fileOutName + " with " + (System.currentTimeMillis() - startTime) + " ms.");
			logger.info("docx格式的word文件已经转换为html文件！");
		} catch (Exception e) {
			logger.error("docx2Html方法出现异常："+e.getMessage(),e);
		}
	}
	
}
