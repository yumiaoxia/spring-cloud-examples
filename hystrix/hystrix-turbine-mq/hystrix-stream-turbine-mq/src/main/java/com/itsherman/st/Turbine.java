package com.itsherman.st;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.turbine.stream.EnableTurbineStream;

/**
 * @author Sherman
 * created in 2019/4/1 2019
 */
@SpringBootApplication
@EnableTurbineStream
public class Turbine {

    public static void main(String[] args) {
        SpringApplication.run(Turbine.class, args);
    }
}
