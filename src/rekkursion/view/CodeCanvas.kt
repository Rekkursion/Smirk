package rekkursion.view

import javafx.geometry.Point2D
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import rekkursion.manager.PreferenceManager
import rekkursion.util.Camera
import rekkursion.util.Token

import java.util.ArrayList
import java.util.HashMap
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

class CodeCanvas(private val mWidth: Double, private val mHeight: Double): Canvas(mWidth, mHeight) {
    // the camera
    private val mCamera: Camera? = null

    // the graphics context
    private var mGphCxt: GraphicsContext? = null

    // the current line index of the caret
    private var mCaretLineIdx = 0

    // the current offset at a certain line of the caret
    private var mCaretOffset = 0

    // be used when going up/down
    private var mOrigestCaretOffset = 0

    // the text buffers for each line
    private val mTextBuffers = ArrayList<StringBuffer>()

    // all of the classified tokens
    private val mTokens = ArrayList<Token>()

    // is holding ctrl
    private var mIsCtrlPressed = false

    // is holding shift
    private var mIsShiftPressed = false

    // the point when the mouse is down
    private var mMouseDownPt: Point2D? = null

    // get the graphics location of the caret
//    private val mGraphicsLocationOfCaret: Point2D
//        get() = Point2D(mCaretOffset * CHARA_WIDTH, mCaretLineIdx * LINE_HEIGHT)

    // constructor
    init {
        mTextBuffers.add(StringBuffer())

        initGraphicsContext()
        initEvents()
    }

    /* ===================================================================== */

    // initialize the graphics-context
    private fun initGraphicsContext() {
        mGphCxt = graphicsContext2D
        mGphCxt!!.font = Font.font("Consolas", 20.0)

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
            val chCode = keyEvent.code

            if (chCode == KeyCode.CONTROL)
                mIsCtrlPressed = false
            else if (chCode == KeyCode.SHIFT)
                mIsShiftPressed = false
        }
    }

    // handle the mouse click-location invoked by the event of on-mouse-pressed
    private fun handleMouseDown(mouseX: Double, mouseY: Double) {
        // get the original line index of the caret
        val origLineIdx = mCaretLineIdx

        // get the original caret offset
        val origOffset = mCaretOffset

        // set the new line index of the caret
        mCaretLineIdx = min(mTextBuffers.size - 1, floor(mouseY / PreferenceManager.EditorPref.lineH).toInt())

        // set the new caret offset
        mCaretOffset = min(mTextBuffers[mCaretLineIdx].length, (mouseX / PreferenceManager.EditorPref.charW).roundToInt())

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
            val res = PreferenceManager.LangPref.getUsedLang()!!.classifyIntoTokens(
                    mTextBuffers.joinToString(separator = "\n") { it.toString() }
            )
            res!!.forEach {
                println(it.toString())
            }
        }

        // left arrow (ctrl-able)
        else if (chCode == KeyCode.LEFT) {
            // ctrl + left: move a word to left
            if (mIsCtrlPressed) {
                if (mCaretOffset > 0) {
                    val identifierRegex = "[0-9A-Za-z_]"
                    val spaceRegex = "\\s"
                    val chInFront = mTextBuffers[mCaretLineIdx].toString().substring(mCaretOffset - 1, mCaretOffset)

                    // search to left
                    while (true) {
                        --mCaretOffset
                        if (mCaretOffset == 0)
                            break

                        val newChInFront = mTextBuffers[mCaretLineIdx].toString().substring(mCaretOffset - 1, mCaretOffset)
                        if (chInFront.matches(identifierRegex.toRegex()) && newChInFront.matches(identifierRegex.toRegex()))
                            continue
                        if (chInFront.matches(spaceRegex.toRegex()) && newChInFront.matches(spaceRegex.toRegex()))
                            continue
                        break
                    }

                    // re-render
                    mOrigestCaretOffset = mCaretOffset
                    render()
                }
            }
            // move a single character to left
            else {
                if (mCaretOffset > 0) {
                    --mCaretOffset
                    mOrigestCaretOffset = mCaretOffset
                    render()
                }
            }
        }

        // right arrow (ctrl-able)
        else if (chCode == KeyCode.RIGHT) {
            // ctrl + right: move a word to right
            if (mIsCtrlPressed) {
                if (mCaretOffset < mTextBuffers[mCaretLineIdx].length) {
                    val identifierRegex = "[0-9A-Za-z_]"
                    val spaceRegex = "\\s"
                    val chBehind = mTextBuffers[mCaretLineIdx].toString().substring(mCaretOffset, mCaretOffset + 1)

                    // search to left
                    while (true) {
                        ++mCaretOffset
                        if (mCaretOffset == mTextBuffers[mCaretLineIdx].length)
                            break

                        val newChBehind = mTextBuffers[mCaretLineIdx].toString().substring(mCaretOffset, mCaretOffset + 1)
                        if (chBehind.matches(identifierRegex.toRegex()) && newChBehind.matches(identifierRegex.toRegex()))
                            continue
                        if (chBehind.matches(spaceRegex.toRegex()) && newChBehind.matches(spaceRegex.toRegex()))
                            continue
                        break
                    }

                    // re-render
                    mOrigestCaretOffset = mCaretOffset
                    render()
                }
            }
            // move a single character to right
            else {
                if (mCaretOffset < mTextBuffers[mCaretLineIdx].length) {
                    ++mCaretOffset
                    mOrigestCaretOffset = mCaretOffset
                    render()
                }
            }
        }

        // up arrow
        else if (chCode == KeyCode.UP) {
            if (mCaretLineIdx > 0) {
                --mCaretLineIdx
                mCaretOffset = min(mOrigestCaretOffset, mTextBuffers[mCaretLineIdx].length)
                render()
            }
        }

        // down arrow
        else if (chCode == KeyCode.DOWN) {
            if (mCaretLineIdx < mTextBuffers.size - 1) {
                ++mCaretLineIdx
                mCaretOffset = min(mOrigestCaretOffset, mTextBuffers[mCaretLineIdx].length)
                render()
            }
        }

        // end (ctrl-able)
        else if (chCode == KeyCode.END) {
            // ctrl + end: go to the last character of the last line
            if (mIsCtrlPressed) {
                mCaretLineIdx = mTextBuffers.size - 1
                mCaretOffset = mTextBuffers[mCaretLineIdx].length
                mOrigestCaretOffset = mCaretOffset
                render()
            }
            // go to the last character of the current line
            else {
                if (mCaretOffset != mTextBuffers[mCaretLineIdx].length) {
                    mCaretOffset = mTextBuffers[mCaretLineIdx].length
                    mOrigestCaretOffset = mCaretOffset
                    render()
                }
            }
        }

        // home (ctrl-able)
        else if (chCode == KeyCode.HOME) {
            // ctrl + home: go to the first character of the first line
            if (mIsCtrlPressed) {
                mCaretLineIdx = 0
                mCaretOffset = 0
                mOrigestCaretOffset = 0
                render()
            }

            // go to the first character of the current line
            else {
                if (mCaretOffset > 0) {
                    mCaretOffset = 0
                    mOrigestCaretOffset = mCaretOffset
                    render()
                }
            }
        }

        // back-space
        else if (chCode == KeyCode.BACK_SPACE) {
            // delete a single character in front of the caret
            if (mCaretOffset > 0) {
                mTextBuffers[mCaretLineIdx].deleteCharAt(mCaretOffset - 1)
                --mCaretOffset
                mOrigestCaretOffset = mCaretOffset
                render()
            }
            // delete a '\n'
            else {
                if (mCaretLineIdx > 0) {
                    mCaretOffset = mTextBuffers[mCaretLineIdx - 1].length
                    mOrigestCaretOffset = mCaretOffset
                    mTextBuffers[mCaretLineIdx - 1].append(mTextBuffers[mCaretLineIdx].toString())
                    mTextBuffers.removeAt(mCaretLineIdx)
                    --mCaretLineIdx
                    render()
                }
            }
        }

        // delete
        else if (chCode == KeyCode.DELETE) {
            // delete a single character behind the caret
            if (mCaretOffset < mTextBuffers[mCaretLineIdx].length) {
                mTextBuffers[mCaretLineIdx].deleteCharAt(mCaretOffset)
                render()
            }
            // delete a '\n'
            else {
                if (mCaretLineIdx < mTextBuffers.size - 1) {
                    mTextBuffers[mCaretLineIdx].append(mTextBuffers[mCaretLineIdx + 1].toString())
                    mTextBuffers.removeAt(mCaretLineIdx + 1)
                    render()
                }
            }
        }

        // visible character & white-space (shift-able)
        else if (chCode.code >= 32) {
            // append to the string-buffer
            mTextBuffers[mCaretLineIdx].insert(mCaretOffset, getVisibleChar(ch))

            // update the caret offset
            ++mCaretOffset
            mOrigestCaretOffset = mCaretOffset

            // re-render
            render()
        }

        // enter (shift-able)
        else if (chCode == KeyCode.ENTER) {
            // if shift is NOT pressed
            if (!mIsShiftPressed) {
                // insert a new string-buffer for this new line
                mTextBuffers.add(
                        mCaretLineIdx + 1,
                        StringBuffer(mTextBuffers[mCaretLineIdx].substring(mCaretOffset))
                )

                // move the sub-string behind the origin caret location to the new line
                mTextBuffers[mCaretLineIdx].delete(mCaretOffset, mTextBuffers[mCaretLineIdx].length)
            }
            // if shift is pressed
            else {
                mTextBuffers.add(mCaretLineIdx + 1, StringBuffer())
            }

            // update the caret offset and line index
            mCaretOffset = 0
            mOrigestCaretOffset = mCaretOffset
            ++mCaretLineIdx

            // re-render
            render()
        }

        // tab
        else if (chCode == KeyCode.TAB) {
            // append to the string-buffer
            mTextBuffers[mCaretLineIdx].insert(mCaretOffset, "    ")

            // update the caret offset
            mCaretOffset += 4
            mOrigestCaretOffset = mCaretOffset

            // re-render
            render()
        }

        // modifier: ctrl
        else if (chCode == KeyCode.CONTROL) {
            mIsCtrlPressed = true
        }

        // modifier: shift
        else if (chCode == KeyCode.SHIFT) {
            mIsShiftPressed = true
        }
    }

    // get the visible character
    private fun getVisibleChar(ch: String): Char {
        return if (!mIsShiftPressed)
            ch[0]
        else
            mShiftableCharactersMap?.getOrDefault(ch, ch[0]) ?: ch[0]
    }

    // render
    private fun render() {
        val lineH = PreferenceManager.EditorPref.lineH

        // render the background
        mGphCxt?.fill = PreferenceManager.codeCanvasBgColor
        mGphCxt?.fillRect(0.0, 0.0, PreferenceManager.codeCvsWidth, PreferenceManager.codeCvsHeight)

        // render the line hint
        mGphCxt?.fill = PreferenceManager.lineHintColor
        mGphCxt?.fillRect(0.0, mCaretLineIdx * lineH, mWidth, lineH)

        // render the text
        renderText()

        // render the caret
        renderCaret()
    }

    // render the text
    private fun renderText() {
        val lineH = PreferenceManager.EditorPref.lineH
        val lineStartOffset = PreferenceManager.EditorPref.lineStartOffset

        // do lexeme analysis
        val tokens = PreferenceManager.LangPref.getUsedLang()!!.classifyIntoTokens(
                mTextBuffers.joinToString(separator = "\n") { it.toString() }
        )

        var pointer = 0
        tokens?.forEach { token ->
            token?.toString()// TODO
        }

        // render the texts
        mGphCxt?.fill = Paint.valueOf("white")
        for (k in mTextBuffers.indices)
            mGphCxt?.fillText(mTextBuffers[k].toString(), lineStartOffset, (k + 1) * lineH - 5)
    }

    // render the caret
    private fun renderCaret() {
        val caretW = PreferenceManager.EditorPref.caretW
        val charW = PreferenceManager.EditorPref.charW
        val halfOfCaretWidth = caretW / 2.0

        mGphCxt?.fillRect(
                mCaretOffset * charW - halfOfCaretWidth + PreferenceManager.EditorPref.lineStartOffset,
                mCaretLineIdx * PreferenceManager.EditorPref.lineH,
                caretW,
                PreferenceManager.EditorPref.lineH
        )
    }

    // static scope
    companion object {
//        private const val CHARA_WIDTH = 11.0
//        private const val LINE_HEIGHT = 24.0
//        private const val CARET_WIDTH = 1.0
//        private const val LINE_START_OFFSET = 3.0

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
