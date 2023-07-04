package com.yolt.creditscoring.service.userjourney.model;

import com.yolt.creditscoring.service.userjourney.JourneyStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.OffsetDateTime;
import java.util.UUID;

import static com.yolt.creditscoring.service.userjourney.model.UserJourneyMetric.TABLE_NAME;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = TABLE_NAME)
public class UserJourneyMetric {

    public static final String TABLE_NAME = "user_journey_metric";

    @Id
    private UUID id;

    private UUID userId;

    @PastOrPresent
    private OffsetDateTime createdDate;

    @NotNull
    private UUID clientId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private JourneyStatus status;

}
