package com.tim4it.payment.comparison.dto.v1.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
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
    List<ComparisonReport> comparisonReports = List.of();

    @Schema(description = "Unmatched results when compering two transactional data files")
    @Builder.Default
    List<List<UnmatchedReport>> unmatchedReports = List.of();
}
