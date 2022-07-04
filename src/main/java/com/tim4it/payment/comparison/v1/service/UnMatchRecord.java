package com.tim4it.payment.comparison.v1.service;

import com.tim4it.payment.comparison.v1.dto.file.DataStorage;
import com.tim4it.payment.comparison.v1.dto.response.ComparisonResponse;
import com.tim4it.payment.comparison.v1.util.Pair;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UnMatchRecord {

    Mono<List<List<ComparisonResponse.UnmatchedReport>>> unMatch(Pair<DataStorage, DataStorage> pairOfDataStorage);
}
