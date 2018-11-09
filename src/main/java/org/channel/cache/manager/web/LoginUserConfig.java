package org.channel.cache.manager.web;

import org.channel.cache.manager.web.login.LoginServletParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhangchanglu
 * @since 2018/05/17 17:49.
 */
public interface LoginUserConfig {
    LoginUser loginUser();

    void saveLoginUser(HttpServletRequest request, HttpServletResponse response, LoginServletParam loginServletParam);

    LoginUser getLoginUser(HttpServletRequest request, HttpServletResponse response, LoginServletParam loginServletParam);
}
