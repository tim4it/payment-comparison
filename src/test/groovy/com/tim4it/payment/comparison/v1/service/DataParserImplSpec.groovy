package com.tim4it.payment.comparison.v1.service

import com.tim4it.payment.comparison.util.HelperTest
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
class DataParserImplSpec extends Specification {

    @Inject
    DataParser dataParser

    def "Check data - with test map data"() {
        given:
        def pairFileData = HelperTest.getCsvDataFromResources(HelperTest.FILE_NAME_TEST)
        def fileUpload = Mock(CompletedFileUpload)
        fileUpload.getFilename() >> pairFileData.getFirst()
        fileUpload.getBytes() >> pairFileData.getSecond()

        when:
        def result = dataParser.parseFile(fileUpload).block()

        then:
        result.getFileName() == HelperTest.FILE_NAME_TEST
        result.getTotalRecords() == result.getParsedMap().size() +
                (result.getDuplicateTransactionRecords() - result.getDuplicateTransactionGroupRecords())
        !result.getParsedMap().isEmpty()
        result.getParsedMap().size() == 7
        result.getTotalRecords() == 15
        result.getDuplicateTransactionGroupRecords() == 3
        result.getDuplicateTransactionRecords() == 11
    }
}
