package com.tim4it.payment.comparison.v1.dto.file;

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

    int totalRecords;
}
