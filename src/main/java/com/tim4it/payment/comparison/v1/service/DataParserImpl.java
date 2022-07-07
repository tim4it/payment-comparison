package com.tim4it.payment.comparison.v1.service;

import com.tim4it.payment.comparison.dto.file.DataFile;
import com.tim4it.payment.comparison.dto.file.DataKey;
import com.tim4it.payment.comparison.dto.file.DataStorage;
import com.tim4it.payment.comparison.util.Helper;
import com.tim4it.payment.comparison.util.Pair;
import io.micronaut.http.multipart.CompletedFileUpload;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Singleton
public class DataParserImpl implements DataParser {

    @Override
    public Mono<DataStorage> parseFile(@NonNull CompletedFileUpload completedFileUpload) {
        return Mono.fromCallable(() -> parseCsvFile(completedFileUpload));
    }

    /**
     * Parse CSV file to object {@link DataStorage}
     *
     * @param completedFileUpload upload file data {@link CompletedFileUpload}
     * @return data storage CSV to types map {@link DataStorage}
     */
    private DataStorage parseCsvFile(@NonNull CompletedFileUpload completedFileUpload) {
        var splitRows = splitRowData(completedFileUpload);
        var mapData = new LinkedHashMap<DataKey, DataFile>();
        var duplicateMap = new HashMap<DataKey, List<Integer>>();
        splitRows.stream()
                .map(this::parseToDataFile)
                .forEach(dataFile -> fillDataMap(dataFile, mapData, duplicateMap));
        var pairDuplicateCount = updateDuplicates(mapData, duplicateMap);
        return DataStorage.builder()
                .fileName(completedFileUpload.getFilename())
                .parsedMap(Collections.unmodifiableMap(mapData))
                .totalRecords(splitRows.size())
                .duplicateTransactionGroupRecords(pairDuplicateCount.getFirst())
                .duplicateTransactionRecords(pairDuplicateCount.getSecond())
                .build();
    }

    /**
     * Fill map data and handle duplicates. If transaction has same time, same transaction id, it is possible to have
     * different amount. That's why amount is stored as list - this is handled
     *
     * @param dataFile     current data file
     * @param mapData      map data to be filled
     * @param duplicateMap duplicate data map - store additional duplicates from master map - mapData
     */
    private void fillDataMap(@NonNull DataFile dataFile,
                             @NonNull LinkedHashMap<DataKey, DataFile> mapData,
                             @NonNull Map<DataKey, List<Integer>> duplicateMap) {

        var dataKey = DataKey.builder()
                .transactionDate(dataFile.getTransactionDate())
                .transactionId(dataFile.getTransactionId())
                .transactionAmount(dataFile.getTransactionAmount())
                .build();
        var absentData = mapData.putIfAbsent(dataKey, dataFile);
        if (absentData != null) {
            if (duplicateMap.containsKey(dataKey)) {
                var amounts = duplicateMap.get(dataKey);
                var amountData = Stream.of(dataFile.getTransactionAmount(), amounts)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toUnmodifiableList());
                duplicateMap.put(dataKey, amountData);
            } else {
                duplicateMap.put(dataKey, dataFile.getTransactionAmount());
            }
        }
    }

    /**
     * Update duplicate map to main map. Duplicates are data with same date, transaction id and amount.
     *
     * @param mapData      main data map
     * @param duplicateMap duplicate data map
     */
    private Pair<Integer, Integer> updateDuplicates(@NonNull LinkedHashMap<DataKey, DataFile> mapData,
                                                    @NonNull HashMap<DataKey, List<Integer>> duplicateMap) {
        var transactionalDuplicateCounter = new AtomicInteger();
        duplicateMap.forEach((key, value) -> {
            if (mapData.containsKey(key)) {
                var duplicateData = mapData.remove(key);
                var amountData = Stream.of(duplicateData.getTransactionAmount(), value)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toUnmodifiableList());
                var dataKey = DataKey.builder()
                        .transactionDate(duplicateData.getTransactionDate())
                        .transactionId(duplicateData.getTransactionId())
                        .transactionAmount(amountData)
                        .build();
                // sum of all duplicate records
                transactionalDuplicateCounter.set(transactionalDuplicateCounter.get() + amountData.size());
                mapData.put(
                        dataKey,
                        duplicateData.toBuilder()
                                .transactionAmount(amountData)
                                .build());
            }
        });
        return new Pair<>(duplicateMap.size(), transactionalDuplicateCounter.get());
    }

    private String[] splitColumnData(@NonNull String rowData) {
        return rowData.split(",");
    }

    private List<String> splitRowData(@NonNull CompletedFileUpload completedFileUpload) {
        try {
            var data = new String(completedFileUpload.getBytes(), StandardCharsets.UTF_8);
            var dataSplit = data.split("\\R");
            return Arrays
                    .asList(dataSplit)
                    // skip headers from CSV file
                    .subList(1, dataSplit.length);
        } catch (IOException e) {
            throw new RuntimeException("Split data error!", e);
        }

    }

    /**
     * Parse provided csv file to types
     *
     * @param row file row (rows separated by new line)
     * @return data file with types {@link DataFile}
     */
    private DataFile parseToDataFile(String row) {
        var columnData = splitColumnData(row);
        var columnDataLen = columnData.length;
        if (columnDataLen < 6 || columnDataLen > 8) {
            throw new RuntimeException("Column file length must be size of 8 is " + columnData.length + "! " + row);
        }
        return DataFile.builder()
                .profileName(Helper.optString(columnData[0]).orElse(""))
                .transactionDate(parseDate(columnData[1]))
                .transactionAmount(List.of(parseAmount(columnData[2])))
                .transactionNarrative(columnData[3])
                .transactionDescription(DataFile.TransactionDescription.valueOf(columnData[4]))
                .transactionId(Helper.optString(columnData[5]).orElseThrow())
                .transactionType(columnDataLen >= 7 && parseTransactionType(columnData[6]))
                .walletReference(columnDataLen < 8 ? "" : Helper.optString(columnData[7]).orElse(""))
                .build();
    }

    /**
     * Data parsing. Date time is hard, use UTC format - client can use localized data. Current CSVs has incorrect date
     * time - without zone information?!?
     *
     * @param dateString date time in string format
     * @return zoned date time {@link ZonedDateTime}
     */
    private ZonedDateTime parseDate(String dateString) {
        var formatter = DateTimeFormatter.ofPattern(Helper.DATE_FORMAT);
        var validateDateString = Helper.optString(dateString)
                .orElseThrow(() -> new RuntimeException("Date format error: " + dateString));
        var localTime = LocalDateTime.parse(validateDateString, formatter);
        // assuming we're UTC here, time is important, always in UTC on server
        return ZonedDateTime.of(localTime, ZoneId.from(ZoneOffset.UTC));
    }

    private boolean parseTransactionType(String transactionType) {
        return Helper.optString(transactionType)
                .map(transactionTypeOK -> transactionTypeOK.equals("1"))
                .orElse(false);

    }

    private Integer parseAmount(String transactionAmountColumn) {
        try {
            return Integer.parseInt(transactionAmountColumn);
        } catch (Exception e) {
            throw new RuntimeException("Wrong transaction amount: " + transactionAmountColumn);
        }
    }
}
