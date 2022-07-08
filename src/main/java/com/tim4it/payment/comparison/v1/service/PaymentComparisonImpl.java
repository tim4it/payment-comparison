package com.tim4it.payment.comparison.v1.service;

import com.tim4it.payment.comparison.dto.file.DataStorage;
import com.tim4it.payment.comparison.dto.v1.response.ComparisonReport;
import com.tim4it.payment.comparison.dto.v1.response.ComparisonResponse;
import com.tim4it.payment.comparison.dto.v1.response.UnmatchedReport;
import com.tim4it.payment.comparison.util.Pair;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.multipart.CompletedFileUpload;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PaymentComparisonImpl implements PaymentComparison {

    @NonNull
    final DataParser dataParser;
    @NonNull
    final MatchRecord matchRecord;
    @NonNull
    final UnMatchRecord unMatchRecord;

    @Override
    public Mono<ComparisonResponse> upload(Publisher<CompletedFileUpload> file) {
        return Flux.from(file)
                .collectList()
                .filter(this::validateFiles)
                .flatMap(this::createDataStorage)
                .flatMap(this::getMatchUnMatchData)
                .map(this::createResponse)
                .switchIfEmpty(Mono.error(new HttpStatusException(HttpStatus.BAD_REQUEST, "Bad request!")));
    }

    @Override
    public List<Map<String, String>> mapper(@NonNull List<List<UnmatchedReport>> unmatchedReports) {
        var result = new ArrayList<Map<String, String>>();
        int max = 0;
        for (var unmatchedReport : unmatchedReports) {
            var innerSize = unmatchedReport.size();
            max = Math.max(max, innerSize);
        }
        for (var unmatchedReport : unmatchedReports) {
            var mapResult = new LinkedHashMap<String, String>();
            var innerSize = unmatchedReport.size();
            for (int i = 0; i < max; i++) {
                var unMatchedReport = Optional.ofNullable(innerSize > i ? unmatchedReport.get(i) : null);
                mapResult.put("file_name_" + (i + 1), unMatchedReport
                        .map(UnmatchedReport::getFileName).orElse(StringUtils.EMPTY));
                mapResult.put("date_" + (i + 1), unMatchedReport
                        .map(UnmatchedReport::getDate).orElse(StringUtils.EMPTY));
                mapResult.put("transaction_amount_" + (i + 1), unMatchedReport.map(UnmatchedReport::getTransactionAmount)
                        .map(Object::toString).orElse(StringUtils.EMPTY));
                mapResult.put("transaction_id_" + (i + 1), unMatchedReport
                        .map(UnmatchedReport::getTransactionId).orElse(StringUtils.EMPTY));
                mapResult.put("wallet_reference_" + (i + 1), unMatchedReport
                        .map(UnmatchedReport::getWalletReference).orElse(StringUtils.EMPTY));
            }
            result.add(mapResult);
        }
        return List.copyOf(result);
    }

    /**
     * Check if we have valid files (2) with some length
     *
     * @param fileUploads list of files CompletedFileUpload
     * @return true if files are present with minimum length
     */
    private boolean validateFiles(@NonNull List<CompletedFileUpload> fileUploads) {
        return fileUploads.size() == 2 &&
                fileUploads.get(0).getSize() > 1 &&
                fileUploads.get(1).getSize() > 1;
    }

    /**
     * Parse files from CSV uploaded to the server. Generate {@link DataStorage} types from CSV. Use parallel execution,
     * since we have independent files
     *
     * @param fileUploads list of files CompletedFileUpload
     * @return data storage with types for first/second file {@link DataStorage}
     */
    private Mono<Pair<DataStorage, DataStorage>> createDataStorage(
            @NonNull List<CompletedFileUpload> fileUploads) {

        return Mono.zip(
                dataParser.parseFile(fileUploads.iterator().next()).subscribeOn(Schedulers.boundedElastic()),
                dataParser.parseFile(fileUploads.get(1)).subscribeOn(Schedulers.boundedElastic()),
                Pair::new);
    }

    /**
     * get matched and unmatched files. Use parallel execution since those formats are independent
     *
     * @param pairOfDataStorage data storage with types for first/second file {@link DataStorage}
     * @return pair of comparison results {@link ComparisonReport} with unmatched results {@link UnmatchedReport} - 2D
     * array with rows and columns
     */
    private Mono<Pair<List<ComparisonReport>, List<List<UnmatchedReport>>>> getMatchUnMatchData(
            @NonNull Pair<DataStorage, DataStorage> pairOfDataStorage) {

        return Mono.zip(
                matchRecord.match(pairOfDataStorage).subscribeOn(Schedulers.boundedElastic()),
                unMatchRecord.unMatch(pairOfDataStorage).subscribeOn(Schedulers.boundedElastic()),
                Pair::new);
    }

    /**
     * Build final response for the clients
     *
     * @param pairResult pair of comparison results {@link ComparisonReport} with unmatched results
     *                   {@link UnmatchedReport} - 2D array with rows and columns
     * @return final {@link ComparisonResponse} result
     */
    private ComparisonResponse createResponse(
            @NonNull Pair<List<ComparisonReport>, List<List<UnmatchedReport>>> pairResult) {

        return ComparisonResponse.builder()
                .comparisonReports(pairResult.getFirst())
                .unmatchedReports(pairResult.getSecond())
                .build();
    }
}
