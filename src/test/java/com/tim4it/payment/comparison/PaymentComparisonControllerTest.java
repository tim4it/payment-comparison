package com.tim4it.payment.comparison;

import com.tim4it.payment.comparison.dto.v1.response.ComparisonResponse;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.multipart.MultipartBody;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@MicronautTest
public class PaymentComparisonControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    public void testComparisonReport() {
        var bodyTypeResponse = Argument.of(ComparisonResponse.class);

        var pairFileData1 = Helper.getCsvDataFromResources(Helper.FILE_NAME_1);
        var pairFileData2 = Helper.getCsvDataFromResources(Helper.FILE_NAME_2);

        var requestBody = MultipartBody.builder()
                .addPart("file", pairFileData1.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData1.getSecond())
                .addPart("file", pairFileData2.getFirst(), MediaType.TEXT_CSV_TYPE, pairFileData2.getSecond())
                .build();

        var request = HttpRequest.POST("/v1/upload", requestBody)
                .contentType(MediaType.MULTIPART_FORM_DATA_TYPE);
        var body = client.toBlocking().retrieve(request, bodyTypeResponse);

        assertNotNull(body);
        assertEquals(pairFileData1.getFirst(), body.getComparisonResults().iterator().next().getFileName());
        assertEquals(pairFileData2.getFirst(), body.getComparisonResults().get(1).getFileName());
        log.info("Report: {}", Helper.jsonToString(body));

    }
}