package com.tirmizee.repositories;

import com.tirmizee.entities.User;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends PagingAndSortingRepository<User, Long> {


}
