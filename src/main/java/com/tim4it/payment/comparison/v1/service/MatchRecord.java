package com.tim4it.payment.comparison.v1.service;

import com.tim4it.payment.comparison.dto.file.DataStorage;
import com.tim4it.payment.comparison.dto.v1.response.ComparisonReport;
import com.tim4it.payment.comparison.util.Pair;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MatchRecord {

    /**
     * Calculate all the transactional matches - time complexity O(N)
     *
     * @param pairOfDataStorage pair of {@link DataStorage} - first/second file
     * @return comparison result - {@link ComparisonReport}
     */
    Mono<List<ComparisonReport>> match(Pair<DataStorage, DataStorage> pairOfDataStorage);
}
