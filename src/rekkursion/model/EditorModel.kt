package rekkursion.model

import rekkursion.util.Camera
import rekkursion.util.Token
import rekkursion.util.tool.MutablePair
import java.util.ArrayList

class EditorModel(editorWidth: Double, editorHeight: Double) {
    // the camera
    private val mCamera = Camera(width = editorWidth, height = editorHeight)
    val camera get() = mCamera

    // the current line index of the caret
    private var mCaretLineIdx = 0
    val caretLineIdx get() = mCaretLineIdx

    // the current offset at a certain line of the caret
    private var mCaretOffset = 0
    val caretOffset get() = mCaretOffset

    // be used when going up/down
    private var mOrigestCaretOffset = 0
    val origestCaretOffset get() = mOrigestCaretOffset

    // the longest line (the line which has the most characters, including spaces)
    /* in the pair: first = line-index, second = length of that line */
    private var mLongestLine = MutablePair(0, 0)
    var longestLine get() = mLongestLine; set(value) { mLongestLine = value }

    // for each line: text buffer & analyzed tokens
    private val mTextBuffersAndTokens = ArrayList<MutablePair<StringBuffer, ArrayList<Token>>>()
    val textBuffersAndTokens get() = mTextBuffersAndTokens

    /* ===================================================================== */

    // get the total number of lines
    fun getNumOfLines(): Int = mTextBuffersAndTokens.size

    // get the text at a certain line
    fun getTextAt(lineIdx: Int): String = mTextBuffersAndTokens[lineIdx].first.toString()

    // get the text at the current line-index
    fun getTextAtCurrentLine(): String = getTextAt(caretLineIdx)

    // get the length of the text at a certain line
    fun getTextLengthAt(lineIdx: Int): Int = getTextAt(lineIdx).length

    // get the string-buffer and its corresponding tokens at a certain line
    fun getTextBufferAndTokensAt(lineIdx: Int): MutablePair<StringBuffer, ArrayList<Token>> = mTextBuffersAndTokens[lineIdx]

    /* ===================================================================== */

    // set the caret-line-index
    fun setCaretLineIndex(newCaretLineIndex: Int) { mCaretLineIdx = newCaretLineIndex }

    // set the caret-offset
    fun setCaretOffset(newCaretOffset: Int, modifyOrigest: Boolean) {
        mCaretOffset = newCaretOffset
        if (modifyOrigest) mOrigestCaretOffset = mCaretOffset
    }

    // search and set the longest line
    fun searchAndSetLongestLine(searchWholeText: Boolean) {
        // search the whole text in the editor
        if (searchWholeText) {
            Thread {
                mTextBuffersAndTokens.forEachIndexed { index, (buffer, _) ->
                    if (buffer.length > mLongestLine.second)
                        mLongestLine.setPair(index, buffer.length)
                }
            }.start()
        }
        // only search the 3 lines round the current caret's line-index
        else {
            if (getTextAtCurrentLine().length > mLongestLine.second)
                mLongestLine.setPair(mCaretLineIdx, getTextAtCurrentLine().length)
            if (mCaretLineIdx > 0 && mTextBuffersAndTokens[mCaretLineIdx - 1].first.length > mLongestLine.second)
                mLongestLine.setPair(mCaretLineIdx - 1, mTextBuffersAndTokens[mCaretLineIdx - 1].first.length)
            if (mCaretLineIdx < getNumOfLines() - 1 && mTextBuffersAndTokens[mCaretLineIdx + 1].first.length > mLongestLine.second)
                mLongestLine.setPair(mCaretLineIdx + 1, mTextBuffersAndTokens[mCaretLineIdx + 1].first.length)
        }
    }

    /* ===================================================================== */

    // insert a new line at a certain line-index
    fun insertNewLine(lineIdx: Int, presetText: String = "") {
        mTextBuffersAndTokens.add(lineIdx, MutablePair(StringBuffer(presetText), arrayListOf()))
    }

    // insert text at a certain line and a certain text index
    fun insertTextAt(lineIdx: Int, textIdx: Int, text: String) {
        mTextBuffersAndTokens[lineIdx].first.insert(textIdx, text)
    }

    // append text at a certain line
    fun appendTextAt(lineIdx: Int, text: String) {
        mTextBuffersAndTokens[lineIdx].first.append(text)
    }

    // add multi-lines of texts at the current caret's location
    fun addMultipleLinesOfTextsAtCurrentCaretLocation(lines: List<String>) {
        var first = true
        var tmpLastHalf = ""
        // insert them
        lines.forEach { line ->
            // add a whole new line if it's NOT the first line
            if (!first) {
                insertNewLine(mCaretLineIdx + 1)
                ++mCaretLineIdx
                mCaretOffset = 0
                mOrigestCaretOffset = mCaretOffset
            }

            // get the last-half text if it's the first line
            if (first) {
                tmpLastHalf = mTextBuffersAndTokens[mCaretLineIdx].first.substring(mCaretOffset)
                mTextBuffersAndTokens[mCaretLineIdx].first.delete(mCaretOffset, getTextAtCurrentLine().length)
            }

            // append behind the current caret
            mTextBuffersAndTokens[mCaretLineIdx].first.append(line)
            mCaretOffset += line.length
            mOrigestCaretOffset = mCaretOffset

            // not the first time at all
            first = false
        }

        // append the the first line's last-half into the last line
        mTextBuffersAndTokens[mCaretLineIdx].first.append(tmpLastHalf)
    }

    // remove a certain line
    fun removeLineAt(lineIdx: Int) {
        mTextBuffersAndTokens.removeAt(lineIdx)
    }

    // remove the current line
    fun removeCurrentLine() {
        removeLineAt(mCaretLineIdx)
    }

    // delete a character at a certain line and a certain text index
    fun deleteCharAt(lineIdx: Int, textIdx: Int) {
        mTextBuffersAndTokens[lineIdx].first.deleteCharAt(textIdx)
    }

    // delete a piece of sub-string at a certain line
    fun deleteSubstringAt(lineIdx: Int, textStartIdx: Int, textEndIdx: Int) {
        mTextBuffersAndTokens[lineIdx].first.delete(textStartIdx, textEndIdx)
    }
}