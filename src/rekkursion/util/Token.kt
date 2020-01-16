package rekkursion.util

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import rekkursion.exception.LexemeAnalysisException
import rekkursion.manager.PreferenceManager
import rekkursion.util.statemachine.edge.EdgeType
import rekkursion.util.statemachine.state.StateMachine
import rekkursion.util.statemachine.state.StateType
import rekkursion.util.tool.FontStyle

// enum: token types for the lexeme analysis
enum class TokenType {
    // FLOATING includes float and double
    COMMENT, KEYWORD, IDENTIFIER, STRING, CHAR, OPERATOR, FLOATING, INTEGER, SPACE,
    UNKNOWN
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
    fun matches(texts: String, predefinedOperatorSymbols: Array<String>): Pair<Boolean, String> {
        var pointer = 0
        val textsLen = texts.length

        // begin at the START state
        var curState = mStateMachine.getStartState() ?: throw LexemeAnalysisException("No START state.")

        while (pointer < textsLen) {
            // currently at an ERROR state -> failed
            if (curState.type == StateType.ERROR)
                return Pair(false, texts.substring(0, pointer))

            // check if the simulation of this state-machine is blocked or not
            var blocked = true

            // try all out-going edges except for OTHERS
            for (outgoingEdge in curState.outgoingEdges.filter { it.type != EdgeType.OTHERS_AND_CONSUMED && it.type != EdgeType.OTHERS_AND_NOT_CONSUMED }) {
                // get the current character as a string
                val chStr = texts[pointer].toString()

                // try to match the character
                val matched: Boolean =
                // special case: operator -> use simple string matching
                if (this.mType == TokenType.OPERATOR) {
                    if (outgoingEdge.edgeText == chStr) {
                        curState = outgoingEdge.dstState
                        true
                    }
                    else false
                }
                // other cases -> use regular expression matching
                else {
                    if (outgoingEdge.type == EdgeType.GENERAL) {
                        if (outgoingEdge.edgeText.toRegex().matches(chStr)) {
                            curState = outgoingEdge.dstState
                            true
                        }
                        else false
                    }
                    else if (outgoingEdge.type == EdgeType.ACCEPTABLE_SYMBOLS) {
                        if ("\\s+".toRegex().matches(chStr) || predefinedOperatorSymbols.contains(chStr))
                            return Pair(true, texts.substring(0, pointer))
                        else false
                    }
                    else false
                }

                // currently at an ERROR state -> failed
                if (curState.type == StateType.ERROR)
                    return Pair(false, texts.substring(0, pointer))
                // other cases
                else {
                    // matched
                    if (matched) {
                        ++pointer
                        blocked = false
                        break
                    }
                }
            } // end of for-loop

            // if the simulation of this state-machine is blocked
            if (blocked) {
                // try the OTHERS edge
                val othersEdge = curState.outgoingEdges.find { it.type == EdgeType.OTHERS_AND_CONSUMED || it.type == EdgeType.OTHERS_AND_NOT_CONSUMED }

                // OTHERS edge does not exist
                if (othersEdge == null) {
                    // at an END state -> succeed
                    return if (curState.type == StateType.END)
                        Pair(true, texts.substring(0, pointer))
                    // not at an END state -> failed
                    else
                        Pair(false, texts.substring(0, pointer))
                }
                // OTHERS edge exists
                else {
                    ++pointer
                    curState = othersEdge.dstState
                }
            }
        } // end of while-loop

        // at an END state -> succeed
        return if (curState.type == StateType.END)
            Pair(true, texts.substring(0, pointer))
        // not at an END state -> failed
        else
            Pair(false, texts.substring(0, pointer))
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
        val offsetX =
                PreferenceManager.EditorPref.lineStartOffset + PreferenceManager.EditorPref.lineNumberAreaWidth

        // render back?ground
        gphCxt?.fill = mBasicFontStyle.bgColor
        gphCxt?.fillRect(
                caretX * PreferenceManager.EditorPref.charW + offsetX,
                caretY * PreferenceManager.EditorPref.lineH,
                partialText.length * PreferenceManager.EditorPref.charW,
                PreferenceManager.EditorPref.lineH
        )

        // render text
        gphCxt?.fill = mBasicFontStyle.fontColor
        gphCxt?.fillText(
                partialText,
                caretX * PreferenceManager.EditorPref.charW + offsetX,
                (caretY + 1) * PreferenceManager.EditorPref.lineH - 5
        )

        // render underline
        gphCxt?.fill = mBasicFontStyle.underlineColor
        gphCxt?.fillRect(
                caretX * PreferenceManager.EditorPref.charW + offsetX,
                (caretY + 1) * PreferenceManager.EditorPref.lineH - 2.5,
                partialText.length * PreferenceManager.EditorPref.charW,
                1.0
        )
    }

    // to-string
    override fun toString(): String = "Token(Type=$mType, Text='$mText')"
}