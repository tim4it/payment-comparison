package com.tim4it.payment.comparison.v1.service;

import com.tim4it.payment.comparison.dto.file.DataFile;
import com.tim4it.payment.comparison.dto.file.DataKey;
import com.tim4it.payment.comparison.dto.file.DataStorage;
import com.tim4it.payment.comparison.dto.v1.response.ComparisonResponse;
import com.tim4it.payment.comparison.util.Pair;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Singleton
public class UnMatchRecordImpl implements UnMatchRecord {

    @Override
    public Mono<List<List<ComparisonResponse.UnmatchedReport>>> unMatch(
            @NonNull Pair<DataStorage, DataStorage> pairOfDataStorage) {
        return Mono.zip(
                        Mono.fromCallable(() -> getUnmatchedData(
                                        pairOfDataStorage.getFirst().getParsedMap(),
                                        pairOfDataStorage.getSecond().getParsedMap()))
                                .subscribeOn(Schedulers.boundedElastic()),
                        Mono.fromCallable(() -> getUnmatchedData(
                                pairOfDataStorage.getSecond().getParsedMap(),
                                pairOfDataStorage.getFirst().getParsedMap())),
                        Pair::new)
                .map(pairUnmatched -> unmatchedResponseData(pairOfDataStorage, pairUnmatched));
    }

    /**
     * Create response data - sorting: first compare transaction ids, then compare wallet references and at the end
     * compare transaction ids using Levenshtein.
     *
     * @param pairOfDataStorage pair of data storage
     * @param pairUnmatched     pair unmatched data
     * @return valid response for unmatched data report
     */
    private List<List<ComparisonResponse.UnmatchedReport>> unmatchedResponseData(
            @NonNull Pair<DataStorage, DataStorage> pairOfDataStorage,
            @NonNull Pair<List<DataFile>, List<DataFile>> pairUnmatched) {

        var response = new ArrayList<List<ComparisonResponse.UnmatchedReport>>();

        var firstDataFile = pairUnmatched.getFirst();
        var secondDataFile = pairUnmatched.getSecond();

        var transactionIdCompare = getTransactionIdCompare(firstDataFile, secondDataFile);
        createTransactionIdReport(pairOfDataStorage, transactionIdCompare, response);

        firstDataFile.removeAll(transactionIdCompare.stream()
                .map(Pair::getFirst)
                .collect(Collectors.toUnmodifiableList()));
        secondDataFile.removeAll(transactionIdCompare.stream()
                .map(Pair::getSecond)
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableList()));

        var walletReferenceCompare = getWalletReferenceCompare(firstDataFile, secondDataFile);
        createWalletReferenceReport(pairOfDataStorage, walletReferenceCompare, response);

        firstDataFile.removeAll(walletReferenceCompare.stream()
                .map(Pair::getFirst)
                .collect(Collectors.toUnmodifiableList()));
        secondDataFile.removeAll(walletReferenceCompare.stream()
                .map(Pair::getSecond)
                .collect(Collectors.toUnmodifiableList()));

        var levenshteinTransactionIdCompare = getLevenshteinTransactionIdCompare(firstDataFile, secondDataFile);
        createLevenshteinTransactionIdReport(pairOfDataStorage, levenshteinTransactionIdCompare, response);

        return response;
    }

    /**
     * First compare by transaction id - transaction id matches - this is most important
     *
     * @param firstDataFiles  first data files {@link DataFile}
     * @param secondDataFiles second data files {@link DataFile}
     * @return compare by transaction id
     */
    private List<Pair<DataFile, List<DataFile>>> getTransactionIdCompare(@NonNull List<DataFile> firstDataFiles,
                                                                         @NonNull List<DataFile> secondDataFiles) {
        var transactionIdCompare = new ArrayList<Pair<DataFile, List<DataFile>>>();
        for (var firstDataFile : firstDataFiles) {
            var secondTmpDataFiles = new ArrayList<DataFile>();
            var firstTransactionId = firstDataFile.getTransactionId();
            for (var secondDataFile : secondDataFiles) {
                if (firstTransactionId.equals(secondDataFile.getTransactionId())) {
                    secondTmpDataFiles.add(secondDataFile);
                }
            }
            if (!secondTmpDataFiles.isEmpty()) {
                transactionIdCompare.add(new Pair<>(firstDataFile, secondTmpDataFiles));
            }
        }
        return List.copyOf(transactionIdCompare);
    }

    /**
     * Create transaction id report
     *
     * @param pairOfDataStorage    pair of data storage
     * @param transactionIdCompare transaction id data
     * @param response             response to append result to
     */
    private void createTransactionIdReport(@NonNull Pair<DataStorage, DataStorage> pairOfDataStorage,
                                           @NonNull List<Pair<DataFile, List<DataFile>>> transactionIdCompare,
                                           @NonNull List<List<ComparisonResponse.UnmatchedReport>> response) {
        transactionIdCompare.stream()
                .map(pair -> createTransactionIdReport(pairOfDataStorage, pair))
                .forEach(response::add);
    }

    private List<ComparisonResponse.UnmatchedReport> createTransactionIdReport(
            @NonNull Pair<DataStorage, DataStorage> pairOfDataStorage,
            @NonNull Pair<DataFile, List<DataFile>> pair) {
        var transactionIdReport = new ArrayList<ComparisonResponse.UnmatchedReport>();
        var firstData = pair.getFirst();
        transactionIdReport.add(
                ComparisonResponse.UnmatchedReport.builder()
                        .fileName(pairOfDataStorage.getFirst().getFileName())
                        .date(firstData.getTransactionDate().toString())
                        .transactionAmount(firstData.getTransactionAmount().iterator().next())
                        .transactionId(firstData.getTransactionId())
                        .walletReference(firstData.getWalletReference())
                        .build());
        var secondData = pair.getSecond();
        secondData.forEach(dataFile ->
                transactionIdReport.add(
                        ComparisonResponse.UnmatchedReport.builder()
                                .fileName(pairOfDataStorage.getSecond().getFileName())
                                .date(dataFile.getTransactionDate().toString())
                                .transactionAmount(dataFile.getTransactionAmount().iterator().next())
                                .transactionId(dataFile.getTransactionId())
                                .walletReference(dataFile.getWalletReference())
                                .build()));
        return List.copyOf(transactionIdReport);
    }

    /**
     * Second compare by wallet reference - wallet reference matches
     *
     * @param firstDataFiles  first data files {@link DataFile}
     * @param secondDataFiles second data files {@link DataFile}
     * @return compare by wallet reference
     */
    private List<Pair<DataFile, DataFile>> getWalletReferenceCompare(@NonNull List<DataFile> firstDataFiles,
                                                                     @NonNull List<DataFile> secondDataFiles) {
        var walletReferenceCompare = new ArrayList<Pair<DataFile, DataFile>>();
        for (var firstDataFile : firstDataFiles) {
            for (var secondDataFile : secondDataFiles) {
                if (firstDataFile.getWalletReference().equals(secondDataFile.getWalletReference())) {
                    walletReferenceCompare.add(new Pair<>(firstDataFile, secondDataFile));
                    break;
                }
            }
        }
        return List.copyOf(walletReferenceCompare);
    }

    /**
     * Create wallet reference report
     *
     * @param pairOfDataStorage      pair of data storage
     * @param walletReferenceCompare wallet reference compare data
     * @param response               response to append result to
     */
    private void createWalletReferenceReport(@NonNull Pair<DataStorage, DataStorage> pairOfDataStorage,
                                             @NonNull List<Pair<DataFile, DataFile>> walletReferenceCompare,
                                             @NonNull List<List<ComparisonResponse.UnmatchedReport>> response) {
        walletReferenceCompare.stream()
                .map(pair -> createReport(pairOfDataStorage, pair))
                .forEach(response::add);
    }

    /**
     * Lats comparator: Levenshtein distance is a string metric for measuring the difference between two sequences.
     * Informally, the Levenshtein distance between two words is the minimum number of single-character edits required
     * to change one word into the other. Apply this technics to transaction id.
     *
     * @param firstDataFiles  first data files {@link DataFile}
     * @param secondDataFiles second data files {@link DataFile}
     * @return compare transaction id with Levenshtein distance
     */
    private List<Pair<DataFile, DataFile>> getLevenshteinTransactionIdCompare(@NonNull List<DataFile> firstDataFiles,
                                                                              @NonNull List<DataFile> secondDataFiles) {
        var levenshteinTransactionIdCompare = new ArrayList<Pair<DataFile, DataFile>>();
        for (var firstDataFile : firstDataFiles) {
            DataFile secondTmpDataFile = null;
            int distance = Integer.MAX_VALUE;
            for (var secondDataFile : secondDataFiles) {
                var distanceLevenshtein = StringUtils
                        .getLevenshteinDistance(firstDataFile.getTransactionId(), secondDataFile.getTransactionId());
                if (distanceLevenshtein < distance) {
                    secondTmpDataFile = secondDataFile;
                    distance = distanceLevenshtein;
                }
            }
            if (secondTmpDataFile != null) {
                levenshteinTransactionIdCompare.add(new Pair<>(firstDataFile, secondTmpDataFile));
                secondDataFiles.remove(secondTmpDataFile);
            }
        }
        return List.copyOf(levenshteinTransactionIdCompare);
    }

    /**
     * Create  Levenshtein transaction id report
     *
     * @param pairOfDataStorage               pair of data storage
     * @param levenshteinTransactionIdCompare levenshtein transaction id compare data
     * @param response                        response to append result to
     */
    private void createLevenshteinTransactionIdReport(
            @NonNull Pair<DataStorage, DataStorage> pairOfDataStorage,
            @NonNull List<Pair<DataFile, DataFile>> levenshteinTransactionIdCompare,
            @NonNull List<List<ComparisonResponse.UnmatchedReport>> response) {
        levenshteinTransactionIdCompare.stream()
                .map(pair -> createReport(pairOfDataStorage, pair))
                .forEach(response::add);
    }

    private List<ComparisonResponse.UnmatchedReport> createReport(
            @NonNull Pair<DataStorage, DataStorage> pairOfDataStorage,
            @NonNull Pair<DataFile, DataFile> pair) {
        var firstData = pair.getFirst();
        var firstDataResponse = ComparisonResponse.UnmatchedReport.builder()
                .fileName(pairOfDataStorage.getFirst().getFileName())
                .date(firstData.getTransactionDate().toString())
                .transactionAmount(firstData.getTransactionAmount().iterator().next())
                .transactionId(firstData.getTransactionId())
                .walletReference(firstData.getWalletReference())
                .build();
        var secondData = pair.getSecond();
        var secondDataResponse = ComparisonResponse.UnmatchedReport.builder()
                .fileName(pairOfDataStorage.getSecond().getFileName())
                .date(secondData.getTransactionDate().toString())
                .transactionAmount(secondData.getTransactionAmount().iterator().next())
                .transactionId(secondData.getTransactionId())
                .walletReference(secondData.getWalletReference())
                .build();
        return List.of(firstDataResponse, secondDataResponse);
    }

    /**
     * Get unmatched data
     *
     * @param first  first data map
     * @param second second data map
     * @return list of unmatched transaction data {@link DataFile}
     */
    private List<DataFile> getUnmatchedData(@NonNull Map<DataKey, DataFile> first,
                                            @NonNull Map<DataKey, DataFile> second) {
        var firstDataFile = new ArrayList<DataFile>();
        first.keySet().stream()
                .filter(dataKey -> !second.containsKey(dataKey))
                .forEach(dataKeyNotFound -> getUnmatchedData(first, dataKeyNotFound, firstDataFile));
        return firstDataFile;
    }

    private void getUnmatchedData(@NonNull Map<DataKey, DataFile> first,
                                  @NonNull DataKey dataKeyNotFound,
                                  @NonNull List<DataFile> firstDataFile) {
        var dataFile = Optional.ofNullable(first.get(dataKeyNotFound)).orElseThrow();
        var transactionAmount = dataFile.getTransactionAmount();
        var transactionAmountLength = transactionAmount.size();
        if (transactionAmountLength > 1) {
            IntStream.range(0, transactionAmountLength)
                    .boxed()
                    .forEach(index -> firstDataFile.add(
                            dataFile.toBuilder()
                                    .transactionAmount(List.of(transactionAmount.get(index)))
                                    .build()));
        } else {
            firstDataFile.add(dataFile);
        }
    }
}
