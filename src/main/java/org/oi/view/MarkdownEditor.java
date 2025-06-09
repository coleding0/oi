package org.oi.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import org.oi.model.Note;
import org.oi.scripting.OiPromptDetector;
import org.oi.service.NoteService;
import org.oi.scripting.ScriptService;

import java.util.List;
import java.util.Optional;

public class MarkdownEditor extends BorderPane {

    private final NoteService noteService;
    private final ListView<Note> noteListView;
    private final TextArea textArea;
    private final WebView previewPane;
    private final SplitPane splitPane;
    private final ObservableList<Note> allNotes = FXCollections.observableArrayList();


    public MarkdownEditor() {
        this.noteService = new NoteService();
        this.textArea = new TextArea();
        this.previewPane = new WebView();
        this.noteListView = new ListView<>();
        this.splitPane = new SplitPane();

        textArea.setWrapText(true);

        initializeUI();
        initializeEventHandlers();
    }

    private void initializeUI() {
        // Top bar buttons
        Button newNoteButton = new Button("New Note");
        Button saveNoteButton = new Button("Save");
        Button renameNoteButton = new Button("Rename");
        Button reformatButton = new Button("Reformat");
        Button togglePreviewButton = new Button("Toggle Preview");
        TextField searchField = new TextField();
        searchField.setPromptText("Search notes...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterNotes(newVal));


        HBox topBar = new HBox(10, searchField, newNoteButton, saveNoteButton, renameNoteButton, reformatButton, togglePreviewButton);
        topBar.setPadding(new Insets(10));
        this.setTop(topBar);

        // Note list setup
        allNotes.addAll(noteService.getAllNotes());
        noteListView.setItems(FXCollections.observableArrayList(allNotes));
        noteListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Note> call(ListView<Note> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Note note, boolean empty) {
                        super.updateItem(note, empty);
                        setText((note == null || empty) ? null : note.getTitle());
                    }
                };
            }
        });

        noteListView.getSelectionModel().selectedItemProperty().addListener((obs, oldNote, newNote) -> {
            if (newNote != null) {
                textArea.setText(newNote.getContent());
            }
        });

        splitPane.getItems().addAll(noteListView, textArea);
        splitPane.setDividerPositions(0.3);
        this.setCenter(splitPane);

        // Button actions
        newNoteButton.setOnAction(e -> {
            Note newNote = noteService.createNote("Untitled", "");
            noteListView.getItems().add(newNote);
            noteListView.getSelectionModel().select(newNote);
        });

        saveNoteButton.setOnAction(e -> {
            Note selected = noteListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setContent(textArea.getText());
                noteService.updateNoteContent(selected.getId(), selected.getContent());
            }
        });

        renameNoteButton.setOnAction(e -> {
            Note selected = noteListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                TextInputDialog dialog = new TextInputDialog(selected.getTitle());
                dialog.setTitle("Rename Note");
                dialog.setHeaderText(null);
                dialog.setContentText("New title:");
                dialog.showAndWait().ifPresent(name -> {
                    selected.setTitle(name);
                    noteListView.refresh();
                });
            }
        });

        reformatButton.setOnAction(e -> handleReformatNote());


        togglePreviewButton.setOnAction(e -> {
            if (splitPane.getItems().get(1) == textArea) {
                String markdown = textArea.getText();
                String html = convertMarkdownToHtml(markdown);
                previewPane.getEngine().loadContent(html);
                splitPane.getItems().set(1, previewPane);
            } else {
                splitPane.getItems().set(1, textArea);
            }
        });
    }
    private void filterNotes(String query) {
        if (query == null || query.isEmpty()) {
            noteListView.setItems(FXCollections.observableArrayList(allNotes));
            return;
        }

        List<Note> filtered = allNotes.stream()
                .filter(note -> note.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        note.getContent().toLowerCase().contains(query.toLowerCase()))
                .toList();

        noteListView.setItems(FXCollections.observableArrayList(filtered));
    }

    private void initializeEventHandlers() {
        textArea.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> {
                    if (!event.isShiftDown()) {
                        OiPromptDetector detector = new OiPromptDetector();
                        Optional<OiPromptDetector.ResponseBlock> result = detector.handleIfTriggered(textArea.getText(), textArea.getCaretPosition());
                        result.ifPresent(responseBlock -> {
                            String[] lines = textArea.getText().split("\n");
                            String originalLine = lines[responseBlock.lineToReplace];
                            String updatedLine = "Me: " + originalLine.substring(3);
                            int lineStart = getLineStartOffset(responseBlock.lineToReplace);
                            textArea.replaceText(lineStart, lineStart + originalLine.length(), updatedLine);

                            int insertPos = lineStart + updatedLine.length();
                            ScriptService scriptService = new ScriptService();

                            String contextInstruction = """
                                The following is a markdown-formatted note. Use the content as context.
                                Only respond to the most recent line that begins with 'me:' — treat that as the user's request.
                                Do not repeat or summarize the context unless it adds clarity.
                                """;

                            String fullNote = textArea.getText();
                            String userPrompt = updatedLine.substring(4).trim(); // remove 'me:' prefix

                            String fullPrompt = contextInstruction + "\n\n" + fullNote + "\n\nMe: " + userPrompt;

                            String gptResponse = scriptService.runSimpleScript(fullPrompt);
                            textArea.insertText(insertPos, "\nGPT: " + gptResponse + "\n");

                        });
                    }
                }
            }
        });
    }

    private int getLineStartOffset(int lineIndex) {
        String[] lines = textArea.getText().split("\n");
        int offset = 0;
        for (int i = 0; i < lineIndex; i++) {
            offset += lines[i].length() + 1;
        }
        return offset;
    }

    private void handleReformatNote() {
        String currentContent = textArea.getText();
        ScriptService scriptService = new ScriptService();
        String instruction = """
            Reformat this note into clean, concise markdown.
            - Preserve all original information and meaning.
            - Use headings, bullet points, or code blocks if appropriate.
            - Remove redundant text and clean up spacing.
            - Do not include chat-style formatting (like 'Me:' or 'GPT:').
            """;
        String combinedPrompt = instruction + "\n\n" + currentContent;
        String reformatted = scriptService.runSimpleScript(combinedPrompt);
        textArea.setText(reformatted);
    }

    private String convertMarkdownToHtml(String markdownText) {
        return com.vladsch.flexmark.html.HtmlRenderer.builder()
                .build()
                .render(com.vladsch.flexmark.parser.Parser.builder().build().parse(markdownText));
    }
}
