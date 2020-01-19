package rekkursion.stage

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration

enum class InputType {
    INTEGER, NON_NEGATIVE_INTEGER, NUMERIC, TEXT
}

class SingleLineTextInputStage(title: String, inputHint: String, inputType: InputType, defaultInput: String = "", width: Double = 320.0, height: Double = 120.0): Stage() {
    // the input hint for users
    private val mInputHint = inputHint

    // the default input
    private val mDefaultInput = defaultInput

    // the input-type of the text-field
    private val mInputType = inputType

    // the output string
    private var mOutputValue: String? = null

    // for primary constructor
    init {
        this.initModality(Modality.APPLICATION_MODAL)
        this.initStyle(StageStyle.UTILITY)
        this.minWidth = 320.0
        this.title = title
        scene = Scene(initViews(), width, height)
        scene.stylesheets.add("rekkursion/css/global.css")
    }

    /* ===================================================================== */

    // initialize views and return a parent for the scene
    private fun initViews(): Parent {
        // region create and add views
        val vBox = VBox()
        vBox.alignment = Pos.CENTER
        vBox.padding = Insets(11.5)
        vBox.spacing = 5.5

        val label = Label(mInputHint)

        val textField = TextField(mDefaultInput)
        textField.alignment = Pos.CENTER

        val hBox = HBox()
        hBox.alignment = Pos.CENTER
        hBox.padding = Insets(11.5)
        hBox.spacing = 8.0

        val btnOkay = Button("OK")
        btnOkay.prefWidth = 70.0
        btnOkay.isDisable = false
        setDisabilityByString(btnOkay, mDefaultInput)

        val btnCancel = Button("Cancel")
        btnCancel.prefWidth = 70.0

        hBox.children.addAll(btnOkay, btnCancel)

        vBox.children.addAll(label, textField, hBox)
        // endregion

        // region add listeners and events
        textField.textProperty().addListener { _, _, newValue -> setDisabilityByString(btnOkay, newValue) }

        textField.onKeyReleased = EventHandler { keyEvent ->
            // enter released
            if (keyEvent.code.code == 10)
                btnOkay.fire()
        }

        btnOkay.setOnAction {
            mOutputValue = textField.text
            super.close()
        }

        btnCancel.setOnAction { close() }
        // endregion

        return vBox
    }

    // set disability of a node by a certain string w/ regular expressions
    private fun setDisabilityByString(node: Node, str: String) {
        node.isDisable = false
        when (mInputType) {
            InputType.INTEGER -> {
                if (!str.matches("^[\\+\\-]?[0-9]+$".toRegex()))
                    node.isDisable = true
            }
            InputType.NON_NEGATIVE_INTEGER -> {
                if (!str.matches("^[0-9]+$".toRegex()))
                    node.isDisable = true
            }
            InputType.NUMERIC -> {
                if (!str.matches("(^[\\+\\-]?\\.?[0-9]+$)|(^[\\+\\-]?[0-9]+\\.?[0-9]*$)|(^[\\+\\-]?\\.?[0-9]+(E|E\\+|E\\-|e|e\\+|e\\-)[0-9]+$)|(^[\\+\\-]?[0-9]+\\.?[0-9]*(E|E\\+|E\\-|e|e\\+|e\\-)[0-9]+$)".toRegex()))
                    node.isDisable = true
            }
        }
    }

    // show the dialog
    fun showDialog(): String? {
        super.showAndWait()
        return mOutputValue
    }

    // close this stage w/ an effect of fading-out
    override fun close() {
        val timeline = Timeline()
        val keyFrame = KeyFrame(
                Duration.millis(100.0),
                KeyValue(this.opacityProperty(), 0)
        )

        timeline.keyFrames.add(keyFrame)
        timeline.setOnFinished { super.close() }
        timeline.play()
    }
}