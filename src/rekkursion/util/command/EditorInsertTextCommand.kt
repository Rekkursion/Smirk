package rekkursion.util.command

import rekkursion.manager.PreferenceManager
import rekkursion.model.EditorModel

class EditorInsertTextCommand(editorModel: EditorModel): EditorCommand {
    // the editor to be operated
    override val mEditor: EditorModel = editorModel

    // execute the operation
    override fun execute(vararg args: Any?) {
        // get the to-be-inserted text and its length
        val text = args[0]!! as String
        val textLen = text.length

        // append to the string-buffer
        mEditor.insertTextAt(mEditor.caretLineIdx, mEditor.caretOffset, text)

        // update the caret offset
        mEditor.setCaretOffset(mEditor.caretOffset + textLen, true)

        // if the character is a symmetric character -> add the symmetric one
        if (PreferenceManager.EditorPref.Typing.symmetricSymbols.containsKey(text))
            mEditor.insertTextAt(mEditor.caretLineIdx, mEditor.caretOffset, PreferenceManager.EditorPref.Typing.symmetricSymbols[text]!!)

        // find out and set the longest line
        mEditor.searchAndSetLongestLine(false)
    }
}