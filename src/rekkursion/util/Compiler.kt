package rekkursion.util

import rekkursion.exception.NoTokenTypeException

class Compiler(language: Language) {
    // the language of this compiler
    private val mLang = language

    /* ===================================================================== */

    // do the lexeme analysis
    fun analyzeLexeme(code: String): ArrayList<Token> {
        // pointer of the processing code
        var pointer = 0

        // length of the processing code
        val codeLen = code.length

        // the return value: an array-list of analyzed tokens
        val ret = ArrayList<Token>()

        // iterate the whole code
        while (pointer < codeLen) {
            // check if all token types are failed for matching
            var allTokenTypesFailed = true

            // store all failed matched sub-strings
            var longestFailedMatchedString = ""

            // try all of token types except for KEYWORD and UNKNOWN
            for (tokenType in TokenType.values().filter { it.name != "KEYWORD" && it.name != "UNKNOWN" }) {
                // get the token prototype
                val prototype = mLang.getTokenPrototype(tokenType) ?: throw NoTokenTypeException()

                // try matching with this prototype
                val (matched, matchedString)
                        = prototype.matches(code.substring(pointer), mLang.predefinedOperators)

                // get the matched token-type (needs since the matched IDENTIFIER could be KEYWORD)
                val matchedTokenType =
                // if the current trying prototype is the type of IDENTIFIER -> try matching KEYWORD
                if (prototype.type == TokenType.IDENTIFIER && matched && mLang.predefinedKeywords.contains(matchedString))
                    TokenType.KEYWORD
                // else -> the original tried token-type
                else
                    tokenType

                // if matched
                if (matched) {
                    pointer += matchedString.length
                    allTokenTypesFailed = false
                    ret.add(Token(matchedTokenType, matchedString, mLang.getTokenPrototype(matchedTokenType)!!.fontStyle))
                    break
                }
                // not matched
                else {
                    if (matchedString.length > longestFailedMatchedString.length)
                        longestFailedMatchedString = matchedString
                }
            }

            // if all token types failed
            if (allTokenTypesFailed) {
                pointer += longestFailedMatchedString.length
                ret.add(Token(TokenType.UNKNOWN, longestFailedMatchedString, mLang.getTokenPrototype(TokenType.UNKNOWN)!!.fontStyle))
                // throw LexemeAnalysisException("Unknown token type.")
            }
        }

        return ret
    }
}