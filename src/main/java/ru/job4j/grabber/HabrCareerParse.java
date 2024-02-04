package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-description__text");
        return rows.text();
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> resultList = new ArrayList<>();
        int pageNumber = 5;
        for (int i = 0; i < pageNumber; i++) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
            Connection connection = Jsoup.connect(fullLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element timeElement = row.select(".vacancy-card__date").first();
                Element linkElement = titleElement.child(0);
                Element timeLinkElement = timeElement.child(0);
                LocalDateTime localDateTime = dateTimeParser.parse(timeLinkElement.attr("datetime"));
                String vacancyName = titleElement.text();
                String reslink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                try {
                    resultList.add(new Post(111, vacancyName, reslink, retrieveDescription(reslink), localDateTime));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return resultList;
    }

    public static void main(String[] args) throws IOException {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> vacancyList = habrCareerParse.list(SOURCE_LINK);
    }
}

