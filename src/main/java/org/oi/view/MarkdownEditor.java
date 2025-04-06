package org.oi.view;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import org.oi.model.Note;
import org.oi.service.NoteService;



public class MarkdownEditor extends BorderPane {
    private final TextArea textArea;
    private final ListView<String> noteListView;
    private final ObservableList<String> noteTitles;
    private final NoteService noteService;
    private Note currentNote;

    public MarkdownEditor() {
        noteService = new NoteService();

        // Left side: note list
        noteTitles = FXCollections.observableArrayList();
        noteListView = new ListView<>(noteTitles);
        noteListView.setPrefWidth(200);
        noteListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> loadNoteByTitle(newVal));


        // Center: markdown editor
        textArea = new TextArea();
        textArea.setPromptText("Type your markdown here...");

        // Top: toolbar
        Button newNoteButton = new Button("New Note");
        newNoteButton.setOnAction(e -> handleNewNote());


        Button saveNoteButton = new Button("Save Note");
        saveNoteButton.setOnAction(e -> handleSaveNote());

        HBox topBar = new HBox(10, newNoteButton, saveNoteButton);
        topBar.setPadding(new Insets(10));
        this.setTop(topBar);

        // Layout
        SplitPane splitPane = new SplitPane(noteListView, textArea);
        this.setTop(topBar);
        this.setCenter(splitPane);
    }
    private int untitledCount = 1;

    private void handleSaveNote() {
        if (currentNote != null) {
            currentNote.setContent(textArea.getText());
            System.out.println("Note saved: " + currentNote.getTitle());
        }
    }

    private void handleNewNote() {
        String title = "Untitled " + untitledCount++;
        currentNote = noteService.createNote(title, "");
        noteTitles.add(currentNote.getTitle());
        textArea.clear();
    }


    private void loadNoteByTitle(String title) {
        noteService.getAllNotes().stream()
                .filter(note -> note.getTitle().equals(title))
                .findFirst()
                .ifPresent(note -> {
                    currentNote = note;
                    textArea.setText(note.getContent());
                });
    }

    public String getMarkdownText() {
        return textArea.getText();
    }
}
