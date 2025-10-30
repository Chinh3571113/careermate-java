package com.fpt.careermate.services.admin_services.domain;

import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.blog_services.domain.Blog;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adminid")
    int adminId;

    @Size(max = 200)
    @Column(name = "name", nullable = false)
    String name;

    @Size(max = 20)
    @Column(name = "phone")
    String phone;

    @OneToOne
    @JoinColumn(name = "accountid", unique = true, nullable = false, updatable = false)
    Account account;

    @OneToMany(mappedBy = "admin")
    List<Blog> blogs;
}

