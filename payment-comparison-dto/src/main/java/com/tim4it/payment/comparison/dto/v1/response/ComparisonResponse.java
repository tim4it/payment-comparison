package com.tim4it.payment.comparison.dto.v1.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Schema(description = "Comparison response object")
@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ComparisonResponse {

    @Schema(description = "Comparison results from two CSV files")
    @Builder.Default
    List<ComparisonResult> comparisonResults = List.of();

    @Schema(description = "Unmatched results when compering two transactional data files")
    @Builder.Default
    List<List<UnmatchedReport>> unmatchedReports = List.of();

    @Schema(description = "Comparison result data")
    @Value
    @Builder(toBuilder = true)
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ComparisonResult {

        @Schema(description = "File name - from provided file")
        @NonNull
        String fileName;

        @Schema(description = "Total records count in comparison file")
        int totalRecords;

        @Schema(description = "Matched records count between two files")
        int matchingRecords;

        @Schema(description = "Un-matched records count between two files")
        int unmatchedRecords;

        @Schema(description = "Duplicate records count between two files")
        int duplicateTransactionRecords;
    }

    @Value
    @Builder(toBuilder = true)
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UnmatchedReport {

        @Schema(description = "File name - from provided file")
        @NonNull
        String fileName;

        @Schema(description = "Date time")
        @NonNull
        String date;

        @Schema(description = "Transactional amount")
        @NonNull
        Integer transactionAmount;

        @Schema(description = "transaction id")
        @NonNull
        String transactionId;

        @Schema(description = "Wallet reference")
        @NonNull
        String walletReference;
    }
}
