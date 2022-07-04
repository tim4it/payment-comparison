package com.tim4it.payment.comparison.v1.service;

import com.tim4it.payment.comparison.v1.dto.file.DataStorage;
import com.tim4it.payment.comparison.v1.dto.response.ComparisonResponse;
import com.tim4it.payment.comparison.v1.util.Pair;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Singleton
public class UnMatchRecordImpl implements UnMatchRecord {

    @Override
    public Mono<List<List<ComparisonResponse.UnmatchedReport>>> unMatch(
            @NonNull Pair<DataStorage, DataStorage> pairOfDataStorage) {
        return Mono.fromCallable(() ->
                List.of(List.of(ComparisonResponse.UnmatchedReport.builder().build())));
    }
}
