package com.tim4it.payment.comparison.v1.service

import com.tim4it.payment.comparison.dto.file.DataStorage
import com.tim4it.payment.comparison.util.HelperTest
import com.tim4it.payment.comparison.util.Pair
import groovy.util.logging.Slf4j
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

@Slf4j
@Unroll
@Stepwise
@MicronautTest
class UnMatchRecordImplSpec extends Specification {

    @Inject
    UnMatchRecord unMatchRecord

    def "Check unmatched data"() {
        given:
        def map1 = HelperTest.parsedMap1()
        def map2 = HelperTest.parsedMap2()
        def dataStorage1 = DataStorage.builder()
                                      .fileName("first.csv")
                                      .parsedMap(map1)
                                      .build()
        def dataStorage2 = DataStorage.builder()
                                      .fileName("second.csv")
                                      .parsedMap(map2)
                                      .build()

        when:
        def result = unMatchRecord.unMatch(new Pair<>(dataStorage1, dataStorage2)).block()

        then:
        result
        result.first().size() == 4
        result[1].size() == 2
        result.last().size() == 2
        result.size() == 3

        // transaction id comparator
        with(result.first().first()) {
            getFileName() == "first.csv"
            getDate() == "2022-05-13T12:00:19Z"
            getTransactionAmount() == 114
            getTransactionId() == "101"
        }
        with(result.first()[1]) {
            getFileName() == "first.csv"
            getDate() == "2022-05-13T12:00:19Z"
            getTransactionAmount() == 114
            getTransactionId() == "101"
        }
        with(result.first()[2]) {
            getFileName() == "second.csv"
            getDate() == "2022-05-13T12:00:19Z"
            getTransactionAmount() == 114
            getTransactionId() == "101"
        }
        with(result.first().last()) {
            getFileName() == "second.csv"
            getDate() == "2022-05-13T12:00:19Z"
            getTransactionAmount() == 114
            getTransactionId() == "101"
        }
        result.first().first().getTransactionId() == result.first()[1].getTransactionId()
        result.first()[2].getTransactionId() == result.first().last().getTransactionId()

        // wallet reference comparator
        with(result[1].first()) {
            getFileName() == "first.csv"
            getDate() == "2022-05-13T10:00:12Z"
            getTransactionAmount() == 116
            getTransactionId() == "102"
            getWalletReference() == "xa102"
        }
        with(result[1].last()) {
            getFileName() == "second.csv"
            getDate() == "2022-05-13T10:00:12Z"
            getTransactionAmount() == 115
            getTransactionId() == "103"
            getWalletReference() == "xa102"
        }
        result[1].first().getWalletReference() == result[1].last().getWalletReference()

        with(result.last().first()) {
            getFileName() == "first.csv"
            getDate() == "2022-05-13T06:00:11Z"
            getTransactionAmount() == 118
            getTransactionId() == "1031"
            getWalletReference() == "xa103"
        }
        with(result.last().last()) {
            getFileName() == "second.csv"
            getDate() == "2022-05-13T06:10:11Z"
            getTransactionAmount() == 119
            getTransactionId() == "109"
            getWalletReference() == "xa111"
        }
    }

    def "Check Levenshtein distance"() {
        given:
        def map1 = HelperTest.parsedMap1()
        def map2 = HelperTest.parsedMap2()

        when:
        def result = ((UnMatchRecordImpl) unMatchRecord)
                .getLevenshteinTransactionIdCompare(map1.values().toList(), map2.values().toList())

        then:
        result.size() == 5
        result.first().getFirst().getTransactionId() == "100"
        result.first().getSecond().getTransactionId() == "100"
        result[1].getFirst().getTransactionId() == "101"
        result[1].getSecond().getTransactionId() == "101"
        result[2].getFirst().getTransactionId() == "102"
        result[2].getSecond().getTransactionId() == "103"
        result[3].getFirst().getTransactionId() == "1031"
        result[3].getSecond().getTransactionId() == "109"
        result.last().getFirst().getTransactionId() == "104"
        result.last().getSecond().getTransactionId() == "104"
    }
}
