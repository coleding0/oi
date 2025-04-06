package org.oi;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.oi.model.Note;
import org.oi.service.NoteService;
import org.oi.view.MarkdownEditor;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {

        // TEMPORARY: Test NoteService
        NoteService service = new NoteService();
        Note myNote = service.createNote("Hello", "This is my first note");
        System.out.println(myNote.getContent());
        service.updateNoteContent(myNote.getId(), "Updated content");
        System.out.println(myNote.getContent());

        MarkdownEditor editor = new MarkdownEditor();
        Scene scene = new Scene(editor, 800, 600);
        primaryStage.setTitle("oi - Markdown Editor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
