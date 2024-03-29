package com.tim4it.payment.comparison.v1.service

import com.tim4it.payment.comparison.dto.v1.response.ComparisonResponse
import com.tim4it.payment.comparison.util.HelperTest
import groovy.util.logging.Slf4j
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
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
class PaymentComparisonImplSpec extends Specification {

    @Inject
    @Client('/')
    HttpClient client

    @Inject
    PaymentComparison paymentComparison

    def "payment comparison service - test mapper"() {
        given:
        def bodyTypeResponse = Argument.of(ComparisonResponse)

        def pairFileData1 = HelperTest.getCsvDataFromResources(HelperTest.FILE_NAME_2)
        def pairFileData2 = HelperTest.getCsvDataFromResources(HelperTest.FILE_NAME_1)

        def requestBody = MultipartBody.builder()
                                       .addPart("file", pairFileData1.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData1.getSecond())
                                       .addPart("file", pairFileData2.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData2.getSecond())
                                       .build()
        def request = HttpRequest.POST("/v1/upload", requestBody)
                                 .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)

        when:
        def body = client.toBlocking().retrieve(request, bodyTypeResponse)
        def result = paymentComparison.mapper(body.getUnmatchedReports())

        then:
        result
        result.size() == 15
        with(result.first()) {
            file_name_1 == "ClientMarkoffFile20140113.csv"
            transaction_amount_1 == "-32400"
            transaction_id_1 == "0384012056029314"
            wallet_reference_1 == "P_NzUyMDI4NjRfMTM4NTM2NjE4OC44Njcy"
            file_name_2 == "PaymentMarkoffFile20140113.csv"
        }
        log.info("Response: {}", HelperTest.jsonToString(result))
    }
}
