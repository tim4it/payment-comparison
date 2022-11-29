package com.tim4it.payment.comparison;

import com.tim4it.payment.comparison.exception.BadRequestException;
import com.tim4it.payment.comparison.exception.ExceptionHandler;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Infrastructure;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Factory
public class BeanFactory {

    @Bean
    @Singleton
    @Infrastructure
    @Primary
    @Replaces(io.micronaut.http.server.exceptions.ExceptionHandler.class)
    ExceptionHandler constructExceptionMapper() {
        return ExceptionHandler.defaults()
                .add(BadRequestException.class, HttpStatus.BAD_REQUEST);
    }
}
