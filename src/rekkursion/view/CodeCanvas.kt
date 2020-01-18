package rekkursion.view

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
import rekkursion.util.Camera
import rekkursion.util.Token
import rekkursion.util.tool.MutablePair
import java.lang.Exception

import java.util.ArrayList
import java.util.HashMap
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

class CodeCanvas(private val mWidth: Double, private val mHeight: Double): Canvas(mWidth, mHeight) {
    // the camera
    private val mCamera = Camera(width = mWidth, height = mHeight)

    // the graphics context
    private var mGphCxt: GraphicsContext? = null

    // the current line index of the caret
    private var mCaretLineIdx = 0

    // the current offset at a certain line of the caret
    private var mCaretOffset = 0

    // be used when going up/down
    private var mOrigestCaretOffset = 0

    // the longest line (the line which has the most characters, including spaces)
    /* in the pair: first = line-index, second = length of that line */
    private var mLongestLine = MutablePair(0, 0)

    // for each line: text buffer & analyzed tokens
    private val mTextBuffersAndTokens = ArrayList<MutablePair<StringBuffer, ArrayList<Token>>>()

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
        mTextBuffersAndTokens.add(MutablePair(StringBuffer(), arrayListOf()))

        initGraphicsContext()
        initEvents()
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
                mMouseDownPt = Point2D(mouseEvent.x, mouseEvent.y)
                handleMouseDown(mouseEvent.x, mouseEvent.y)
            }
        }

        // mouse-moved
        setOnMouseMoved { mouseEvent ->
            // if the left-button is down
            if (mouseEvent.isPrimaryButtonDown && mMouseDownPt != null) {
                // TODO
            }
        }

        // mouse-released
        setOnMouseReleased { mMouseDownPt = null }

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

    // handle the mouse click-location invoked by the event of on-mouse-pressed
    private fun handleMouseDown(mouseX: Double, mouseY: Double) {
        // get the original line index of the caret
        val origLineIdx = mCaretLineIdx

        // get the original caret offset
        val origOffset = mCaretOffset

        // set the new line index of the caret
        mCaretLineIdx = min(mTextBuffersAndTokens.size - 1, floor(mouseY / PreferenceManager.EditorPref.lineH).toInt())

        // set the new caret offset
        mCaretOffset = min(mTextBuffersAndTokens[mCaretLineIdx].first.length, (mouseX / PreferenceManager.EditorPref.charW).roundToInt())

        // re-render if the current line changed
        if (mCaretLineIdx != origLineIdx || mCaretOffset != origOffset) {
            mOrigestCaretOffset = mCaretOffset
            render()
        }
    }

    // handle the keyboard input invoked by the event of on-key-pressed
    private fun handleKeyboardInput(ch: String, chCode: KeyCode) {
        // F5
        if (chCode == KeyCode.F5) {
        }

        /* ===================================================================== */

        // left arrow (ctrl-able, shift-able)
        else if (chCode == KeyCode.LEFT) {
            // ctrl + left: move a word to left
            if (mIsCtrlPressed) {
                if (mCaretOffset > 0) {
                    // for text selection
                    val origCaretOffset = mCaretOffset

                    // regular expressions for word searching
                    val identifierRegex = "[0-9A-Za-z_]"
                    val spaceRegex = "\\s"

                    // the character in front of the caret
                    val chInFront = mTextBuffersAndTokens[mCaretLineIdx].first.toString().substring(mCaretOffset - 1, mCaretOffset)

                    // search to left
                    while (true) {
                        --mCaretOffset
                        if (mCaretOffset == 0)
                            break

                        val newChInFront = mTextBuffersAndTokens[mCaretLineIdx].first.toString().substring(mCaretOffset - 1, mCaretOffset)
                        if (chInFront.matches(identifierRegex.toRegex()) && newChInFront.matches(identifierRegex.toRegex()))
                            continue
                        if (chInFront.matches(spaceRegex.toRegex()) && newChInFront.matches(spaceRegex.toRegex()))
                            continue
                        break
                    }

                    // update the origest-caret-offset
                    mOrigestCaretOffset = mCaretOffset

                    // deal w/ selection
                    manageSelectionWithAnInterval(mCaretLineIdx, origCaretOffset, mCaretLineIdx, mCaretOffset)
                }
            }
            // left-only: move a single character to left
            else {
                if (mCaretOffset > 0) {
                    --mCaretOffset
                    mOrigestCaretOffset = mCaretOffset

                    // deal w/ selection
                    manageSelectionWithAnInterval(mCaretLineIdx, mCaretOffset + 1, mCaretLineIdx, mCaretOffset)
                }
            }
        }

        // right arrow (ctrl-able, shift-able)
        else if (chCode == KeyCode.RIGHT) {
            // ctrl + right: move a word to right
            if (mIsCtrlPressed) {
                if (mCaretOffset < mTextBuffersAndTokens[mCaretLineIdx].first.length) {
                    // for text selection
                    val origCaretOffset = mCaretOffset

                    // regular expressions for word searching
                    val identifierRegex = "[0-9A-Za-z_]"
                    val spaceRegex = "\\s"

                    // the character behind the caret
                    val chBehind = mTextBuffersAndTokens[mCaretLineIdx].first.toString().substring(mCaretOffset, mCaretOffset + 1)

                    // search to left
                    while (true) {
                        ++mCaretOffset
                        if (mCaretOffset == mTextBuffersAndTokens[mCaretLineIdx].first.length)
                            break

                        val newChBehind = mTextBuffersAndTokens[mCaretLineIdx].first.toString().substring(mCaretOffset, mCaretOffset + 1)
                        if (chBehind.matches(identifierRegex.toRegex()) && newChBehind.matches(identifierRegex.toRegex()))
                            continue
                        if (chBehind.matches(spaceRegex.toRegex()) && newChBehind.matches(spaceRegex.toRegex()))
                            continue
                        break
                    }

                    // update the origest-caret-offset
                    mOrigestCaretOffset = mCaretOffset

                    // deal w/ selection
                    manageSelectionWithAnInterval(mCaretLineIdx, origCaretOffset, mCaretLineIdx, mCaretOffset)
                }
            }
            // move a single character to right
            else {
                if (mCaretOffset < mTextBuffersAndTokens[mCaretLineIdx].first.length) {
                    ++mCaretOffset
                    mOrigestCaretOffset = mCaretOffset

                    // deal w/ selection
                    manageSelectionWithAnInterval(mCaretLineIdx, mCaretOffset - 1, mCaretLineIdx, mCaretOffset)
                }
            }
        }

        // up arrow (shift-able)
        else if (chCode == KeyCode.UP) {
            if (mCaretLineIdx > 0) {
                val origCaretOffset = mCaretOffset

                --mCaretLineIdx
                mCaretOffset = min(mOrigestCaretOffset, mTextBuffersAndTokens[mCaretLineIdx].first.length)

                // deal w/ selection
                manageSelectionWithAnInterval(mCaretLineIdx + 1, origCaretOffset, mCaretLineIdx, mCaretOffset)
            }
        }

        // down arrow (shift-able)
        else if (chCode == KeyCode.DOWN) {
            if (mCaretLineIdx < mTextBuffersAndTokens.size - 1) {
                val origCaretOffset = mCaretOffset

                ++mCaretLineIdx
                mCaretOffset = min(mOrigestCaretOffset, mTextBuffersAndTokens[mCaretLineIdx].first.length)

                // deal w/ selection
                manageSelectionWithAnInterval(mCaretLineIdx - 1, origCaretOffset, mCaretLineIdx, mCaretOffset)
            }
        }

        // end (ctrl-able, shift-able)
        else if (chCode == KeyCode.END) {
            val origCaretOffset = mCaretOffset
            val origLineIdx = mCaretLineIdx

            // ctrl + end: go to the last character of the last line
            if (mIsCtrlPressed) {
                mCaretLineIdx = mTextBuffersAndTokens.size - 1
                mCaretOffset = mTextBuffersAndTokens[mCaretLineIdx].first.length
                mOrigestCaretOffset = mCaretOffset
            }
            // go to the last character of the current line
            else {
                if (mCaretOffset != mTextBuffersAndTokens[mCaretLineIdx].first.length) {
                    mCaretOffset = mTextBuffersAndTokens[mCaretLineIdx].first.length
                    mOrigestCaretOffset = mCaretOffset
                }
            }

            // deal w/ selection
            manageSelectionWithAnInterval(origLineIdx, origCaretOffset, mCaretLineIdx, mCaretOffset)
        }

        // home (ctrl-able, shift-able)
        else if (chCode == KeyCode.HOME) {
            val origCaretOffset = mCaretOffset
            val origLineIdx = mCaretLineIdx

            // ctrl + home: go to the first character of the first line
            if (mIsCtrlPressed) {
                mCaretLineIdx = 0
                mCaretOffset = 0
                mOrigestCaretOffset = 0
            }
            // go to the first character of the current line
            else {
                if (mCaretOffset > 0) {
                    mCaretOffset = 0
                    mOrigestCaretOffset = mCaretOffset
                }
            }

            // deal w/ selection
            manageSelectionWithAnInterval(origLineIdx, origCaretOffset, mCaretLineIdx, mCaretOffset)
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
                if (mCaretOffset > 0) {
                    mTextBuffersAndTokens[mCaretLineIdx].first.deleteCharAt(mCaretOffset - 1)
                    --mCaretOffset
                    mOrigestCaretOffset = mCaretOffset
                }
                // delete a '\n'
                else {
                    if (mCaretLineIdx > 0) {
                        mCaretOffset = mTextBuffersAndTokens[mCaretLineIdx - 1].first.length
                        mOrigestCaretOffset = mCaretOffset
                        mTextBuffersAndTokens[mCaretLineIdx - 1].first.append(mTextBuffersAndTokens[mCaretLineIdx].first.toString())
                        mTextBuffersAndTokens.removeAt(mCaretLineIdx)
                        --mCaretLineIdx
                    }
                }
            }

            // find out and set the longest line
            setLongestLine()
        }

        // delete
        else if (chCode == KeyCode.DELETE) {
            // delete the selected text (as the same operation of back-space)
            if (mSelectionManager.hasSelection())
                removeSelectedText()

            // delete a single character or '\n' behind the caret
            else {
                // delete a single character behind the caret
                if (mCaretOffset < mTextBuffersAndTokens[mCaretLineIdx].first.length) {
                    mTextBuffersAndTokens[mCaretLineIdx].first.deleteCharAt(mCaretOffset)
                }
                // delete a '\n'
                else {
                    if (mCaretLineIdx < mTextBuffersAndTokens.size - 1) {
                        mTextBuffersAndTokens[mCaretLineIdx].first.append(mTextBuffersAndTokens[mCaretLineIdx + 1].first.toString())
                        mTextBuffersAndTokens.removeAt(mCaretLineIdx + 1)
                    }
                }
            }

            // find out and set the longest line
            setLongestLine()
        }

        // enter (shift-able)
        else if (chCode == KeyCode.ENTER) {
            // if shift is NOT pressed
            if (!mIsShiftPressed) {
                // remove the selected text
                removeSelectedText()

                // insert a new string-buffer for this new line
                mTextBuffersAndTokens.add(
                        mCaretLineIdx + 1,
                        MutablePair(
                                StringBuffer(mTextBuffersAndTokens[mCaretLineIdx].first.substring(mCaretOffset)),
                                arrayListOf()
                        )
                )

                // move the sub-string behind the origin caret location to the new line
                mTextBuffersAndTokens[mCaretLineIdx].first.delete(mCaretOffset, mTextBuffersAndTokens[mCaretLineIdx].first.length)
            }
            // if shift is pressed
            else {
                mTextBuffersAndTokens.add(mCaretLineIdx + 1, MutablePair(StringBuffer(), arrayListOf()))
            }

            // update the caret offset and line index
            mCaretOffset = 0
            mOrigestCaretOffset = mCaretOffset
            ++mCaretLineIdx

            // clear selections
            mSelectionManager.clearSelections()

            // find out and set the longest line
            setLongestLine()
        }

        /* ===================================================================== */

        // visible character & white-space (shift-able)
        else if (chCode.code >= 32) {
            // try to do a certain special operation, e.g., Ctrl+C, Ctrl+X, ...
            if (doSpecialEditorOperation(ch)) return

            // get the visible character
            val vCh = getVisibleChar(ch) ?: return

            // remove the selected text
            removeSelectedText()

            // append to the string-buffer
            mTextBuffersAndTokens[mCaretLineIdx].first.insert(mCaretOffset, vCh)

            // update the caret offset
            ++mCaretOffset
            mOrigestCaretOffset = mCaretOffset

            // if the character is a symmetric character -> add the symmetric one
            if (PreferenceManager.EditorPref.Typing.symmetricSymbols.containsKey(vCh.toString())) {
                // append to the string-buffer
                mTextBuffersAndTokens[mCaretLineIdx].first.insert(
                        mCaretOffset,
                        PreferenceManager.EditorPref.Typing.symmetricSymbols[vCh.toString()]
                )
            }

            // find out and set the longest line
            setLongestLine()
        }

        // tab
        else if (chCode == KeyCode.TAB) {
            // remove the selected text
            removeSelectedText()

            // append to the string-buffer
            mTextBuffersAndTokens[mCaretLineIdx].first.insert(mCaretOffset, "    ")

            // update the caret offset
            mCaretOffset += 4
            mOrigestCaretOffset = mCaretOffset

            // find out and set the longest line
            setLongestLine()
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
        if (chCode != KeyCode.CONTROL && chCode != KeyCode.SHIFT) {
            // deal w/ the camera
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
            var maxX = mLongestLine.second * PreferenceManager.EditorPref.charW -
                    mCamera.width +
                    PreferenceManager.EditorPref.blankWidth
            if (maxX < 0.0) maxX = 0.0

            // move the camera left/right
            mCamera.move(
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
            var maxY = mTextBuffersAndTokens.size * PreferenceManager.EditorPref.lineH -
                    mCamera.height +
                    PreferenceManager.EditorPref.blankHeight
            if (maxY < 0.0) maxY = 0.0

            // move the camera up/down
            mCamera.move(
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
    private fun doSpecialEditorOperation(ch: String): Boolean {
        // if it's not a single character -> false
        if (ch.length != 1) return false

        // get the key code from ch, then try to get the corresponding function
        val keyCode = KeyCode.getKeyCode(ch.toUpperCase()) ?: return false
        val func = PreferenceManager.EditorPref.Shortcuts.getOperation(
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
    private fun setLongestLine() {
        if (mTextBuffersAndTokens[mCaretLineIdx].first.length > mLongestLine.second)
            mLongestLine.setPair(mCaretLineIdx, mTextBuffersAndTokens[mCaretLineIdx].first.length)
        if (mCaretLineIdx > 0 && mTextBuffersAndTokens[mCaretLineIdx - 1].first.length > mLongestLine.second)
            mLongestLine.setPair(mCaretLineIdx - 1, mTextBuffersAndTokens[mCaretLineIdx - 1].first.length)
        if (mCaretLineIdx < mTextBuffersAndTokens.size - 1 && mTextBuffersAndTokens[mCaretLineIdx + 1].first.length > mLongestLine.second)
            mLongestLine.setPair(mCaretLineIdx + 1, mTextBuffersAndTokens[mCaretLineIdx + 1].first.length)
    }

    // remove the selected text
    private fun removeSelectedText() {
        val pair = mSelectionManager.removeSelectedText(mTextBuffersAndTokens)
        if (pair != null) {
            mCaretLineIdx = pair.first
            mCaretOffset = pair.second
            mOrigestCaretOffset = mCaretOffset
        }
    }

    // deal w/ an unprocessed interval
    private fun manageSelectionWithAnInterval(startLineIdx: Int, startLineOffset: Int, endLineIdx: Int, endLineOffset: Int) {
        if (mIsShiftPressed)
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
    private fun manageCamera() {
        // get the total height to the current line index
        val totalHeightToLineIdx = (mCaretLineIdx + 1) * PreferenceManager.EditorPref.lineH - mCamera.locY

        // scroll down if the current line is over-flowing (bigger than the editor height)
        if (totalHeightToLineIdx > PreferenceManager.codeCvsHeight) {
            val difference = totalHeightToLineIdx - PreferenceManager.codeCvsHeight
            mCamera.move(0.0, difference)
        }
        // scroll up if the current line is under-flowing (smaller than two lines' height)
        else if (totalHeightToLineIdx < PreferenceManager.EditorPref.lineH * 2.0) {
            val difference =
                    if (mCaretLineIdx == 0)
                        PreferenceManager.EditorPref.lineH - totalHeightToLineIdx
                    else
                        PreferenceManager.EditorPref.lineH * 2.0 - totalHeightToLineIdx
            mCamera.move(0.0, -difference)
        }

        /**/

        // the start x-offset of texts
        val offsetX =
                PreferenceManager.EditorPref.lineStartOffsetX + PreferenceManager.EditorPref.LineNumberArea.width

        // get the total width to the current caret offset
        val totalWidthToCaretOffset = offsetX + mCaretOffset * PreferenceManager.EditorPref.charW - mCamera.locX

        // scroll right if the current caret offset is over-flowing (bigger than the editor width)
        if (totalWidthToCaretOffset > PreferenceManager.codeCvsWidth - PreferenceManager.EditorPref.lineStartOffsetX) {
            val difference = totalWidthToCaretOffset - (PreferenceManager.codeCvsWidth - PreferenceManager.EditorPref.lineStartOffsetX)
            mCamera.move(difference, 0.0)
        }
        // scroll left if the current caret offset is under-flowing (smaller than offset-x)
        else if (totalWidthToCaretOffset < offsetX) {
            val difference = offsetX - totalWidthToCaretOffset
            mCamera.move(-difference, 0.0)
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
        mGphCxt?.fillRect(-mCamera.locX, mCaretLineIdx * lineH - mCamera.locY, mWidth + mCamera.locX, lineH)

        // render the text
        renderText()

        // render the caret
        renderCaret()

        // render the line number area
        renderLineNumberArea()
    }

    // render the text, including background-hints of selections
    private fun renderText() {
        // do lexeme analysis
        try {
            // analyze each line
            mTextBuffersAndTokens.forEach { pair ->
                pair.second = PreferenceManager.LangPref.getUsedLang()!!.compile(
                        pair.first.toString() + "\n"
                )!!
            }
        } catch (e: Exception) { println(e.message) }

        // render all of the tokens
        var caretY = 0
        // iterate every line to render the backgrounds
        mTextBuffersAndTokens.forEach { (_, tokens) ->
            var caretX = 0
            // iterate every token in each line
            tokens.forEach { token ->
                if (token.text != "\n") {
                    // render text
                    token.renderBackground(mGphCxt, mCamera, caretX, caretY)

                    // update the caret of x-axis
                    caretX += token.text.length
                }
            }

            // move the caret-y
            ++caretY
        }

        /* ====== */

        mSelectionManager.renderSelectionBackground(mGphCxt, mCamera)

        /* ====== */

        caretY = 0
        // iterate every line to render the texts
        mTextBuffersAndTokens.forEach { (_, tokens) ->
            var caretX = 0
            // iterate every token in each line
            tokens.forEach { token ->
                if (token.text != "\n") {
                    // render text
                    token.render(mGphCxt, mCamera, caretX, caretY)

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
                mCaretOffset * charW - halfOfCaretWidth +
                        PreferenceManager.EditorPref.lineStartOffsetX +
                        PreferenceManager.EditorPref.LineNumberArea.width -
                        mCamera.locX,
                mCaretLineIdx * PreferenceManager.EditorPref.lineH -
                        mCamera.locY,
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
        val maxDigitLen = (mTextBuffersAndTokens.size - 1).toString().length
        mGphCxt?.fill = PreferenceManager.EditorPref.LineNumberArea.fontClr
        mGphCxt?.font = PreferenceManager.EditorPref.font
        for (y in 0 until mTextBuffersAndTokens.size) {
            if (y == mCaretLineIdx)
                continue
            mGphCxt?.fillText(
                    " ".repeat(maxDigitLen - y.toString().length) + y.toString(),
                    PreferenceManager.EditorPref.LineNumberArea.numberOffsetX,
                    (y + 1) * PreferenceManager.EditorPref.lineH -
                            PreferenceManager.EditorPref.differenceBetweenLineHeightAndFontSize -
                            mCamera.locY,
                    PreferenceManager.EditorPref.LineNumberArea.width
            )
        }

        // render the selected line number
        mGphCxt?.fill = PreferenceManager.EditorPref.LineNumberArea.selectedFontClr
        mGphCxt?.fillText(
                " ".repeat(maxDigitLen - mCaretLineIdx.toString().length) + mCaretLineIdx.toString(),
                PreferenceManager.EditorPref.LineNumberArea.numberOffsetX,
                (mCaretLineIdx + 1) * PreferenceManager.EditorPref.lineH -
                        PreferenceManager.EditorPref.differenceBetweenLineHeightAndFontSize -
                        mCamera.locY,
                PreferenceManager.EditorPref.LineNumberArea.width
        )
    }

    /* ===================================================================== */

    // copy the selected text
    fun copySelectedText() {
        // get the selected text
        val selectedText = mSelectionManager.getSelectedText(mTextBuffersAndTokens)

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
    }

    // paste the selected text
    fun pasteSelectedText() {
        // get the system clipboard
        val clipboard = Clipboard.getSystemClipboard()

        // if the clipboard has a string -> paste it on
        if (clipboard.hasString()) {
            // remove the selected text first
            removeSelectedText()

            // get the several lines each separated by a single '\n'
            val lines = clipboard.string.split("\n")
            var first = true
            var tmpLastHalf = ""
            // insert them
            lines.forEach { line ->
                // add a whole new line if it's NOT the first line
                if (!first) {
                    mTextBuffersAndTokens.add(mCaretLineIdx + 1, MutablePair(StringBuffer(), arrayListOf()))
                    ++mCaretLineIdx
                    mCaretOffset = 0
                    mOrigestCaretOffset = mCaretOffset
                }

                // get the last-half text if it's the first line
                if (first) {
                    tmpLastHalf = mTextBuffersAndTokens[mCaretLineIdx].first.substring(mCaretOffset)
                    mTextBuffersAndTokens[mCaretLineIdx].first.delete(mCaretOffset, mTextBuffersAndTokens[mCaretLineIdx].first.length)
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
