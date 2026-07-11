package com.masl.goofy_protocol_fis_be.entity;

import com.masl.goofy_protocol_fis_be.service.UserBucketService;
import com.masl.goofy_protocol_fis_be.service.UserDbService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class ServiceEntry {
    @Id
    @Column(nullable = false, length = FieldSize.GENERIC_CODE_LEN)
    private String uuid; // Should be randomly generated unless its coming from an import

    // NOTE: Not encrypted, because it's not really important
    @Column(nullable = false, length = FieldSize.TITLE_LEN)
    @ColumnDefault("")
    private String name;

    @Column(length = FieldSize.NORMAL_TEXT_LEN)
    private String usedService;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User createdBy;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private IdentityStorageEntry linkedIdentity;

    @Column(nullable = false)
    private Instant createdAt;

    // Helper Stuff

    private static final Logger log = LoggerFactory.getLogger(ServiceEntry.class);

    @PrePersist
    public void initEntryHandler() throws IOException, SQLException {
        log.info("Creating ServiceEntry: " + this.uuid);
        UserDbService.getSingleton().createEntry(this);
        UserBucketService.getSingleton().createEntry(this);
    }

    @PreRemove
    public void deleteEntryHandler() throws IOException {
        log.info("Deleting ServiceEntry: " + this.uuid);
        UserDbService.getSingleton().deleteEntry(this);
        UserBucketService.getSingleton().deleteEntry(this);
    }
}
