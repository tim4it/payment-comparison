package com.tim4it.payment.comparison.v1.service;

import com.tim4it.payment.comparison.dto.file.DataStorage;
import io.micronaut.http.multipart.CompletedFileUpload;
import lombok.NonNull;
import reactor.core.publisher.Mono;

public interface DataParser {

    /**
     * Parse Csv files to right types defined in {@link DataStorage}
     *
     * @param completedFileUpload file upload data {@link CompletedFileUpload}
     * @return data storage {@link DataStorage}
     */
    Mono<DataStorage> parseFile(@NonNull CompletedFileUpload completedFileUpload);
}
