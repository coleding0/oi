package org.oi;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.oi.model.Note;
import org.oi.persistence.DatabaseManager;
import org.oi.service.NoteService;
import org.oi.view.MarkdownEditor;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        // init db
        DatabaseManager.initializeDatabase();

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
