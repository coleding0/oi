package org.oi.service;

import org.oi.model.Note;
import org.oi.persistence.NoteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NoteService {
    private final List<Note> notes;


    private final NoteRepository repository;

    public NoteService() {
        this.repository = new NoteRepository();
        this.notes = new ArrayList<>(repository.getAllNotes());  // load from DB on start
        System.out.println("Loaded notes from DB: " + notes.size());
    }


    // Create and store a new note
    public Note createNote(String title, String content) {
        Note note = new Note(title, content);
        notes.add(note);
        //repository.insertNote(note); this doesnt work here as it is insterting to the db before anything is saved to
        //the note so the notes in the db are always blank and untitles. this needs to go in save
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
            Note note = noteOpt.get();
            note.setContent(newContent);
            repository.updateNote(note); // sync change to DB
            return true;
        }
        return false;
    }
    // Delete a note
    public boolean deleteNote(String id) {
        return notes.removeIf(note -> note.getId().equals(id));
    }
}
