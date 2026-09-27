package com.rikkeibank.identity.service;
import com.rikkeibank.common.api.BusinessException; import com.rikkeibank.identity.domain.AppUser; import com.rikkeibank.identity.repo.UserRepository; import org.springframework.data.domain.*; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.UUID;
@Service public class UserService { private final UserRepository repo; private final PasswordEncoder passwords; public UserService(UserRepository r,PasswordEncoder p){repo=r;passwords=p;}
 @Transactional public AppUser create(String u,String p,AppUser.Role role,String name,UUID customerId){if(repo.existsByUsername(u))throw BusinessException.conflict("USERNAME_EXISTS","Username already exists");return repo.save(new AppUser(u,passwords.encode(p),role,name,customerId));}
 public Page<AppUser> list(Pageable p){return repo.findAll(p);} public AppUser get(UUID id){return repo.findById(id).orElseThrow(()->BusinessException.notFound("User not found"));}
 @Transactional public AppUser update(UUID id,String name,AppUser.Role role,boolean active,UUID customerId){AppUser u=get(id);u.update(name,role,active,customerId);return u;} @Transactional public void delete(UUID id){if(!repo.existsById(id))throw BusinessException.notFound("User not found");repo.deleteById(id);}
}

