package com.drive.flashbox.repository;

import com.drive.flashbox.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface TokenRepository extends CrudRepository<RefreshToken, Long> {

}
