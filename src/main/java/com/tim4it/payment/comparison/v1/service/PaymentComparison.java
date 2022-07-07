package com.tim4it.payment.comparison.v1.service;

import com.tim4it.payment.comparison.dto.v1.response.ComparisonResponse;
import com.tim4it.payment.comparison.dto.v1.response.UnmatchedReport;
import io.micronaut.http.multipart.CompletedFileUpload;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PaymentComparison {

    /**
     * Comparison controller service
     *
     * @param file uploaded files from client - max. 2 CSV files for comparison
     * @return comparison report {@link ComparisonResponse}
     */
    Mono<ComparisonResponse> upload(Publisher<CompletedFileUpload> file);

    /**
     * Mapper maps data from 2D array {@link UnmatchedReport} to 1D array
     * {@link com.tim4it.payment.comparison.dto.v1.view.UnmatchedReport} for presentation
     *
     * @param unmatchedReports 2d array of unmatched reports
     * @return 1D array of {@link com.tim4it.payment.comparison.dto.v1.view.UnmatchedReport}
     */
    List<com.tim4it.payment.comparison.dto.v1.view.UnmatchedReport> mapper(List<List<UnmatchedReport>> unmatchedReports);
}
