package com.example.server.utils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@NoArgsConstructor
public class SqlUtils {

    public static String loadSql(String filename) {
        try (InputStream inputStream = SqlUtils.class.getClassLoader().getResourceAsStream("sql/" + filename)) {
            if (inputStream == null) {
                throw new FileNotFoundException("SQL file not found: " + filename);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read SQL file", e);
        }
    }
}
