package com.tim4it.payment.comparison.dto.v1.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Schema(description = "Comparison result data")
@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ComparisonReport {

    @Schema(description = "File name - from provided file")
    @NonNull
    @Builder.Default
    String fileName = "";

    @Schema(description = "Total records count in comparison file")
    @Builder.Default
    int totalRecords = 0;

    @Schema(description = "Matched records count between two files")
    @Builder.Default
    int matchingRecords = 0;

    @Schema(description = "Un-matched records count between two files")
    @Builder.Default
    int unmatchedRecords = 0;

    @Schema(description = "Duplicate group records count between two files")
    @Builder.Default
    int duplicateTransactionGroupRecords = 0;

    @Schema(description = "Duplicate records (all duplicates) count between two files")
    @Builder.Default
    int duplicateTransactionRecords = 0;
}