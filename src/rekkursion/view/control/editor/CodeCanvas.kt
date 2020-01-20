package rekkursion.view.control.editor

import javafx.geometry.Point2D
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.KeyCode
import javafx.scene.input.ScrollEvent
import javafx.scene.paint.Color
import rekkursion.manager.PreferenceManager
import rekkursion.manager.SelectionManager
import rekkursion.model.EditorModel
import rekkursion.view.stage.InputType
import rekkursion.view.stage.SingleLineTextInputStage
import rekkursion.util.Camera
import rekkursion.util.Token
import rekkursion.util.command.Command
import rekkursion.util.command.EditorCommand
import rekkursion.util.command.EditorInsertTextCommand
import rekkursion.util.tool.MutablePair
import java.lang.Exception

import java.util.ArrayList
import java.util.HashMap
import kotlin.math.max
import kotlin.math.min

class CodeCanvas(private val mWidth: Double, private val mHeight: Double): Canvas(mWidth, mHeight) {
    // the editor-model
    private val mModel = EditorModel(mWidth, mHeight)

    // some commands for editor operations
    private val mEditorCommands = HashMap<Int, Command>()

    // the graphics context
    private var mGphCxt: GraphicsContext? = null

    // is holding ctrl
    private var mIsCtrlPressed = false

    // is holding shift
    private var mIsShiftPressed = false

    // is holding alt
    private var mIsAltPressed = false

    // the point when the mouse is down
    private var mMouseDownPt: Point2D? = null

    // for managing the selections of texts
    private val mSelectionManager = SelectionManager()

    // for primary constructor
    init {
        // insert the first line initially
        mModel.insertNewLine(0)

        // initialize commands
        mEditorCommands[32] = EditorInsertTextCommand(mModel)

        initGraphicsContext()
        initEvents()
        initPropertyListeners()
    }

    /* ===================================================================== */

    // initialize the graphics-context
    private fun initGraphicsContext() {
        mGphCxt = graphicsContext2D
        mGphCxt?.font = PreferenceManager.EditorPref.font

        render()
    }

    // initialize the events
    private fun initEvents() {
        // mouse-pressed
        setOnMousePressed { mouseEvent ->
            // request the focus
            requestFocus()

            // deal with the mouse-pressed location
            if (mouseEvent.isPrimaryButtonDown) {
                handleMouseDown(mouseEvent.x, mouseEvent.y)
            }
        }

        // mouse-dragged (mouse-pressed + mouse-moved)
        setOnMouseDragged { mouseEvent ->
            // if the left-button is down
            if (mouseEvent.isPrimaryButtonDown && mMouseDownPt != null) {
                val origLineIdx = min(
                        mModel.camera.toLineIndex(mMouseDownPt!!.y),
                        mModel.getNumOfLines() - 1
                )

                val origOffset = min(
                        mModel.camera.toCaretOffset(mMouseDownPt!!.x),
                        mModel.getTextAt(origLineIdx).length
                )

                // set the new line index of the caret
                mModel.setCaretLineIndex(
                        max(min(
                                mModel.camera.toLineIndex(mouseEvent.y),
                                mModel.getNumOfLines() - 1
                        ), 0)
                )

                // set the new caret offset
                mModel.setCaretOffset(
                        max(min(
                                mModel.camera.toCaretOffset(mouseEvent.x),
                                mModel.getTextAt(mModel.caretLineIdx).length
                        ), 0),
                        true
                )

                // deal w/ selection
                manageSelectionWithAnInterval(
                        origLineIdx,
                        origOffset,
                        mModel.caretLineIdx,
                        mModel.caretOffset,
                        origLineIdx != mModel.caretLineIdx || origOffset != mModel.caretOffset
                )

                // re-render if the current line changed
                if (mModel.caretLineIdx != origLineIdx || mModel.caretOffset != origOffset)
                    render()
            }
        }

        // mouse-released
        setOnMouseReleased { mMouseDownPt = null }

        // mouse-scrolling
        setOnScroll { scrollEvent ->
            handleScrollEvent(scrollEvent)
        }

        /* related to mouse */
        /* ===================================================================== */
        /* related to keyboard */

        // key-pressed
        setOnKeyPressed { keyEvent ->
            val text = keyEvent.text
            val charCode = keyEvent.code

            println(charCode.code.toString() + "|" + text)

            // deal with the pressed input
            handleKeyboardInput(text, charCode)
        }

        // key-released
        setOnKeyReleased { keyEvent ->
            when (keyEvent.code) {
                KeyCode.CONTROL -> mIsCtrlPressed = false
                KeyCode.SHIFT -> mIsShiftPressed = false
                KeyCode.ALT -> mIsAltPressed = false
            }
        }
    }

    // initialize some properties' listeners
    private fun initPropertyListeners() {
        // the focused-property
        focusedProperty().addListener { _, _, _ ->
            mIsCtrlPressed = false
            mIsShiftPressed = false
            mIsAltPressed = false
        }
    }

    // handle the mouse click-location invoked by the event of on-mouse-pressed
    private fun handleMouseDown(mouseX: Double, mouseY: Double) {
        // set the mouse location when mouse-down
        mMouseDownPt = Point2D(mouseX, mouseY)

        // get the original line index of the caret
        val origLineIdx = mModel.caretLineIdx

        // get the original caret offset
        val origOffset = mModel.caretOffset

        // set the new line index of the caret
        mModel.setCaretLineIndex(min(mModel.camera.toLineIndex(mouseY), mModel.getNumOfLines() - 1))

        // set the new caret offset
        mModel.setCaretOffset(min(mModel.camera.toCaretOffset(mouseX), mModel.getTextAt(mModel.caretLineIdx).length), true)

        // deal w/ selection
        manageSelectionWithAnInterval(origLineIdx, origOffset, mModel.caretLineIdx, mModel.caretOffset)

        // re-render if the current line changed
        if (mModel.caretLineIdx != origLineIdx || mModel.caretOffset != origOffset)
            render()
    }

    // handle the keyboard input invoked by the event of on-key-pressed
    private fun handleKeyboardInput(ch: String, chCode: KeyCode) {
        // the original line-index & caret-offset before handling the operation
        val origLineIdx = mModel.caretLineIdx
        val origCaretOffset = mModel.caretOffset
        var shouldMoveCamera = true

        /* ===================================================================== */

        // F5
        if (chCode == KeyCode.F5) {
        }

        /* ===================================================================== */

        // left arrow (ctrl-able, shift-able)
        else if (chCode == KeyCode.LEFT) {
            // ctrl + left: move a word to left
            if (mIsCtrlPressed) {
                if (mModel.caretOffset > 0) {
                    // regular expressions for word searching
                    val identifierRegex = "[0-9A-Za-z_]"
                    val spaceRegex = "\\s"

                    // the character in front of the caret
                    val chInFront = mModel.getTextAtCurrentLine().substring(mModel.caretOffset - 1, mModel.caretOffset)

                    // search to left
                    while (true) {
                        mModel.setCaretOffset(mModel.caretOffset - 1, true)
                        if (mModel.caretOffset == 0)
                            break

                        val newChInFront = mModel.getTextAtCurrentLine().substring(mModel.caretOffset - 1, mModel.caretOffset)
                        if (chInFront.matches(identifierRegex.toRegex()) && newChInFront.matches(identifierRegex.toRegex()))
                            continue
                        if (chInFront.matches(spaceRegex.toRegex()) && newChInFront.matches(spaceRegex.toRegex()))
                            continue
                        break
                    }

                    // deal w/ selection
                    manageSelectionWithAnInterval(mModel.caretLineIdx, origCaretOffset, mModel.caretLineIdx, mModel.caretOffset)
                }
            }
            // left-only: move a single character to left
            else {
                if (mModel.caretOffset > 0) {
                    // update the caret-offset
                    mModel.setCaretOffset(mModel.caretOffset - 1, true)

                    // deal w/ selection
                    manageSelectionWithAnInterval(mModel.caretLineIdx, mModel.caretOffset + 1, mModel.caretLineIdx, mModel.caretOffset)
                }
            }
        }

        // right arrow (ctrl-able, shift-able)
        else if (chCode == KeyCode.RIGHT) {
            // ctrl + right: move a word to right
            if (mIsCtrlPressed) {
                if (mModel.caretOffset < mModel.getTextAtCurrentLine().length) {
                    // regular expressions for word searching
                    val identifierRegex = "[0-9A-Za-z_]"
                    val spaceRegex = "\\s"

                    // the character behind the caret
                    val chBehind = mModel.getTextAtCurrentLine().substring(mModel.caretOffset, mModel.caretOffset + 1)

                    // search to left
                    while (true) {
                        mModel.setCaretOffset(mModel.caretOffset + 1, true)
                        if (mModel.caretOffset == mModel.getTextAtCurrentLine().length)
                            break

                        val newChBehind = mModel.getTextAtCurrentLine().substring(mModel.caretOffset, mModel.caretOffset + 1)
                        if (chBehind.matches(identifierRegex.toRegex()) && newChBehind.matches(identifierRegex.toRegex()))
                            continue
                        if (chBehind.matches(spaceRegex.toRegex()) && newChBehind.matches(spaceRegex.toRegex()))
                            continue
                        break
                    }

                    // deal w/ selection
                    manageSelectionWithAnInterval(mModel.caretLineIdx, origCaretOffset, mModel.caretLineIdx, mModel.caretOffset)
                }
            }
            // move a single character to right
            else {
                if (mModel.caretOffset < mModel.getTextAtCurrentLine().length) {
                    // update the caret-offset
                    mModel.setCaretOffset(mModel.caretOffset + 1, true)

                    // deal w/ selection
                    manageSelectionWithAnInterval(mModel.caretLineIdx, mModel.caretOffset - 1, mModel.caretLineIdx, mModel.caretOffset)
                }
            }
        }

        // up arrow (shift-able)
        else if (chCode == KeyCode.UP) {
            if (mModel.caretLineIdx > 0) {
                mModel.setCaretLineIndex(mModel.caretLineIdx - 1)
                mModel.setCaretOffset(min(mModel.origestCaretOffset, mModel.getTextAtCurrentLine().length), false)

                // deal w/ selection
                manageSelectionWithAnInterval(mModel.caretLineIdx + 1, origCaretOffset, mModel.caretLineIdx, mModel.caretOffset)
            }
        }

        // down arrow (shift-able)
        else if (chCode == KeyCode.DOWN) {
            if (mModel.caretLineIdx < mModel.getNumOfLines() - 1) {
                mModel.setCaretLineIndex(mModel.caretLineIdx + 1)
                mModel.setCaretOffset(min(mModel.origestCaretOffset, mModel.getTextAtCurrentLine().length), false)

                // deal w/ selection
                manageSelectionWithAnInterval(mModel.caretLineIdx - 1, origCaretOffset, mModel.caretLineIdx, mModel.caretOffset)
            }
        }

        // end (ctrl-able, shift-able)
        else if (chCode == KeyCode.END) {
            // ctrl + end: go to the last character of the last line
            if (mIsCtrlPressed) {
                mModel.setCaretLineIndex(mModel.getNumOfLines() - 1)
                mModel.setCaretOffset(mModel.getTextAtCurrentLine().length, true)
            }
            // go to the last character of the current line
            else {
                if (mModel.caretOffset != mModel.getTextAtCurrentLine().length)
                    mModel.setCaretOffset(mModel.getTextAtCurrentLine().length, true)
            }

            // deal w/ selection
            manageSelectionWithAnInterval(origLineIdx, origCaretOffset, mModel.caretLineIdx, mModel.caretOffset)
        }

        // home (ctrl-able, shift-able)
        else if (chCode == KeyCode.HOME) {
            // ctrl + home: go to the first character of the first line
            if (mIsCtrlPressed) {
                mModel.setCaretLineIndex(0)
                mModel.setCaretOffset(0, true)
            }
            // go to the first character of the current line
            else {
                if (mModel.caretOffset > 0)
                    mModel.setCaretOffset(0, true)
            }

            // deal w/ selection
            manageSelectionWithAnInterval(origLineIdx, origCaretOffset, mModel.caretLineIdx, mModel.caretOffset)
        }

        // page-up (ctrl-able, shift-able)
        else if (chCode == KeyCode.PAGE_UP) {
            // ctrl + page-up: go the smallest line of the current camera's location w/o moving it
            if (mIsCtrlPressed) {
                val smallestLineIdx = mModel.camera.toLineIndex(0.0, PreferenceManager.EditorPref.lineH / 2.0)
                mModel.setCaretLineIndex(max(smallestLineIdx, 0))
                mModel.setCaretOffset(min(mModel.origestCaretOffset, mModel.getTextAtCurrentLine().length), false)

                shouldMoveCamera = false
            }

            // only page-up: go up w/ a distance of camera's height
            // TODO: fix the calculation error for the operation of page-up
            else {
                val dis = (mModel.camera.height / PreferenceManager.EditorPref.lineH).toInt()
                mModel.setCaretLineIndex(max(mModel.caretLineIdx - dis, 0))
                mModel.setCaretOffset(min(mModel.origestCaretOffset, mModel.getTextAtCurrentLine().length), false)
            }

            // deal w/ selection
            manageSelectionWithAnInterval(origLineIdx, origCaretOffset, mModel.caretLineIdx, mModel.caretOffset)
        }

        // page-down (ctrl-able, shift-able)
        else if (chCode == KeyCode.PAGE_DOWN) {
            // ctrl + page-down: go the biggest line of the current camera's location w/o moving it
            if (mIsCtrlPressed) {
                val biggestLineIdx = mModel.camera.toLineIndex(height)
                mModel.setCaretLineIndex(min(max(biggestLineIdx - 1, 0), mModel.getNumOfLines() - 1))
                mModel.setCaretOffset(min(mModel.origestCaretOffset, mModel.getTextAtCurrentLine().length), false)

                shouldMoveCamera = false
            }

            // only page-down: go down w/ a distance of camera's height
            // TODO: fix the calculation error for the operation of page-down
            else {
                val dis = (mModel.camera.height / PreferenceManager.EditorPref.lineH).toInt()
                mModel.setCaretLineIndex(min(max(mModel.caretLineIdx + dis, 0), mModel.getNumOfLines() - 1))
                mModel.setCaretOffset(min(mModel.origestCaretOffset, mModel.getTextAtCurrentLine().length), false)
            }

            // deal w/ selection
            manageSelectionWithAnInterval(origLineIdx, origCaretOffset, mModel.caretLineIdx, mModel.caretOffset)
        }

        /* ===================================================================== */

        // back-space
        else if (chCode == KeyCode.BACK_SPACE) {
            // delete the selected text
            if (mSelectionManager.hasSelection())
                removeSelectedText()

            // delete a single character or '\n' in front of the caret
            else {
                // delete a single character in front of the caret
                if (mModel.caretOffset > 0) {
                    mModel.deleteCharAt(mModel.caretLineIdx, mModel.caretOffset - 1)
                    mModel.setCaretOffset(mModel.caretOffset - 1, true)
                }
                // delete a '\n'
                else {
                    if (mModel.caretLineIdx > 0) {
                        mModel.setCaretOffset(mModel.getTextLengthAt(mModel.caretLineIdx - 1), true)
                        mModel.appendTextAt(mModel.caretLineIdx - 1, mModel.getTextAtCurrentLine())
                        mModel.removeCurrentLine()
                        mModel.setCaretLineIndex(mModel.caretLineIdx - 1)
                    }
                }
            }

            // find out and set the longest line
            setLongestLine(false)
        }

        // delete
        else if (chCode == KeyCode.DELETE) {
            // delete the selected text (as the same operation of back-space)
            if (mSelectionManager.hasSelection())
                removeSelectedText()

            // delete a single character or '\n' behind the caret
            else {
                // delete a single character behind the caret
                if (mModel.caretOffset < mModel.getTextAtCurrentLine().length)
                    mModel.deleteCharAt(mModel.caretLineIdx, mModel.caretOffset)
                // delete a '\n'
                else {
                    if (mModel.caretLineIdx < mModel.getNumOfLines() - 1) {
                        mModel.appendTextAt(mModel.caretLineIdx, mModel.getTextAt(mModel.caretLineIdx + 1))
                        mModel.removeLineAt(mModel.caretLineIdx + 1)
                    }
                }
            }

            // find out and set the longest line
            setLongestLine(false)
        }

        // enter (shift-able)
        else if (chCode == KeyCode.ENTER) {
            // if shift is NOT pressed
            if (!mIsShiftPressed) {
                // remove the selected text
                removeSelectedText()

                // insert a new string-buffer for this new line
                mModel.insertNewLine(mModel.caretLineIdx + 1, mModel.getTextAtCurrentLine().substring(mModel.caretOffset))

                // move the sub-string behind the origin caret location to the new line
                mModel.deleteSubstringAt(mModel.caretLineIdx, mModel.caretOffset, mModel.getTextAtCurrentLine().length)
            }
            // if shift is pressed
            else
                mModel.insertNewLine(mModel.caretLineIdx + 1)

            // update the caret offset and line index
            mModel.setCaretOffset(0, true)
            mModel.setCaretLineIndex(mModel.caretLineIdx + 1)

            // clear selections
            mSelectionManager.clearSelections()

            // find out and set the longest line
            setLongestLine(false)
        }

        /* ===================================================================== */

        // visible character & white-space (shift-able)
        else if (chCode.code >= 32) {
            // try to do a certain special operation, e.g., Ctrl+C, Ctrl+X, ...
            if (doSpecialEditorOperationByShortcut(ch)) return

            // get the visible character and also convert it into a string
            val vCh = getVisibleChar(ch) ?: return
            val vStr = vCh.toString()

            // remove the selected text
            removeSelectedText()

            // do the command of insert the visible (includes space) string
            mEditorCommands[32]?.execute(vStr)
        }

        // tab
        else if (chCode == KeyCode.TAB) {
            // remove the selected text
            removeSelectedText()

            // do the command of insert 4 white-spaces as a '\t' character
            mEditorCommands[32]?.execute("    ")
        }

        /* ===================================================================== */

        // modifier: ctrl
        else if (chCode == KeyCode.CONTROL) {
            mIsCtrlPressed = true
        }

        // modifier: shift
        else if (chCode == KeyCode.SHIFT) {
            mIsShiftPressed = true
        }

        // modifier: alt
        else if (chCode == KeyCode.ALT) {
            mIsAltPressed = true
        }

        /* ===================================================================== */

        // deal w/ the camera and render the editor
        if (chCode != KeyCode.CONTROL && chCode != KeyCode.SHIFT && chCode != KeyCode.ALT) {
            // deal w/ the camera
            if (shouldMoveCamera)
                manageCamera()

            // re-render
            render()
        }
    }

    // handle the scroll event invoked by the event of on-scroll
    private fun handleScrollEvent(scrollEvent: ScrollEvent) {
        // shift-pressed -> scrolls horizontal
        if (mIsShiftPressed) {
            // the max x of the editor when scrolling
            var maxX = mModel.longestLine.second * PreferenceManager.EditorPref.charW -
                    mModel.camera.width +
                    PreferenceManager.EditorPref.blankWidth
            if (maxX < 0.0) maxX = 0.0

            // move the camera left/right
            mModel.camera.move(
                    // mouse-wheeling down -> move camera to right
                    offsetX = if (scrollEvent.deltaX < 0.0) PreferenceManager.EditorPref.editorScrollingStepSizeX
                    // mouse-wheeling up -> move camera to left
                    else -PreferenceManager.EditorPref.editorScrollingStepSizeX,
                    maxX = maxX
            )
        }
        // not pressed -> scrolls vertical
        else {
            // the max y of the editor when scrolling
            var maxY = mModel.getNumOfLines() * PreferenceManager.EditorPref.lineH -
                    mModel.camera.height +
                    PreferenceManager.EditorPref.blankHeight
            if (maxY < 0.0) maxY = 0.0

            // move the camera up/down
            mModel.camera.move(
                    // mouse-wheeling down -> move camera to down
                    offsetY = if (scrollEvent.deltaY < 0.0) PreferenceManager.EditorPref.editorScrollingStepSizeY
                    // mouse-wheeling up -> move camera to up
                    else -PreferenceManager.EditorPref.editorScrollingStepSizeY,
                    maxY = maxY
            )
        }

        // re-render
        render()
    }

    // do the special editor operation
    private fun doSpecialEditorOperationByShortcut(ch: String): Boolean {
        // if it's not a single character -> false
        if (ch.length != 1) return false

        // get the key code from ch, then try to get the corresponding function
        val keyCode = KeyCode.getKeyCode(ch.toUpperCase()) ?: return false
        val func = PreferenceManager.EditorPref.Shortcuts.getOperationFunction(
                mIsCtrlPressed,
                mIsShiftPressed,
                mIsAltPressed,
                keyCode.code
        ) ?: return false

        func.call(this)
        return true
    }

    // get the visible character
    private fun getVisibleChar(ch: String): Char? {
        return if (!mIsShiftPressed)
            if (ch.isEmpty()) null else ch[0]
        else
            mShiftableCharactersMap?.getOrDefault(ch, null)
    }

    // set the longest line
    private fun setLongestLine(searchWholeText: Boolean) { mModel.searchAndSetLongestLine(searchWholeText) }

    // remove the selected text
    private fun removeSelectedText() {
        val pair = mSelectionManager.removeSelectedText(mModel.textBuffersAndTokens)
        if (pair != null) {
            mModel.setCaretLineIndex(pair.first)
            mModel.setCaretOffset(pair.second, true)
        }
    }

    // deal w/ an unprocessed interval
    private fun manageSelectionWithAnInterval(startLineIdx: Int, startLineOffset: Int, endLineIdx: Int, endLineOffset: Int, isSelecting: Boolean = mIsShiftPressed) {
        if (isSelecting)
            mSelectionManager.exclusizeSelection(
                    startLineIdx,
                    startLineOffset,
                    endLineIdx,
                    endLineOffset
            )
        else
            mSelectionManager.clearSelections()
    }

    // deal w/ the camera
    private fun manageCamera(designatedNumOfLineIdxFromTop: Int? = null) {
        // get the total height to the current line index
        val totalHeightToLineIdx = (mModel.caretLineIdx + 1) * PreferenceManager.EditorPref.lineH - mModel.camera.locY

        // adjust the camera's y-offset w/o designating a certain line-index
        if (designatedNumOfLineIdxFromTop == null) {
            // scroll down if the current line is over-flowing (bigger than the editor height)
            if (totalHeightToLineIdx > PreferenceManager.codeCvsHeight) {
                val difference = totalHeightToLineIdx - PreferenceManager.codeCvsHeight
                mModel.camera.move(0.0, difference)
            }
            // scroll up if the current line is under-flowing (smaller than two lines' height)
            else if (totalHeightToLineIdx < PreferenceManager.EditorPref.lineH * 2.0) {
                val difference =
                        if (mModel.caretLineIdx == 0)
                            PreferenceManager.EditorPref.lineH - totalHeightToLineIdx
                        else
                            PreferenceManager.EditorPref.lineH * 2.0 - totalHeightToLineIdx
                mModel.camera.move(0.0, -difference)
            }
        }
        // a certain line-index designated, move the camera directly
        else {
            mModel.camera.moveTo(
                    newLocY = max((mModel.caretLineIdx - designatedNumOfLineIdxFromTop), 0) * PreferenceManager.EditorPref.lineH
            )
        }

        /**/

        // the start x-offset of texts
        val offsetX =
                PreferenceManager.EditorPref.lineStartOffsetX + PreferenceManager.EditorPref.LineNumberArea.width

        // get the total width to the current caret offset
        val totalWidthToCaretOffset = offsetX + mModel.caretOffset * PreferenceManager.EditorPref.charW - mModel.camera.locX

        // scroll right if the current caret offset is over-flowing (bigger than the editor width)
        if (totalWidthToCaretOffset > PreferenceManager.codeCvsWidth - PreferenceManager.EditorPref.lineStartOffsetX) {
            val difference = totalWidthToCaretOffset - (PreferenceManager.codeCvsWidth - PreferenceManager.EditorPref.lineStartOffsetX)
            mModel.camera.move(difference, 0.0)
        }
        // scroll left if the current caret offset is under-flowing (smaller than offset-x)
        else if (totalWidthToCaretOffset < offsetX) {
            val difference = offsetX - totalWidthToCaretOffset
            mModel.camera.move(-difference, 0.0)
        }
    }

    // render
    private fun render() {
        val lineH = PreferenceManager.EditorPref.lineH

        // render the background
        mGphCxt?.fill = PreferenceManager.EditorPref.editorBgClr
        mGphCxt?.fillRect(0.0, 0.0, PreferenceManager.codeCvsWidth, PreferenceManager.codeCvsHeight)

        // render the selected line hint
        mGphCxt?.fill = PreferenceManager.EditorPref.selectedLineHintClr
        mGphCxt?.fillRect(-mModel.camera.locX, mModel.caretLineIdx * lineH - mModel.camera.locY, mWidth + mModel.camera.locX, lineH)

        // render the text
        renderText()

        // render the caret
        renderCaret()

        // render the line number area
        renderLineNumberArea()
    }

    // render the text, including background-hints of selections
    private fun renderText() {
        // get the lower and upper bounds of current camera location
        val (lBound, uBound) = mModel.camera.getCameraCoveredBoundsLinesIndices()
        val lowerBound = max(lBound, 0)
        val upperBound = min(uBound, mModel.getNumOfLines() - 1)

        // do lexeme analysis
        try {
            // analyze each line
            for (idx in lowerBound..upperBound) {
                val pair = mModel.getTextBufferAndTokensAt(idx)
                pair.second = PreferenceManager.LangPref.getUsedLang()!!.compile(
                        pair.first.toString() + "\n"
                )!!
            }
        } catch (e: Exception) { println(e.message) }

        // render all of the tokens between the bounds
        var caretY = lowerBound
        for (idx in lowerBound..upperBound) {
            // initialize caret-x to zero
            var caretX = 0

            // get classified tokens' backgrounds of each line
            val tokens = mModel.getTextBufferAndTokensAt(idx).second

            // iterate every token in each line
            tokens.forEach { token ->
                if (token.text != "\n") {
                    // render text
                    token.renderBackground(mGphCxt, mModel.camera, caretX, caretY)

                    // update the caret of x-axis
                    caretX += token.text.length
                }
            }

            // move the caret-y
            ++caretY
        }

        /* ====== */

        // render the selections hint (background)
        mSelectionManager.renderSelectionBackground(mGphCxt, mModel.camera)

        /* ====== */

        // render all of the tokens' texts between the bounds
        caretY = lowerBound
        for (idx in lowerBound..upperBound) {
            // initialize caret-x to zero
            var caretX = 0

            // get classified tokens of each line
            val tokens = mModel.getTextBufferAndTokensAt(idx).second

            // iterate every token in each line
            tokens.forEach { token ->
                if (token.text != "\n") {
                    // render text
                    token.render(mGphCxt, mModel.camera, caretX, caretY)

                    // update the caret of x-axis
                    caretX += token.text.length
                }
            }

            // move the caret-y
            ++caretY
        }
    }

    // render the caret
    private fun renderCaret() {
        val caretW = PreferenceManager.EditorPref.caretW
        val charW = PreferenceManager.EditorPref.charW
        val halfOfCaretWidth = caretW / 2.0

        mGphCxt?.fill = Color.WHITESMOKE
        mGphCxt?.fillRect(
                mModel.caretOffset * charW - halfOfCaretWidth +
                        PreferenceManager.EditorPref.lineStartOffsetX +
                        PreferenceManager.EditorPref.LineNumberArea.width -
                        mModel.camera.locX,
                mModel.caretLineIdx * PreferenceManager.EditorPref.lineH -
                        mModel.camera.locY,
                caretW,
                PreferenceManager.EditorPref.lineH
        )
    }

    // render the line number area
    private fun renderLineNumberArea() {
        // render the background
        mGphCxt?.fill = PreferenceManager.EditorPref.LineNumberArea.bgClr
        mGphCxt?.fillRect(
                0.0,
                0.0,
                PreferenceManager.EditorPref.LineNumberArea.width,
                PreferenceManager.codeCvsHeight
        )

        // render the vertical-line
        mGphCxt?.fill = PreferenceManager.EditorPref.LineNumberArea.fontClr
        mGphCxt?.fillRect(
                PreferenceManager.EditorPref.LineNumberArea.width -
                        (PreferenceManager.EditorPref.LineNumberArea.verticalLineWidth / 2.0),
                0.0,
                PreferenceManager.EditorPref.LineNumberArea.verticalLineWidth,
                PreferenceManager.codeCvsHeight
        )

        // render the non-selected line numbers
        val maxDigitLen = (mModel.getNumOfLines() - 1).toString().length
        mGphCxt?.fill = PreferenceManager.EditorPref.LineNumberArea.fontClr
        mGphCxt?.font = PreferenceManager.EditorPref.font
        for (y in 0 until mModel.getNumOfLines()) {
            if (y == mModel.caretLineIdx)
                continue
            mGphCxt?.fillText(
                    " ".repeat(maxDigitLen - y.toString().length) + y.toString(),
                    PreferenceManager.EditorPref.LineNumberArea.numberOffsetX,
                    (y + 1) * PreferenceManager.EditorPref.lineH -
                            PreferenceManager.EditorPref.differenceBetweenLineHeightAndFontSize -
                            mModel.camera.locY,
                    PreferenceManager.EditorPref.LineNumberArea.width
            )
        }

        // render the selected line number
        mGphCxt?.fill = PreferenceManager.EditorPref.LineNumberArea.selectedFontClr
        mGphCxt?.fillText(
                " ".repeat(maxDigitLen - mModel.caretLineIdx.toString().length) + mModel.caretLineIdx.toString(),
                PreferenceManager.EditorPref.LineNumberArea.numberOffsetX,
                (mModel.caretLineIdx + 1) * PreferenceManager.EditorPref.lineH -
                        PreferenceManager.EditorPref.differenceBetweenLineHeightAndFontSize -
                        mModel.camera.locY,
                PreferenceManager.EditorPref.LineNumberArea.width
        )
    }

    /* ===================================================================== */

    // copy the selected text
    fun copySelectedText() {
        // get the selected text
        val selectedText = mSelectionManager.getSelectedText(mModel.textBuffersAndTokens)

        // get the system clipboard and put the selected string into it
        val clipboard = Clipboard.getSystemClipboard()
        val content = ClipboardContent()
        content.putString(selectedText)
        clipboard.setContent(content)
    }

    // cut the selected text
    fun cutSelectedText() {
        /* cut = copy + delete */

        // copy
        copySelectedText()
        // then delete
        removeSelectedText()

        // re-render
        render()

        // find the longest line among all of the text
        setLongestLine(true)
    }

    // paste the selected text
    fun pasteSelectedText() {
        // get the system clipboard
        val clipboard = Clipboard.getSystemClipboard()

        // if the clipboard has a string -> paste it on
        if (clipboard.hasString()) {
            // remove the selected text first
            removeSelectedText()

            // add all the lines of texts
            val lines = clipboard.string.split("\n")
            mModel.addMultipleLinesOfTextsAtCurrentCaretLocation(lines)

            // re-render
            render()

            // find the longest line among all of the text
            setLongestLine(true)
        }
    }

    // select all text
    fun selectAllText() {
        // clear all selections first
        mSelectionManager.clearSelections()
        // then select all text
        mSelectionManager.exclusizeSelection(
                0,
                0,
                mModel.getNumOfLines() - 1,
                mModel.getTextLengthAt(mModel.getNumOfLines() - 1)
        )

        // re-render
        render()
    }

    // jump to the designated line
    fun jumpToDesignatedLine() {
        // create a single-line text input stage
        val inputStage = SingleLineTextInputStage(
                "Jump to a certain line",
                "Line index:",
                InputType.NON_NEGATIVE_INTEGER,
                mModel.caretLineIdx.toString()
        )
        // show the stage and get the line-index
        val lineIdxStr = inputStage.showDialog() ?: return
        val lineIdx = max(min(lineIdxStr.toInt(), mModel.getNumOfLines() - 1), 0)

        // if it's a different line-index
        if (lineIdx != mModel.caretLineIdx) {
            // update the caret's location (line-index & offset)
            mModel.setCaretLineIndex(lineIdx)
            mModel.setCaretOffset(min(mModel.caretOffset, mModel.getTextLengthAt(lineIdx)), false)

            // deal w/ the camera
            manageCamera()

            // re-render
            render()
        }
    }

    /* ===================================================================== */

    // static scope
    companion object {
        // the hash-map for shift-able characters
        private val mShiftableCharactersMap: MutableMap<String, Char>? = HashMap()

        // static initialization
        init {
            // build up the shift-able characters-map
            for (k in 65..90) {
                mShiftableCharactersMap!![k.toChar().toString()] = (k + 32).toChar()
                mShiftableCharactersMap[(k + 32).toChar().toString()] = k.toChar()
            }
            mShiftableCharactersMap!!["1"] = '!'
            mShiftableCharactersMap["2"] = '@'
            mShiftableCharactersMap["3"] = '#'
            mShiftableCharactersMap["4"] = '$'
            mShiftableCharactersMap["5"] = '%'
            mShiftableCharactersMap["6"] = '^'
            mShiftableCharactersMap["7"] = '&'
            mShiftableCharactersMap["8"] = '*'
            mShiftableCharactersMap["9"] = '('
            mShiftableCharactersMap["0"] = ')'
            mShiftableCharactersMap["-"] = '_'
            mShiftableCharactersMap["="] = '+'
            mShiftableCharactersMap["\\"] = '|'
            mShiftableCharactersMap["]"] = '}'
            mShiftableCharactersMap["["] = '{'
            mShiftableCharactersMap["\'"] = '\"'
            mShiftableCharactersMap[";"] = ':'
            mShiftableCharactersMap["/"] = '?'
            mShiftableCharactersMap["."] = '>'
            mShiftableCharactersMap[","] = '<'
            mShiftableCharactersMap["`"] = '~'
        }
    }
}
