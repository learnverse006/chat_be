            package com.example.chatbe.config;

            import org.springframework.context.annotation.Configuration;
            import org.springframework.web.servlet.config.annotation.*;

            @Configuration
            public class WebMvcConfig implements WebMvcConfigurer {

                @Override
                public void addResourceHandlers(ResourceHandlerRegistry registry) {
                    registry.addResourceHandler("/files/**")
                            .addResourceLocations("file:uploads/");
                }
            }