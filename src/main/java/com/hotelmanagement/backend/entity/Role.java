package com.hotelmanagement.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    String name;
    String description;

    @OneToMany(mappedBy = "role")
    Set<User> users;

    @ManyToMany(fetch = FetchType.EAGER)
    Set<Permisson> permissions;
}
