package com.yolt.creditscoring.service.estimate.storage;

import com.yolt.creditscoring.service.creditscore.model.PdStatus;
import com.yolt.creditscoring.service.estimate.provider.dto.RiskClassification;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.UUID;

import static com.yolt.creditscoring.service.estimate.storage.EstimateEntity.TABLE_NAME;

@Data
@Entity
@NoArgsConstructor
@Table(name = TABLE_NAME)
@Accessors(chain = true)
public class EstimateEntity {
    public static final String TABLE_NAME = "estimate_report";

    @Id
    private UUID id;

    private UUID userId;

    @Min(0)
    @Max(100)
    private Integer score;

    @Enumerated(EnumType.STRING)
    private RiskClassification grade;

    @Enumerated(EnumType.STRING)
    private PdStatus status;

}
