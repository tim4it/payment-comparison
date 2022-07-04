package com.tim4it.payment.comparison;

import com.tim4it.payment.comparison.v1.util.Pair;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.Optional;

@UtilityClass
public class Helper {

    public static final String FILE_NAME_1 = "PaymentologyMarkoffFile20140113.csv";
    public static final String FILE_NAME_2 = "ClientMarkoffFile20140113.csv";


    public Pair<String, byte[]> getCsvDataFromResources(final String fileName) {
        return Optional.ofNullable(Helper.class.getClassLoader())
                .map(loader -> loader.getResourceAsStream(fileName))
                .map(it -> {
                    try {
                        return it.readAllBytes();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(fileBytes -> new Pair<>(fileName, fileBytes))
                .orElseThrow(() -> new RuntimeException("File:" + fileName + " can't be read!"));
    }
}
