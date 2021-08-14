package cn.crabapples.jwt.config;

import cn.crabapples.jwt.service.JwtTokenService;
import cn.crabapples.jwt.exception.ApplicationException;
import cn.crabapples.jwt.utils.JwtIgnore;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TODO 配置jwt拦截器(不拦截@JwtIgnore标记的url)
 *
 * @author Mr.He
 * 9/5/20 2:54 PM
 * e-mail crabapples.cn@gmail.com
 * qq 294046317
 * pc-name root
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(JwtInterceptor.class);
    private final cn.crabapples.jwt.ConfigureProperties jwtConfigure;

    public JwtInterceptor(cn.crabapples.jwt.ConfigureProperties jwtConfigure) {
        this.jwtConfigure = jwtConfigure;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (jwtConfigure.isDebug()) {
            return true;
        }
        // 忽略带JwtIgnore注解的请求, 不做后续token认证校验
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            JwtIgnore jwtIgnore = handlerMethod.getMethodAnnotation(JwtIgnore.class);
            if (jwtIgnore != null) {
                return true;
            }
        }
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }
        final String authHeader = request.getHeader(jwtConfigure.getAuthKey());
        logger.debug("Auth Token:[{}]", authHeader);
        if (StringUtils.isBlank(authHeader)) {
            logger.debug("token check fail");
            throw new ApplicationException("Token Check Fail", 401);
        }
        JwtTokenService.parseJWT(authHeader, jwtConfigure.getBase64Secret());
        return true;
    }

}
