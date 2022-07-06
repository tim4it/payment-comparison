package com.tim4it.payment.comparison.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature

import java.nio.charset.StandardCharsets

class HelperTest {

    static final String FILE_NAME_1 = "PaymentologyMarkoffFile20140113.csv"
    static final String FILE_NAME_2 = "ClientMarkoffFile20140113.csv"


    static Pair<String, byte[]> getCsvDataFromResources(final String fileName) {
        Optional.ofNullable(HelperTest.getClassLoader())
                .map(loader -> loader.getResourceAsStream(fileName))
                .map(HelperTest::getFileBytes)
                .map(fileBytes -> new Pair<>(fileName, fileBytes))
                .orElseThrow(() -> new RuntimeException("File:" + fileName + " can't be read!"))
    }

    static byte[] getFileBytes(InputStream it) {
        try {
            it.readAllBytes()
        } catch (IOException e) {
            throw new RuntimeException(e)
        }
    }

    static String jsonToString(Object clazz) {
        try {
            var mapper = new ObjectMapper()
            mapper.enable(SerializationFeature.INDENT_OUTPUT)
            new String(mapper.writeValueAsBytes(clazz), StandardCharsets.UTF_8)
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e)
        }
    }
}
