package ui;

import javafx.application.Application;

/** Classe auxiliar: evita o erro "JavaFX runtime components are missing" ao rodar fora do Maven. */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
