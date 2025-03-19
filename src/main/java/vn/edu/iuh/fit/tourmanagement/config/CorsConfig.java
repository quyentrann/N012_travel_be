package vn.edu.iuh.fit.tourmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000", "https://de-smba.onrender.com") // Đảm bảo đúng frontend URL
                        .allowedHeaders("*")
                        .allowedMethods("*");
//                        .allowCredentials(true); // Cho phép cookie/token nếu cần
            }
        };
    }
}
