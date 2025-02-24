package com.drive.flashbox.service;

import com.drive.flashbox.dto.UserDTO;
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

        User user = signupRequestDTO.toEntity();
        return SignupResponseDTO.of(userRepository.save(user));
    }

    public Optional<UserDTO> searchUser(String email) {
        return Optional.of(UserDTO.from(userRepository.findByEmail(email)));
    }

}
