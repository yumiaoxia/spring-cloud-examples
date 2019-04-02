package com.itsherman.scm;

import com.itsherman.scm.pojo.User;
import com.itsherman.scm.web.feignclient.UserFeignClient;
import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sherman
 * created in 2019/4/1 2019
 */
public class FeignClientFallbackFactory implements FallbackFactory<UserFeignClient> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeignClientFallbackFactory.class);

    @Override
    public UserFeignClient create(Throwable cause) {
        return new UserFeignClient() {
            @Override
            public User findById(Long id) {
                LOGGER.info("fallback;reason was:", cause);
                User user = new User();
                user.setId(-1L);
                user.setUsername("默认用户");
                return user;
            }
        };
    }
}
