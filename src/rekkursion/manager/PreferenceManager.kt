package rekkursion.manager

import javafx.scene.paint.Color
import rekkursion.util.*

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
        // hash-map of supported languages
        private val mSupportedLanguageMap: HashMap<String, Language> = HashMap()

        // the index of the current language used
        private var mUsedLanguageName: String = ""

        // set up all languages
        fun setUpDefaultSupportedLanguages() {
            val xogue = Language(
                    "Xogue",
                    arrayOf(
                            // comment
                            TokenPrototype(
                                    TokenType.COMMENT,
                                    arrayOf(
                                            "^//[^\n]*".toRegex(),
                                            "^/\\*(.|\n)*\\*/".toRegex()
                                    ),
                                    FontStyle.Builder().setFontColor(Color.DARKGRAY).create()
                            ),
                            // keyword
                            TokenPrototype(
                                    TokenType.KEYWORD,
                                    arrayOf(
                                            "^if".toRegex(),
                                            "^elif".toRegex(),
                                            "^else".toRegex(),
                                            "^for".toRegex(),
                                            "^while".toRegex()
                                    ),
                                    FontStyle.Builder().setFontColor(Color.ORANGE).create()
                            ),
                            // string
                            TokenPrototype(
                                    TokenType.STRING,
                                    arrayOf(
                                            "^\".*\"".toRegex(),
                                            "^\'.*\'".toRegex()
                                    ),
                                    FontStyle.Builder().setFontColor(Color.FORESTGREEN).create()
                            ),
                            // operator
                            TokenPrototype(
                                    TokenType.OPERATOR,
                                    arrayOf(
                                            "^[\\+\\-\\*\\/%&\\|\\^=\\!\\(\\)\\[\\]\\{\\};\\?:]".toRegex(),
                                            "^\\-\\-".toRegex(),
                                            "^\\+\\+".toRegex(),
                                            "^\\*\\*".toRegex(),
                                            "^[\\+\\-\\*\\/%&\\|\\^=\\!]=".toRegex()
                                    ),
                                    FontStyle.Builder().setFontColor(Color.LIGHTGRAY).create()
                            ),
                            // identifier
                            TokenPrototype(
                                    TokenType.IDENTIFIER,
                                    arrayOf(
                                            "^[_A-Za-z][_A-Za-z0-9]*".toRegex()
                                    ),
                                    FontStyle.Builder().create()
                            ),
                            // float
                            TokenPrototype(
                                    TokenType.FLOAT,
                                    arrayOf(
                                            "(^([0-9]+\\.[0-9]*|[0-9]*\\.[0-9]+|[0-9]+)(E\\+|E\\-|e\\+|e\\-|E|e)[0-9]+)|(^([0-9]+\\.[0-9]*|[0-9]*\\.[0-9]+))".toRegex()
                                    ),
                                    FontStyle.Builder().setFontColor(Color.SKYBLUE).create()
                            ),
                            // integer
                            TokenPrototype(
                                    TokenType.INTEGER,
                                    arrayOf(
                                            "^[0-9](_?[0-9])*".toRegex()
                                    ),
                                    FontStyle.Builder().setFontColor(Color.SKYBLUE).create()
                            ),
                            // space
                            TokenPrototype(
                                    TokenType.SPACE,
                                    arrayOf(
                                            "^\\s+".toRegex()
                                    ),
                                    FontStyle.Builder().create()
                            )
                    )
            )

            addLang(xogue)
            setUsedLang(xogue.name)
        }

        // add a supported language
        private fun addLang(lang: Language) {
            mSupportedLanguageMap[lang.name] = lang
        }

        // set the used language
        private fun setUsedLang(langName: String) {
            mUsedLanguageName = mSupportedLanguageMap[langName]?.name ?: ""
        }

        // get the used language
        fun getUsedLang() = mSupportedLanguageMap[mUsedLanguageName]
    }
}