package rekkursion.util.command

import rekkursion.manager.SelectionManager
import rekkursion.model.EditorModel

interface EditorCommand: Command {
    // the editor to be operated
    val mEditor: EditorModel

    // remove the selected text
    fun removeSelectedText() {
        val pair = mEditor.selectionManager.removeSelectedText(mEditor.textBuffersAndTokens)
        if (pair != null) {
            mEditor.setCaretLineIndex(pair.first)
            mEditor.setCaretOffset(pair.second, true)
        }
    }
}