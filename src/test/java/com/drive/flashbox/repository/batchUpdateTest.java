package com.drive.flashbox.repository;

import com.drive.flashbox.config.SecurityConfig;
import com.drive.flashbox.entity.Box;
import com.drive.flashbox.entity.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 테스트 진행시 유의사항: mysql table PK가 auto_increment 이므로
 * 해당 값이 1부터 시작되게끔 초기화 먼저 진행 (아래 명령어 사용)
 * ALTER TABLE flashbox.box AUTO_INCREMENT=1;
 * ALTER TABLE flashbox.user AUTO_INCREMENT=1;
 */

@DisplayName("batch insert test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class batchUpdateTest {


    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final BoxRepository boxRepository;


    public batchUpdateTest(@Autowired PasswordEncoder passwordEncoder,
                           @Autowired JdbcTemplate jdbcTemplate,
                           @Autowired UserRepository userRepository,
                           @Autowired BoxRepository boxRepository) {
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
        this.boxRepository = boxRepository;
    }


    @DisplayName("Repository 객체 확인")
    @Test
    void test() {
        System.out.println(passwordEncoder);
        System.out.println(jdbcTemplate);
        System.out.println(userRepository);
        System.out.println(boxRepository);
    }

    @DisplayName("Dummy 데이터 삽입 - saveAllAndFlush() 사용")
    @Transactional
    @Test
    void insertDummyDataV1() {
        List<User> users = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> new User(
                        "유저이름"+i,
                        "user"+i+"@user.com",
                        passwordEncoder.encode("user"+i)))
                .collect(Collectors.toList());

        userRepository.saveAllAndFlush(users);


        List<Box> boxes = IntStream.rangeClosed(1, 50000)
                .mapToObj(i -> new Box (
                        "박스 "+i,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        users.get(i%users.size())))
                .collect(Collectors.toList());


        boxRepository.saveAllAndFlush(boxes);

    }

    @DisplayName("Dummy 데이터 삽입 - jdbcTemplate batchUpdate() 사용")
    @Transactional
//    @Rollback(value = false)
    @Test
    void insertDummyDataV2() {
        List<User> users = IntStream.rangeClosed(101, 200)
                .mapToObj(i -> new User(
                        (long) i,
                        "유저이름"+i,
                        "user"+i+"@user.com",
                        "user"+i))
                .collect(Collectors.toList());

        saveUsersBatch(users,false);


        List<Box> boxes = IntStream.rangeClosed(50001, 100000)
                .mapToObj(i -> new Box(
                        "박스 " + i,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        users.get(i % users.size()))
                )
                .collect(Collectors.toList());

        saveBoxesBatch(boxes,false);

    }

    @DisplayName("Dummy 데이터 삽입 - jdbcTemplate batchUpdate() with type")
    @Transactional
    @Test
    void insertDummyDataV3() {
        List<User> users = IntStream.rangeClosed(201, 300)
                .mapToObj(i -> new User(
                        (long) i,
                        "유저이름"+i,
                        "user"+i+"@user.com",
                        "user"+i))
                .collect(Collectors.toList());

        saveUsersBatch(users,true);


        List<Box> boxes = IntStream.rangeClosed(100001, 150000)
                .mapToObj(i -> new Box(
                        "박스 " + i,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        users.get(i % users.size()))
                )
                .collect(Collectors.toList());

        saveBoxesBatch(boxes,true);

    }
    private void saveUsersBatch(List<User> users, boolean type) {

        // 배치 INSERT 실행
        String sql = "INSERT INTO user (created_date, name, email, password) VALUES (?,?,?,?)";
        List<Object[]> batchArgs = users.stream()
                .map(user -> new Object[]{
                        LocalDateTime.now(),
                        user.getName(),
                        user.getEmail(),
                        passwordEncoder.encode(user.getPassword())})
                .collect(Collectors.toList());

        // 각 컬럼에 대한 SQL 데이터 타입 지정
        int[] argTypes = new int[]{
                Types.DATE,  // created_date → DATE
                Types.VARCHAR,    // name → VARCHAR
                Types.VARCHAR,    // email → VARCHAR
                Types.VARCHAR     // password → VARCHAR
        };

        if(!type) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
            return;
        }

        jdbcTemplate.batchUpdate(sql, batchArgs, argTypes);


    }

    private void saveBoxesBatch(List<Box> boxes, boolean type) {

        // 배치 INSERT 실행
        String sql = "INSERT INTO box (count, boom_date, created_date, event_end_date, event_start_date, modified_date, uid, name) VALUES (?,?,?,?,?,?,?,?)";
        List<Object[]> batchArgs = boxes.stream()
                .map(box -> new Object[]{
                        box.getCount(),
                        box.getBoomDate(),
                        LocalDateTime.now(),
                        box.getEventEndDate(),
                        box.getEventStartDate(),
                        LocalDateTime.now(),
                        box.getUser().getId(),
                        box.getName()
                })
                .collect(Collectors.toList());

        // 각 컬럼에 대한 SQL 데이터 타입 지정
        int[] argTypes = new int[]{
                Types.INTEGER,
                Types.DATE,
                Types.DATE,
                Types.DATE,
                Types.DATE,
                Types.DATE,
                Types.BIGINT,
                Types.VARCHAR
        };

        if(!type){
            jdbcTemplate.batchUpdate(sql, batchArgs);
            return;
        }
        jdbcTemplate.batchUpdate(sql, batchArgs, argTypes);

    }


}
