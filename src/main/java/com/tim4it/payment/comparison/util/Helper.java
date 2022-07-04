package com.tim4it.payment.comparison.util;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@UtilityClass
public class Helper {

    public final Predicate<List<?>> IS_NOT_EMPTY_LIST = Predicate.not(List::isEmpty);
    public final Predicate<Map<?, ?>> IS_NOT_EMPTY_MAP = Predicate.not(Map::isEmpty);
    public final String DATE_FORMAT = "yyyy-MM-dd HH:m:s";

    public Optional<String> optString(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(valueCheck -> !valueCheck.isEmpty());
    }
}
