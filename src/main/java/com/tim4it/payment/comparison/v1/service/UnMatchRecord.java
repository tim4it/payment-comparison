package com.tim4it.payment.comparison.v1.service;

import com.tim4it.payment.comparison.dto.file.DataStorage;
import com.tim4it.payment.comparison.dto.v1.response.UnmatchedReport;
import com.tim4it.payment.comparison.util.Pair;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UnMatchRecord {

    /**
     * Create report for unmatched data - sorted by transactional closeness. Time complexity is slower (worst O(n2)),
     * but we have few data to process, so it is OK. We can speed up process - prepare data in map and compare with map.
     * This will take algorithm time complexity down to O(n) - except for Levenshtein
     *
     * @param pairOfDataStorage pair of {@link DataStorage} - first/second file
     * @return row/column - matrix data of unmatched data
     */
    Mono<List<List<UnmatchedReport>>> unMatch(Pair<DataStorage, DataStorage> pairOfDataStorage);
}
