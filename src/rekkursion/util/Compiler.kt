package rekkursion.util

import rekkursion.exception.LexemeAnalysisException
import rekkursion.exception.NoTokenTypeException
import java.lang.Exception

class Compiler(language: Language) {
    // the language of this compiler
    private val mLang = language

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

            // try all of token types
            for (tokenType in TokenType.values().filter { it.name != "UNKNOWN" }) {
                // get the token prototype
                val prototype = mLang.getTokenPrototype(tokenType) ?: throw NoTokenTypeException()

                // try matching with this prototype
                val (matched, matchedString)
                        = prototype.matches(code.substring(pointer), mLang.predefinedOperators)

                // if matched
                if (matched) {
                    pointer += matchedString.length
                    allTokenTypesFailed = false
                    ret.add(Token(tokenType, matchedString, prototype.fontStyle))
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
                ret.add(Token(TokenType.UNKNOWN, longestFailedMatchedString, TokenPrototype.unknownTokenFontStyle))
                // throw LexemeAnalysisException("Unknown token type.")
            }
        }

        return ret
    }
}