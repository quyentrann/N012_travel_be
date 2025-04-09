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
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:3000","http://localhost:5173", "http://52.77.233.97", "https://app.botpress.cloud", "https://studio.botpress.cloud", "https://botpress.studio")  // Địa chỉ của frontend
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS","HEAD")
                        .allowedHeaders("*")
                        .allowCredentials(true);  // Nếu bạn sử dụng cookie/tokens
            }
        };
    }
}