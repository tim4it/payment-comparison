package com.tim4it.payment.comparison;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tim4it.payment.comparison.util.Pair;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@UtilityClass
public class Helper {

    public static final String FILE_NAME_1 = "PaymentologyMarkoffFile20140113.csv";
    public static final String FILE_NAME_2 = "ClientMarkoffFile20140113.csv";


    public Pair<String, byte[]> getCsvDataFromResources(final String fileName) {
        return Optional.ofNullable(Helper.class.getClassLoader())
                .map(loader -> loader.getResourceAsStream(fileName))
                .map(Helper::getFileBytes)
                .map(fileBytes -> new Pair<>(fileName, fileBytes))
                .orElseThrow(() -> new RuntimeException("File:" + fileName + " can't be read!"));
    }

    private byte[] getFileBytes(InputStream it) {
        try {
            return it.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String jsonToString(Object clazz) {
        try {
            var mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return new String(mapper.writeValueAsBytes(clazz), StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
