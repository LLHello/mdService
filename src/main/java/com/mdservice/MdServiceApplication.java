package com.mdservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.mdservice.mapper")
public class MdServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MdServiceApplication.class, args);
    }

}
