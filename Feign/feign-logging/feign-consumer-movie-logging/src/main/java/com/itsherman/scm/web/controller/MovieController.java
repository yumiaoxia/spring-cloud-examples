package com.itsherman.scm.web.controller;

import com.itsherman.scm.pojo.User;
import com.itsherman.scm.web.feignclient.UserFeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Sherman
 * created in 2019/3/15 2019
 */
@RestController
public class MovieController {

    private final UserFeignClient userFeignClient;

    public MovieController(UserFeignClient userFeignClient) {
        this.userFeignClient = userFeignClient;
    }

    @GetMapping("/user/{id}")
    public User findById(@PathVariable Long id) {
        return this.userFeignClient.findById(id);
    }
}
