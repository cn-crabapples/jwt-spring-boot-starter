package cn.crabapples.jwt.service;

import cn.crabapples.jwt.ConfigureProperties;
import cn.crabapples.jwt.exception.ApplicationException;
import io.jsonwebtoken.*;
import org.apache.logging.log4j.util.Base64Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

/**
 * TODO jwt工具(用于解析和生成token)
 *
 * @author Mr.He
 * 9/5/20 2:52 PM
 * e-mail crabapples.cn@gmail.com
 * qq 294046317
 * pc-name root
 */
public class JwtTokenService {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenService.class);
    /**
     * 解析jwt
     *
     * @param jsonWebToken
     * @param base64Security
     * @return
     */
    public static Claims parseJWT(String jsonWebToken, String base64Security) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(base64Security))
                    .parseClaimsJws(jsonWebToken).getBody();
            return claims;
        } catch (ExpiredJwtException e) {
            logger.warn("Token过期", e);
            throw new ApplicationException("登录状态异常", 401);
        } catch (Exception e) {
            logger.warn("token解析异常", e);
            throw new ApplicationException("登录状态异常", 401);
        }
    }

    /**
     * 构建jwt
     *
     * @param userId
     * @param username
     * @param jwtConfigure
     * @return
     */
    public static String createJWT(String userId, String username, ConfigureProperties jwtConfigure) {
        try {
            // 使用HS256加密算法
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);
            //生成签名密钥
            byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(jwtConfigure.getBase64Secret());
            Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
            //userId是重要信息，进行加密下
            String encryId = Base64Util.encode(userId);
            //添加构成JWT的参数
            JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT")
                    // 可以将基本不重要的对象信息放到claims
                    .claim("userId", userId)
                    .setSubject(username)           // 代表这个JWT的主体，即它的所有人
                    .setIssuer(jwtConfigure.getClientId())              // 代表这个JWT的签发主体；
                    .setIssuedAt(new Date())        // 是一个时间戳，代表这个JWT的签发时间；
                    .setAudience(jwtConfigure.getName())          // 代表这个JWT的接收对象；
                    .signWith(signatureAlgorithm, signingKey);
            //添加Token过期时间
            int TTLMillis = jwtConfigure.getExpiresSecond();
            if (TTLMillis >= 0) {
                long expMillis = nowMillis + TTLMillis;
                Date exp = new Date(expMillis);
                builder.setExpiration(exp)  // 是一个时间戳，代表这个JWT的过期时间；
                        .setNotBefore(now); // 是一个时间戳，代表这个JWT生效的开始时间，意味着在这个时间之前验证JWT是会失败的
            }

            //生成JWT
            return builder.compact();
        } catch (Exception e) {
            logger.error("签名失败", e);
            throw new ApplicationException("签名失败", 401);
        }
    }
}
