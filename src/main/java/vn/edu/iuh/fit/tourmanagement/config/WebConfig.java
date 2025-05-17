package vn.edu.iuh.fit.tourmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("Configuring resource handler for /avatars/**");
        System.out.println("Absolute path: " + new File("uploads/avatars/").getAbsolutePath());
        registry
                .addResourceHandler("/avatars/**")
                .addResourceLocations("file:uploads/avatars/");
    }


}