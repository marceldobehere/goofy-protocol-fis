package com.masl.goofy_protocol_fis_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fis_user")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Getter @Setter
    private String handle;

    @Column(nullable = false, length = 20_000)
    @Getter @Setter
    private String pubSplitKey;

    @Column(nullable = false)
    @Getter @Setter
    private boolean admin;

    @Override
    public String toString() {
        return "User{" +
                "handle='" + handle + '\'' +
                ", admin=" + admin +
                '}';
    }
}
