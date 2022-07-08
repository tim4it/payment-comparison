package com.tim4it.payment.comparison.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.tim4it.payment.comparison.dto.file.DataFile
import com.tim4it.payment.comparison.dto.file.DataKey

import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class HelperTest {

    static final String FILE_NAME_1 = "PaymentologyMarkoffFile20140113.csv"
    static final String FILE_NAME_2 = "ClientMarkoffFile20140113.csv"
    static final String FILE_NAME_TEST = "TestMarkoff.csv"

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
            def mapper = new ObjectMapper()
            mapper.enable(SerializationFeature.INDENT_OUTPUT)
            new String(mapper.writeValueAsBytes(clazz), StandardCharsets.UTF_8)
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e)
        }
    }

    static Map<DataKey, DataFile> parsedMap1() {
        [
                (DataKey.builder()
                        .transactionDate(parseTime("2022-05-13 22:00:19"))
                        .transactionAmount(List.of(112))
                        .transactionId("100")
                        .build()): DataFile.builder()
                                           .transactionDate(parseTime("2022-05-13 22:00:19"))
                                           .transactionAmount(List.of(112))
                                           .transactionId("100")
                                           .walletReference("xa100")
                                           .transactionDescription(DataFile.TransactionDescription.DEDUCT)
                                           .build(),
                (DataKey.builder()
                        .transactionDate(parseTime("2022-05-13 12:00:19"))
                        .transactionAmount(List.of(114))
                        .transactionId("101")
                        .build()): DataFile.builder()
                                           .transactionDate(parseTime("2022-05-13 12:00:19"))
                                           .transactionAmount(List.of(114, 114))
                                           .transactionId("101")
                                           .walletReference("xa101xa214")
                                           .transactionDescription(DataFile.TransactionDescription.DEDUCT)
                                           .build(),
                (DataKey.builder()
                        .transactionDate(parseTime("2022-05-13 10:00:12"))
                        .transactionAmount(List.of(116))
                        .transactionId("102")
                        .build()): DataFile.builder()
                                           .transactionDate(parseTime("2022-05-13 10:00:12"))
                                           .transactionAmount(List.of(116))
                                           .transactionId("102")
                                           .walletReference("xa102")
                                           .transactionDescription(DataFile.TransactionDescription.DEDUCT)
                                           .build(),
                (DataKey.builder()
                        .transactionDate(parseTime("2022-05-13 06:00:11"))
                        .transactionAmount(List.of(118))
                        .transactionId("103")
                        .build()): DataFile.builder()
                                           .transactionDate(parseTime("2022-05-13 06:00:11"))
                                           .transactionAmount(List.of(118))
                                           .transactionId("1031")
                                           .walletReference("xa103")
                                           .transactionDescription(DataFile.TransactionDescription.DEDUCT)
                                           .build(),
                (DataKey.builder()
                        .transactionDate(parseTime("2022-05-13 03:00:13"))
                        .transactionAmount(List.of(120))
                        .transactionId("104")
                        .build()): DataFile.builder()
                                           .transactionDate(parseTime("2022-05-13 03:00:13"))
                                           .transactionAmount(List.of(120))
                                           .transactionId("104")
                                           .walletReference("xa104")
                                           .transactionDescription(DataFile.TransactionDescription.DEDUCT)
                                           .build(),
        ]
    }

    static Map<DataKey, DataFile> parsedMap2() {
        [
                (DataKey.builder()
                        .transactionDate(parseTime("2022-05-13 22:00:19"))
                        .transactionAmount(List.of(112))
                        .transactionId("100")
                        .build()): DataFile.builder()
                                           .transactionDate(parseTime("2022-05-13 22:00:19"))
                                           .transactionAmount(List.of(112))
                                           .transactionId("100")
                                           .walletReference("xa100")
                                           .transactionDescription(DataFile.TransactionDescription.DEDUCT)
                                           .build(),
                (DataKey.builder()
                        .transactionDate(parseTime("2022-05-13 12:00:19"))
                        .transactionAmount(List.of(116))
                        .transactionId("101")
                        .build()): DataFile.builder()
                                           .transactionDate(parseTime("2022-05-13 12:00:19"))
                                           .transactionAmount(List.of(114, 114))
                                           .transactionId("101")
                                           .walletReference("xa101xa214")
                                           .transactionDescription(DataFile.TransactionDescription.DEDUCT)
                                           .build(),
                (DataKey.builder()
                        .transactionDate(parseTime("2022-05-13 10:00:12"))
                        .transactionAmount(List.of(115))
                        .transactionId("103")
                        .build()): DataFile.builder()
                                           .transactionDate(parseTime("2022-05-13 10:00:12"))
                                           .transactionAmount(List.of(115))
                                           .transactionId("103")
                                           .walletReference("xa102")
                                           .transactionDescription(DataFile.TransactionDescription.DEDUCT)
                                           .build(),
                (DataKey.builder()
                        .transactionDate(parseTime("2022-05-13 06:10:11"))
                        .transactionAmount(List.of(119))
                        .transactionId("109")
                        .build()): DataFile.builder()
                                           .transactionDate(parseTime("2022-05-13 06:10:11"))
                                           .transactionAmount(List.of(119))
                                           .transactionId("109")
                                           .walletReference("xa111")
                                           .transactionDescription(DataFile.TransactionDescription.DEDUCT)
                                           .build(),
                (DataKey.builder()
                        .transactionDate(parseTime("2022-05-13 03:00:13"))
                        .transactionAmount(List.of(120))
                        .transactionId("104")
                        .build()): DataFile.builder()
                                           .transactionDate(parseTime("2022-05-13 03:00:13"))
                                           .transactionAmount(List.of(120))
                                           .transactionId("104")
                                           .walletReference("xa104")
                                           .transactionDescription(DataFile.TransactionDescription.DEDUCT)
                                           .build(),
        ]
    }

    static ZonedDateTime parseTime(String dateTime) {
        def formatter = DateTimeFormatter.ofPattern(Helper.DATE_FORMAT)
        def localTime = LocalDateTime.parse(dateTime, formatter)
        ZonedDateTime.of(localTime, ZoneId.from(ZoneOffset.UTC))
    }
}
