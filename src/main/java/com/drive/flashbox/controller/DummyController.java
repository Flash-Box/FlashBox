package com.drive.flashbox.controller;

import com.drive.flashbox.entity.Box;
import com.drive.flashbox.entity.User;
import com.drive.flashbox.repository.BoxRepository;
import com.drive.flashbox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/dummy")
@RequiredArgsConstructor
public class DummyController {

    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final BoxRepository boxRepository;


    @GetMapping("/user")
    public String userDummy() {


        return "userDummy";
    }


    @GetMapping("/v1")
    @Transactional
    public String boxDummyV1() {

        List<User> users = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> new User(
                        "유저이름"+i,
                        "user"+i+"@user.com",
                        "user"+i))
                .collect(Collectors.toList());

        userRepository.saveAllAndFlush(users);


        List<Box> boxes = IntStream.rangeClosed(1, 1000)
                .mapToObj(i -> new Box (
                        "박스 "+i,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        users.get(i%users.size())))
                .collect(Collectors.toList());


        boxRepository.saveAllAndFlush(boxes);



        return "create dummy data v1-saveAllAndFlush: success";
    }


    @GetMapping("/v2")
    @Transactional
    public String boxDummyV2() {

        // 1️⃣ User 데이터 생성
        List<User> users = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> new User((long)i,
                        "유저이름"+i,
                        "user"+i+"@user.com",
                        "user"+i))
                .collect(Collectors.toList());


        // 2️⃣ User 데이터 배치 저장
        saveUsersBatch(users);

        // 3️⃣ Box 데이터 생성
        List<Box> boxes = IntStream.rangeClosed(1, 1000)
                .mapToObj(i -> new Box((long) i, "박스 " + i, LocalDateTime.now(), LocalDateTime.now(), users.get(i % users.size())))
                .collect(Collectors.toList());

        // 4️⃣ Box 데이터 배치 저장
        saveBoxesBatch(boxes);

        return "create dummy data v2-jdbcTemplate: success";
    }

    private void saveUsersBatch(List<User> users) {

        // 배치 INSERT 실행
        String sql = "INSERT INTO user (created_date, name, email, password) VALUES (?,?,?,?)";
        List<Object[]> batchArgs = users.stream()
                .map(user -> new Object[]{
                        Timestamp.valueOf(LocalDateTime.now()),
                        user.getName(),
                        user.getEmail(),
                        passwordEncoder.encode(user.getPassword())})
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql, batchArgs);


    }

    private void saveBoxesBatch(List<Box> boxes) {

        // 배치 INSERT 실행
        String sql = "INSERT INTO box (count, boom_date, created_date, event_end_date, event_start_date, modified_date, uid, name) VALUES (?,?,?,?,?,?,?,?)";
        List<Object[]> batchArgs = boxes.stream()
                .map(box -> new Object[]{
                        box.getCount(),
                        box.getBoomDate(),
                        Timestamp.valueOf(LocalDateTime.now()),
                        box.getEventEndDate(),
                        box.getEventStartDate(),
                        Timestamp.valueOf(LocalDateTime.now()),
                        box.getUser().getId(),
                        box.getName()
                })
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql, batchArgs);

    }


}
