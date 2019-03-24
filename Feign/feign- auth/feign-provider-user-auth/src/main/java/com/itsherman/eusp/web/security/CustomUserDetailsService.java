package com.itsherman.eusp.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * @author Sherman
 * created in 2019/3/24 2019
 */
@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if("user".equals(username)){
            return new SecurityUser(username,"123456","user-role");
        }else if("admin".equals(username)){
            return new SecurityUser(username,"1234","admin-role");
        }else{
            return null;
        }
    }
}
