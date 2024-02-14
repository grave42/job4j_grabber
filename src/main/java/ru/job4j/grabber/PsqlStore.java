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

    private Post makeNewPost(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        String text = resultSet.getString("text");
        String link = resultSet.getString("link");
        Timestamp created = resultSet.getTimestamp("created");
        return new Post(id, name, link, text, created.toLocalDateTime());
    }

    @Override
    public void save(Post post) {
        Timestamp timestampFromLDT = Timestamp.valueOf(post.getCreated());
        String sql = """
                INSERT INTO post (name, text, link, created) VALUES (?, ?, ?, ?)
                ON CONFLICT (link)
                DO NOTHING""";
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
                Post post = makeNewPost(resultSet);
                posts.add(post);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error retrieving items from the database", e);
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        String sql = "SELECT * FROM post WHERE id = ?";
        Post postById = null;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    postById = makeNewPost(resultSet);
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
}