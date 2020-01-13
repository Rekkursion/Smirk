package rekkursion.util

class Language(
        langName: String,
        tokenPrototypes: Array<TokenPrototype>) {

    // the language's name
    private val mLangName = langName

    // the prototypes of each kind of tokens
    private var mTokenPrototypes: Array<TokenPrototype>
            = Array(TokenType.values().size) { tokenPrototypes[it] }
}