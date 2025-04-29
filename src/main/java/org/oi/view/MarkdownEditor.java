package org.oi.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.oi.model.Note;
import org.oi.scripting.OiPromptDetector;
import org.oi.service.NoteService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;


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
        for (Note note : noteService.getAllNotes()) {
            noteTitles.add(note.getTitle());
        }

        noteListView = new ListView<>(noteTitles);
        noteListView.setPrefWidth(100);
        noteListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> loadNoteByTitle(newVal));


        // Center: markdown editor
        textArea = new TextArea();
        textArea.setPromptText("Type your markdown here...");

        // Top: toolbar
        Button newNoteButton = new Button("New Note");
        newNoteButton.setOnAction(e -> handleNewNote());

        Button saveNoteButton = new Button("Save Note");
        saveNoteButton.setOnAction(e -> handleSaveNote());

        Button renameNoteButton = new Button("Rename Note");
        renameNoteButton.setOnAction(e -> handleRenameNote());

        Button loadNoteButton = new Button("Load Note");
        loadNoteButton.setOnAction(e -> handleLoadNote());

        HBox topBar = new HBox(10, newNoteButton, saveNoteButton, renameNoteButton, loadNoteButton);
        topBar.setPadding(new Insets(10));
        this.setTop(topBar);

        // Layout
        SplitPane splitPane = new SplitPane(noteListView, textArea);
        this.setTop(topBar);
        this.setCenter(splitPane);

        textArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                OiPromptDetector detector = new OiPromptDetector();
                Optional<String> result = detector.handleIfTriggered(
                        textArea.getText(),
                        textArea.getCaretPosition()
                );

                result.ifPresent(response -> {
                    textArea.insertText(textArea.getCaretPosition(), "\n" + response + "\n");
                });
            }
        });

    }
    private int untitledCount = 1;

    private void handleSaveNote() {
        if (currentNote != null) {
            // Get the current text from the editor
            String content = textArea.getText();

            // Update the in-memory note object
            currentNote.setContent(content);

            // Check if the note already exists in the service
            if (!noteService.findNoteById(currentNote.getId()).isPresent()) {
                noteService.createNote(currentNote.getTitle(), content);
            }

            // Update the note in the database
            noteService.updateNoteContent(currentNote.getId(), content);
        }
    }


    private void handleNewNote() {
        String title = "Untitled " + untitledCount++;
        currentNote = noteService.createNote(title, "");
        noteTitles.add(currentNote.getTitle());
        textArea.clear();
    }

    private void handleRenameNote() {
        if (currentNote == null) return;

        TextInputDialog dialog = new TextInputDialog(currentNote.getTitle());
        dialog.setTitle("Rename Note");
        dialog.setHeaderText("Enter a new title:");
        dialog.setContentText("Title:");

        dialog.showAndWait().ifPresent(newTitle -> {
            // Update the note title
            String oldTitle = currentNote.getTitle();
            currentNote.setTitle(newTitle);

            // Update list view
            int index = noteTitles.indexOf(oldTitle);
            if (index != -1) {
                noteTitles.set(index, newTitle);
            }
        });
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
    // saving to file
    private void saveNoteToFile(Note note) {
        try {
            String userHome = System.getProperty("user.home");
            File notesDir = new File(userHome, "oi-notes");
            if (!notesDir.exists()) {
                notesDir.mkdirs();
            }

            File file = new File(notesDir, note.getTitle() + ".md");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(note.getContent());
                System.out.println("Saved to: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
// load a note from file

    private void handleLoadNote() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Markdown Note");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Markdown Files", "*.md"));

        File selectedFile = fileChooser.showOpenDialog(this.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String content = Files.readString(selectedFile.toPath());
                String title = selectedFile.getName().replace(".md", "");

                Note loadedNote = new Note(title, content);
                noteService.getAllNotes().add(loadedNote);
                noteTitles.add(loadedNote.getTitle());
                currentNote = loadedNote;
                textArea.setText(content);

                System.out.println("Loaded note: " + title);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
