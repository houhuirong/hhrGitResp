package com.nantian.erp.common.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.nantian.erp.common.base.Pojo.ErpUser;

/**
 *
 * Description: 过滤器（登录验证、接口验证）
 *
 * @author ZhangYuWei
 * @version 1.1
 * 
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年10月12日       	    ZhangYuWei       	1.0
 * 2018年10月30日       	    ZhangYuWei       	1.1
 * 2019年03月06日       	    ZhangYuWei       	1.2
 * </pre>
 */
public class ErpAuthorizationFilter implements Filter {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private RedisTemplate<Object,Object> redisTemplate;

	/*
	 * add by 张玉伟 增加不过滤、直接放行的请求路径
	 */
	private List<String> allowedPaths = new ArrayList<>();

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		// 定义请求对象，用来获取客户端信息
		HttpServletRequest httpRequest = (HttpServletRequest) req;
		// 定义返回对象，用来向客户端返回验证失败信息
		HttpServletResponse httpResp = (HttpServletResponse) resp;
		try {
			httpResp.setCharacterEncoding("UTF-8");
			httpResp.setContentType("application/json;charset=utf-8");
			// httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			// 为了解决IE识别重复请求时，读缓存的问题
			httpResp.setHeader( "Pragma", "no-cache" );
			httpResp.addHeader( "Cache-Control", "must-revalidate" );
			httpResp.addHeader( "Cache-Control", "no-cache" );
			httpResp.addHeader( "Cache-Control", "no-store" );
			// 获取当前接口请求路径
			String requestUrl = httpRequest.getRequestURI();
			// 白名单中是否包含该请求URL
			boolean isAllowed = allowedPaths.contains(requestUrl);
			logger.info("前端请求路径：" + requestUrl + "，是否白名单：" + isAllowed);
			// 获取获取当前接口token
			String token = httpRequest.getHeader("token");
			logger.info("token=" + token);
			
			/*
			 * 如果是白名单中的URL直接放行；否则需要校验token
			 */
			if (isAllowed) {
				// 将前端请求放行
				chain.doFilter(req, resp);
			} else {
				if (token == null || "null".equals(token) || "".equals(token)) {
					logger.info("该请求没有传token！");
					// 与前端约定返回的字符串内容
					String responseStr = "{\"msg\":\"token未找到！您没有访问权限！\",\"status\":\"401\"}";
					httpResp.getWriter().write(responseStr);
					return;
				}

				/*
				 * 登录验证
				 */
				ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
				if (erpUser == null) {
					logger.info("登录验证未通过！token已经过期！");
					// 与前端约定返回的字符串内容
					String responseStr = "{\"msg\":\"您两小时内没有任何操作，为了保证安全，请您重新登录！\",\"status\":\"201\"}";
					httpResp.getWriter().write(responseStr);
					return;
				}
				//用户半小时内（如果觉得半小时短的话，就增加到两个小时）有操作，给token续期；如果没有操作，redis会自动删除token
				redisTemplate.expire(token, 2, TimeUnit.HOURS);
				logger.info("登录验证已通过！" + erpUser);

				/*
				 * 接口验证
				 */
				List<Integer> roles = erpUser.getRoles();//从用户信息中获取角色信息
				Boolean flag = false;//一个用户多个角色  url重合标识
				/*
				 * 针对一个用户多个角色的情况
				 * 其中一个角色没有访问其他角色对应接口的权限，
				 * 无论哪个角色拥有接口的访问权限都行，此时该用户是拥有所有角色对应的接口权限
				 */
				for (Integer roleId : roles) {
					// 获取redis缓存的接口路径信息 key由角色ID+请求路径组成
					String redisPathUrl = stringRedisTemplate.opsForValue().get(roleId + "_" + requestUrl);
					logger.info("redis缓存中的权限接口路径："+redisPathUrl);
					//contains可以解决请求URL上拼接参数的情况
					if (null != redisPathUrl && !"".equals(redisPathUrl) && requestUrl.contains(redisPathUrl)) {
						flag = true;
						break;
					}
				}
				if (flag) {
					logger.info("接口验证已通过！方法即将开始执行！");
					// 将前端请求放行
					chain.doFilter(req, resp);
				} else {
					logger.info("接口验证未通过！没有接口访问权限！被拦截的接口是："+requestUrl);
					// 与前端约定返回的字符串内容
					String responseStr = "{\"msg\":\"没有接口访问权限！\",\"status\":\"202\"}";
					httpResp.getWriter().write(responseStr);
					return;
				}
			}
		} catch (Exception e) {
			logger.error("过滤器发生异常！原因是："+e.getMessage(),e);
			httpResp.getWriter().write("{\"msg\":"+e.getMessage()+",\"status\":\"500\"}");
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		allowedPaths.add("/nantian-erp/authentication/user/login");//登录
		allowedPaths.add("/nantian-erp/authentication/user/findPasswordSend");//找回密码-发送验证码
		allowedPaths.add("/nantian-erp/authentication/user/findPasswordReset");//找回密码-通过验证码重置密码
		allowedPaths.add("/nantian-erp/authentication/role/load");//菜单
		allowedPaths.add("/nantian-erp/erp/postive/autoPosition");//定时任务
		allowedPaths.add("/nantian-erp/erp/postive/autoPosition");//定时任务 --HR salary一致性校对
		allowedPaths.add("/nantian-erp/erp/employee/findEmployeeAllForScheduler");//定时任务
		allowedPaths.add("/nantian-erp/erp/employee/findEmpInfoOfAllFirDepByParamsScheduler");//定时任务
		allowedPaths.add("/nantian-erp/erp/employee/updateProjectIdForScheduler");//定时任务（项目组调动时修改关联的项目组ID）
		allowedPaths.add("/nantian-erp/authentication/user/findUserByEmpIdScheduler");//定时任务（通过员工ID查询员工邮箱）
		allowedPaths.add("/nantian-erp/erp/employee/findEmployeeTableForScheduler");//定时任务（查找员工表信息，供项目工程调用）
		allowedPaths.add("/nantian-erp/erp/employee/findEmployeeDetailForLogin");//登录时查询员工姓名
		allowedPaths.add("/nantian-erp/authentication/user/findUserByMobile");//根据手机号批量查询员工id
		allowedPaths.add("/nantian-erp/erp/employee/findEmployeeByDeptAndUserNoToken");//查询权限下人员列表
		allowedPaths.add("/nantian-erp-hr/nantian-erp/erp/offer/getEmailUserNameAndPassword");//获取邮件的用户名密码
		allowedPaths.add("/nantian-erp/erp/employee/findEmployeeInfoMap");//获取员工信息
		allowedPaths.add("/nantian-erp/project/leave/batchUpdateProcessor");//批量更新员工请假错误的审批人
		allowedPaths.add("/nantian-erp/salary/payRollFlow/batchUpdateCurrentPersonId");//批量更新员工薪酬错误的审批人




		SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
	}

}
