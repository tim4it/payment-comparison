package com.tim4it.payment.comparison.v1.service;

import com.tim4it.payment.comparison.dto.file.DataFile;
import com.tim4it.payment.comparison.dto.file.DataKey;
import com.tim4it.payment.comparison.dto.file.DataStorage;
import com.tim4it.payment.comparison.util.Helper;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        var duplicateRecords = new AtomicInteger();
        splitRows.stream()
                .map(this::parseToDataFile)
                .forEach(dataFile -> fillDataMap(mapData, dataFile, duplicateRecords));
        return DataStorage.builder()
                .fileName(completedFileUpload.getFilename())
                .parsedMap(Collections.unmodifiableMap(mapData))
                .totalRecords(splitRows.size())
                .duplicateRecords(duplicateRecords.get())
                .build();
    }

    /**
     * Fill map data and handle duplicates. If transaction has same time, same transaction id, it is possible to have
     * different amount. That's why amount is stored as list - this is handled
     *
     * @param mapData  map data to be filled
     * @param dataFile current data file
     */
    private void fillDataMap(@NonNull LinkedHashMap<DataKey, DataFile> mapData,
                             @NonNull DataFile dataFile,
                             @NonNull AtomicInteger duplicateRecords) {
        var transactionAmount = dataFile.getTransactionAmount().iterator().next();
        var dataKey = DataKey.builder()
                .transactionDate(dataFile.getTransactionDate())
                .transactionId(dataFile.getTransactionId())
                .transactionAmount(List.of(transactionAmount))
                .build();
        var absentData = mapData.putIfAbsent(dataKey, dataFile);
        if (absentData != null) {
            var amountData = new ArrayList<>(absentData.getTransactionAmount());
            amountData.add(transactionAmount);

            var newDataKey = dataKey.toBuilder()
                    .transactionAmount(List.copyOf(amountData))
                    .build();
            var newDataFile = absentData.toBuilder()
                    .transactionAmount(List.copyOf(amountData))
                    .build();
            mapData.remove(dataKey);
            var absentDataAmount = mapData.putIfAbsent(newDataKey, newDataFile);
            if (absentDataAmount != null) {
                throw new RuntimeException("Data is already present - we expect clean put in map! " + absentDataAmount);
            }
            duplicateRecords.incrementAndGet();
        }
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
        if (columnDataLen < 7 || columnDataLen > 8) {
            throw new RuntimeException("Column file length must be size of 8 is " + columnData.length + "! " + row);
        }
        return DataFile.builder()
                .profileName(Helper.optString(columnData[0]).orElse(""))
                .transactionDate(parseDate(columnData[1]))
                .transactionAmount(List.of(parseAmount(columnData[2])))
                .transactionNarrative(columnData[3])
                .transactionDescription(DataFile.TransactionDescription.valueOf(columnData[4]))
                .transactionId(Helper.optString(columnData[5]).orElseThrow())
                .transactionType(parseTransactionType(columnData[6]))
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
                .orElseThrow();

    }

    private Integer parseAmount(String transactionAmountColumn) {
        try {
            return Integer.parseInt(transactionAmountColumn);
        } catch (Exception e) {
            throw new RuntimeException("Wrong transaction amount: " + transactionAmountColumn);
        }
    }
}
