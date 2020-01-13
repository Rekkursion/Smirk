package rekkursion.util

// enum: token types for the lexeme analysis
enum class TokenType {
    COMMENT, KEYWORD, STRING, OPERATOR, IDENTIFIER, INTEGER, FLOAT, SPACE
}

// the token prototypes are the same in a certain language
// but are differentiated by languages
class TokenPrototype(type: TokenType, regex: Regex, fontStyle: FontStyle) {
    // the token type
    private val mType = type
    val type get() = mType

    // the regular expression of this token type
    private val mRegex = regex
    val regex get() = mRegex

    // the font-style of this token type
    private var mFontStyle = fontStyle
    var fontStyle
        get() = mFontStyle
        set(value) { mFontStyle = value }
}

// the real tokens are differentiated in a certain language
class Token(type: TokenType, fontStyle: FontStyle) {
}