package rekkursion.util

import javafx.scene.canvas.GraphicsContext
import rekkursion.manager.PreferenceManager
import rekkursion.util.statemachine.state.StateMachine

// enum: token types for the lexeme analysis
enum class TokenType {
    // FLOATING includes float and double
    COMMENT, KEYWORD, IDENTIFIER, STRING, CHAR, OPERATOR, FLOATING, INTEGER, SPACE
}

/* ===================================================================== */

// the token prototypes are the same in a certain language
// but are differentiated by languages
class TokenPrototype(type: TokenType, regexArr: Array<Regex>, fontStyle: FontStyle) {
    // the token type
    private val mType = type
    val type get() = mType

    // all regular expressions of this token type
    private val mRegexArray = Array(regexArr.size) { regexArr[it] }

    // the font-style of this token type
    private var mFontStyle = fontStyle
    var fontStyle
        get() = mFontStyle
        set(value) { mFontStyle = value }

    // the state-machine of this token prototype for the lexeme analysis
    private val mStateMachine: StateMachine = StateMachine()

    /* ===================================================================== */

    // check if a certain piece of text matches this token or not
    fun matches(text: String): Pair<Boolean, String> {
        for (regex in mRegexArray) {
            val pattern = regex.toPattern()
            val matcher = pattern.matcher(text)
            if (matcher.find())
                return Pair(true, matcher.group())
        }
        return Pair(false, "")
    }

    // get the state-machine builder
    fun getStateMachineBuilder(): StateMachine.Builder = mStateMachine.getBuilder()
}

/* ===================================================================== */

// the real tokens are differentiated in a certain language
class Token(type: TokenType, text: String, basicFontStyle: FontStyle) {
    // the type of this token
    private val mType: TokenType = type
    val type get() = mType

    // the raw text of this token
    private val mText: String = text
    val text get() = mText

    // the basic font-style of this token
    private val mBasicFontStyle: FontStyle = basicFontStyle
    val basicFontStyle get() = mBasicFontStyle

    // render this token
    fun render(gphCxt: GraphicsContext?, caretX: Int, caretY: Int, partialText: String = mText) {
        val lineStartOffset = PreferenceManager.EditorPref.lineStartOffset

        // render back?ground
        gphCxt?.fill = mBasicFontStyle.bgColor
        gphCxt?.fillRect(
                caretX * PreferenceManager.EditorPref.charW + lineStartOffset,
                caretY * PreferenceManager.EditorPref.lineH,
                partialText.length * PreferenceManager.EditorPref.charW,
                PreferenceManager.EditorPref.lineH
        )

        // render text
        gphCxt?.fill = mBasicFontStyle.fontColor
        gphCxt?.fillText(
                partialText,
                caretX * PreferenceManager.EditorPref.charW + lineStartOffset,
                (caretY + 1) * PreferenceManager.EditorPref.lineH - 5
        )

        // render underline
        gphCxt?.fill = mBasicFontStyle.underlineColor
        gphCxt?.fillRect(
                caretX * PreferenceManager.EditorPref.charW + lineStartOffset,
                (caretY + 1) * PreferenceManager.EditorPref.lineH - 2.5,
                partialText.length * PreferenceManager.EditorPref.charW,
                1.0
        )
    }

    // to-string
    override fun toString(): String = "Token(Type=$mType, Text='$mText')"
}