package rekkursion.util.command

import rekkursion.manager.SelectionManager
import rekkursion.model.EditorModel

interface EditorCommand: Command {
    // the editor to be operated
    val mEditor: EditorModel

    // the selection-manager for selection management
    val mSelectionManager: SelectionManager

    // clear the selection if needed
    fun clearSelection() {
        val pair = mSelectionManager.removeSelectedText(mEditor.textBuffersAndTokens)
        if (pair != null) {
            mEditor.setCaretLineIndex(pair.first)
            mEditor.setCaretOffset(pair.second, true)
        }
    }
}