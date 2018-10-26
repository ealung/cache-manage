package org.channel.cache.manager.web.login;

import lombok.Builder;
import lombok.Data;

/**
 * @author zhangchanglu
 * @since 2018/10/25 16:30.
 */
@Builder
@Data
public class LoginServletParam {
    private String username;
    private String password;
    private String sessionUserKey;
    private String resourcePath;
    private String paramUserName;
    private String paramUserPassword;
    //登录提交地址
    private String loginUrl;
    //登录页面
    private String loginPage;
    //servlet拦截映射前缀
    private String prefix;

    public static LoginServletParamBuilder defaultBuilder() {
        return LoginServletParam.builder()
                .loginUrl("/login")
                .paramUserName("username")
                .paramUserPassword("password")
                .loginPage("/login.html");
    }
}
