package com.tim4it.payment.comparison;

import io.micronaut.context.annotation.Factory;
import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Factory
@OpenAPIDefinition(info = @Info(
        title = "Payment comparison application",
        description = "Payment comparison application - compare two files",
        version = "1"))
public class PaymentComparisonApplication {
    public static void main(String[] args) {
        Micronaut.run(PaymentComparisonApplication.class, args);
    }
}
