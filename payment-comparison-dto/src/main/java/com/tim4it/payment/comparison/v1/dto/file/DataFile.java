package com.tim4it.payment.comparison.v1.dto.file;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class DataFile {

    @Builder.Default
    String profileName = "";

    @NonNull
    ZonedDateTime transactionDate;

    @Builder.Default
    List<Integer> transactionAmount = List.of();

    @Builder.Default
    String transactionNarrative = "";

    @NonNull
    TransactionDescription transactionDescription;

    @NonNull
    String transactionId;

    boolean transactionType;

    @Builder.Default
    String walletReference = "";

    public enum TransactionDescription {
        DEDUCT,
        REVERSAL
    }
}
