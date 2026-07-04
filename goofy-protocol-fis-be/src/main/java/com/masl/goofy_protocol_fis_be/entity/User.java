package com.masl.goofy_protocol_fis_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "fis_user")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(nullable = false, length = FieldSize.HANDLE_LEN)
    @Getter @Setter
    private String handle;

    @Column(nullable = false, length = FieldSize.PUB_KEY_LEN)
    @Getter @Setter
    private String pubSplitKey;

    @Column(nullable = false)
    @Getter @Setter
    @ColumnDefault("false")
    private boolean admin;

    // Future thing to temporarily block users from doing stuff (if the user is reported, or otherwise temporarily restricted
    // Should ideally put the user in a basically read-only mode
    // TODO: Implement role or auth checks for that and add to required endpoints
    @Column(nullable = false)
    @Getter @Setter
    @ColumnDefault("false")
    private boolean restricted;

    @Column
    @Getter @Setter
    private Instant lastCheck;

    @Override
    public String toString() {
        return "User{" +
                "handle='" + handle + '\'' +
                ", admin=" + admin +
                ", restricted=" + restricted +
                '}';
    }
}
