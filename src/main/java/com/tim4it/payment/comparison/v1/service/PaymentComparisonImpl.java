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
import java.util.List;
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
    public List<com.tim4it.payment.comparison.dto.v1.view.UnmatchedReport> mapper(
            @NonNull List<List<UnmatchedReport>> unmatchedReports) {
        var result = new ArrayList<com.tim4it.payment.comparison.dto.v1.view.UnmatchedReport>();
        for (var unmatchedReport : unmatchedReports) {
            if (unmatchedReport.size() == 1) {
                result.add(mapper(unmatchedReport.get(0), null, null));
            } else if (unmatchedReport.size() == 2) {
                result.add(mapper(unmatchedReport.get(0), unmatchedReport.get(1), null));
            } else if (unmatchedReport.size() == 3) {
                result.add(mapper(unmatchedReport.get(0), unmatchedReport.get(1), unmatchedReport.get(2)));
            }
        }
        return List.copyOf(result);
    }

    private com.tim4it.payment.comparison.dto.v1.view.UnmatchedReport mapper(
            UnmatchedReport firstReport,
            UnmatchedReport secondReport,
            UnmatchedReport thirdReport) {
        var firstReportOpt = Optional.ofNullable(firstReport);
        var secondReportOpt = Optional.ofNullable(secondReport);
        var thirdReportOpt = Optional.ofNullable(thirdReport);
        return com.tim4it.payment.comparison.dto.v1.view.UnmatchedReport.builder()
                .fileName1(firstReportOpt.map(UnmatchedReport::getFileName).orElse(StringUtils.EMPTY))
                .date1(firstReportOpt.map(UnmatchedReport::getDate).orElse(StringUtils.EMPTY))
                .transactionAmount1(firstReportOpt.map(UnmatchedReport::getTransactionAmount)
                        .map(Object::toString).orElse(StringUtils.EMPTY))
                .transactionId1(firstReportOpt.map(UnmatchedReport::getTransactionId).orElse(StringUtils.EMPTY))
                .walletReference1(firstReportOpt.map(UnmatchedReport::getWalletReference).orElse(StringUtils.EMPTY))
                .fileName2(secondReportOpt.map(UnmatchedReport::getFileName).orElse(StringUtils.EMPTY))
                .date2(secondReportOpt.map(UnmatchedReport::getDate).orElse(StringUtils.EMPTY))
                .transactionAmount2(secondReportOpt.map(UnmatchedReport::getTransactionAmount)
                        .map(Object::toString).orElse(StringUtils.EMPTY))
                .transactionId2(secondReportOpt.map(UnmatchedReport::getTransactionId).orElse(StringUtils.EMPTY))
                .walletReference2(secondReportOpt.map(UnmatchedReport::getWalletReference).orElse(StringUtils.EMPTY))
                .fileName3(thirdReportOpt.map(UnmatchedReport::getFileName).orElse(StringUtils.EMPTY))
                .date3(thirdReportOpt.map(UnmatchedReport::getDate).orElse(StringUtils.EMPTY))
                .transactionAmount3(thirdReportOpt.map(UnmatchedReport::getTransactionAmount)
                        .map(Object::toString).orElse(StringUtils.EMPTY))
                .transactionId3(thirdReportOpt.map(UnmatchedReport::getTransactionId).orElse(StringUtils.EMPTY))
                .walletReference3(thirdReportOpt.map(UnmatchedReport::getWalletReference).orElse(StringUtils.EMPTY))
                .build();
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
