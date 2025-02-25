package com.drive.flashbox.repository;

import com.drive.flashbox.entity.Box;
import com.drive.flashbox.entity.BoxUser;
import com.drive.flashbox.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoxUserRepository extends JpaRepository<BoxUser, Long> {
    // 이미 초대된 유저인지 확인 (Box, User 둘 다 필요)
    boolean existsByBoxAndUser(Box box, User user);

    // 특정 박스에 소속된 모든 BoxUser 조회
    List<BoxUser> findAllByBox(Box box);

    // 특정 박스 + 특정 유저에 해당하는 BoxUser 찾기
    Optional<BoxUser> findByBoxAndUser(Box box, User user);

    // Box의 bid만으로 조회할 수도 있음
    List<BoxUser> findAllByBoxBid(Long bid);

    // 필요한 경우, user uid로도 조회 가능
    List<BoxUser> findAllByUserId(Long id);
}
