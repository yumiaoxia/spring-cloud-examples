package com.itsherman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author Sherman
 * created in 2019/4/6 2019
 */
@SpringBootApplication
@EnableEurekaClient
public class FileUploadApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileUploadApplication.class, args);
    }
}
