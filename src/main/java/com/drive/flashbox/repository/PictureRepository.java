package com.drive.flashbox.repository;

import com.drive.flashbox.entity.Picture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PictureRepository extends JpaRepository<Picture, Long> {
    // bid와 pid로 Picture를 찾는 메서드
    Optional<Picture> findByPidAndBoxBid(Long pid, Long bid);
    // 1개의 박스에는 여러개의 사진이 있으니 List로 받아오기
    List<Picture> findAllByBoxBid(Long bid);
}
