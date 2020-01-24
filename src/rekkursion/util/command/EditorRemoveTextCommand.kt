package rekkursion.util.command

import rekkursion.manager.SelectionManager
import rekkursion.model.EditorModel

enum class TextRemovingActionType {
    BACK_SPACE, DELETE, SELECTED
}

class EditorRemoveTextCommand(editorModel: EditorModel): EditorCommand {
    override val mEditor: EditorModel = editorModel

    /* number of args: 1
     * -----------------
     * 1. TextRemovingActionType: the type of text-removing action
     * */
    override fun execute(vararg args: Any?) {
        when (args[0]!! as TextRemovingActionType) {
            TextRemovingActionType.BACK_SPACE -> removeCharacterInFrontOfCaret()
            TextRemovingActionType.DELETE -> removeCharacterBehindCaret()
            TextRemovingActionType.SELECTED -> removeSelected()
        }
    }

    // back-space key pressed -> remove a character in front of the caret
    private fun removeCharacterInFrontOfCaret() {
        // delete the selected text
        if (mEditor.selectionManager.hasSelection()) {
            removeSelectedText()
            mEditor.searchAndSetLongestLine(true)
        }

        // delete a single character or '\n' in front of the caret
        else {
            // delete a single character in front of the caret
            if (mEditor.caretOffset > 0) {
                mEditor.deleteCharAt(mEditor.caretLineIdx, mEditor.caretOffset - 1)
                mEditor.setCaretOffset(mEditor.caretOffset - 1, true)
            }
            // delete a '\n'
            else {
                if (mEditor.caretLineIdx > 0) {
                    mEditor.setCaretOffset(mEditor.getTextLengthAt(mEditor.caretLineIdx - 1), true)
                    mEditor.appendTextAt(mEditor.caretLineIdx - 1, mEditor.getTextAtCurrentLine())
                    mEditor.removeCurrentLine()
                    mEditor.setCaretLineIndex(mEditor.caretLineIdx - 1)
                }
            }

            // find out and set the longest line
            mEditor.searchAndSetLongestLine(false)
        }
    }

    // delete key pressed -> remove a character behind the caret
    private fun removeCharacterBehindCaret() {
        // delete the selected text (as the same operation of back-space)
        if (mEditor.selectionManager.hasSelection()) {
            removeSelectedText()
            mEditor.searchAndSetLongestLine(true)
        }

        // delete a single character or '\n' behind the caret
        else {
            // delete a single character behind the caret
            if (mEditor.caretOffset < mEditor.getTextAtCurrentLine().length)
                mEditor.deleteCharAt(mEditor.caretLineIdx, mEditor.caretOffset)
            // delete a '\n'
            else {
                if (mEditor.caretLineIdx < mEditor.getNumOfLines() - 1) {
                    mEditor.appendTextAt(mEditor.caretLineIdx, mEditor.getTextAt(mEditor.caretLineIdx + 1))
                    mEditor.removeLineAt(mEditor.caretLineIdx + 1)
                }
            }

            // find out and set the longest line
            mEditor.searchAndSetLongestLine(false)
        }
    }

    // there's selected text -> remove the selected text
    private fun removeSelected() {
        removeSelectedText()
        mEditor.searchAndSetLongestLine(true)
    }
}