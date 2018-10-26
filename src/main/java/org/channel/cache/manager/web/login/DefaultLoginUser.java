package org.channel.cache.manager.web.login;

import org.channel.cache.manager.web.LoginUser;
import org.channel.cache.manager.web.LoginUserConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhangchanglu
 * @since 2018/10/25 17:13.
 */
@Configuration
public class DefaultLoginUser {
    @Bean
    @ConditionalOnMissingBean(LoginUserConfig.class)
    public LoginUserConfig cacheUserConfig() {
        return new LoginUserConfig() {
            @Override
            public LoginUser loginUser() {
                LoginUser loginUser = new LoginUser();
                loginUser.setUserName("cache");
                loginUser.setUserPwd("cache");
                return loginUser;
            }
        };
    }
}
