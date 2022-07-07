package com.tim4it.payment.comparison.dto.v1.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UnmatchedReport {

    @Schema(description = "File name - from provided file")
    @NonNull
    @Builder.Default
    String fileName = "";

    @Schema(description = "Date time")
    @NonNull
    @Builder.Default
    String date = "";

    @Schema(description = "Transactional amount")
    @NonNull
    @Builder.Default
    Integer transactionAmount = 0;

    @Schema(description = "transaction id")
    @NonNull
    @Builder.Default
    String transactionId = "";

    @Schema(description = "Wallet reference")
    @NonNull
    @Builder.Default
    String walletReference = "";
}
