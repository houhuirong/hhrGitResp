package com.mashibing.springboot.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Auther: hhr
 * @Date: 2020/9/10 - 09 - 10 - 9:49
 * @Description: 用户权限处理
 * @version: 1.0
 */
@Component
@WebFilter(urlPatterns = "/*")
public class AccountFilter implements Filter {
    private final String[] IGNORE_URI={"/index","/account/validataAccount","/css/","/js/","/account/login","/images"};
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest)req;
        HttpServletResponse response=(HttpServletResponse) resp;
        /**1.从session里找Account对象
        1.1找到就行
         1.2找不到
        1.3当前访问是不是在ignore——uri列表里
         1.3.1不在ignore被拦截,跳到登录页面
         1.3.2在则放行
         */
        //当前思考：先查ignore-uri后查session会减少查询，效率问题，要注意

        String requestURI = request.getRequestURI();
        System.out.println("-----请求--"+requestURI);
        boolean pass=canPassIgnore(requestURI);
        if (pass){
            // 在的话 放行
            chain.doFilter(request,response);
            return;
        }
        Object account = request.getSession().getAttribute("account");
        System.out.println("getSession account:" + account);
        if (null==account){
            // 没登录 跳转登录页面
            response.sendRedirect("/account/login");
            return;
        }
        chain.doFilter(request,response);



    }

    private boolean canPassIgnore(String requestURI) {
        // /index=uri
        //判断 访问的uri 起始部分是否包含ignore
        //下级目录资源也能访问
        for (String val:IGNORE_URI){
            if (requestURI.startsWith(val)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("--------AccountFilter-------");
        Filter.super.init(filterConfig);
    }
}
