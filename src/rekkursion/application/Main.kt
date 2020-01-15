package rekkursion.application

import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import rekkursion.manager.PreferenceManager
import rekkursion.view.CodeCanvas

class Main: Application() {
    override fun start(primaryStage: Stage) {
        // set up supported languages
        setUpLang()

        primaryStage.title = "Smirk"
        primaryStage.scene = Scene(
                initViews(),
                PreferenceManager.windowWidth,
                PreferenceManager.windowHeight
        )
        primaryStage.show()

        // for testing
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
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java, *args)
        }
    }
}
