package com.taskmanager; // Or the correct package if it's different

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends TypeAdapter<LocalDate> {

    private final DateTimeFormatter formatter;

    public LocalDateAdapter() {
        this("yyyy-MM-dd"); // Default format
    }

    public LocalDateAdapter(String pattern) {
        this.formatter = DateTimeFormatter.ofPattern(pattern);
    }

    @Override
    public void write(JsonWriter out, LocalDate date) throws IOException {
        if (date == null) {
            out.nullValue();
        } else {
            out.value(formatter.format(date));
        }
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        } else {
            String dateString = in.nextString();
            try {
                return LocalDate.parse(dateString, formatter);
            } catch (java.time.format.DateTimeParseException e) {
                System.err.println("Error parsing date: " + dateString);
                return null;
            }
        }
    }
}