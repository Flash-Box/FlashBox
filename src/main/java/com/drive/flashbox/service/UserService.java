package com.drive.flashbox.service;

import com.drive.flashbox.dto.UserDto;
import com.drive.flashbox.dto.request.SignupRequestDTO;
import com.drive.flashbox.dto.response.SignupResponseDTO;
import com.drive.flashbox.entity.User;
import com.drive.flashbox.repository.UserRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    // 회원 탈퇴
    public void deleteUser(Long uid) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new IllegalStateException("User not found with id: " + uid));

        userRepository.delete(user);
    }

    public User getUser(Long uid) {
        return userRepository.findById(uid)
                .orElseThrow(() -> new IllegalStateException("User not found with id: " + uid));

    }
}
