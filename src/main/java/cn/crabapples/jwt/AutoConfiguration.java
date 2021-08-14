package cn.crabapples.jwt;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration //开启配置
@ConditionalOnClass(cn.crabapples.jwt.ConfigureProperties.class)
@EnableConfigurationProperties(cn.crabapples.jwt.ConfigureProperties.class) //开启使用映射实体对象
@ConditionalOnProperty(//存在对应配置信息时初始化该配置类
        prefix = "crabapples.jwt",//存在配置前缀
        value = "enabled",//开启
        matchIfMissing = true//缺失检查
)
public class AutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public cn.crabapples.jwt.ConfigureProperties properties(cn.crabapples.jwt.ConfigureProperties properties) {
        return properties;
    }
}
