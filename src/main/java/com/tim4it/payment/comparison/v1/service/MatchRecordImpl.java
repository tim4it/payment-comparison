package com.tim4it.payment.comparison.v1.service;

import com.tim4it.payment.comparison.dto.file.DataFile;
import com.tim4it.payment.comparison.dto.file.DataKey;
import com.tim4it.payment.comparison.dto.file.DataStorage;
import com.tim4it.payment.comparison.dto.v1.response.ComparisonReport;
import com.tim4it.payment.comparison.util.Helper;
import com.tim4it.payment.comparison.util.Pair;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Singleton
public class MatchRecordImpl implements MatchRecord {

    @Override
    public Mono<List<ComparisonReport>> match(
            @NonNull Pair<DataStorage, DataStorage> pairOfDataStorage) {

        return Mono.fromCallable(() -> comparisonResults(pairOfDataStorage));
    }

    private List<ComparisonReport> comparisonResults(
            @NonNull Pair<DataStorage, DataStorage> pairOfDataStorage) {

        var firstDataStorage = Optional.ofNullable(pairOfDataStorage.getFirst()).orElseThrow();
        var secondDataStorage = Optional.ofNullable(pairOfDataStorage.getSecond()).orElseThrow();

        var firstParsedMap = validateStorageMap(firstDataStorage);
        var secondParsedMap = validateStorageMap(secondDataStorage);

        var matchingRecords = firstParsedMap.keySet().stream()
                .filter(secondParsedMap::containsKey)
                .count();

        long firstUnmatchedRecords;
        long secondUnmatchedRecords;
        var duplicates = firstDataStorage.getDuplicateTransactionRecords() - firstDataStorage.getDuplicateTransactionGroupRecords();
        if ((duplicates + matchingRecords) == firstDataStorage.getTotalRecords()) {
            firstUnmatchedRecords = 0;
            secondUnmatchedRecords = 0;
        } else {
            firstUnmatchedRecords = firstDataStorage.getTotalRecords() - matchingRecords;
            secondUnmatchedRecords = secondDataStorage.getTotalRecords() - matchingRecords;
        }
        var firstComparisonResult = ComparisonReport.builder()
                .fileName(firstDataStorage.getFileName())
                .totalRecords(firstDataStorage.getTotalRecords())
                .duplicateTransactionGroupRecords(firstDataStorage.getDuplicateTransactionGroupRecords())
                .duplicateTransactionRecords(firstDataStorage.getDuplicateTransactionRecords())
                .matchingRecords((int) matchingRecords)
                .unmatchedRecords((int) firstUnmatchedRecords)
                .build();
        var secondComparisonResult = ComparisonReport.builder()
                .fileName(secondDataStorage.getFileName())
                .totalRecords(secondDataStorage.getTotalRecords())
                .duplicateTransactionGroupRecords(secondDataStorage.getDuplicateTransactionGroupRecords())
                .duplicateTransactionRecords(secondDataStorage.getDuplicateTransactionRecords())
                .matchingRecords((int) matchingRecords)
                .unmatchedRecords((int) secondUnmatchedRecords)
                .build();
        return List.of(firstComparisonResult, secondComparisonResult);
    }

    private Map<DataKey, DataFile> validateStorageMap(@NonNull DataStorage dataStorage) {
        return Optional.of(dataStorage)
                .map(DataStorage::getParsedMap)
                .filter(Helper.IS_NOT_EMPTY_MAP)
                .orElseThrow();
    }
}
