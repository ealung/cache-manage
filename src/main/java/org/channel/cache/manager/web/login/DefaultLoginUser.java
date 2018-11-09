package org.channel.cache.manager.web.login;

import lombok.extern.slf4j.Slf4j;
import org.channel.cache.manager.storage.h2.CacheSessionEntity;
import org.channel.cache.manager.storage.h2.CacheSessionRepository;
import org.channel.cache.manager.web.LoginUser;
import org.channel.cache.manager.web.LoginUserConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author zhangchanglu
 * @since 2018/10/25 17:13.
 */
@Slf4j
@Configuration
public class DefaultLoginUser {
    @Resource
    private CacheSessionRepository cacheSessionRepository;

    @Bean
    @ConditionalOnMissingBean(LoginUserConfig.class)
    public LoginUserConfig cacheUserConfig() {

        return new LoginUserConfig() {

            private long expireTime = 18000000;

            @Override
            public LoginUser loginUser() {
                LoginUser loginUser = new LoginUser();
                loginUser.setUserName("kadacache");
                loginUser.setUserPwd("kadacache");
                return loginUser;
            }

            @Override
            public void saveLoginUser(HttpServletRequest request, HttpServletResponse response, LoginServletParam loginServletParam) {
                CacheSessionEntity cacheSession = new CacheSessionEntity();
                cacheSession.setUserName(loginServletParam.getUsername());
                cacheSession.setSessionId(request.getSession().getId());
                cacheSession.setLoginTime(LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli());
                cacheSessionRepository.save(cacheSession);
            }

            @Override
            public LoginUser getLoginUser(HttpServletRequest request, HttpServletResponse response, LoginServletParam loginServletParam) {
                HttpSession session = request.getSession();
                CacheSessionEntity byUserNameAndSessionId = cacheSessionRepository.findByUserNameAndSessionId(loginServletParam.getUsername(), session.getId());
                if (null != byUserNameAndSessionId) {
                    Long loginTime = byUserNameAndSessionId.getLoginTime();
                    long now = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
                    if (now - loginTime <= expireTime) {
                        return loginServletParam.getLoginUserConfig().loginUser();
                    }else{
                        log.info("用户:{}登录已过期，需重新登录",loginServletParam.getUsername());
                    }
                }
                return null;
            }
        };
    }

    public static void main(String[] args) {
        System.out.println();
    }
}
