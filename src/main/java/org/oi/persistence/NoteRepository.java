package org.oi.persistence;

import org.oi.model.Note;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NoteRepository {

    public void insertNote(Note note) {
        String sql = "INSERT INTO notes (id, title, content, created_at, last_modified, metadata) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, note.getId());
            stmt.setString(2, note.getTitle());
            stmt.setString(3, note.getContent());
            stmt.setString(4, note.getCreatedAt().toString());
            stmt.setString(5, note.getLastModified().toString());
            stmt.setString(6, "{}"); // empty JSON metadata for now

            // 🔍 debug logging
            System.out.println("Saving note:");
            System.out.println("  Title: " + note.getTitle());
            System.out.println("  Content: " + note.getContent());


            stmt.executeUpdate();
            System.out.println("Note saved to DB: " + note.getTitle());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Note note = new Note(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        LocalDateTime.parse(rs.getString("created_at")),
                        LocalDateTime.parse(rs.getString("last_modified"))
                );

                notes.add(note);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return notes;
    }

    public void updateNote(Note note) {
        String sql = "UPDATE notes SET title = ?, content = ?, last_modified = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, note.getTitle());
            stmt.setString(2, note.getContent());
            stmt.setString(3, note.getLastModified().toString());
            stmt.setString(4, note.getId());

            stmt.executeUpdate();
            System.out.println("Note updated in DB: " + note.getTitle());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
