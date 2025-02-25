package com.drive.flashbox.service;

import com.drive.flashbox.dto.UserDto;
import com.drive.flashbox.dto.request.SignupRequestDTO;
import com.drive.flashbox.dto.response.SignupResponseDTO;
import com.drive.flashbox.entity.User;
import com.drive.flashbox.repository.UserRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public SignupResponseDTO registerUser(SignupRequestDTO signupRequestDTO) {

        Optional<UserDto> found = searchUser(signupRequestDTO.getEmail());
        if(found.isPresent()) {
            throw new IllegalStateException("동일한 email의 유저가 이미 존재합니다.");
        }


        User user = signupRequestDTO.toEntity();
        return SignupResponseDTO.of(userRepository.save(user));
    }

    public Optional<UserDto> searchUser(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email))
                .map(UserDto::from);
    }

    // 회원 탈퇴
    public void deleteUser(Long uid) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new IllegalStateException("User not found with id: " + uid));

        userRepository.delete(user);
    }

}
