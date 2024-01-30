package ru.job4j.grabber;

import org.junit.jupiter.api.Test;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HabrCareerParseTest {

    @Test
    public void testParseValidDateTime() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String dateTimeString = "2022-01-30T12:34:56";
        LocalDateTime parsedDateTime = parser.parse(dateTimeString);
        LocalDateTime expectedDateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);
        assertEquals(expectedDateTime, parsedDateTime);
    }

    @Test
    public void testParseInvalidDateTime() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String invalidDateTimeString = "invalidDateTimeString";
        LocalDateTime parsedDateTime = parser.parse(invalidDateTimeString);
        assertEquals(null, parsedDateTime);
    }
}

