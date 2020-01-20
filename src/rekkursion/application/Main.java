//package rekkursion.application;
//
//import javafx.application.Application;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Node;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.layout.BorderPane;
//import javafx.stage.Stage;
//import rekkursion.manager.PreferenceManager;
//import rekkursion.util.Language;
//import rekkursion.util.TokenPrototype;
//import rekkursion.view.control.editor.CodeCanvas;
//
//public class Main extends Application {
//    @Override
//    public void start(Stage primaryStage) {
//        primaryStage.setTitle("Smirk");
//        primaryStage.setScene(new Scene(
//                initViews(),
//                PreferenceManager.INSTANCE.getWindowWidth(),
//                PreferenceManager.INSTANCE.getWindowHeight()
//        ));
//        primaryStage.show();
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    // initialize the views
//    private Parent initViews() {
//        BorderPane bdpMain = new BorderPane();
//        bdpMain.setLayoutX(0.0);
//        bdpMain.setLayoutY(0.0);
//        bdpMain.setPrefSize(
//                PreferenceManager.INSTANCE.getWindowWidth(),
//                PreferenceManager.INSTANCE.getWindowHeight()
//        );
//        bdpMain.setCenter(new CodeCanvas(
//                PreferenceManager.INSTANCE.getCodeCvsWidth(),
//                PreferenceManager.INSTANCE.getCodeCvsHeight()
//        ));
//
//        return bdpMain;
//    }
//
//    private void setUpLang() {
//    }
//}
