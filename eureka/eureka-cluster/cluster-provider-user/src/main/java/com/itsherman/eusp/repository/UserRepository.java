package com.itsherman.eusp.repository;

import com.itsherman.eusp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Sherman
 * created in 2019/3/15 2019
 */
public interface UserRepository extends JpaRepository<User, Long> {
}
