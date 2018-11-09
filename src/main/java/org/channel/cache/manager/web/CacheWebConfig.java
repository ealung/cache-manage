package org.channel.cache.manager.web;

import org.channel.cache.manager.web.login.LoginFilter;
import org.channel.cache.manager.web.login.LoginServletParam;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;

/**
 * @author zhangchanglu
 * @since 2018/05/14 20:06.
 */
@Configuration
public class CacheWebConfig extends WebMvcConfigurerAdapter {
    @Resource
    private LoginUserConfig loginUserConfig;

//    @Bean
//    public ServletRegistrationBean cacheRegistration() {
//        LoginServletParam loginServletParam = LoginServletParam.defaultBuilder()
//                .resourcePath("cache.http.resources")
//                .prefix("/cache")
//                .username(loginUserConfig.loginUser().getUserName())
//                .password(loginUserConfig.loginUser().getUserPwd())
//                .sessionUserKey("CACHESESSION")
//                .build();
//        return new ServletRegistrationBean(new BaseLoginServlet(loginServletParam), loginServletParam.getPrefix() + "/*");
//    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        LoginServletParam loginServletParam = LoginServletParam.defaultBuilder()
                .resourcePath("cache.http.resources")
                .prefix("/cache")
                .username(loginUserConfig.loginUser().getUserName())
                .password(loginUserConfig.loginUser().getUserPwd())
                .sessionUserKey("CACHESESSION")
                .loginUserConfig(loginUserConfig)
                .build();
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new LoginFilter(loginServletParam));
        registration.addUrlPatterns(loginServletParam.getPrefix() + "/*");
        registration.setName("loginFilter");
        registration.setOrder(1);
        return registration;
    }

}
