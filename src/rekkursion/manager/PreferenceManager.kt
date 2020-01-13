package rekkursion.manager

import javafx.scene.paint.Color
import rekkursion.util.FontStyle

object PreferenceManager {
    // region size related
    // width of the window
    private var mWinW = 1200.0
    var windowWidth
        get() = mWinW
        set(value) { mWinW = value }

    // height of the window
    private var mWinH = 750.0
    var windowHeight
        get() = mWinH
        set(value) { mWinH = value }

    // width of the code-canvas
    private var mCodeCvsW = 850.0
    var codeCvsWidth
        get() = mCodeCvsW
        set(value) { mCodeCvsW = value }

    // height of the code-canvas
    private var mCodeCvsH = 600.0
    var codeCvsHeight
        get() = mCodeCvsH
        set(value) { mCodeCvsH = value }
    // endregion

    // region color related
    // color of code-canvas background
    private var mCodeCvsBgClr: Color = Color.color(0.3, 0.3, 0.3)
    var codeCanvasBgColor
        get() = mCodeCvsBgClr
        set(value) { mCodeCvsBgClr = value }

    // color of the current line hint of the code-canvas
    private var mLineHintClr: Color = Color.color(0.4, 0.4, 0.4)
    var lineHintColor
        get() = mLineHintClr
        set(value) { mLineHintClr = value }
    // endregion

    // editor preference
    object EditorPref {
        private const val BASIC_CHARA_WIDTH = 11.0
        private const val BASIC_LINE_HEIGHT = 24.0
        private const val BASIC_CARET_WIDTH = 1.0
        private const val BASIC_LINE_START_OFFSET = 3.0

        // the ratio of the editor font size
        private var mEditorFontSizeRatio = 1.0
        var editorFontSizeRatio
            get() = mEditorFontSizeRatio
            set(value) { mEditorFontSizeRatio = value }

        // get the true character width
        val charW get() = BASIC_CHARA_WIDTH * mEditorFontSizeRatio
        // get the true line height
        val lineH get() = BASIC_LINE_HEIGHT * mEditorFontSizeRatio
        // get the true caret width
        val caretW get() = BASIC_CARET_WIDTH * mEditorFontSizeRatio
        // get the true line start offset
        val lineStartOffset get() = BASIC_LINE_START_OFFSET * mEditorFontSizeRatio

        // step size when scrolling the editor
        private var mEditorScrollingStepSize = lineH * 3.0
        var editorScrollingStepSize
            get() = mEditorScrollingStepSize
            set(value) { mEditorScrollingStepSize = value }
    }

    // language preference
    object LangPref {
        // enum: token for lexeme analysis
        enum class Token {
            COMMENT, KEYWORD, STRING, OPERATOR, IDENTIFIER, INTEGER, FLOAT, SPACE
        }

        // enum: language
        enum class Language(
                val tokenRegexMap: HashMap<Token, ArrayList<String>>,
                val tokenFontMap: HashMap<Token, FontStyle>) {
            Xogue(hashMapOf(
                    Token.COMMENT to arrayListOf(
                            "^\\/\\/[^\r\n]*",
                            "^\\/\\*(.|\r|\n|\r\n)*\\*\\/"
                    ),
                    Token.KEYWORD to arrayListOf(
                            "^if", "^elif", "^else",
                            "^for", "^while"
                    ),
                    Token.STRING to arrayListOf("^\".*\"", "^\'.*\'"),
                    Token.OPERATOR to arrayListOf(
                            "[\\+\\-\\*\\/%&\\|\\^=\\!\\(\\)\\[\\]\\{\\};\\?:]",
                            "\\-\\-", "\\+\\+", "\\*\\*",
                            "[\\+\\-\\*\\/%&\\|\\^=\\!]="
                    ),
                    Token.IDENTIFIER to arrayListOf("^[_A-Za-z][_A-Za-z0-9]*"),
                    Token.INTEGER to arrayListOf("^[0-9](_?[0-9])*"),
                    Token.FLOAT to arrayListOf("(^([0-9]+\\.[0-9]*|[0-9]*\\.[0-9]+|[0-9]+)(E\\+|E\\-|e\\+|e\\-|E|e)[0-9]+)|(^([0-9]+\\.[0-9]*|[0-9]*\\.[0-9]+))"),
                    Token.SPACE to arrayListOf("^\\s+")
            ),
            hashMapOf(
                    Token.COMMENT to FontStyle.Builder().setFontColor(Color.LIGHTGRAY).create(),
                    Token.KEYWORD to FontStyle.Builder().setFontColor(Color.ORANGE).create(),
                    Token.STRING to FontStyle.Builder().setFontColor(Color.FORESTGREEN).create(),
                    Token.OPERATOR to FontStyle.Builder().setFontColor(Color.ANTIQUEWHITE).create(),
                    Token.IDENTIFIER to FontStyle.Builder().create(),
                    Token.INTEGER to FontStyle.Builder().setFontColor(Color.BLUE).create(),
                    Token.FLOAT to FontStyle.Builder().setFontColor(Color.BLUE).create(),
                    Token.SPACE to FontStyle.Builder().create()
            ));
        }
    }
}