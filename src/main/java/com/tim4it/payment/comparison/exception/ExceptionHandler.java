package com.tim4it.payment.comparison.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.exceptions.HttpStatusException;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Exception mapper.
 */
@Slf4j
@EqualsAndHashCode(of = "mapping")
@Produces(MediaType.APPLICATION_JSON)
public class ExceptionHandler implements io.micronaut.http.server.exceptions.ExceptionHandler<Throwable, HttpResponse<?>>, Ordered {

    private final Map<Class<? extends Throwable>, HttpStatus> mapping;

    private ExceptionHandler(@NonNull Map<Class<? extends Throwable>, HttpStatus> mappings) {
        this.mapping = Collections.unmodifiableMap(new LinkedHashMap<>(mappings));
    }

    private static Map<Class<? extends Throwable>, HttpStatus> defaultExceptionMappings() {
        var map = new LinkedHashMap<Class<? extends Throwable>, HttpStatus>();
        map.put(ConversionErrorException.class, HttpStatus.BAD_REQUEST);
        map.put(JsonProcessingException.class, HttpStatus.BAD_REQUEST);
        map.put(ConstraintViolationException.class, HttpStatus.BAD_REQUEST);
        map.put(RuntimeException.class, HttpStatus.INTERNAL_SERVER_ERROR);
        map.put(IllegalStateException.class, HttpStatus.INTERNAL_SERVER_ERROR);
        return map;
    }

    /**
     * Returns exception mapper with default exception mappings
     *
     * @return exception mapper
     */
    public static ExceptionHandler defaults() {
        return new ExceptionHandler(defaultExceptionMappings());
    }

    /**
     * Creates new exception mapper <b>by appending new exception mapping</b> (mapping is placed at the end of the
     * list).
     *
     * @param exceptionClass exception class
     * @param status         http status.
     * @return exception mapper
     */
    public ExceptionHandler add(@NonNull Class<? extends Throwable> exceptionClass, @NonNull HttpStatus status) {
        if (mapping.containsKey(exceptionClass)) {
            return this;
        }

        var newMap = new LinkedHashMap<>(this.mapping);
        newMap.put(exceptionClass, status);
        return new ExceptionHandler(newMap);
    }

    @Override
    public HttpResponse<?> handle(HttpRequest request, Throwable exception) {
        var status = getStatus(exception);
        var statusMessage = status.getReason();

        if (status.getCode() < 500) {
            log.warn("{} {} error: {}, {}", request.getMethod(), request.getUri(), status, getMessage(exception), exception);
            return createResponse(status, getMessage(exception));
        }
        log.error("{} {} error: {}", request.getMethod(), request.getUri(), status, exception);
        return createResponse(status, statusMessage);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(mappings=" + mapping.size() + ")";
    }

    private HttpResponse<Map<String, String>> createResponse(HttpStatus status, String errorMsg) {
        return HttpResponse
                .<Map<String, String>>status(status)
                .characterEncoding(UTF_8)
                .body(Map.of("error", errorMsg));
    }

    private HttpStatus getStatus(Throwable exception) {
        if (exception instanceof HttpStatusException) {
            return ((HttpStatusException) exception).getStatus();
        }

        return Optional.ofNullable(this.mapping.get(exception.getClass()))
                .or(() -> scanExceptionMap(exception.getClass()))
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Optional<HttpStatus> scanExceptionMap(Class<? extends Throwable> exceptionClass) {
        return this.mapping.keySet().stream()
                .filter(it -> it.isAssignableFrom(exceptionClass))
                .map(mapping::get)
                .findFirst();
    }

    public String getMessage(Throwable exception) {
        if (exception == null) {
            return "";
        }
        return (exception.getMessage() == null) ? exception.toString() : exception.getMessage();
    }
}
