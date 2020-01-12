package rekkursion.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import rekkursion.view.CodeCanvas;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
//        Parent root = FXMLLoader.load(getClass().getResource("../layout/main.fxml"));
        primaryStage.setTitle("Smirk");
        primaryStage.setScene(new Scene(initViews(), 1100, 700));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // initialize the views
    private Parent initViews() {
        BorderPane bdpMain = new BorderPane();
        bdpMain.setLayoutX(0.0);
        bdpMain.setLayoutY(0.0);
        bdpMain.setPrefSize(1100.0, 700.0);
        bdpMain.setCenter(new CodeCanvas(600.0, 400.0));

        return bdpMain;
    }
}
