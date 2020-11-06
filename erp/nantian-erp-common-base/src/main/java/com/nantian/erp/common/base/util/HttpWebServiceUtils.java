package com.nantian.erp.common.base.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.alibaba.fastjson.JSONObject;

public class HttpWebServiceUtils {
	
	/**
	 * 获取Token的权限列表
	 * @param url
	 * @param param
	 * @param contentType
	 * @param timeout
	 * @return
	 * @throws IOException 
	 */
	public static String executeTokenForGrant(String sendUrl,JSONObject param, String contentType,int timeout) throws IOException{

			URL url = new URL(sendUrl);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
	        connection.setRequestMethod(contentType.toUpperCase());  
	        connection.setRequestProperty("content-type", "text/xml;charset=utf-8");  
	        connection.setDoInput(true);  
	        connection.setDoOutput(true); 
	        String soapXML = getXML(param);  
	        OutputStream os = connection.getOutputStream();  
	        os.write(soapXML.getBytes());  
	        int responseCode = connection.getResponseCode();  
	        if(200 == responseCode){
	            InputStream is = connection.getInputStream();  
	            InputStreamReader isr = new InputStreamReader(is);  
	            BufferedReader br = new BufferedReader(isr);  
	              
	            StringBuilder sb = new StringBuilder();  
	            String temp = null;  
	              
	            while(null != (temp = br.readLine())){  
	                sb.append(temp);  
	            }  
	              

	            is.close();  
	            isr.close();  
	            br.close();  
	            return sb.toString();
	        }  
	        
            os.close();  
		
		return null;
	}
	
	
	/**
	 * 获取XML的组装
	 * @param param
	 * @return
	 */
	public static String getXML(JSONObject param){
		
        String soapXML = " <?xml version=\"1.0\" encoding=\"utf-8\"?> "
	        +"<TRANSACTION>"
				+"<TRANSATCION_HEADER>"
				  +"<SYS_TX_CODE>authInboundService</SYS_TX_CODE>"
				+"</TRANSATCION_HEADER>"
				+"<TRANSACTION_BODY>"
				    +"<ST>"+param.get("TOKEN")+"</ST>"
				    +"<SYS_CODE>iomp-cloud</SYS_CODE>"
				    +"<CLIENT_IP>"+param.get("IP")+"</CLIENT_IP>"
					+"<LOGIN_NAME></LOGIN_NAME>"
					+"<PASSWORD></PASSWORD>"
				+"</TRANSACTION_BODY>"
			+"</TRANSACTION>";
        
        return soapXML;  
	}
	
	/**
	 * 解析XML获得分类信息
	 * @param result
	 * @param nodeName
	 * @param leavel
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static Map<String, List<String>> sloveXmlForNodeName(String xml,String nodeName) throws ParserConfigurationException, SAXException, IOException{
		
			StringReader sr = new StringReader(xml);
			InputSource is = new InputSource(sr);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder bulider = factory.newDocumentBuilder();
			Document document = bulider.parse(is);
			NodeList noodList = document.getElementsByTagName(nodeName);
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			for(int i=0;i<noodList.getLength();i++){
				//获取第0级节点
				if(noodList.item(i).getNodeType()==Node.ELEMENT_NODE){
					Node node = noodList.item(i);
					String nodeValue = null ;
					if(null!=node.getNodeValue()){
						nodeValue = node.getNodeValue();
					}
					if(null!=node.getTextContent()){
						nodeValue = node.getTextContent();
					}
					List<String> tmpList=new ArrayList<String>();
					if(map.containsKey(node.getNodeName())) {tmpList=(List<String>)map.get(node.getNodeName());}
					tmpList.add(nodeValue.trim());
//					map.put(node.getNodeName(), nodeValue.trim());
					map.put(node.getNodeName(), tmpList);
				}
				
				//获取第1级节点
				if(noodList.item(i).getNodeType()!=Node.ELEMENT_NODE){
					NamedNodeMap attrs = noodList.item(i).getAttributes();
					for (int j = 0; j < attrs.getLength(); j++) {
						if(noodList.item(j).getNodeType()==Node.ELEMENT_NODE){
								Node node = noodList.item(j);
								String nodeValue = null ;
								if(null!=node.getNodeValue()){
									nodeValue = node.getNodeValue();
								}
								if(null!=node.getTextContent()){
									nodeValue = node.getTextContent();
								}
								List<String> tmpList=new ArrayList<String>();
								if(map.containsKey(node.getNodeName())) {tmpList=(List<String>)map.get(node.getNodeName());}
								tmpList.add(nodeValue.trim());
//								map.put(node.getNodeName(), nodeValue.trim());
								map.put(node.getNodeName(), tmpList);
						}else{
								NodeList childNodes = noodList.item(i).getChildNodes();
								for (int k = 0; k < childNodes.getLength(); k++) {
				                    if (childNodes.item(k).getNodeType() == Node.ELEMENT_NODE) {
										Node node = noodList.item(k);
										String nodeValue = null ;
										if(null!=node.getNodeValue()){
											nodeValue = node.getNodeValue();
										}
										if(null!=node.getTextContent()){
											nodeValue = node.getTextContent();
										}
										List<String> tmpList=new ArrayList<String>();
										if(map.containsKey(node.getNodeName())) {tmpList=(List<String>)map.get(node.getNodeName());}
										tmpList.add(nodeValue.trim());
//										map.put(node.getNodeName(), nodeValue.trim());
										map.put(node.getNodeName(), tmpList);
				                    }
				                }
						}
					}

					
				}

			}
			
			return map;
		
	}



}
