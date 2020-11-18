package com.midnight.school;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        value = "school.starter.enable",
        havingValue = "true",
        matchIfMissing = true
)
public class SchoolStarterConfiguration {
    @Bean("student100")
    public Student student(){
        return new Student(111, "student100");
    }

    @Bean
    public Klass klass(){
        return new Klass();
    }

    /**
     * 确保School加载时可以依赖注入student,kclass
     */
    @Bean
    @ConditionalOnBean(name = {"student100", "klass"})
    public School school(){
        return new School();
    }
}