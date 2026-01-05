package com.together.workeezy.user.repository;

import com.together.workeezy.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    // 이메일이 존재하지 않을 수도 있기 때문에
    // null 체크 대신 Optional 사용
    Optional<User> findByEmail(String email);

    boolean existsByPhone(String trimmedPhone);
}
