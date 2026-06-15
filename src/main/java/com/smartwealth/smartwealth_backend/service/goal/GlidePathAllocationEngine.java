package com.smartwealth.smartwealth_backend.service.goal;

import com.smartwealth.smartwealth_backend.dto.common.AssetMix;
import com.smartwealth.smartwealth_backend.entity.enums.DurationBucket;
import org.springframework.stereotype.Component;

@Component
public class GlidePathAllocationEngine {

    public AssetMix getAllocation(int riskId, int months) {

        DurationBucket bucket = getBucket(months);

        return switch (riskId) {

            case 1 -> riskAverse(bucket);
            case 2 -> conservative(bucket);
            case 3 -> moderate(bucket);
            case 4 -> aggressive(bucket);
            case 5 -> veryAggressive(bucket);

            default -> throw new RuntimeException("Invalid risk");
        };
    }

    private DurationBucket getBucket(int months) {

        if (months <= 36) return DurationBucket.SHORT;
        if (months <= 84) return DurationBucket.MID;
        return DurationBucket.LONG;
    }

    private AssetMix riskAverse(DurationBucket b) {
        return switch (b) {
            case SHORT -> new AssetMix(0, 100);
            case MID   -> new AssetMix(5, 95);
            case LONG  -> new AssetMix(20, 80);
        };
    }

    private AssetMix conservative(DurationBucket b) {
        return switch (b) {
            case SHORT -> new AssetMix(5, 95);
            case MID   -> new AssetMix(25, 75);
            case LONG  -> new AssetMix(55, 45);
        };
    }

    private AssetMix moderate(DurationBucket b) {
        return switch (b) {
            case SHORT -> new AssetMix(35, 65);
            case MID   -> new AssetMix(55, 45);
            case LONG  -> new AssetMix(85, 15);
        };
    }

    private AssetMix aggressive(DurationBucket b) {
        return switch (b) {
            case SHORT -> new AssetMix(55, 45);
            case MID   -> new AssetMix(75, 25);
            case LONG  -> new AssetMix(90, 10);
        };
    }

    private AssetMix veryAggressive(DurationBucket b) {
        return switch (b) {
            case SHORT -> new AssetMix(70, 30);
            case MID   -> new AssetMix(90, 10);
            case LONG  -> new AssetMix(95, 5);
        };
    }
}
