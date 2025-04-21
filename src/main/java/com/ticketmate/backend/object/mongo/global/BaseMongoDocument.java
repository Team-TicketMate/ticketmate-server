package com.ticketmate.backend.object.mongo.global;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class BaseMongoDocument {
    @CreatedDate
    private java.time.LocalDateTime createdDate;

    @LastModifiedDate
    private java.time.LocalDateTime updatedDate;
}
