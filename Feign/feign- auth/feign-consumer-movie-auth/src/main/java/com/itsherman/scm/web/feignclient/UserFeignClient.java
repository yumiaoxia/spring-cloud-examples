package com.itsherman.scm.web.feignclient;

import com.itsherman.scm.pojo.User;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Sherman
 * created in 2019/3/24 2019
 */
public interface UserFeignClient {

    @GetMapping("/user/{id}")
    public User findById(@PathVariable("id") Long id);
}
