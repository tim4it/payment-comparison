package com.tim4it.payment.comparison.v1.service;

import com.tim4it.payment.comparison.dto.file.DataFile;
import com.tim4it.payment.comparison.dto.file.DataKey;
import com.tim4it.payment.comparison.dto.file.DataStorage;
import com.tim4it.payment.comparison.dto.v1.response.ComparisonResponse;
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
    public Mono<List<ComparisonResponse.ComparisonResult>> match(
            @NonNull Pair<DataStorage, DataStorage> pairOfDataStorage) {

        return Mono.fromCallable(() -> comparisonResults(pairOfDataStorage));
    }

    private List<ComparisonResponse.ComparisonResult> comparisonResults(
            @NonNull Pair<DataStorage, DataStorage> pairOfDataStorage) {

        var firstDataStorage = Optional.ofNullable(pairOfDataStorage.getFirst()).orElseThrow();
        var secondDataStorage = Optional.ofNullable(pairOfDataStorage.getSecond()).orElseThrow();

        var firstParsedMap = validateStorageMap(pairOfDataStorage.getFirst());
        var secondParsedMap = validateStorageMap(pairOfDataStorage.getSecond());

        var matchingRecords = firstParsedMap.keySet().stream()
                .filter(secondParsedMap::containsKey)
                .count();

        var firstUnmatchedRecords = firstDataStorage.getTotalRecords() - matchingRecords;
        var secondUnmatchedRecords = secondDataStorage.getTotalRecords() - matchingRecords;

        var firstComparisonResult = ComparisonResponse.ComparisonResult.builder()
                .fileName(firstDataStorage.getFileName())
                .totalRecords(firstDataStorage.getTotalRecords())
                .matchingRecords((int) matchingRecords)
                .unmatchedRecords((int) firstUnmatchedRecords)
                .build();
        var secondComparisonResult = ComparisonResponse.ComparisonResult.builder()
                .fileName(secondDataStorage.getFileName())
                .totalRecords(secondDataStorage.getTotalRecords())
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
