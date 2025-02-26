package com.drive.flashbox.security;

import com.drive.flashbox.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


@ToString
@Getter
@AllArgsConstructor
public class FBUserDetails implements UserDetails {

    private Long uid;
    private String username;
    private String password;
    private String email;

    public static FBUserDetails of(Long uid, String username, String password, String email) {
        return new FBUserDetails(
                uid,
                username,
                password,
                email
        );
    }

    public static FBUserDetails from(UserDto userDto) {
        return FBUserDetails.of(
                userDto.getId(),
                userDto.getName(),
                userDto.getPassword(),
                userDto.getEmail()
        );
    }

    public UserDto toDto() {
        return UserDto.of(uid,
                username,
                password,
                email);
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

//    @Override
//    public String getPassword() {
//        return password;
//    }
//
//    @Override
//    public String getUsername() {
//        return username;
//    }


}
