package com.fpt.careermate.services.account_services.domain;

import com.fpt.careermate.services.authentication_services.domain.ForgotPassword;
import com.fpt.careermate.services.authentication_services.domain.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String username;
    @Column(name = "email", unique = true)
    String email;
    String password;
    @Column(name = "status")
    String status;
    
    @Column(name = "avatar_url", length = 500)
    String avatarUrl;

    @ManyToMany
    Set<Role> roles;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    ForgotPassword forgotPassword;

}
