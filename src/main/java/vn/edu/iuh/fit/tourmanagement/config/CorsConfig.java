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
                        .allowedOrigins("http://localhost:3000","http://localhost:5173", "http://18.138.107.49", "https://app.botpress.cloud", "https://studio.botpress.cloud", "https://botpress.studio", "https://18.138.107.49/api/tours", "https://master.d13wgnx834f8rx.amplifyapp.com/", "https://tadatour.io.vn/") // Địa chỉ của frontend
                        .allowedMethods("GET", "POST", "PUT","PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);  // Nếu bạn sử dụng cookie/tokens
              System.out.println("🔥 CORS Config Loaded!"); // Debug log

            }
        };
    }
}