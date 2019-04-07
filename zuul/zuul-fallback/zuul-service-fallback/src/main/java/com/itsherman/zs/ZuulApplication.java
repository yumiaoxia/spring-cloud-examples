package com.itsherman.zs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * @author Sherman
 * created in 2019/4/5 2019
 */
@SpringBootApplication
@EnableZuulProxy

public class ZuulApplication {


    public static void main(String[] args) {
        SpringApplication.run(ZuulApplication.class, args);
    }
}
