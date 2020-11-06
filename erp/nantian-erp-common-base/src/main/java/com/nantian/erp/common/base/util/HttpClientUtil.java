package com.nantian.erp.common.base.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nantian.erp.common.base.exception.BizException;
import com.nantian.erp.common.constants.ExceptionConstants;
import com.nantian.erp.common.model.PostParameter;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * <strong>Title : HttpClientUtil </strong>. <br>
 * <strong>Description : HttpClient工具类.</strong> <br>
 * <p>
 */
public class HttpClientUtil {
	
	private static String IO_EXCEPTION_STATUS = "005";                          //厂商API调用时，IO流异常所表示的API调用返回定义状态
	private static String CLIENT_PROTOCOL_EXCEPTION_STATUS = "006";             //厂商API调用时，CLIENT_PROTOCOL异常所表示的API调用返回定义状态
	private static String UNSUPPORTED_ENCODING_EXCEPTION_STATUS = "007";        //厂商API调用时，UNSUPPORTED_ENCODING异常所表示的API调用返回定义状态
	
	public static String HTTP_CLIENT_HEADER_AUTHORIZATION = "authorization";    //厂商API调用时，请求头认证方式
	public static String HTTP_CLIENT_HEADER_COOKIE = "cookie";                  //厂商API调用时，请求头认证方式
	
	/**
	 * <code>log</code>-日志使用
	 */
	private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

	/**
	 * 执行httpClient postMethod,用于配置下发服务，调用驱动工程微服务接口
	 * @param url 请求的路径
	 * @param param 请求的参数(map集合)
	 * @param contentType json/form/formdata
	 * @return
	 */
	public static Map<String, String> executeDriverServer(String driverUrl, Map<String, Object> param, String contentType,int timeout) {
		Map<String, String> map = new HashMap<String, String>();
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		String res = ""; // 请求返回默认的支持json串
		try {
			HttpPost httpPost = new HttpPost(driverUrl);
			//设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(5000).build();
			httpPost.setConfig(requestConfig);
			// BTW 4.3版本不设置超时的话,一旦服务器没有响应,等待时间N久(>24小时)。 
			HttpEntity entity = convertParam(param, contentType);
			if(entity!=null){
				if("formdata".equals(contentType)){
					httpPost.setHeader("enctype", "multipart/form-data");
				}
				httpPost.setEntity(entity);
				HttpResponse httpResponse = closeableHttpClient.execute(httpPost);
				// 获取返回的状态码
				int status = httpResponse.getStatusLine().getStatusCode();
				logger.info("Post请求URL="+driverUrl+",请求的参数="+param.toString()+",请求的格式"+contentType+",状态="+status);
				if(status == HttpStatus.SC_OK){
					HttpEntity entity2 = httpResponse.getEntity();
					InputStream ins = entity2.getContent();
					res = toString(ins);
					ins.close();
				}else{
					logger.error("Post请求URL="+driverUrl+",请求的参数="+param.toString()+",请求的格式"+contentType+",错误Code:"+status);
				}
				map.put("code", String.valueOf(status));
				map.put("result", res);
				logger.info("执行Post方法请求返回的结果 = " + res);
			}else{
				logger.error("convertParam转换参数异常,转换参数结果为entity="+entity);
			}
		} catch (ClientProtocolException e) {
			map.put("code", HttpClientUtil.CLIENT_PROTOCOL_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			map.put("code", HttpClientUtil.UNSUPPORTED_ENCODING_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (IOException e) {
			map.put("code", HttpClientUtil.IO_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} finally {
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				logger.error("调用httpClient出错", e);
			}
		}
		return map;
	}

	/**
	 * 转换参数
	 * @param param
	 * @param contentType
	 * @return
	 */
	private static HttpEntity convertParam(Map<String, Object> param, String contentType) throws IOException {
		if ("form".equals(contentType)) {// 模拟普通的form表单提交
			List<NameValuePair> formParams = new ArrayList<NameValuePair>();
			Set<String> keyset = param.keySet();
			for (String key : keyset) {
				Object paramObj = Validate.notNull(param.get(key));
				// 追加参数
				addFormParams(key, paramObj, formParams);
			}
			HttpEntity entity = new UrlEncodedFormEntity(formParams, "UTF-8");
			return entity;
		} else if ("json".equals(contentType)) { // json串用
			JSONObject json = JSONObject.fromObject(param);
			logger.info("请求的json参数=" + json.toString());
			StringEntity entity = new StringEntity(json.toString(), "UTF-8");
			entity.setContentEncoding("UTF-8");
			entity.setContentType("application/json");
			return entity;
		}
		return null;
	}
	
	/**
	 * 设置NameValuePair参数(支持keyValue为List)
	 * @param key
	 * @param keyValue
	 * @param builder
	 */
	private static void addFormParams(String key,Object keyValue,List<NameValuePair> formParams){
		if(keyValue instanceof ArrayList){ // 添加参数为list的参数
			@SuppressWarnings("unchecked")
			List<Map<String,Object>> paramList = (List<Map<String,Object>>)keyValue;
			for(int i=0;i<paramList.size();i++){
				Map<String,Object> param = paramList.get(i);
				Set<String> paramKeySet = param.keySet();
				for(String paramKey : paramKeySet){
					String name = key+"["+i+"]."+paramKey;
					String value = param.get(paramKey).toString();
					logger.info("form参数中map为list的参数name="+name+",value="+value);
					formParams.add(new BasicNameValuePair(name, value));
				}
			}
		}else{// 添加普通类型的参数
			formParams.add(new BasicNameValuePair(key, keyValue.toString()));
		}
	}
	
	/**
	 * 
	 * 执行httpClient getMethod
	 * @param url 请求的url参数
	 * @return
	 */
	public static Map<String, String> executeGetMethod(String url) {
		Map<String, String> map = new HashMap<String, String>();
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		HttpGet httpGet = new HttpGet(url);
		// 设置请求和传输超时时间
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(5000).build(); 
		httpGet.setConfig(requestConfig);
		// BTW 4.3版本不设置超时的话,一旦服务器没有响应,等待时间N久(>24小时)。 
		String res = "";
		try {
			CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpGet);
			// 获取response状态码
			int status = httpResponse.getStatusLine().getStatusCode();
			logger.info("GET请求URL="+url+",状态="+status);
			if(HttpStatus.SC_OK == status){
				HttpEntity entity = httpResponse.getEntity();
				res = HttpClientUtil.toString(entity.getContent());
				logger.info("执行get方法请求返回的结果 = " + res);
			}else{
				logger.error("GET请求URL="+url+",状态值="+status);
			}
			map.put("code", String.valueOf(status));
			map.put("result", res);
		} catch (ClientProtocolException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.CLIENT_PROTOCOL_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.UNSUPPORTED_ENCODING_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (IOException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.IO_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} finally {
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				logger.error("调用httpClient出错", e);
			}
		}
		return map;
	}


	/**
	 * 执行httpClient postMethod
	 * @param url 请求的路径
	 * @param param 请求的参数字符串
	 * @param contentType json/form/formdata
	 * @param headers 请求头
	 * @return
	 */
	public static Map<String, String> executePostMethodWithParas(String url,
			String paramStr, Map<String, String> headers, String contentType, int timeout) {
		Map<String, String> map = new HashMap<String, String>();
		CloseableHttpClient client = null;
		String res = ""; // 请求返回默认的支持json串
		try {
			if (client == null) {
				// Create HttpClient Object
				client = HttpClientUtil.getenableSSLClient();

			}

			HttpPost httpPost = new HttpPost(url);
			if (null != headers && headers.size() > 0) {
				for (Iterator<String> it = headers.keySet().iterator(); it
						.hasNext();) {
					String key = it.next();
					String value = headers.get(key);
					httpPost.addHeader(key, value);
				}
			}

			// 设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(5000).build();
			httpPost.setConfig(requestConfig);
			// BTW 4.3版本不设置超时的话,一旦服务器没有响应,等待时间N久(>24小时)。

			if (StringUtils.isNotEmpty(paramStr)) {
				StringEntity stringEntity = new StringEntity(paramStr, "UTF-8");
				stringEntity.setContentEncoding("UTF-8");
				stringEntity.setContentType(contentType);
				httpPost.setEntity(stringEntity);
			}

			HttpResponse httpResponse = client.execute(httpPost);
			// 获取返回的状态码
			int status = httpResponse.getStatusLine().getStatusCode();
			logger.info("Post请求URL=" + url + ",请求的参数=" + paramStr
					+ ",请求的header:" + headers + ",状态=" + status);
			HttpEntity entity2 = httpResponse.getEntity();
			if(null != entity2){
				InputStream ins = entity2.getContent();
				res = HttpClientUtil.toString(ins);
				ins.close();
			}
			map.put("code", String.valueOf(status));
			map.put("result", res);
			map.put("headers", httpResponse.getAllHeaders().toString());
			logger.info("执行Post方法请求返回的结果 = " + res);

		} catch (ClientProtocolException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.CLIENT_PROTOCOL_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.UNSUPPORTED_ENCODING_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (IOException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.IO_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} finally {
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					logger.error("调用httpClient出错", e);
				}
			}
		}
		return map;
	}
	
	/**
	 * 执行httpClient putMethod
	 * @param url 请求的路径
	 * @param param 请求的参数字符串
	 * @param contentType json/form/formdata
	 * @param headers 请求头
	 * @return
	 */
	public static Map<String, String> executePutMethodWithParas(String url,
			String paramStr, Map<String, String> headers, String contentType, int timeout) {
		Map<String, String> map = new HashMap<String, String>();
		CloseableHttpClient client = null;
		String res = ""; // 请求返回默认的支持json串
		try {
			if (client == null) {
				// Create HttpClient Object
				client = HttpClientUtil.getenableSSLClient();

			}

			HttpPut httpPut = new HttpPut(url);
			if (null != headers && headers.size() > 0) {
				for (Iterator<String> it = headers.keySet().iterator(); it
						.hasNext();) {
					String key = it.next();
					String value = headers.get(key);
					httpPut.addHeader(key, value);
				}
			}

			// 设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(5000).build();
			httpPut.setConfig(requestConfig);
			// BTW 4.3版本不设置超时的话,一旦服务器没有响应,等待时间N久(>24小时)。

			if (StringUtils.isNotEmpty(paramStr)) {
				StringEntity stringEntity = new StringEntity(paramStr, "UTF-8");
				stringEntity.setContentEncoding("UTF-8");
				stringEntity.setContentType(contentType);
				httpPut.setEntity(stringEntity);
			}

			HttpResponse httpResponse = client.execute(httpPut);
			// 获取返回的状态码
			int status = httpResponse.getStatusLine().getStatusCode();
			logger.info("Put请求URL=" + url + ",请求的参数=" + paramStr
					+ ",请求的header:" + headers + ",状态=" + status);
			HttpEntity entity2 = httpResponse.getEntity();
			if(null != entity2){
				InputStream ins = entity2.getContent();
				res = HttpClientUtil.toString(ins);
				ins.close();
			}
			map.put("code", String.valueOf(status));
			map.put("result", res);
			logger.info("执行Put方法请求返回的结果 = " + res);

		} catch (ClientProtocolException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.CLIENT_PROTOCOL_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.UNSUPPORTED_ENCODING_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (IOException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.IO_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} finally {
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					logger.error("调用httpClient出错", e);
				}
			}
		}
		return map;
	}
	
	/**
	 * 执行httpClient patchMethod
	 * @param url 请求的路径
	 * @param param 请求的参数字符串
	 * @param contentType json/form/formdata
	 * @param headers 请求头
	 * @return
	 */
	public static Map<String, String> executePatchMethodWithParas(String url,
			String paramStr, Map<String, String> headers, String contentType, int timeout) {
		Map<String, String> map = new HashMap<String, String>();
		CloseableHttpClient client = null;
		String res = ""; // 请求返回默认的支持json串
		try {
			if (client == null) {
				// Create HttpClient Object
				client = HttpClientUtil.getenableSSLClient();

			}

//			HttpPost httpPost = new HttpPost(url);
			HttpPatch httpPatch = new HttpPatch(url);
			if (null != headers && headers.size() > 0) {
				for (Iterator<String> it = headers.keySet().iterator(); it
						.hasNext();) {
					String key = it.next();
					String value = headers.get(key);
					httpPatch.addHeader(key, value);
				}
			}

			// 设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(5000).build();
			httpPatch.setConfig(requestConfig);
			// BTW 4.3版本不设置超时的话,一旦服务器没有响应,等待时间N久(>24小时)。

			if (StringUtils.isNotEmpty(paramStr)) {
				StringEntity stringEntity = new StringEntity(paramStr, "UTF-8");
				stringEntity.setContentEncoding("UTF-8");
				stringEntity.setContentType(contentType);
				httpPatch.setEntity(stringEntity);
			}

			HttpResponse httpResponse = client.execute(httpPatch);
			// 获取返回的状态码
			int status = httpResponse.getStatusLine().getStatusCode();
			logger.info("Patch请求URL=" + url + ",请求的参数=" + paramStr
					+ ",请求的header:" + headers + ",状态=" + status);
			HttpEntity entity2 = httpResponse.getEntity();
			if(null != entity2){
				InputStream ins = entity2.getContent();
				res = HttpClientUtil.toString(ins);
				ins.close();
			}
			map.put("code", String.valueOf(status));
			map.put("result", res);
			logger.info("执行Patch方法请求返回的结果 = " + res);
		} catch (ClientProtocolException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.CLIENT_PROTOCOL_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.UNSUPPORTED_ENCODING_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (IOException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.IO_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} finally {
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					logger.error("调用httpClient出错", e);
				}
			}
		}
		return map;
	}
	
	
	/**
	 * 执行httpClient postMethod
	 * @param url 请求的路径
	 * @param param 请求的参数(map集合)
	 * @param contentType json/form/formdata
	 * @param headers 请求头
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static Map<String, String> executePostMethodWithParas(String url,
			Map<String, String> params, Map<String, String> headers) {
		Map<String, String> map = new HashMap<String, String>();
		CloseableHttpClient client = null;
		String res = ""; // 请求返回默认的支持json串
		try {
			if (client == null) {
				// Create HttpClient Object
				client = HttpClientUtil.getenableSSLClient();

			}

			HttpPost httpPost = new HttpPost(url);
			if (null != headers && headers.size() > 0) {
				for (Iterator<String> it = headers.keySet().iterator(); it
						.hasNext();) {
					String key = it.next();
					String value = headers.get(key);
					httpPost.addHeader(key, value);
				}
			}

			// 设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(5000).build();
			httpPost.setConfig(requestConfig);
			// BTW 4.3版本不设置超时的话,一旦服务器没有响应,等待时间N久(>24小时)。

			if (params != null && params.size() > 0) {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				for (Iterator<String> it = params.keySet().iterator(); it
						.hasNext();) {
					String key = it.next();
					if (params.get(key) != null) {
						String value = params.get(key);
						nvps.add(new BasicNameValuePair(key, value));
					}

				}
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

			}

			HttpResponse httpResponse = client.execute(httpPost);
			// 获取返回的状态码
			int status = httpResponse.getStatusLine().getStatusCode();
			logger.info("Post请求URL=" + url + ",请求的参数=" + params.toString()
					+ ",请求的header:" + headers + ",状态=" + status);
			if (status == HttpStatus.SC_OK) {
				HttpEntity entity2 = httpResponse.getEntity();
				InputStream ins = entity2.getContent();
				res = HttpClientUtil.toString(ins);
				ins.close();
			} else {
				HttpEntity entity2 = httpResponse.getEntity();
				InputStream ins = entity2.getContent();
				res = HttpClientUtil.toString(ins);
				ins.close();
				logger.error("Post请求URL=" + url + ",请求的参数=" + params.toString()
						+ ",请求的header:" + headers + ",错误Code:"
						+ status);
			}
			map.put("code", String.valueOf(status));
			map.put("result", res);
			logger.info("执行Post方法请求返回的结果 = " + res);

		} catch (ClientProtocolException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.CLIENT_PROTOCOL_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.UNSUPPORTED_ENCODING_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (IOException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.IO_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} finally {
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					logger.error("调用httpClient出错", e);
				}
			}
		}
		return map;
	}
	
	/**
	 * 执行httpClient putMethod
	 * @param url 请求的路径
	 * @param param 请求的参数字符串
	 * @param contentType json/form/formdata
	 * @param headers 请求头
	 * @return
	 */
	public static Map<String, String> executePutMethodWithParas(String url,
			String paramStr, Map<String, String> headers, int timeout) {
		Map<String, String> map = new HashMap<String, String>();
		CloseableHttpClient client = null;
		String res = ""; // 请求返回默认的支持json串
		try {
			if (client == null) {
				// Create HttpClient Object
				client = HttpClientUtil.getenableSSLClient();

			}

			HttpPut httpPost = new HttpPut(url);
			if (null != headers && headers.size() > 0) {
				for (Iterator<String> it = headers.keySet().iterator(); it
						.hasNext();) {
					String key = it.next();
					String value = headers.get(key);
					httpPost.addHeader(key, value);
				}
			}

			// 设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(5000).build();
			httpPost.setConfig(requestConfig);
			// BTW 4.3版本不设置超时的话,一旦服务器没有响应,等待时间N久(>24小时)。

			if (StringUtils.isNotEmpty(paramStr)) {
				StringEntity stringEntity = new StringEntity(paramStr, "UTF-8");
				stringEntity.setContentEncoding("UTF-8");
				stringEntity.setContentType("application/json");
				httpPost.setEntity(stringEntity);
			}

			HttpResponse httpResponse = client.execute(httpPost);
			// 获取返回的状态码
			int status = httpResponse.getStatusLine().getStatusCode();
		
			HttpEntity entity2 = httpResponse.getEntity();
			InputStream ins = entity2.getContent();
			res = HttpClientUtil.toString(ins);
			ins.close();
			
			map.put("code", String.valueOf(status));
			map.put("result", res);
			logger.error("Put请求URL=" + url + ",请求的参数=" + paramStr
					+ ",请求的header:" + headers + ",错误Code:"
					+ status);
			logger.info("执行Put方法请求返回的结果 = " + res);

		} catch (ClientProtocolException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.CLIENT_PROTOCOL_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.UNSUPPORTED_ENCODING_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (IOException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.IO_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} finally {
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					logger.error("调用httpClient出错", e);
				}
			}
		}
		return map;
	}
	
	/**
	 * 
	 * @param managerip
	 * @param managerport
	 * @param url
	 * @param url_type
	 * @param param
	 * @param username
	 * @param password
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static Map<String, String> execute(String managerip, String managerport, String url, String url_type, String param, String username, String password, String contentType, int timeout) throws UnsupportedEncodingException{
		String usernameAndPassword = convertUserNameAndPassword(username, password);
		Map<String, String> map = new TreeMap<String, String>();
		map.put("Authorization", "Basic " + usernameAndPassword);
		if (StringUtils.isEmpty(contentType)) {
			contentType = "application/json";
		}
//		String response = null;
		Map<String, String> resultMap = null;
//		map.put("password", "admin");
		if ("get".equals(url_type)) {
			resultMap = executeGetMethodWithParas(url, null, map, timeout);
		} else if ("patch".equals(url_type)){
			resultMap = executePatchMethodWithParas(url, param, map, contentType, timeout);
		} else if ("put".equals(url_type)){
			resultMap = executePutMethodWithParas(url, param, map, contentType, timeout);
		} else {
			resultMap = executePostMethodWithParas(url, param, map, contentType, timeout);
		}
		return resultMap;
	}
	
	/**
	 * 执行带参数和待请求头的get请求
	 * @param url 链接
	 * @param params 参数
	 * @param headers
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static Map<String, String> executeGetMethodWithParas(String url,Map<String,String> params,Map<String,String> headers, int timeout) {
		CloseableHttpClient closeableHttpClient = null;
		Map<String, String> map = new HashMap<String, String>();
		StringBuilder urlsb = new StringBuilder(url);
		if(params != null && params.size()>0){
			PostParameter[] pps = new PostParameter[params.size()];
			int i = 0;
			for (Iterator<String> it = params.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				String value = params.get(key);
				PostParameter p = new PostParameter(key, value);
				pps[i++] = p;
			}

			String encodedParams = HttpClientUtil.encodeParameters(pps);
			if (-1 == url.indexOf("?")) {

				urlsb.append("?").append(encodedParams);
			} else {
				urlsb.append("&").append(encodedParams);

			}
		}
		
		HttpGet httpGet = new HttpGet(urlsb.toString());
		if (closeableHttpClient == null) {
			// Create HttpClient Object
//			closeableHttpClient = new DefaultHttpClient();
//			enableSSL(closeableHttpClient);
			closeableHttpClient = HttpClientUtil.getenableSSLClient();
		}
		httpGet.getParams().setParameter("http.method.retry-handler",
				new DefaultHttpRequestRetryHandler(0, false));
		
		if(null != headers && headers.size()>0){
			for (Iterator<String> it = headers.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				String value = headers.get(key);
				httpGet.addHeader(key, value);
			}
		}

		// 设置请求和传输超时时间
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(5000).build(); 
		httpGet.setConfig(requestConfig);
		// BTW 4.3版本不设置超时的话,一旦服务器没有响应,等待时间N久(>24小时)。 
		String res = "";
		try {
			CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpGet);
			// 获取response状态码
			int status = httpResponse.getStatusLine().getStatusCode();
			logger.info("GET请求URL="+url+",状态="+status);
			if(HttpStatus.SC_OK == status){
				HttpEntity entity = httpResponse.getEntity();
				res = HttpClientUtil.toString(entity.getContent());
				logger.info("执行get方法请求返回的结果 = " + res);
			}else{
				logger.error("GET请求URL="+url+",状态值="+status);
			}
			map.put("code", String.valueOf(status));
			map.put("result", res);
		} catch (ClientProtocolException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.CLIENT_PROTOCOL_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.UNSUPPORTED_ENCODING_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (IOException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.IO_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} finally {
			if(closeableHttpClient != null){
				try {
					closeableHttpClient.close();
				} catch (IOException e) {
					logger.error("调用httpClient出错", e);
				}
			}
		}
		return map;
	}
	
	
	@SuppressWarnings("deprecation")
	public static Map<String, String> executeDeleteMethodWithParas(String url,Map<String,String> params,Map<String,String> headers, int timeout) {
//		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = null;
		Map<String, String> map = new HashMap<String, String>();
		if (closeableHttpClient == null) {
			// Create HttpClient Object
//			closeableHttpClient = new DefaultHttpClient();
//			enableSSL(closeableHttpClient);
			closeableHttpClient = HttpClientUtil.getenableSSLClient();
		}
		
		StringBuilder urlsb = new StringBuilder(url);
		if(params != null && params.size()>0){
			PostParameter[] pps = new PostParameter[params.size()];
			int i = 0;
			for (Iterator<String> it = params.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				String value = params.get(key);
				PostParameter p = new PostParameter(key, value);
				pps[i++] = p;
			}

			String encodedParams = HttpClientUtil.encodeParameters(pps);
			if (-1 == url.indexOf("?")) {

				urlsb.append("?").append(encodedParams);
			} else {
				urlsb.append("&").append(encodedParams);

			}
		}
		
		HttpDelete httpDelete = new HttpDelete(urlsb.toString());
		httpDelete.getParams().setParameter("http.method.retry-handler",
				new DefaultHttpRequestRetryHandler(0, false));
		
		if(null != headers && headers.size()>0){
			for (Iterator<String> it = headers.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				String value = headers.get(key);
				httpDelete.addHeader(key, value);
			}
		}

		// 设置请求和传输超时时间
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(5000).build(); 
		httpDelete.setConfig(requestConfig);
		// BTW 4.3版本不设置超时的话,一旦服务器没有响应,等待时间N久(>24小时)。 
		String res = "";
		try {
			CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpDelete);
			// 获取response状态码
			int status = httpResponse.getStatusLine().getStatusCode();
			logger.info("GET请求URL="+url+",状态="+status);
			if(HttpStatus.SC_OK == status){
				HttpEntity entity = httpResponse.getEntity();
				res = HttpClientUtil.toString(entity.getContent());
				logger.info("执行get方法请求返回的结果 = " + res);
			}else{
				logger.error("GET请求URL="+url+",状态值="+status);
			}
			map.put("code", String.valueOf(status));
			map.put("result", res);
		} catch (ClientProtocolException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.CLIENT_PROTOCOL_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.UNSUPPORTED_ENCODING_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (IOException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.IO_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} finally {
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				logger.error("调用httpClient出错", e);
			}
		}
		return map;
	}
	
	/**
	 * doGet
	 * @param targetUrl
	 * @param paramStr
	 * @param headers
	 * @param timeout
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String> doDelete(String targetUrl,String jsonParam,Map<String,String> headers, int timeout) {
		String result = "";
		URL url = null;
		HttpURLConnection urlConn = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		Map<String, String> map = new HashMap<String, String>();
		try {
			url = new URL(targetUrl);
			if (url != null) {
				urlConn = (HttpURLConnection) url.openConnection();
				urlConn.setRequestProperty("content-type", "application/json");
				urlConn.setDoInput(true);
				urlConn.setDoOutput(true);
				urlConn.setConnectTimeout(timeout);
				//设置请求方式为 PUT
				urlConn.setRequestMethod("DELETE");
				
				urlConn.setRequestProperty("Content-Type", "application/json");
				urlConn.setRequestProperty("Accept", "application/json");
				
				urlConn.setRequestProperty("Charset", "UTF-8");
				DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
				//写入请求参数
				//这里要注意的是，在构造JSON字符串的时候，实践证明，最好不要使用单引号，而是用“\”进行转义，否则会报错
				// 关于这一点在上面给出的参考文章里面有说明
				dos.writeBytes(jsonParam);
				dos.flush();
				dos.close();
				
				if (urlConn.getResponseCode() == 200) {
					isr = new InputStreamReader(urlConn.getInputStream());
					br = new BufferedReader(isr);
					String inputLine = null;
					while ((inputLine = br.readLine()) != null) {
						result += inputLine;
					}
					isr.close();
					urlConn.disconnect();
					map.put("code", "204");
					map.put("result", result);
				}
			}
		} catch (IOException e) {
			logger.error("调用Delete出错", e);
			map.put("code", HttpClientUtil.CLIENT_PROTOCOL_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} finally {
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (urlConn != null) {
				urlConn.disconnect();
			}
		}
		return map;
	}
	
	public static Map<String, String> executeGetMethodWithParasAndCredentials(String url,Map<String,String> params,Map<String,String> headers,Map<String,String> credentials, int timeout) {
		CloseableHttpClient closeableHttpClient = null;
		Map<String, String> map = new HashMap<String, String>();
		StringBuilder urlsb = new StringBuilder(url);
		if(params != null && params.size()>0){
			PostParameter[] pps = new PostParameter[params.size()];
			int i = 0;
			for (Iterator<String> it = params.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				String value = params.get(key);
				PostParameter p = new PostParameter(key, value);
				pps[i++] = p;
			}

			String encodedParams = HttpClientUtil.encodeParameters(pps);
			if (-1 == url.indexOf("?")) {

				urlsb.append("?").append(encodedParams);
			} else {
				urlsb.append("&").append(encodedParams);

			}
		}
		
		//添加认证信息
		HttpHost targetHost = new HttpHost(credentials.get("host"), Integer.valueOf(credentials.get("port")), "http");
		UsernamePasswordCredentials credential = new UsernamePasswordCredentials(credentials.get("username"), credentials.get("password"));
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope(credentials.get("host"), Integer.valueOf(credentials.get("port"))), credential);
		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicScheme = new BasicScheme();
		authCache.put(targetHost, basicScheme);
		HttpClientContext context = HttpClientContext.create();
		context.setCredentialsProvider(credentialsProvider);
		context.setAuthCache(authCache);
		
		HttpGet httpGet = new HttpGet(urlsb.toString());
		if (closeableHttpClient == null) {
			closeableHttpClient = HttpClientUtil.getenableSSLClient();
		}
		httpGet.getParams().setParameter("http.method.retry-handler",
				new DefaultHttpRequestRetryHandler(0, false));
		
		
		if(null != headers && headers.size()>0){
			for (Iterator<String> it = headers.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				String value = headers.get(key);
				httpGet.addHeader(key, value);
			}
		}

		// 设置请求和传输超时时间
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(5000).build(); 
		httpGet.setConfig(requestConfig);
		// BTW 4.3版本不设置超时的话,一旦服务器没有响应,等待时间N久(>24小时)。 
		String res = "";
		try {
			CloseableHttpResponse httpResponse = closeableHttpClient.execute(targetHost, httpGet, context);
			// 获取response状态码
			int status = httpResponse.getStatusLine().getStatusCode();
			logger.info("GET请求URL="+url+",状态="+status);
			if(HttpStatus.SC_OK == status){
				HttpEntity entity = httpResponse.getEntity();
				res = HttpClientUtil.toString(entity.getContent());
				logger.info("执行get方法请求返回的结果 = " + res);
			}else{
				logger.error("GET请求URL="+url+",状态值="+status);
			}
			map.put("code", String.valueOf(status));
			map.put("result", res);
		} catch (ClientProtocolException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.CLIENT_PROTOCOL_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.UNSUPPORTED_ENCODING_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (IOException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.IO_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} finally {
			if(closeableHttpClient != null){
				try {
					closeableHttpClient.close();
				} catch (IOException e) {
					logger.error("调用httpClient出错", e);
				}
			}
		}
		return map;
	}
	
	public static Map<String, String> executePostMethodWithParasAndCredentials(String url,
			String paramStr, Map<String, String> headers, Map<String, String> credentials, String contentType, int timeout) {
		Map<String, String> map = new HashMap<String, String>();
		CloseableHttpClient client = null;
		String res = ""; // 请求返回默认的支持json串
		try {
			if (client == null) {
				client = HttpClientUtil.getenableSSLClient();
			}

			HttpPost httpPost = new HttpPost(url);
			if (null != headers && headers.size() > 0) {
				for (Iterator<String> it = headers.keySet().iterator(); it
						.hasNext();) {
					String key = it.next();
					String value = headers.get(key);
					httpPost.addHeader(key, value);
				}
			}
			
			//添加认证信息
			HttpHost targetHost = new HttpHost(credentials.get("host"), Integer.valueOf(credentials.get("port")), "http");
			UsernamePasswordCredentials credential = new UsernamePasswordCredentials(credentials.get("username"), credentials.get("password"));
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(new AuthScope(credentials.get("host"), Integer.valueOf(credentials.get("port"))), credential);
			AuthCache authCache = new BasicAuthCache();
			BasicScheme basicScheme = new BasicScheme();
			authCache.put(targetHost, basicScheme);
			HttpClientContext context = HttpClientContext.create();
			context.setCredentialsProvider(credentialsProvider);
			context.setAuthCache(authCache);

			// 设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(5000).build();
			httpPost.setConfig(requestConfig);
			// BTW 4.3版本不设置超时的话,一旦服务器没有响应,等待时间N久(>24小时)。

			if (StringUtils.isNotEmpty(paramStr)) {
				StringEntity stringEntity = new StringEntity(paramStr, "UTF-8");
				stringEntity.setContentEncoding("UTF-8");
				stringEntity.setContentType(contentType);
				httpPost.setEntity(stringEntity);
			}

			HttpResponse httpResponse = client.execute(targetHost, httpPost, context);
			// 获取返回的状态码
			int status = httpResponse.getStatusLine().getStatusCode();
			logger.info("Post请求URL=" + url + ",请求的参数=" + paramStr
					+ ",请求的header:" + headers + ",状态=" + status);
			HttpEntity entity2 = httpResponse.getEntity();
			if(null != entity2){
				InputStream ins = entity2.getContent();
				res = HttpClientUtil.toString(ins);
				ins.close();
			}
	
			map.put("code", String.valueOf(status));
			map.put("result", res);
			map.put("headers", JSONArray.fromObject(httpResponse.getAllHeaders()).toString());
			logger.info("执行Post方法请求返回的结果 = " + res);

		} catch (ClientProtocolException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.CLIENT_PROTOCOL_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.UNSUPPORTED_ENCODING_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} catch (IOException e) {
			logger.error("调用httpClient出错", e);
			map.put("code", HttpClientUtil.IO_EXCEPTION_STATUS);
			map.put("result", e.getMessage());
		} finally {
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					logger.error("调用httpClient出错", e);
				}
			}
		}
		return map;
	}

	public static String encodeParameters(PostParameter[] postParams) {
		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < postParams.length; j++) {

			if (postParams[j].getValue() == null) {
				continue;
			}

			if (j != 0) {
				buf.append("&");
			}

			try {
				buf.append(URLEncoder.encode(postParams[j].getName(), "UTF-8"))
						.append("=")
						.append(URLEncoder.encode(postParams[j].getValue(),
								"UTF-8"));
			} catch (java.io.UnsupportedEncodingException neverHappen) {
				logger.error("调用httpClient出错", neverHappen);
				throw new BizException(ExceptionConstants.EX_INNER_ERROR, "字符串转换出错", neverHappen);
			}
		}
		return buf.toString();
	}
	
	/**
	 * 访问https的网站
	 * 
	 * @param httpclient
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException 
	 * @throws KeyStoreException 
	 */
	@SuppressWarnings("deprecation")
	private static CloseableHttpClient getenableSSLClient() {
		// 调用ssl
		try {
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
					builder.build(), NoopHostnameVerifier.INSTANCE);
			Registry<ConnectionSocketFactory> registry = RegistryBuilder
					.<ConnectionSocketFactory> create()
					.register("http", new PlainConnectionSocketFactory())
					.register("https", sslConnectionSocketFactory).build();

			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
					registry);
			cm.setMaxTotal(100);
			CloseableHttpClient httpclient = HttpClients.custom()
					.setSSLSocketFactory(sslConnectionSocketFactory)
					.setConnectionManager(cm).build();

			return httpclient;
		} catch (Exception e) {
			logger.error("调用httpClient出错", e);
		}
		return HttpClientBuilder.create().build();
	}
	
	
	private static String toString(InputStream in) throws IOException{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		int len;
		while((len = in.read(b)) != -1) {
			os.write(b, 0, len);
		}
		return os.toString("UTF-8");
	}
	
	/**
	 * 用户名和密码转码
	 * @param username
	 * @param password
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static String convertUserNameAndPassword(String username, String password) throws UnsupportedEncodingException{
		String usernameAndPassword = DatatypeConverter.printBase64Binary((username +":" + password).getBytes("UTF-8"));
		return usernameAndPassword;
	}
		
}
