package com.tim4it.payment.comparison.dto.v1.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Only for simplifying data view for unmatched. We are using shitty html template, so we need to simplify things
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UnmatchedReport {

    @Schema(description = "File name - from provided file for first unmatched")
    @NonNull
    String fileName1;

    @Schema(description = "Date time for first unmatched")
    @NonNull
    String date1;

    @Schema(description = "Transactional amount for first unmatched")
    @NonNull
    String transactionAmount1;

    @Schema(description = "transaction id for first unmatched")
    @NonNull
    String transactionId1;

    @Schema(description = "Wallet reference for first unmatched")
    @NonNull
    String walletReference1;

    @Schema(description = "File name - from provided file for second unmatched")
    @NonNull
    String fileName2;

    @Schema(description = "Date time for second unmatched")
    @NonNull
    String date2;

    @Schema(description = "Transactional amount for second unmatched")
    @NonNull
    String transactionAmount2;

    @Schema(description = "transaction id for second unmatched")
    @NonNull
    String transactionId2;

    @Schema(description = "Wallet reference for second unmatched")
    @NonNull
    String walletReference2;

    @Schema(description = "File name - from provided file for second unmatched")
    @NonNull
    String fileName3;

    @Schema(description = "Date time for third unmatched")
    @NonNull
    String date3;

    @Schema(description = "Transactional amount for third unmatched")
    @NonNull
    String transactionAmount3;

    @Schema(description = "transaction id for third unmatched")
    @NonNull
    String transactionId3;

    @Schema(description = "Wallet reference for third unmatched")
    @NonNull
    String walletReference3;
}
