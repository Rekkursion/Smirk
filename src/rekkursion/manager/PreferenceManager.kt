package rekkursion.manager

import javafx.scene.paint.Color
import rekkursion.util.*
import rekkursion.util.statemachine.edge.EdgeType
import rekkursion.util.statemachine.state.State
import rekkursion.util.statemachine.state.StateType

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
            // region build Xogue
            val xogue = Language.Builder("Xogue")
                    // comment
                    .addTokenPrototype(TokenPrototype(
                            TokenType.COMMENT,
                            arrayOf(
                                    "^//[^\n]*".toRegex(),
                                    "^/\\*(.|\n)*\\*/".toRegex()
                            ),
                            FontStyle.Builder().setFontColor(Color.DARKGRAY).create()
                    ))
                    // keyword
                    .addTokenPrototype(TokenPrototype(
                            TokenType.KEYWORD,
                            arrayOf(
                                    "^if".toRegex(),
                                    "^elif".toRegex(),
                                    "^else".toRegex(),
                                    "^for".toRegex(),
                                    "^while".toRegex(),

                                    "^Byte".toRegex(),
                                    "^Short".toRegex(),
                                    "^Int".toRegex(),
                                    "^Int64".toRegex(),
                                    "^Char".toRegex(),
                                    "^String".toRegex(),
                                    "^Float".toRegex(),
                                    "^Double".toRegex()
                            ),
                            FontStyle.Builder().setFontColor(Color.ORANGE).create()
                    ))
                    // identifier
                    .addTokenPrototype(TokenPrototype(
                            TokenType.IDENTIFIER,
                            arrayOf(
                                    "^[_A-Za-z][_A-Za-z0-9]*".toRegex()
                            ),
                            FontStyle.Builder().create()
                    ))
                    // string
                    .addTokenPrototype(TokenPrototype(
                            TokenType.STRING,
                            arrayOf(
                                    "^\".*\"".toRegex()
                            ),
                            FontStyle.Builder().setFontColor(Color.FORESTGREEN).create()
                    ))
                    // character
                    .addTokenPrototype(TokenPrototype(
                            TokenType.CHAR,
                            arrayOf(
                                    "^\'.\'".toRegex()
                            ),
                            FontStyle.Builder().setFontColor(Color.FORESTGREEN).create()
                    ))
                    // operator
                    .addTokenPrototype(TokenPrototype(
                            TokenType.OPERATOR,
                            arrayOf(
                                    "^[\\+\\-\\*\\/%&\\|\\^=\\!\\(\\)\\[\\]\\{\\};\\?:]".toRegex(),
                                    "^\\-\\-".toRegex(),
                                    "^\\+\\+".toRegex(),
                                    "^\\*\\*".toRegex(),
                                    "^[\\+\\-\\*\\/%&\\|\\^=\\!]=".toRegex()
                            ),
                            FontStyle.Builder().setFontColor(Color.LIGHTGRAY).create()
                    ))
                    // floating point
                    .addTokenPrototype(TokenPrototype(
                            TokenType.FLOATING,
                            arrayOf(
                                    "(^([0-9]+\\.[0-9]*|[0-9]*\\.[0-9]+|[0-9]+)(E\\+|E\\-|e\\+|e\\-|E|e)[0-9]+)|(^([0-9]+\\.[0-9]*|[0-9]*\\.[0-9]+))".toRegex()
                            ),
                            FontStyle.Builder().setFontColor(Color.SKYBLUE).create()
                    ))
                    // integer
                    .addTokenPrototype(TokenPrototype(
                            TokenType.INTEGER,
                            arrayOf(
                                    "^[0-9](_?[0-9])*".toRegex()
                            ),
                            FontStyle.Builder().setFontColor(Color.SKYBLUE).create()
                    ))
                    // space
                    .addTokenPrototype(TokenPrototype(
                            TokenType.SPACE,
                            arrayOf(
                                    "^\\s+".toRegex()
                            ),
                            FontStyle.Builder().create()
                    ))
                    .create()
            // endregion

            // region build Xogue's state-machine
            TokenType.values().forEach { tokenType ->
                val prototype = xogue.getTokenPrototype(tokenType)
                when (tokenType) {
                    // comment
                    TokenType.COMMENT -> {
                        prototype?.getStateMachineBuilder()
                                ?.addState(State("/"))
                                ?.addState(State("//"))
                                ?.addState(State("/*"))
                                ?.addState(State("/*...*"))
                                ?.addState(State("//...END", StateType.END))
                                ?.addState(State("/*...*/END", StateType.END))

                                ?.addEdge("START", "\\/", "/")
                                ?.addEdge("/", "\\/", "//")
                                ?.addEdge("//", "\n", "//...END")
                                ?.addEdge("//", "OTHERS", "//", EdgeType.OTHERS_AND_CONSUMED)
                                ?.addEdge("/", "\\*", "/*")
                                ?.addEdge("/*", "\\*", "/*...*")
                                ?.addEdge("/*", "OTHERS", "/*", EdgeType.OTHERS_AND_CONSUMED)
                                ?.addEdge("/*...*", "\\*", "/*...*")
                                ?.addEdge("/*...*", "\\/", "/*...*/END")
                                ?.addEdge("/*...*", "OTHERS", "/*...*", EdgeType.OTHERS_AND_CONSUMED)

                    }
                    // keyword
                    TokenType.KEYWORD -> {
                        // TODO: keyword's state-machine
                    }
                    // identifier
                    TokenType.IDENTIFIER -> {
                        prototype?.getStateMachineBuilder()
                                ?.addState(State("building"))
                                ?.addState(State("identifierEND", StateType.END))

                                ?.addEdge("START", "[_A-Za-z]", "building")
                                ?.addEdge("building", "[_A-Za-z0-9]", "building")
                                ?.addEdge("building", "OTHERS", "identifierEND", EdgeType.OTHERS_AND_NOT_CONSUMED)
                    }
                    // string
                    TokenType.STRING -> {
                        prototype?.getStateMachineBuilder()
                                ?.addState(State("opened"))
                                ?.addState(State("ends_w_back_slash"))
                                ?.addState(State("strEND", StateType.END))

                                ?.addEdge("START", "\"", "opened")
                                ?.addEdge("opened", "\\\\", "ends_w_back_slash")
                                ?.addEdge("ends_w_back_slash", "OTHERS", "opened", EdgeType.OTHERS_AND_CONSUMED)
                                ?.addEdge("opened", "OTHERS", "opened", EdgeType.OTHERS_AND_CONSUMED)
                                ?.addEdge("opened", "\"", "strEND")
                    }
                    // character
                    TokenType.CHAR -> {
                        prototype?.getStateMachineBuilder()
                                ?.addState(State("opened"))
                                ?.addState(State("back_slash"))
                                ?.addState(State("a_char"))
                                ?.addState(State("charEND", StateType.END))

                                ?.addEdge("START", "\'", "opened")
                                ?.addEdge("opened", "\\\\", "back_slash")
                                ?.addEdge("back_slash", "OTHERS", "a_char", EdgeType.OTHERS_AND_CONSUMED)
                                ?.addEdge("opened", "OTHERS", "a_char", EdgeType.OTHERS_AND_CONSUMED)
                                ?.addEdge("a_char", "\'", "charEND")
                    }
                    // operator
                    TokenType.OPERATOR -> {
                        // TODO: operator's state-machine
                    }
                    TokenType.FLOATING -> {
                        prototype?.getStateMachineBuilder()
                                ?.addState(State("digits"))
                                ?.addState(State("a_dot"))
                                ?.addState(State("floating"))
                                ?.addState(State("floating_exp"))
                                ?.addState(State("floating_exp_sign"))
                                ?.addState(State("floating_exp_digits"))
                                ?.addState(State("floatEND", StateType.END))
                                ?.addState(State("doubleEND", StateType.END))

                                ?.addEdge("START", "[0-9]", "digits")
                                ?.addEdge("START", "\\.", "a_dot")
                                ?.addEdge("digits", "\\.", "floating")
                                ?.addEdge("digits", "[0-9]", "digits")
                                ?.addEdge("digits", "E|e", "floating_exp")
                                ?.addEdge("digits", "F|f", "floatEND")
                                ?.addEdge("a_dot", "[0-9]", "floating")
                                ?.addEdge("floating", "[0-9]", "floating")
                                ?.addEdge("floating", "E|e", "floating_exp")
                                ?.addEdge("floating", "F|f", "floatEND")
                                ?.addEdge("floating", "OTHERS", "doubleEND", EdgeType.OTHERS_AND_NOT_CONSUMED)
                                ?.addEdge("floating_exp", "[0-9]", "floating_exp_digits")
                                ?.addEdge("floating_exp", "\\+|\\-", "floating_exp_sign")
                                ?.addEdge("floating_exp_digits", "F|f", "floatEND")
                                ?.addEdge("floating_exp_digits", "[0-9]", "floating_exp_digits")
                                ?.addEdge("floating_exp_digits", "OTHERS", "doubleEND", EdgeType.OTHERS_AND_NOT_CONSUMED)
                                ?.addEdge("floating_exp_sign", "[0-9]", "floating_exp_digits")
                    }
                    TokenType.INTEGER -> {
                        prototype?.getStateMachineBuilder()
                                ?.addState(State("starts_w_zero"))
                                ?.addState(State("digits"))
                                ?.addState(State("digits_underline"))
                                ?.addState(State("binary_mode"))
                                ?.addState(State("octal_mode"))
                                ?.addState(State("hex_mode"))
                                ?.addState(State("binaries"))
                                ?.addState(State("binaries_underline"))
                                ?.addState(State("octals"))
                                ?.addState(State("octals_underline"))
                                ?.addState(State("hices"))
                                ?.addState(State("hices_underline"))
                                ?.addState(State("intEND", StateType.END))

                                ?.addEdge("START", "0", "starts_w_zero")
                                ?.addEdge("START", "[1-9]", "digits")
                                ?.addEdge("starts_w_zero", "b", "binary_mode")
                                ?.addEdge("starts_w_zero", "o", "octal_mode")
                                ?.addEdge("starts_w_zero", "x", "hex_mode")
                                ?.addEdge("starts_w_zero", "_", "digits_underline")
                                ?.addEdge("starts_w_zero", "[0-9]", "digits")
                                ?.addEdge("starts_w_zero", "OTHERS", "intEND", EdgeType.OTHERS_AND_NOT_CONSUMED)
                                ?.addEdge("digits", "[0-9]", "digits")
                                ?.addEdge("digits", "_", "digits_underline")
                                ?.addEdge("digits", "OTHERS", "intEND", EdgeType.OTHERS_AND_NOT_CONSUMED)
                                ?.addEdge("digits_underline", "[0-9]", "digits")
                                ?.addEdge("binary_mode", "[0-1]", "binaries")
                                ?.addEdge("binaries", "[0-1]", "binaries")
                                ?.addEdge("binaries", "_", "binaries_underline")
                                ?.addEdge("binaries", "OTHERS", "intEND", EdgeType.OTHERS_AND_NOT_CONSUMED)
                                ?.addEdge("binaries_underline", "[0-1]", "binaries")
                                ?.addEdge("octal_mode", "[0-7]", "octals")
                                ?.addEdge("octals", "[0-7]", "octals")
                                ?.addEdge("octals", "_", "octals_underline")
                                ?.addEdge("octals", "OTHERS", "intEND", EdgeType.OTHERS_AND_NOT_CONSUMED)
                                ?.addEdge("octals_underline", "[0-7]", "octals")
                                ?.addEdge("hex_mode", "[0-9A-Fa-f]", "hices")
                                ?.addEdge("hices", "[0-9A-Fa-f]", "hices")
                                ?.addEdge("hices", "_", "hices_underline")
                                ?.addEdge("hices", "OTHERS", "intEND", EdgeType.OTHERS_AND_NOT_CONSUMED)
                                ?.addEdge("hices_underline", "[0-9A-Fa-f]", "hices")
                    }
                    TokenType.SPACE -> {
                        prototype?.getStateMachineBuilder()
                                ?.addState(State("spaceEND", StateType.END))

                                ?.addEdge("START", "\\s+", "spaceEND")
                    }
                }
            }
            // endregion

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