package rekkursion.manager

import javafx.scene.paint.Color
import javafx.scene.text.Font
import rekkursion.util.*
import rekkursion.util.statemachine.edge.EdgeType
import rekkursion.util.statemachine.state.State
import rekkursion.util.statemachine.state.StateType
import rekkursion.util.tool.FontStyle

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

    // editor preference
    object EditorPref {
        private const val BASIC_CHARA_WIDTH = 11.0
        private const val BASIC_LINE_HEIGHT = 26.0
        private const val BASIC_CARET_WIDTH = 1.0
        private const val BASIC_LINE_START_OFFSET = 6.0

        /* ========== */

        // region the ratio of the editor font size
        private var mEditorFontSizeRatio = 1.0
        var editorFontSizeRatio
            get() = mEditorFontSizeRatio
            set(value) { mEditorFontSizeRatio = value }

        // region get the true character width
        val charW get() = BASIC_CHARA_WIDTH * mEditorFontSizeRatio
        // get the true line height
        val lineH get() = BASIC_LINE_HEIGHT * mEditorFontSizeRatio
        // get the true caret width
        val caretW get() = BASIC_CARET_WIDTH * mEditorFontSizeRatio
        // get the true line start offset
        val lineStartOffsetX get() = BASIC_LINE_START_OFFSET * mEditorFontSizeRatio
        // get the true font size
        val fontSize get() = 20.0 * mEditorFontSizeRatio
        // get the true difference between true line-h and true font-size
        val differenceBetweenLineHeightAndFontSize get() = lineH - fontSize
        // endregion
        // endregion

        /* ========== */

        // region get the font
        val font: Font = Font.font("Consolas", fontSize)
        // endregion

        /* ========== */

        // region the background color of the editor
        private var mEditorBgClr: Color = Color.color(0.3, 0.3, 0.3)
        var editorBgClr
            get() = mEditorBgClr
            set(value) { mEditorBgClr = value }
        // endregion

        /* ========== */

        // region the color of the current selected line hint of the editor
        private var mSelectedLineHintClr: Color = Color.color(0.43, 0.41, 0.39)
        var selectedLineHintClr
            get() = mSelectedLineHintClr
            set(value) { mSelectedLineHintClr = value }
        // endregion

        /* ========== */

        // region step size when scrolling the editor
        private var mEditorScrollingStepSize = lineH * 3.0
        var editorScrollingStepSize
            get() = mEditorScrollingStepSize
            set(value) { mEditorScrollingStepSize = value }
        // endregion

        /* ========== */

        // region the blank's height when scrolling the lowest of the editor
        private var mBlankHeight = 150.0
        var blankHeight
            get() = mBlankHeight
            set(value) { mBlankHeight = value }
        // endregion

        /* ========== */

        // region the color of selection
        private var mSelectionClr: Color = Color.color(14 / 255.0, 96 / 255.0, 171 / 255.0)
        var selectionClr
            get() = mSelectionClr
            set(value) { mSelectionClr = value }
        // endregion

        /* ========== */

        // line number area object
        object LineNumberArea {
            // region the width of line number area
            private var mLineNumberAreaWidth = 75.0
            var width
                get() = mLineNumberAreaWidth
                set(value) { mLineNumberAreaWidth = value }
            // endregion

            /* ========== */

            // region the bg color of line number area
            private var mLineNumberAreaBgClr: Color = Color.color(0.35, 0.35, 0.35)
            var bgClr
                get() = mLineNumberAreaBgClr
                set(value) { mLineNumberAreaBgClr = value }
            // endregion

            /* ========== */

            // region the font color of line number area
            private var mLineNumberAreaFontClr: Color = Color.color(0.7, 0.7, 0.7)
            var fontClr
                get() = mLineNumberAreaFontClr
                set(value) { mLineNumberAreaFontClr = value }
            // endregion

            /* ========== */

            // region the selected-line font color of line number area
            private var mLineNumberAreaSelectedFontClr: Color = Color.color(0.94, 0.94, 0.94)
            var selectedFontClr
                get() = mLineNumberAreaSelectedFontClr
                set(value) { mLineNumberAreaSelectedFontClr = value }
            // endregion

            /* ========== */

            // region the width of vertical-line which is used to separate the line number area and the typing area
            private var mVerticalLineWidth = 0.4
            var verticalLineWidth
                get() = mVerticalLineWidth
                set(value) { mVerticalLineWidth = value }
            // endregion

            /* ========== */

            // region the x-offset of line numbers
            private var mLineNumberOffsetX = 6.0
            var numberOffsetX
                get() = mLineNumberOffsetX
                set(value) { mLineNumberOffsetX = value }
            // endregion
        }

        /* ========== */

        // some settings about typing operations
        object Typing {
            // the symmetric symbols
            val symmetricSymbols = hashMapOf<String, String>(
                    "(" to ")",
                    "[" to "]",
                    "{" to "}",
                    "/*" to "*/"
            )
        }
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
                    .addPredefinedOperators(
                            "+", "-", "*", "/", "%", "&", "|", "^", "**",
                            "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "**=", "=",
                            "++", "--",
                            ",", ".", ";", "?", ":", "->",
                            "(", ")", "[", "]", "{", "}",
                            "!", ">", "<", "==", ">=", "<=", "!="
                    )
                    .addPredefinedKeywords(
                            "if", "else", "elif", "switch", "case", "default", "when",
                            "for", "while", "repeat", "break", "continue", "goto",
                            "var", "fun", "return",
                            "try", "throw", "catch", "finally",
                            "int8", "int16", "int32", "int64", "float", "double", "string", "char",
                            "za_warudo"
                    )
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
                            FontStyle.Builder().setFontColor(Color.LIGHTGREEN).create()
                    ))
                    // character
                    .addTokenPrototype(TokenPrototype(
                            TokenType.CHAR,
                            arrayOf(
                                    "^\'.\'".toRegex()
                            ),
                            FontStyle.Builder().setFontColor(Color.GREENYELLOW).create()
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
                            FontStyle.Builder().setFontColor(Color.PEACHPUFF).create()
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
                    // unknown
                    .addTokenPrototype(TokenPrototype(
                            TokenType.UNKNOWN,
                            arrayOf(),
                            FontStyle.Builder()
                                    .setFontColor(Color.WHITE)
                                    .setBgColor(Color.color(0.3, 0.3, 0.3, 0.6))
                                    .setStyle(isUnderlined = true, underlineColor = Color.RED)
                                    .create()
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
                        prototype?.getStateMachineBuilder()
                                ?.buildStateMachineByLiteralSymbols(xogue.predefinedKeywords)
                    }
                    // identifier (un-closing problem)
                    TokenType.IDENTIFIER -> {
                        prototype?.getStateMachineBuilder()
                                ?.addState(State("building"))
                                ?.addState(State("ERROR", StateType.ERROR))
                                ?.addState(State("identifierEND", StateType.END))

                                // acceptable: \\s+, operators
                                ?.addEdge("START", "[_A-Za-z]", "building")
                                ?.addEdge("building", "[_A-Za-z0-9]", "building")
                                ?.addEdge("building", "ACCEPTABLE_SYMBOLS", "identifierEND", EdgeType.ACCEPTABLE_SYMBOLS)
                                ?.addEdge("building", "OTHERS", "ERROR", EdgeType.OTHERS_AND_NOT_CONSUMED)
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
                        prototype?.getStateMachineBuilder()
                                ?.buildStateMachineByLiteralSymbols(xogue.predefinedOperators)
                    }
                    // floating point (un-closing problem)
                    TokenType.FLOATING -> {
                        prototype?.getStateMachineBuilder()
                                ?.addState(State("digits"))
                                ?.addState(State("a_dot"))
                                ?.addState(State("floating"))
                                ?.addState(State("floating_exp"))
                                ?.addState(State("floating_exp_sign"))
                                ?.addState(State("floating_exp_digits"))
                                ?.addState(State("ERROR", StateType.ERROR))
                                ?.addState(State("floatEND", StateType.END))
                                ?.addState(State("doubleEND", StateType.END))
                                // acceptable: //s+, operators
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
                                ?.addEdge("floating", "ACCEPTABLE_SYMBOLS", "doubleEND", EdgeType.ACCEPTABLE_SYMBOLS)
                                ?.addEdge("floating", "OTHERS", "ERROR", EdgeType.OTHERS_AND_NOT_CONSUMED)
                                ?.addEdge("floating_exp", "[0-9]", "floating_exp_digits")
                                ?.addEdge("floating_exp", "\\+|\\-", "floating_exp_sign")
                                ?.addEdge("floating_exp_digits", "F|f", "floatEND")
                                ?.addEdge("floating_exp_digits", "[0-9]", "floating_exp_digits")
                                ?.addEdge("floating_exp_digits", "ACCEPTABLE_SYMBOLS", "doubleEND", EdgeType.ACCEPTABLE_SYMBOLS)
                                ?.addEdge("floating_exp_digits", "OTHERS", "ERROR", EdgeType.OTHERS_AND_NOT_CONSUMED)
                                ?.addEdge("floating_exp_sign", "[0-9]", "floating_exp_digits")
                    }
                    // integer (un-closing problem)
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
                                ?.addState(State("ERROR", StateType.ERROR))
                                ?.addState(State("intEND", StateType.END))
                                // acceptable: //s+, operators
                                ?.addEdge("START", "0", "starts_w_zero")
                                ?.addEdge("START", "[1-9]", "digits")
                                ?.addEdge("starts_w_zero", "b", "binary_mode")
                                ?.addEdge("starts_w_zero", "o", "octal_mode")
                                ?.addEdge("starts_w_zero", "x", "hex_mode")
                                ?.addEdge("starts_w_zero", "_", "digits_underline")
                                ?.addEdge("starts_w_zero", "[0-9]", "digits")
                                ?.addEdge("starts_w_zero", "ACCEPTABLE_SYMBOLS", "intEND", EdgeType.ACCEPTABLE_SYMBOLS)
                                ?.addEdge("starts_w_zero", "OTHERS", "ERROR", EdgeType.OTHERS_AND_NOT_CONSUMED)
                                ?.addEdge("digits", "[0-9]", "digits")
                                ?.addEdge("digits", "_", "digits_underline")
                                ?.addEdge("digits", "ACCEPTABLE_SYMBOLS", "intEND", EdgeType.ACCEPTABLE_SYMBOLS)
                                ?.addEdge("digits", "OTHERS", "ERROR", EdgeType.OTHERS_AND_NOT_CONSUMED)
                                ?.addEdge("digits_underline", "[0-9]", "digits")
                                ?.addEdge("binary_mode", "[0-1]", "binaries")
                                ?.addEdge("binaries", "[0-1]", "binaries")
                                ?.addEdge("binaries", "_", "binaries_underline")
                                ?.addEdge("binaries", "ACCEPTABLE_SYMBOLS", "intEND", EdgeType.ACCEPTABLE_SYMBOLS)
                                ?.addEdge("binaries", "OTHERS", "ERROR", EdgeType.OTHERS_AND_NOT_CONSUMED)
                                ?.addEdge("binaries_underline", "[0-1]", "binaries")
                                ?.addEdge("octal_mode", "[0-7]", "octals")
                                ?.addEdge("octals", "[0-7]", "octals")
                                ?.addEdge("octals", "_", "octals_underline")
                                ?.addEdge("octals", "ACCEPTABLE_SYMBOLS", "intEND", EdgeType.ACCEPTABLE_SYMBOLS)
                                ?.addEdge("octals", "OTHERS", "ERROR", EdgeType.OTHERS_AND_NOT_CONSUMED)
                                ?.addEdge("octals_underline", "[0-7]", "octals")
                                ?.addEdge("hex_mode", "[0-9A-Fa-f]", "hices")
                                ?.addEdge("hices", "[0-9A-Fa-f]", "hices")
                                ?.addEdge("hices", "_", "hices_underline")
                                ?.addEdge("hices", "ACCEPTABLE_SYMBOLS", "intEND", EdgeType.ACCEPTABLE_SYMBOLS)
                                ?.addEdge("hices", "OTHERS", "ERROR", EdgeType.OTHERS_AND_NOT_CONSUMED)
                                ?.addEdge("hices_underline", "[0-9A-Fa-f]", "hices")
                    }
                    // space
                    TokenType.SPACE -> {
                        prototype?.getStateMachineBuilder()
                                ?.addState(State("spaceEND", StateType.END))

                                ?.addEdge("START", "\\s+", "spaceEND")
                    }
                }
            }
            // endregion

            TokenType.values().forEach { tokenType ->
                val prototype = xogue.getTokenPrototype(tokenType)
                println(tokenType.name)
                prototype?.getStateMachineBuilder()?.create()?.print()
                println("--------------------------------------------------------\n")
            }

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