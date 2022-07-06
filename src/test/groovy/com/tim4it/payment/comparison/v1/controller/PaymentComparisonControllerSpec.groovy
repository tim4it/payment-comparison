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
        pairFileData1.getFirst() == body.getComparisonResults().iterator().next().getFileName()
        pairFileData2.getFirst() == body.getComparisonResults().get(1).getFileName()
        body.getComparisonResults().size() == 2
        body.getUnmatchedReports().size() == 15
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
        pairFileData1.getFirst() == body.getComparisonResults().iterator().next().getFileName()
        pairFileData2.getFirst() == body.getComparisonResults().get(1).getFileName()
        body.getComparisonResults().size() == 2
        body.getUnmatchedReports().size() == 15
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
        first.getComparisonResults().size() == 2
        with(first.getComparisonResults().first()) {
            getFileName() == HelperTest.FILE_NAME_1
            getTotalRecords() == 305
            getMatchingRecords() == 289
            getUnmatchedRecords() == 16
            getDuplicateTransactionRecords() == 1
        }
        with(first.getComparisonResults().last()) {
            getFileName() == HelperTest.FILE_NAME_2
            getTotalRecords() == 306
            getMatchingRecords() == 289
            getUnmatchedRecords() == 17
            getDuplicateTransactionRecords() == 2
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
        second.getComparisonResults().size() == 2
        with(second.getComparisonResults().first()) {
            getFileName() == HelperTest.FILE_NAME_2
            getTotalRecords() == 306
            getMatchingRecords() == 289
            getUnmatchedRecords() == 17
            getDuplicateTransactionRecords() == 2
        }
        with(second.getComparisonResults().last()) {
            getFileName() == HelperTest.FILE_NAME_1
            getTotalRecords() == 305
            getMatchingRecords() == 289
            getUnmatchedRecords() == 16
            getDuplicateTransactionRecords() == 1
        }
        first.getUnmatchedReports().stream()
             .map {
                 it.stream()
                   .filter { it.getFileName() == HelperTest.FILE_NAME_1 }
                   .count()
             }
             .reduce((a, b) -> a + b)
             .get().intValue() == first.getComparisonResults().first().getUnmatchedRecords()
        first.getUnmatchedReports().stream()
             .map {
                 it.stream()
                   .filter { it.getFileName() == HelperTest.FILE_NAME_2 }
                   .count()
             }
             .reduce((a, b) -> a + b)
             .get().intValue() == first.getComparisonResults().last().getUnmatchedRecords()

        second.getUnmatchedReports().stream()
              .map {
                  it.stream()
                    .filter { it.getFileName() == HelperTest.FILE_NAME_2 }
                    .count()
              }
              .reduce((a, b) -> a + b)
              .get().intValue() == second.getComparisonResults().first().getUnmatchedRecords()
        second.getUnmatchedReports().stream()
              .map {
                  it.stream()
                    .filter { it.getFileName() == HelperTest.FILE_NAME_1 }
                    .count()
              }
              .reduce((a, b) -> a + b)
              .get().intValue() == second.getComparisonResults().last().getUnmatchedRecords()
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
