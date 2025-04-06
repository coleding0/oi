package org.oi.service;

import org.oi.model.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NoteService {
    private final List<Note> notes;

    public NoteService() {
        this.notes = new ArrayList<>();
    }

    // Create and store a new note
    public Note createNote(String title, String content) {
        Note note = new Note(title, content);
        notes.add(note);
        return note;
    }

    // Get all notes
    public List<Note> getAllNotes() {
        return new ArrayList<>(notes); // return a copy to protect internal list
    }

    // Find a note by its ID
    public Optional<Note> findNoteById(String id) {
        return notes.stream()
                .filter(note -> note.getId().equals(id))
                .findFirst();
    }

    // Update content of a note
    public boolean updateNoteContent(String id, String newContent) {
        Optional<Note> noteOpt = findNoteById(id);
        if (noteOpt.isPresent()) {
            noteOpt.get().setContent(newContent);
            return true;
        }
        return false;
    }

    // Delete a note
    public boolean deleteNote(String id) {
        return notes.removeIf(note -> note.getId().equals(id));
    }
}
