package com.tim4it.payment.comparison.v1.service;

import com.tim4it.payment.comparison.dto.file.DataFile;
import com.tim4it.payment.comparison.dto.file.DataKey;
import com.tim4it.payment.comparison.dto.file.DataStorage;
import com.tim4it.payment.comparison.dto.v1.response.UnmatchedReport;
import com.tim4it.payment.comparison.util.Pair;
import com.tim4it.payment.comparison.util.ReferenceCompare;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Singleton
public class UnMatchRecordImpl implements UnMatchRecord {

    @Override
    public Mono<List<List<UnmatchedReport>>> unMatch(
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
     * Get unmatched data
     *
     * @param first  first data map
     * @param second second data map
     * @return list of unmatched transaction data {@link DataFile}
     */
    private List<DataFile> getUnmatchedData(@NonNull Map<DataKey, DataFile> first,
                                            @NonNull Map<DataKey, DataFile> second) {
        return first.entrySet().stream()
                .filter(mapEntry -> !second.containsKey(mapEntry.getKey()))
                .map(Map.Entry::getValue)
                // mutable here - we want to remove already parsed report,
                // because we do it in stage with different comparison methods
                .collect(Collectors.toList());
    }

    /**
     * Create response data - sorting: first compare transaction ids, then compare wallet references and at the end
     * compare transaction ids using Levenshtein.
     *
     * @param pairOfDataStorage pair of data storage
     * @param pairUnmatched     pair unmatched data
     * @return valid response for unmatched data report
     */
    private List<List<UnmatchedReport>> unmatchedResponseData(
            @NonNull Pair<DataStorage, DataStorage> pairOfDataStorage,
            @NonNull Pair<List<DataFile>, List<DataFile>> pairUnmatched) {

        var firstDataFile = pairUnmatched.getFirst();
        var secondDataFile = pairUnmatched.getSecond();

        // compare with transaction id and create report
        var transactionIdCompare = getReferenceCompare(firstDataFile, secondDataFile, ReferenceCompare.TRANSACTION_ID);
        var transactionIdReport = createReport(pairOfDataStorage, transactionIdCompare);

        // remove transaction id data that was already compared
        firstDataFile.removeAll(
                transactionIdCompare.stream()
                        .map(Pair::getFirst)
                        .collect(Collectors.toUnmodifiableList()));
        secondDataFile.removeAll(
                transactionIdCompare.stream()
                        .map(Pair::getSecond)
                        .collect(Collectors.toUnmodifiableList()));

        // compare with wallet reference and create report
        var walletReferenceCompare = getReferenceCompare(firstDataFile, secondDataFile, ReferenceCompare.WALLET_REFERENCE);
        var walletReferenceReport = createReport(pairOfDataStorage, walletReferenceCompare);

        // remove wallet reference data that was already compared
        firstDataFile.removeAll(
                walletReferenceCompare.stream()
                        .map(Pair::getFirst)
                        .collect(Collectors.toUnmodifiableList()));
        secondDataFile.removeAll(
                walletReferenceCompare.stream()
                        .map(Pair::getSecond)
                        .collect(Collectors.toUnmodifiableList()));

        // compare transaction id with Levenshtein and create report
        var levenshteinTransactionIdCompare = getLevenshteinTransactionIdCompare(firstDataFile, secondDataFile);
        var levenshteinTransactionIdReport = createReport(pairOfDataStorage, levenshteinTransactionIdCompare);

        return Stream.of(transactionIdReport,
                        walletReferenceReport,
                        levenshteinTransactionIdReport)
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * First compare by transaction id - transaction id matches - this is most important
     *
     * @param firstDataFiles  first data files {@link DataFile}
     * @param secondDataFiles second data files {@link DataFile}
     * @return compare by transaction id
     */
    private List<Pair<DataFile, DataFile>> getReferenceCompare(@NonNull List<DataFile> firstDataFiles,
                                                               @NonNull List<DataFile> secondDataFiles,
                                                               @NonNull ReferenceCompare referenceCompare) {
        var transactionIdCompare = new ArrayList<Pair<DataFile, DataFile>>();
        for (var firstDataFile : firstDataFiles) {
            for (var secondDataFile : secondDataFiles) {
                if (referenceCompare.equals(ReferenceCompare.TRANSACTION_ID) &&
                        firstDataFile.getTransactionId().equals(secondDataFile.getTransactionId())) {
                    transactionIdCompare.add(new Pair<>(firstDataFile, secondDataFile));
                    break;
                } else if (referenceCompare.equals(ReferenceCompare.WALLET_REFERENCE) &&
                        firstDataFile.getWalletReference().equals(secondDataFile.getWalletReference())) {
                    transactionIdCompare.add(new Pair<>(firstDataFile, secondDataFile));
                    break;
                }
            }
        }
        return List.copyOf(transactionIdCompare);
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
     * Create report for data
     *
     * @param pairOfDataStorage pair of data storage
     * @param comparatorData    transaction id data
     */
    private List<List<UnmatchedReport>> createReport(@NonNull Pair<DataStorage, DataStorage> pairOfDataStorage,
                                                     @NonNull List<Pair<DataFile, DataFile>> comparatorData) {
        return comparatorData.stream()
                .map(pair -> createReport(pairOfDataStorage, pair))
                .collect(Collectors.toUnmodifiableList());
    }

    private List<UnmatchedReport> createReport(
            @NonNull Pair<DataStorage, DataStorage> pairOfDataStorage,
            @NonNull Pair<DataFile, DataFile> pair) {

        var firstDataReport = createReport(pairOfDataStorage.getFirst(), pair.getFirst());
        var secondDataReport = createReport(pairOfDataStorage.getSecond(), pair.getSecond());

        return Stream.of(firstDataReport, secondDataReport)
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableList());
    }

    private List<UnmatchedReport> createReport(@NonNull DataStorage dataStorage,
                                               @NonNull DataFile firstData) {
        var transactionalAmounts = firstData.getTransactionAmount();
        var transactionalAmountLength = transactionalAmounts.size();
        if (transactionalAmountLength > 1) {
            return IntStream.range(0, transactionalAmountLength)
                    .mapToObj(index -> createReport(
                            dataStorage.getFileName(),
                            firstData.toBuilder()
                                    .transactionAmount(List.of(transactionalAmounts.get(index)))
                                    .build()))
                    .collect(Collectors.toUnmodifiableList());
        } else {
            return List.of(createReport(dataStorage.getFileName(), firstData));
        }
    }

    private UnmatchedReport createReport(@NonNull String fileName,
                                         @NonNull DataFile dataFile) {
        return UnmatchedReport.builder()
                .fileName(fileName)
                .date(dataFile.getTransactionDate().toString())
                .transactionAmount(dataFile.getTransactionAmount().iterator().next())
                .transactionId(dataFile.getTransactionId())
                .walletReference(dataFile.getWalletReference())
                .build();
    }
}
