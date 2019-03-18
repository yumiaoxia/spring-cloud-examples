package com.itsherman.scm.web.controller;

import com.itsherman.scm.pojo.User;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author Sherman
 * created in 2019/3/15 2019
 */
@RestController
public class MovieController {

    private final RestTemplate restTemplate;

    private final DiscoveryClient discoveryClient;

    public MovieController(RestTemplate restTemplate, DiscoveryClient discoveryClient) {
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/user/{id}")
    public User findById(@PathVariable Long id) {
        return this.restTemplate.getForObject("http://localhost:8010/user/" + id, User.class);
    }

    /***
     * 查询microservice-provider-user 服务的信息并返回
     */
    @GetMapping("/user-instance")
    public List<ServiceInstance> showInfo() {
        return discoveryClient.getInstances("eureka-provider-user");
    }
}
