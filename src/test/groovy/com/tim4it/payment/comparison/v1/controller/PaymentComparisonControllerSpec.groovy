package com.tim4it.payment.comparison.v1.controller

import com.tim4it.payment.comparison.dto.v1.response.ComparisonResponse
import com.tim4it.payment.comparison.util.HelperTest
import groovy.util.logging.Slf4j
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.client.multipart.MultipartBody
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

@Slf4j
@Unroll
@Stepwise
@MicronautTest
class PaymentComparisonControllerSpec extends Specification {

    @Inject
    @Client('/')
    HttpClient client

    def "Payment controller - happy flow"() {
        given:
        def bodyTypeResponse = Argument.of(ComparisonResponse)

        def pairFileData1 = HelperTest.getCsvDataFromResources(HelperTest.FILE_NAME_1)
        def pairFileData2 = HelperTest.getCsvDataFromResources(HelperTest.FILE_NAME_2)

        def requestBody = MultipartBody.builder()
                                       .addPart("file", pairFileData1.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData1.getSecond())
                                       .addPart("file", pairFileData2.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData2.getSecond())
                                       .build()
        var request = HttpRequest.POST("/v1/upload", requestBody)
                                 .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)

        when:
        var body = client.toBlocking().retrieve(request, bodyTypeResponse)

        then:
        body
        pairFileData1.getFirst() == body.getComparisonReports().iterator().next().getFileName()
        pairFileData2.getFirst() == body.getComparisonReports().get(1).getFileName()
        body.getComparisonReports().size() == 2
        body.getUnmatchedReports().size() == 15
        with(body.getComparisonReports().first()) {
            getFileName() == HelperTest.FILE_NAME_1
            getTotalRecords() == 305
            getMatchingRecords() == 289
            getUnmatchedRecords() == 16
            getDuplicateTransactionGroupRecords() == 1
            getDuplicateTransactionRecords() == 2
        }
        with(body.getComparisonReports().last()) {
            getFileName() == HelperTest.FILE_NAME_2
            getTotalRecords() == 306
            getMatchingRecords() == 289
            getUnmatchedRecords() == 17
            getDuplicateTransactionGroupRecords() == 2
            getDuplicateTransactionRecords() == 4
        }
        log.info("Response: {}", HelperTest.jsonToString(body))
    }

    def "Payment controller - same file - first file"() {
        given:
        def bodyTypeResponse = Argument.of(ComparisonResponse.class)

        def pairFileData1 = HelperTest.getCsvDataFromResources(HelperTest.FILE_NAME_1)
        def pairFileData2 = HelperTest.getCsvDataFromResources(HelperTest.FILE_NAME_1)

        def requestBody = MultipartBody.builder()
                                       .addPart("file", pairFileData1.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData1.getSecond())
                                       .addPart("file", pairFileData2.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData2.getSecond())
                                       .build()
        var request = HttpRequest.POST("/v1/upload", requestBody)
                                 .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)

        when:
        var body = client.toBlocking().retrieve(request, bodyTypeResponse)

        then:
        body
        pairFileData1.getFirst() == body.getComparisonReports().iterator().next().getFileName()
        pairFileData2.getFirst() == body.getComparisonReports().get(1).getFileName()
        body.getComparisonReports().size() == 2
        with(body.getComparisonReports().first()) {
            getFileName() == HelperTest.FILE_NAME_1
            getTotalRecords() == 305
            getMatchingRecords() == 304
            getUnmatchedRecords() == 0
            getDuplicateTransactionGroupRecords() == 1
            getDuplicateTransactionRecords() == 2
        }
        with(body.getComparisonReports().last()) {
            getFileName() == HelperTest.FILE_NAME_1
            getTotalRecords() == 305
            getMatchingRecords() == 304
            getUnmatchedRecords() == 0
            getDuplicateTransactionGroupRecords() == 1
            getDuplicateTransactionRecords() == 2
        }
        body.getUnmatchedReports().size() == 0
        log.info("Response: {}", HelperTest.jsonToString(body))
    }

    def "Payment controller - same file - second file"() {
        given:
        def bodyTypeResponse = Argument.of(ComparisonResponse.class)

        def pairFileData1 = HelperTest.getCsvDataFromResources(HelperTest.FILE_NAME_2)
        def pairFileData2 = HelperTest.getCsvDataFromResources(HelperTest.FILE_NAME_2)

        def requestBody = MultipartBody.builder()
                                       .addPart("file", pairFileData1.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData1.getSecond())
                                       .addPart("file", pairFileData2.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData2.getSecond())
                                       .build()
        var request = HttpRequest.POST("/v1/upload", requestBody)
                                 .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)

        when:
        var body = client.toBlocking().retrieve(request, bodyTypeResponse)

        then:
        body
        pairFileData1.getFirst() == body.getComparisonReports().iterator().next().getFileName()
        pairFileData2.getFirst() == body.getComparisonReports().get(1).getFileName()
        body.getComparisonReports().size() == 2
        with(body.getComparisonReports().first()) {
            getFileName() == HelperTest.FILE_NAME_2
            getTotalRecords() == 306
            getMatchingRecords() == 304
            getUnmatchedRecords() == 0
            getDuplicateTransactionGroupRecords() == 2
            getDuplicateTransactionRecords() == 4
        }
        with(body.getComparisonReports().last()) {
            getFileName() == HelperTest.FILE_NAME_2
            getTotalRecords() == 306
            getMatchingRecords() == 304
            getUnmatchedRecords() == 0
            getDuplicateTransactionGroupRecords() == 2
            getDuplicateTransactionRecords() == 4
        }
        body.getUnmatchedReports().size() == 0
        log.info("Response: {}", HelperTest.jsonToString(body))
    }

    def "Payment controller - swap files"() {
        given:
        def bodyTypeResponse = Argument.of(ComparisonResponse.class)

        def pairFileData1 = HelperTest.getCsvDataFromResources(HelperTest.FILE_NAME_2)
        def pairFileData2 = HelperTest.getCsvDataFromResources(HelperTest.FILE_NAME_1)

        def requestBody = MultipartBody.builder()
                                       .addPart("file", pairFileData1.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData1.getSecond())
                                       .addPart("file", pairFileData2.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData2.getSecond())
                                       .build()
        var request = HttpRequest.POST("/v1/upload", requestBody)
                                 .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)

        when:
        var body = client.toBlocking().retrieve(request, bodyTypeResponse)

        then:
        body
        pairFileData1.getFirst() == body.getComparisonReports().iterator().next().getFileName()
        pairFileData2.getFirst() == body.getComparisonReports().get(1).getFileName()
        body.getComparisonReports().size() == 2
        body.getUnmatchedReports().size() == 15
        with(body.getComparisonReports().first()) {
            getFileName() == HelperTest.FILE_NAME_2
            getTotalRecords() == 306
            getMatchingRecords() == 289
            getUnmatchedRecords() == 17
            getDuplicateTransactionGroupRecords() == 2
            getDuplicateTransactionRecords() == 4
        }
        with(body.getComparisonReports().last()) {
            getFileName() == HelperTest.FILE_NAME_1
            getTotalRecords() == 305
            getMatchingRecords() == 289
            getUnmatchedRecords() == 16
            getDuplicateTransactionGroupRecords() == 1
            getDuplicateTransactionRecords() == 2
        }
        log.info("Response: {}", HelperTest.jsonToString(body))
    }

    def "Payment controller - check normal and swapped files"() {
        given:
        def bodyTypeResponse = Argument.of(ComparisonResponse.class)

        def pairFileData1 = HelperTest.getCsvDataFromResources(HelperTest.FILE_NAME_1)
        def pairFileData2 = HelperTest.getCsvDataFromResources(HelperTest.FILE_NAME_2)

        def requestBody = MultipartBody.builder()
                                       .addPart("file", pairFileData1.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData1.getSecond())
                                       .addPart("file", pairFileData2.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData2.getSecond())
                                       .build()
        var request = HttpRequest.POST("/v1/upload", requestBody)
                                 .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)

        when:
        var first = client.toBlocking().retrieve(request, bodyTypeResponse)

        then:
        first
        first.getComparisonReports().size() == 2
        with(first.getComparisonReports().first()) {
            getFileName() == HelperTest.FILE_NAME_1
            getTotalRecords() == 305
            getMatchingRecords() == 289
            getUnmatchedRecords() == 16
            getDuplicateTransactionGroupRecords() == 1
            getDuplicateTransactionRecords() == 2
        }
        with(first.getComparisonReports().last()) {
            getFileName() == HelperTest.FILE_NAME_2
            getTotalRecords() == 306
            getMatchingRecords() == 289
            getUnmatchedRecords() == 17
            getDuplicateTransactionGroupRecords() == 2
            getDuplicateTransactionRecords() == 4
        }

        when:
        requestBody = MultipartBody.builder()
                                   .addPart("file", pairFileData2.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData2.getSecond())
                                   .addPart("file", pairFileData1.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData1.getSecond())
                                   .build()
        request = HttpRequest.POST("/v1/upload", requestBody)
                             .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
        var second = client.toBlocking().retrieve(request, bodyTypeResponse)

        then:
        second
        second.getComparisonReports().size() == 2
        with(second.getComparisonReports().first()) {
            getFileName() == HelperTest.FILE_NAME_2
            getTotalRecords() == 306
            getMatchingRecords() == 289
            getUnmatchedRecords() == 17
            getDuplicateTransactionGroupRecords() == 2
            getDuplicateTransactionRecords() == 4
        }
        with(second.getComparisonReports().last()) {
            getFileName() == HelperTest.FILE_NAME_1
            getTotalRecords() == 305
            getMatchingRecords() == 289
            getUnmatchedRecords() == 16
            getDuplicateTransactionGroupRecords() == 1
            getDuplicateTransactionRecords() == 2
        }
        first.getUnmatchedReports().stream()
             .map {
                 it.stream()
                   .filter { it.getFileName() == HelperTest.FILE_NAME_1 }
                   .count()
             }
             .reduce((a, b) -> a + b)
             .get().intValue() == first.getComparisonReports().first().getUnmatchedRecords()
        first.getUnmatchedReports().stream()
             .map {
                 it.stream()
                   .filter { it.getFileName() == HelperTest.FILE_NAME_2 }
                   .count()
             }
             .reduce((a, b) -> a + b)
             .get().intValue() == first.getComparisonReports().last().getUnmatchedRecords()

        second.getUnmatchedReports().stream()
              .map {
                  it.stream()
                    .filter { it.getFileName() == HelperTest.FILE_NAME_2 }
                    .count()
              }
              .reduce((a, b) -> a + b)
              .get().intValue() == second.getComparisonReports().first().getUnmatchedRecords()
        second.getUnmatchedReports().stream()
              .map {
                  it.stream()
                    .filter { it.getFileName() == HelperTest.FILE_NAME_1 }
                    .count()
              }
              .reduce((a, b) -> a + b)
              .get().intValue() == second.getComparisonReports().last().getUnmatchedRecords()
    }

    def "Payment controller - only one file present - bad request"() {
        given:
        def bodyTypeResponse = Argument.of(ComparisonResponse.class)

        def pairFileData1 = HelperTest.getCsvDataFromResources(HelperTest.FILE_NAME_1)

        def requestBody = MultipartBody.builder()
                                       .addPart("file", pairFileData1.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData1.getSecond())
                                       .build()
        var request = HttpRequest.POST("/v1/upload", requestBody)
                                 .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)

        when:
        client.toBlocking().retrieve(request, bodyTypeResponse)

        then:
        def e = thrown(HttpClientResponseException)
        e.getMessage() == "Bad Request"
        e.getStatus() == HttpStatus.BAD_REQUEST
    }
}
