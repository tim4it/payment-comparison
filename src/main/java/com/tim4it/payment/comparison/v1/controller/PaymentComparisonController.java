package com.tim4it.payment.comparison.v1.controller;

import com.tim4it.payment.comparison.v1.dto.file.DataStorage;
import com.tim4it.payment.comparison.v1.dto.response.ComparisonResponse;
import com.tim4it.payment.comparison.v1.service.DataParser;
import com.tim4it.payment.comparison.v1.service.MatchRecord;
import com.tim4it.payment.comparison.v1.service.UnMatchRecord;
import com.tim4it.payment.comparison.v1.util.Pair;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
@OpenAPIDefinition(info = @Info(description = "Payment comparison controller V1"))
@Controller("/v1")
public class PaymentComparisonController {

    final DataParser dataParser;
    final MatchRecord matchRecord;
    final UnMatchRecord unMatchRecord;

    @Inject
    public PaymentComparisonController(@NonNull DataParser dataParser,
                                       @NonNull MatchRecord matchRecord,
                                       @NonNull UnMatchRecord unMatchRecord) {
        this.dataParser = dataParser;
        this.matchRecord = matchRecord;
        this.unMatchRecord = unMatchRecord;
    }

    @Operation(summary = "Payment comparison - compare two files and provide reports")
    @ApiResponse(responseCode = "200", description = "Payment comparison succeed!",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ComparisonResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad request")
    @Post(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON)
    public Mono<ComparisonResponse> upload(Publisher<CompletedFileUpload> file) {
        return Flux.from(file)
                .collectList()
                // check if we have only two files
                .filter(fileUploadCheck -> (fileUploadCheck.size() == 2))
                .flatMap(this::createDataStorage)
                .flatMap(this::getMatchUnMatchData)
                .map(this::createResponse)
                .switchIfEmpty(Mono.error(new HttpStatusException(HttpStatus.BAD_REQUEST, "Bad request!")));
    }

    /**
     * Parse files from CSV uploaded to the server. Generate {@link DataStorage} types from CSV. Use parallel execution,
     * since we have independent files
     *
     * @param fileUploads list of files CompletedFileUpload
     * @return data storage with types for first/second file {@link DataStorage}
     */
    private Mono<Pair<DataStorage, DataStorage>> createDataStorage(
            @NonNull List<CompletedFileUpload> fileUploads) {

        return Mono.zip(
                dataParser.parseFile(fileUploads.iterator().next()).subscribeOn(Schedulers.boundedElastic()),
                dataParser.parseFile(fileUploads.get(1)).subscribeOn(Schedulers.boundedElastic()),
                Pair::new);
    }

    /**
     * get matched and unmatched files. Use parallel execution since those formats are independent
     *
     * @param pairOfDataStorage data storage with types for first/second file {@link DataStorage}
     * @return pair of comparison results {@link ComparisonResponse.ComparisonResult} with unmatched results
     * {@link ComparisonResponse.UnmatchedReport} - 2D array with rows and columns
     */
    private Mono<Pair<List<ComparisonResponse.ComparisonResult>, List<List<ComparisonResponse.UnmatchedReport>>>> getMatchUnMatchData(
            @NonNull Pair<DataStorage, DataStorage> pairOfDataStorage) {

        return Mono.zip(
                matchRecord.match(pairOfDataStorage).subscribeOn(Schedulers.boundedElastic()),
                unMatchRecord.unMatch(pairOfDataStorage).subscribeOn(Schedulers.boundedElastic()),
                Pair::new);
    }

    /**
     * Build final response for the clients
     *
     * @param pairResult pair of comparison results {@link ComparisonResponse.ComparisonResult} with unmatched results
     *                   {@link ComparisonResponse.UnmatchedReport} - 2D array with rows and columns
     * @return final {@link ComparisonResponse} result
     */
    private ComparisonResponse createResponse(
            @NonNull Pair<List<ComparisonResponse.ComparisonResult>, List<List<ComparisonResponse.UnmatchedReport>>> pairResult) {

        return ComparisonResponse.builder()
                .comparisonResults(pairResult.getFirst())
                .unmatchedReports(pairResult.getSecond())
                .build();
    }
}
