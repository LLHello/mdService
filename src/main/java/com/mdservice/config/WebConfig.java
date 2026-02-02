package com.mdservice.config;

import com.mdservice.interceptor.JwtInterceptor;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 跨域、拦截器、资源访问配置
@Configuration
public class WebConfig implements WebMvcConfigurer{
    @Autowired
    public JwtInterceptor jwtInterceptor;
    // 跨域配置
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE","OPTIONS")
                .allowedOriginPatterns("*")
                .allowedHeaders("*")
                .allowCredentials(true)//允许cookie
                .maxAge(3600);
    }
    // 拦截器配置
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/users/login/**", "/error")
                .excludePathPatterns("/users/register/**")
                .excludePathPatterns("/users/upload/**")
                .excludePathPatterns("/goods/**")
                .excludePathPatterns("/goods/**")
                .excludePathPatterns("/upload/**");
        ;
    }

    //资源访问配置
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("classpath:upload/")
                .setCachePeriod(3600);
    }
}

