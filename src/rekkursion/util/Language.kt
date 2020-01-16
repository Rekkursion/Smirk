package rekkursion.util

class Language(langName: String) {
    // language builder
    class Builder(langName: String) {
        private val mLang: Language = Language(langName)

        // create the language
        fun create(): Language = mLang

        // add the token prototype into this language
        fun addTokenPrototype(tokenPrototype: TokenPrototype): Builder {
            mLang.mTokenPrototypes.add(tokenPrototype)
            return this
        }

        // add pre-defined operator symbols
        fun addPredefinedOperators(vararg operatorSymbols: String): Builder {
            for (opSym in operatorSymbols)
                mLang.mPredefinedOperatorSymbols.add(opSym)
            return this
        }

        // add pre-defined keywords
        fun addPredefinedKeywords(vararg keywords: String): Builder {
            for (kw in keywords)
                mLang.mPredefinedKeywords.add(kw)
            return this
        }
    }

    /* ===================================================================== */

    // the language's name
    private val mLangName = langName
    val name get() = mLangName

    // the prototypes of each kind of tokens
    private val mTokenPrototypes = ArrayList<TokenPrototype>()

    // pre-defined operators
    private val mPredefinedOperatorSymbols = ArrayList<String>()
    val predefinedOperators get() = mPredefinedOperatorSymbols.toTypedArray()

    // pre-defined keywords
    private val mPredefinedKeywords = ArrayList<String>()
    val predefinedKeywords get() = mPredefinedKeywords.toTypedArray()

    // compiler of this language
    private val mCompiler = Compiler(this)

    /* ===================================================================== */

    // get a certain token prototype
    fun getTokenPrototype(tokenType: TokenType): TokenPrototype? {
        if (tokenType.ordinal < 0 || tokenType.ordinal >= mTokenPrototypes.size)
            return null
        return mTokenPrototypes[tokenType.ordinal]
    }

    // compile a certain piece of code
    fun compile(code: String): ArrayList<Token>? {
        return mCompiler.analyzeLexeme(code)
    }

    // deprecated: classify the code into tokens
    fun classifyIntoTokens(code: String): ArrayList<Token>? {
        val ret = ArrayList<Token>()
        var pointer = 0
        val codeLen = code.length

        // iterate the whole raw code
        while (pointer < codeLen) {
            var matched = false
            val subCode = code.substring(pointer)

            // if it's a literal string
            if (subCode[0] == '\"' || subCode[0] == '\'') {
                val nextQuoteCharIdx =
                        if (subCode[0] == '\"')
                            subCode.indexOf('\"', 1)
                        else
                            subCode.indexOf('\'', 1)

                if (nextQuoteCharIdx == -1)
                    return null

                ret.add(Token(
                        TokenType.STRING,
                        subCode.substring(0, nextQuoteCharIdx + 1),
                        mTokenPrototypes[TokenType.STRING.ordinal].fontStyle
                ))
                pointer = nextQuoteCharIdx + 1

                continue
            }

            // check every kind of tokens
            for (tokenType in TokenType.values()) {
                // get the prototype of this token
                val tokenPrototype = mTokenPrototypes[tokenType.ordinal]

                // try matching
                val pair: Pair<Boolean, String> = tokenPrototype.matches(subCode, arrayOf())

                // if matches successfully
                if (pair.first) {
                    ret.add(Token(tokenPrototype.type, pair.second, tokenPrototype.fontStyle))
                    pointer += pair.second.length

                    matched = true
                    break
                }
            }

            if (!matched)
                return null
        }

        return ret
    }
}