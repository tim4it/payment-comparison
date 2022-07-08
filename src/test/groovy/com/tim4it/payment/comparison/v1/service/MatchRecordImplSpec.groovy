package com.tim4it.payment.comparison.v1.service

import com.tim4it.payment.comparison.dto.file.DataFile
import com.tim4it.payment.comparison.dto.file.DataKey
import com.tim4it.payment.comparison.dto.file.DataStorage
import com.tim4it.payment.comparison.util.HelperTest
import com.tim4it.payment.comparison.util.Pair
import groovy.util.logging.Slf4j
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

@Slf4j
@Unroll
@Stepwise
@MicronautTest
class MatchRecordImplSpec extends Specification {

    @Inject
    DataParser dataParser

    @Inject
    MatchRecord matchRecord

    def "Check data - with test map data"() {
        given:
        def map1 = HelperTest.parsedMap1()
        def map2 = HelperTest.parsedMap2()
        def dataStorage1 = createDataStorage("first.csv", map1)
        def dataStorage2 = createDataStorage("second.csv", map2)

        when:
        def result = matchRecord.match(new Pair<>(dataStorage1, dataStorage2)).block()

        then:
        result
        result.size() == 2
        with(result.first()) {
            fileName == "first.csv"
            totalRecords == 6
            matchingRecords == 2
            unmatchedRecords == 4
            duplicateTransactionGroupRecords == 1
            duplicateTransactionRecords == 2
        }
        with(result.last()) {
            fileName == "second.csv"
            totalRecords == 6
            matchingRecords == 2
            unmatchedRecords == 4
            duplicateTransactionGroupRecords == 1
            duplicateTransactionRecords == 2
        }
    }

    def "Check data - with test map data - same data"() {
        given:
        def map1 = HelperTest.parsedMap1()
        def dataStorage1 = createDataStorage("first.csv", map1)
        def dataStorage2 = createDataStorage("second.csv", map1)

        when:
        def result = matchRecord.match(new Pair<>(dataStorage1, dataStorage2)).block()

        then:
        result
        result.size() == 2
        with(result.first()) {
            fileName == "first.csv"
            totalRecords == 6
            matchingRecords == 5
            unmatchedRecords == 0
            duplicateTransactionGroupRecords == 1
            duplicateTransactionRecords == 2
        }
        with(result.last()) {
            fileName == "second.csv"
            totalRecords == 6
            matchingRecords == 5
            unmatchedRecords == 0
            duplicateTransactionGroupRecords == 1
            duplicateTransactionRecords == 2
        }
    }

    def "Check comparison matches - test file"() {
        given:
        def parsedFile = parseTestFile()

        when:
        def result = matchRecord.match(new Pair<>(parsedFile, parsedFile)).block()

        then:
        result
        result.size() == 2
        with(result.first()) {
            fileName == "TestMarkoff.csv"
            totalRecords == 15
            matchingRecords == 7
            unmatchedRecords == 0
            duplicateTransactionGroupRecords == 3
            duplicateTransactionRecords == 11
        }
        with(result.last()) {
            fileName == "TestMarkoff.csv"
            totalRecords == 15
            matchingRecords == 7
            unmatchedRecords == 0
            duplicateTransactionGroupRecords == 3
            duplicateTransactionRecords == 11
        }
    }

    def createDataStorage(String fileName, Map<DataKey, DataFile> map,
                          int duplicateTransactionGroupRecords = 1, int duplicateTransactionRecords = 2) {
        DataStorage.builder()
                   .fileName(fileName)
                   .parsedMap(map)
                   .totalRecords(map.size() + 1)
                   .duplicateTransactionGroupRecords(duplicateTransactionGroupRecords)
                   .duplicateTransactionRecords(duplicateTransactionRecords)
                   .build()
    }

    def parseTestFile() {
        def pairFileData = HelperTest.getCsvDataFromResources(HelperTest.FILE_NAME_TEST)
        def fileUpload = Mock(CompletedFileUpload)
        fileUpload.getFilename() >> pairFileData.getFirst()
        fileUpload.getBytes() >> pairFileData.getSecond()
        dataParser.parseFile(fileUpload).block()
    }
}
