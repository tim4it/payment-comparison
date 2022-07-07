package com.tim4it.payment.comparison.v1.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tim4it.payment.comparison.dto.v1.response.ComparisonResponse;
import com.tim4it.payment.comparison.dto.v1.view.ComparisonView;
import com.tim4it.payment.comparison.v1.service.PaymentComparison;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.views.View;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Map;

@Controller("/views")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PaymentComparisonViewController {

    @NonNull
    final PaymentComparison paymentComparison;

    @View("comparison")
    @Post(value = "/comparison", consumes = MediaType.MULTIPART_FORM_DATA)
    public Mono<HttpResponse<Map<String, Object>>> upload(Publisher<CompletedFileUpload> file) {
        return paymentComparison.upload(file)
                .map(this::createViewResponse)
                .map(HttpResponse::ok);
    }

    @View("comparison")
    @Get("/comparison")
    public HttpResponse<?> empty() {
        return HttpResponse.ok();
    }

    /**
     * create mapped view response
     *
     * @param comparisonResponse main comparison response {@link ComparisonResponse}
     * @return mapped response - converted to map for html template rendering
     */
    private Map<String, Object> createViewResponse(@NonNull ComparisonResponse comparisonResponse) {
        var comparisonReport = comparisonResponse.getComparisonReports();
        var unmatchedReport = paymentComparison.mapper(comparisonResponse.getUnmatchedReports());
        var result = ComparisonView.builder()
                .comparisonReports(comparisonReport)
                .unmatchedReports(unmatchedReport)
                .build();
        return new ObjectMapper().convertValue(
                result, new TypeReference<>() {
                });
    }
}
