package rekkursion.manager

import javafx.scene.canvas.GraphicsContext
import rekkursion.util.tool.MutablePair
import rekkursion.util.tool.TextInterval
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SelectionManager {
    // selected text
    private var mMainSelection: TextInterval? = null
    val mainSelection get() = mMainSelection?.copy()

    /* ===================================================================== */

    // exclusize (selected -> unselected, unselected -> selected) the selection
    fun exclusizeSelection(startLineIdx: Int, startLineOffset: Int, endLineIdx: Int, endLineOffset: Int) {
        val newInterval = TextInterval(startLineIdx, startLineOffset, endLineIdx, endLineOffset)

        if (mMainSelection == null)
            mMainSelection = newInterval
        else
            mMainSelection?.setEnd(newInterval.endCopied)
    }

    // clear all selections
    fun clearSelections() {
        mMainSelection = null
    }

    // render the selection effect
    fun renderSelection(gphCxt: GraphicsContext?) {
        // if main-selection is NOT null
        mMainSelection?.let { mainSelection ->
            // get the offset-x of typing area
            val offsetX = PreferenceManager.EditorPref.lineStartOffsetX + PreferenceManager.EditorPref.LineNumberArea.width

            // set the text-selection background color
            gphCxt?.fill = PreferenceManager.EditorPref.selectionClr

            // get start's & end's line-indices & line-offsets
            val startLineIdx = mainSelection.startCopied.first
            val startLineOffset = mainSelection.startCopied.second
            val endLineIdx = mainSelection.endCopied.first
            val endLineOffset = mainSelection.endCopied.second

            // if start & end are at the same line (has only a single line)
            if (startLineIdx == endLineIdx) {
                val selectionLen = abs(startLineOffset - endLineOffset)
                val smaller = min(startLineOffset, endLineOffset)
                gphCxt?.fillRect(
                        smaller * PreferenceManager.EditorPref.charW + offsetX,
                        startLineIdx * PreferenceManager.EditorPref.lineH,
                        selectionLen * PreferenceManager.EditorPref.charW,
                        PreferenceManager.EditorPref.lineH
                )
            }

            // has multiple lines
            else {
                /* sm = smaller, bg = bigger */
                var smLineIdx: Int
                var smLineOffset: Int
                var bgLineIdx: Int
                var bgLineOffset: Int
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

                // iterate from smallest line to highest line
                for (idx in smLineIdx..bgLineIdx) {
                    var x: Double
                    var w: Double
                    when (idx) {
                        smLineIdx -> {
                            x = smLineOffset * PreferenceManager.EditorPref.charW + offsetX
                            w = PreferenceManager.codeCvsWidth - x
                        }
                        bgLineIdx -> {
                            x = 0.0
                            w = bgLineOffset * PreferenceManager.EditorPref.charW + offsetX
                        }
                        else -> {
                            x = 0.0
                            w = PreferenceManager.codeCvsWidth
                        }
                    }

                    gphCxt?.fillRect(
                            x,
                            idx * PreferenceManager.EditorPref.lineH,
                            w,
                            PreferenceManager.EditorPref.lineH
                    )
                }
            }
        }
    }
}