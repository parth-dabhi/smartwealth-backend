package com.smartwealth.smartwealth_backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(
        name = "mutual_fund_schemes",
        indexes = {
                @Index(name = "idx_scheme_amc", columnList = "amc_id"),
                @Index(name = "idx_scheme_asset", columnList = "asset_id"),
                @Index(name = "idx_scheme_category", columnList = "category_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MutualFundScheme {

    @Id
    @Column(name = "scheme_id")
    private Integer schemeId;

    @Column(name = "amc_id", nullable = false)
    private Integer amcId;

    @Column(name = "scheme_name", nullable = false, length = 255)
    private String schemeName;

    @Column(name = "asset_id")
    private Integer assetId;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "benchmark_id")
    private Integer benchmarkId;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
