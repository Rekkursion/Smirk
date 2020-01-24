package rekkursion.manager

import javafx.scene.canvas.GraphicsContext
import rekkursion.util.Camera
import rekkursion.util.Token
import rekkursion.util.tool.MutablePair
import rekkursion.util.tool.TextInterval
import java.util.ArrayList
import kotlin.math.abs
import kotlin.math.min

class SelectionManager {
    // selected text
    private var mPrimarySelection: TextInterval? = null
    val primarySelection get() = mPrimarySelection?.copy()

    /* ===================================================================== */

    // exclusize (selected -> unselected, unselected -> selected) the selection
    fun exclusizeSelection(startLineIdx: Int, startLineOffset: Int, endLineIdx: Int, endLineOffset: Int) {
        val newInterval = TextInterval(startLineIdx, startLineOffset, endLineIdx, endLineOffset)

        if (mPrimarySelection == null)
            mPrimarySelection = newInterval
        else
            mPrimarySelection?.setEnd(newInterval.endCopied)
    }

    // clear all selections
    fun clearSelections() {
        mPrimarySelection = null
    }

    // check if there's any selection
    fun hasSelection() = mPrimarySelection != null

    // get the selected text
    fun getSelectedText(buffersAndTokens: ArrayList<MutablePair<StringBuffer, ArrayList<Token>>>): String {
        mPrimarySelection?.let {
            // get the smaller line- index & offset
            val (smLineIdx, smLineOffset) = getSmallerBound()!!

            // get the bigger line- index & offset
            val (bgLineIdx, bgLineOffset) = getBiggerBound()!!

            // build the string by string-buffer
            val sBuf = StringBuffer()
            // single line
            if (smLineIdx == bgLineIdx)
                sBuf.append(buffersAndTokens[smLineIdx].first.toString().substring(smLineOffset, bgLineOffset))
            // multiple lines
            else {
                for (lineIdx in smLineIdx..bgLineIdx) {
                    when (lineIdx) {
                        smLineIdx -> sBuf.append(buffersAndTokens[lineIdx].first.toString().substring(smLineOffset), "\n")
                        bgLineIdx -> sBuf.append(buffersAndTokens[lineIdx].first.toString().substring(0, bgLineOffset))
                        else -> sBuf.append(buffersAndTokens[lineIdx].first.toString(), "\n")
                    }
                }
            }

            // return the built string
            return sBuf.toString()
        }

        // no selection, return the empty string
        return ""
    }

    // set the primary-selection
    fun setPrimarySelection(
            startLineIdx: Int? = primarySelection?.startCopied?.first,
            startLineOffset: Int? = primarySelection?.startCopied?.second,
            endLineIdx: Int? = primarySelection?.endCopied?.first,
            endLineOffset: Int? = primarySelection?.endCopied?.second) {
        mPrimarySelection?.setStart(startLineIdx, startLineOffset)
        mPrimarySelection?.setEnd(endLineIdx, endLineOffset)
    }

    // remove the selected text
    fun removeSelectedText(buffersAndTokens: ArrayList<MutablePair<StringBuffer, ArrayList<Token>>>): MutablePair<Int, Int>? {
        mPrimarySelection?.let {
            // get the smaller line- index & offset
            val (smLineIdx, smLineOffset) = getSmallerBound()!!

            // get the bigger line- index & offset
            val (bgLineIdx, bgLineOffset) = getBiggerBound()!!

            // single line
            if (smLineIdx == bgLineIdx)
                buffersAndTokens[smLineIdx].first.delete(smLineOffset, bgLineOffset)
            // multiple lines
            else {
                // the smallest-line: delete the last-half of the sub-string
                buffersAndTokens[smLineIdx].first.delete(smLineOffset, buffersAndTokens[smLineIdx].first.length)
                // then put the biggest-line's last-half of the sub-string
                buffersAndTokens[smLineIdx].first.append(buffersAndTokens[bgLineIdx].first.toString().substring(bgLineOffset))
                // last step, remove all lines except for the smallest-line
                buffersAndTokens.removeAll(buffersAndTokens.subList(smLineIdx + 1, bgLineIdx + 1))
            }

            // clear selections
            clearSelections()

            // return the new caret location (line-index & caret-offset)
            return MutablePair(smLineIdx, smLineOffset)
        }

        return null
    }

    // render the selection effect
    fun renderSelectionBackground(gphCxt: GraphicsContext?, camera: Camera) {
        // if main-selection is NOT null
        mPrimarySelection?.let { primarySelection ->
            // get the offset-x of typing area
            val offsetX = PreferenceManager.EditorPref.lineStartOffsetX + PreferenceManager.EditorPref.LineNumberArea.width

            // set the text-selection background color
            gphCxt?.fill = PreferenceManager.EditorPref.selectionClr

            // get start's & end's line-indices & line-offsets
            val startLineIdx = primarySelection.startCopied.first
            val startLineOffset = primarySelection.startCopied.second
            val endLineIdx = primarySelection.endCopied.first
            val endLineOffset = primarySelection.endCopied.second

            // if start & end are at the same line (has only a single line)
            if (startLineIdx == endLineIdx) {
                val selectionLen = abs(startLineOffset - endLineOffset)
                val smaller = min(startLineOffset, endLineOffset)
                gphCxt?.fillRect(
                        smaller * PreferenceManager.EditorPref.charW + offsetX - camera.locX,
                        startLineIdx * PreferenceManager.EditorPref.lineH - camera.locY,
                        selectionLen * PreferenceManager.EditorPref.charW,
                        PreferenceManager.EditorPref.lineH
                )
            }

            // has multiple lines
            else {
                /* sm = smaller, bg = bigger */
                // region determine and find out the smaller one & bigger one
                val smLineIdx: Int
                val smLineOffset: Int
                val bgLineIdx: Int
                val bgLineOffset: Int
                if (startLineIdx < endLineIdx || (startLineIdx == endLineIdx && startLineOffset < endLineOffset)) {
                    smLineIdx = startLineIdx
                    smLineOffset = startLineOffset
                    bgLineIdx = endLineIdx
                    bgLineOffset = endLineOffset
                }
                else {
                    smLineIdx = endLineIdx
                    smLineOffset = endLineOffset
                    bgLineIdx = startLineIdx
                    bgLineOffset = startLineOffset
                }
                // endregion

                // iterate from smallest line to highest line
                for (idx in smLineIdx..bgLineIdx) {
                    var x: Double
                    var w: Double
                    when (idx) {
                        smLineIdx -> {
                            x = smLineOffset * PreferenceManager.EditorPref.charW + offsetX
                            w = PreferenceManager.codeCvsWidth - x + camera.locX + camera.width
                        }
                        bgLineIdx -> {
                            x = offsetX
                            w = bgLineOffset * PreferenceManager.EditorPref.charW
                        }
                        else -> {
                            x = offsetX
                            w = PreferenceManager.codeCvsWidth + camera.locX + camera.width
                        }
                    }

                    gphCxt?.fillRect(
                            x - camera.locX,
                            idx * PreferenceManager.EditorPref.lineH - camera.locY,
                            w,
                            PreferenceManager.EditorPref.lineH
                    )
                }
            }
        }
    }

    /* ===================================================================== */

    // region get the smaller line-index and line-offset
    fun getSmallerBound(): MutablePair<Int, Int>? = when {
        mPrimarySelection == null -> null
        mPrimarySelection!!.isReversed() -> mPrimarySelection!!.endCopied
        else -> mPrimarySelection!!.startCopied
    }
    // endregion

    // region get the bigger line-index and line-offset
    fun getBiggerBound(): MutablePair<Int, Int>? = when {
        mPrimarySelection == null -> null
        mPrimarySelection!!.isReversed() -> mPrimarySelection!!.startCopied
        else -> mPrimarySelection!!.endCopied
    }
    // endregion
}