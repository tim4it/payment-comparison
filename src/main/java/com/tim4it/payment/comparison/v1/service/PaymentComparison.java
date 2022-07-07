package com.tim4it.payment.comparison.v1.service;

import com.tim4it.payment.comparison.dto.v1.response.ComparisonResponse;
import io.micronaut.http.multipart.CompletedFileUpload;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public interface PaymentComparison {

    /**
     * Comparison controller service
     *
     * @param file uploaded files from client - max. 2 CSV files for comparison
     * @return comparison report {@link ComparisonResponse}
     */
    Mono<ComparisonResponse> upload(Publisher<CompletedFileUpload> file);
}
