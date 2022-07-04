package com.tim4it.payment.comparison.v1.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class Pair<A, B> {
    /**
     * First element (may be null)
     */
    A first;

    /**
     * Second element (may be null)
     */
    B second;
}
