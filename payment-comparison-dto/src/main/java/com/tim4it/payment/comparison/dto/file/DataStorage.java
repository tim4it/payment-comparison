package com.tim4it.payment.comparison.dto.file;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder(toBuilder = true)
public class DataStorage {

    @Builder.Default
    String fileName = "";

    @Builder.Default
    Map<DataKey, DataFile> parsedMap = Map.of();

    @Builder.Default
    int totalRecords = 0;

    @Builder.Default
    int duplicateTransactionRecords = 0;

    @Builder.Default
    int duplicateTransactionGroupRecords = 0;
}
