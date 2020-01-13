package rekkursion.util

import java.util.regex.Matcher
import java.util.regex.Pattern

// enum: token types for the lexeme analysis
enum class TokenType {
    COMMENT, KEYWORD, STRING, OPERATOR, IDENTIFIER, FLOAT, INTEGER, SPACE
}

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
}

// the real tokens are differentiated in a certain language
class Token(type: TokenType, text: String, basicFontStyle: FontStyle) {
    // the type of this token
    private val mType: TokenType = type

    // the raw text of this token
    private val mText: String = text

    // the basic font-style of this token
    private val mBasicFontStyle: FontStyle = basicFontStyle

    // to-string
    override fun toString(): String = "Token(Type=$mType, Text='$mText')"
}