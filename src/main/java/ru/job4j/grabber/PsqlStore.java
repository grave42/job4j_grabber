package ru.job4j.grabber;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection connection;

    public PsqlStore(Properties config) throws SQLException {
        try {
            Class.forName(config.getProperty("driver_class"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        connection = DriverManager.getConnection(
                config.getProperty("url_db"),
                config.getProperty("login_db"),
                config.getProperty("password_db")
        );
    }

    @Override
    public void save(Post post) {
        Timestamp timestampFromLDT = Timestamp.valueOf(post.getCreated());
        String sql = """
                INSERT INTO post (name, text, link, created) VALUES (?, ?, ?, ?)
                ON CONFLICT (link)
                DO UPDATE SET name = EXCLUDED.name, text = EXCLUDED.text, created = EXCLUDED.created""";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, timestampFromLDT);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Error adding item to the database", e);
        }

    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM post";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String text = resultSet.getString("text");
                String link = resultSet.getString("link");
                Timestamp created = resultSet.getTimestamp("created");

                Post post = new Post(id, name, text, link, created.toLocalDateTime());
                post.setId(id);
                posts.add(post);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error retrieving items from the database", e);
        }

        return posts;
    }

    @Override
    public Post findById(int id) {
        Post postById = null;
        String sql = "SELECT * FROM post WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String text = resultSet.getString("text");
                    String link = resultSet.getString("link");
                    Timestamp created = resultSet.getTimestamp("created");

                    postById = new Post(id, name, text, link, created.toLocalDateTime());
                    postById.setId(id);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error retrieving item by ID from the database", e);
        }

        return postById;
    }
    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public static void main(String[] args) throws SQLException {
        try {
            Properties config = new Properties();
            try (FileInputStream input = new FileInputStream("src/main/resources/rabbit.properties")) {
                config.load(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
            PsqlStore psqlStore = new PsqlStore(config);
            LocalDateTime currentDateTime = LocalDateTime.now();
            Post example = new Post(1, "Java", "http://example.com", "ЭТО ПРИМЕР", currentDateTime);
            psqlStore.save(example);
            Post example2 = new Post(1, "Python", "http://lol.com", "ЭТО ПРИМЕР 2", currentDateTime);
            psqlStore.save(example2);
            List<Post> exampleListPost = psqlStore.getAll();
            Post postById = psqlStore.findById(1);
            psqlStore.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}