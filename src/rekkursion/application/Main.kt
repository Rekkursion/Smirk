package rekkursion.application

import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import rekkursion.manager.PreferenceManager
import rekkursion.view.control.editor.CodeCanvas

class Main: Application() {
    override fun start(primaryStage: Stage) {
        // set up supported languages
        setUpLang()

        // get the parent, build the scene, and show the stage
        primaryStage.scene = Scene(
                initViews(),
                PreferenceManager.windowWidth,
                PreferenceManager.windowHeight
        )
        primaryStage.scene.stylesheets.add("rekkursion/css/global.css")
        primaryStage.title = "Smirk"
        primaryStage.show()

        // for testing, for debugging
        test()
    }

    // initialize the views
    private fun initViews(): Parent {
        val bdpMain = BorderPane()
        bdpMain.layoutX = 0.0
        bdpMain.layoutY = 0.0
        bdpMain.setPrefSize(
                PreferenceManager.windowWidth,
                PreferenceManager.windowHeight
        )
        bdpMain.center = CodeCanvas(
                PreferenceManager.codeCvsWidth,
                PreferenceManager.codeCvsHeight
        )

        return bdpMain
    }

    // set up the supported languages
    private fun setUpLang() {
        PreferenceManager.LangPref.setUpDefaultSupportedLanguages()
    }

    // only for testing
    private fun test() {
//        val scManager = ShortcutManager()
//        val f = scManager.getOperationByFunctionName("testYo", "rekkursion.global.EditorOperations")
//        println(f?.call("sss"))
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java, *args)
        }
    }
}
