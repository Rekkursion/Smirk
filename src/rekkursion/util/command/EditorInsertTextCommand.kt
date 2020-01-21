package rekkursion.util.command

import rekkursion.manager.PreferenceManager
import rekkursion.manager.SelectionManager
import rekkursion.model.EditorModel

class EditorInsertTextCommand(editorModel: EditorModel, selectionManager: SelectionManager): EditorCommand {
    override val mEditor: EditorModel = editorModel
    override val mSelectionManager: SelectionManager = selectionManager

    /* number of args: 1
     * -----------------
     * 1. String: the text which will be inserted (could be single-line text or multiple-line text)
     * */
    override fun execute(vararg args: Any?) {
        // clear the selected texts before the insertion
        removeSelectedText()

        // get the to-be-inserted text (could be single-line or multiple-line)
        val text = args[0]!! as String

        // the passed text has at least one '\n' -> multiple lines
        if (text.contains("\n"))
            insertMultipleLineText(text.split("\n"))
        // otherwise -> single line
        else
            insertSingleLineText(text)
    }

    // insert the single-line text
    private fun insertSingleLineText(text: String) {
        // get the length of this single-line text
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

    // insert the multiple-line text
    private fun insertMultipleLineText(texts: List<String>) {
        mEditor.addMultipleLinesOfTextsAtCurrentCaretLocation(texts)
    }
}