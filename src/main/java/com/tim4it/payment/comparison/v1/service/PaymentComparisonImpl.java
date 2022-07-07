package com.tim4it.payment.comparison.v1.service;

import com.tim4it.payment.comparison.dto.file.DataStorage;
import com.tim4it.payment.comparison.dto.v1.response.ComparisonResponse;
import com.tim4it.payment.comparison.util.Pair;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.multipart.CompletedFileUpload;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

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
     * @return pair of comparison results {@link ComparisonResponse.ComparisonResult} with unmatched results
     * {@link ComparisonResponse.UnmatchedReport} - 2D array with rows and columns
     */
    private Mono<Pair<List<ComparisonResponse.ComparisonResult>, List<List<ComparisonResponse.UnmatchedReport>>>> getMatchUnMatchData(
            @NonNull Pair<DataStorage, DataStorage> pairOfDataStorage) {

        return Mono.zip(
                matchRecord.match(pairOfDataStorage).subscribeOn(Schedulers.boundedElastic()),
                unMatchRecord.unMatch(pairOfDataStorage).subscribeOn(Schedulers.boundedElastic()),
                Pair::new);
    }

    /**
     * Build final response for the clients
     *
     * @param pairResult pair of comparison results {@link ComparisonResponse.ComparisonResult} with unmatched results
     *                   {@link ComparisonResponse.UnmatchedReport} - 2D array with rows and columns
     * @return final {@link ComparisonResponse} result
     */
    private ComparisonResponse createResponse(
            @NonNull Pair<List<ComparisonResponse.ComparisonResult>, List<List<ComparisonResponse.UnmatchedReport>>> pairResult) {

        return ComparisonResponse.builder()
                .comparisonResults(pairResult.getFirst())
                .unmatchedReports(pairResult.getSecond())
                .build();
    }
}
