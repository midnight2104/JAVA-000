package homework;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Java Config配置
 */
@Configuration
public class JavaConfig {

    @Bean
    public People peopleBean() {
        return new People();
    }
}
