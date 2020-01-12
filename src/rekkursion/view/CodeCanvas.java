package rekkursion.view;

import com.sun.source.tree.ReturnTree;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CodeCanvas extends Canvas {
    private static final double CHARA_WIDTH = 11.0;
    private static final double LINE_HEIGHT = 24.0;
    private static final double CARET_WIDTH = 1.0;
    private static final double LINE_START_OFFSET = 3.0;

    private static Map<String, Character> mShiftableCharactersMap;

    // static initialization
    static {
        // build up the shift-able characters-map
        mShiftableCharactersMap = new HashMap<>();
        for (int k = 65; k <= 90; ++k) {
            mShiftableCharactersMap.put(String.valueOf((char) k), (char) (k + 32));
            mShiftableCharactersMap.put(String.valueOf((char) (k + 32)), (char) k);
        }
        mShiftableCharactersMap.put("1", '!');
        mShiftableCharactersMap.put("2", '@');
        mShiftableCharactersMap.put("3", '#');
        mShiftableCharactersMap.put("4", '$');
        mShiftableCharactersMap.put("5", '%');
        mShiftableCharactersMap.put("6", '^');
        mShiftableCharactersMap.put("7", '&');
        mShiftableCharactersMap.put("8", '*');
        mShiftableCharactersMap.put("9", '(');
        mShiftableCharactersMap.put("0", ')');
        mShiftableCharactersMap.put("-", '_');
        mShiftableCharactersMap.put("=", '+');
        mShiftableCharactersMap.put("\\", '|');
        mShiftableCharactersMap.put("]", '}');
        mShiftableCharactersMap.put("[", '{');
        mShiftableCharactersMap.put("\'", '\"');
        mShiftableCharactersMap.put(";", ':');
        mShiftableCharactersMap.put("/", '?');
        mShiftableCharactersMap.put(".", '>');
        mShiftableCharactersMap.put(",", '<');
        mShiftableCharactersMap.put("`", '~');
    }

    /* ===================================================================== */

    // the graphics context
    private GraphicsContext mGphCxt;

    // the width
    private double mWidth;

    // the height
    private double mHeight;

    // the current line index of the caret
    private int mCaretLineIdx = 0;

    // the current offset at a certain line of the caret
    private int mCaretOffset = 0;

    // be used when going up/down
    private int mOrigestCaretOffset = 0;

    // the text buffers for each line
    private ArrayList<StringBuffer> mTextBuffers = new ArrayList<>();

    // is holding ctrl
    private boolean mIsCtrlPressed = false;

    // is holding shift
    private boolean mIsShiftPressed = false;

    /* ===================================================================== */

    // constructor w/ a width and a height
    public CodeCanvas(double width, double height) {
        super(width, height);

        mWidth = width;
        mHeight = height;

        mTextBuffers.add(new StringBuffer());

        initGraphicsContext();
        initEvents();
    }

    /* ===================================================================== */

    // initialize the graphics-context
    private void initGraphicsContext() {
        mGphCxt = getGraphicsContext2D();
        mGphCxt.setFont(Font.font("Consolas", 20.0));

        render();
    }

    // initialize the events
    private void initEvents() {
        // mouse-clicked
        setOnMouseClicked(mouseEvent -> {
            // request the focus
            requestFocus();

            // deal with the mouse-clicked location
            handleMouseClick(mouseEvent.getX(), mouseEvent.getY());
        });

        // key-pressed
        setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String text = keyEvent.getText();
                KeyCode charCode = keyEvent.getCode();

                System.out.println(charCode.getCode() + "|" + text);

                // deal with the pressed input
                handleKeyboardInput(text, charCode);
            }
        });

        // key-released
        setOnKeyReleased(keyEvent -> {
            KeyCode chCode = keyEvent.getCode();

            if (chCode == KeyCode.CONTROL)
                mIsCtrlPressed = false;
            else if (chCode == KeyCode.SHIFT)
                mIsShiftPressed = false;
        });
    }

    // get the graphics location of the caret
    private Point2D getGraphicsLocationOfCaret() {
        return new Point2D(mCaretOffset * CHARA_WIDTH, mCaretLineIdx * LINE_HEIGHT);
    }

    // handle the mouse click-location invoked by the event of on-mouse-clicked
    private void handleMouseClick(double mouseX, double mouseY) {
        // get the original line index of the caret
        int origLineIdx = mCaretLineIdx;

        // get the original caret offset
        int origOffset = mCaretOffset;

        // set the new line index of the caret
        mCaretLineIdx = Math.min(mTextBuffers.size() - 1, (int) Math.floor(mouseY / LINE_HEIGHT));

        // set the new caret offset
        mCaretOffset = Math.min(mTextBuffers.get(mCaretLineIdx).length(), (int) Math.floor(mouseX / CHARA_WIDTH));

        // re-render if the current line changed
        if (mCaretLineIdx != origLineIdx || mCaretOffset != origOffset) {
            mOrigestCaretOffset = mCaretOffset;
            render();
        }
    }

    // handle the keyboard input invoked by the event of on-key-pressed
    private void handleKeyboardInput(String ch, KeyCode chCode) {
        // left arrow (ctrl-able)
        if (chCode == KeyCode.LEFT) {
            // ctrl + left: move a word to left
            if (mIsCtrlPressed) {
                if (mCaretOffset > 0) {
                    String identifierRegex = "[0-9A-Za-z_]";
                    String spaceRegex = "\\s";
                    String chInFront = mTextBuffers.get(mCaretLineIdx).toString().substring(mCaretOffset - 1, mCaretOffset);

                    // search to left
                    while (true) {
                        --mCaretOffset;
                        if (mCaretOffset == 0)
                            break;

                        String newChInFront = mTextBuffers.get(mCaretLineIdx).toString().substring(mCaretOffset - 1, mCaretOffset);
                        if (chInFront.matches(identifierRegex) && newChInFront.matches(identifierRegex))
                            continue;
                        if (chInFront.matches(spaceRegex) && newChInFront.matches(spaceRegex))
                            continue;
                        break;
                    }

                    // re-render
                    mOrigestCaretOffset = mCaretOffset;
                    render();
                }
            }

            // move a single character to left
            else {
                if (mCaretOffset > 0) {
                    --mCaretOffset;
                    mOrigestCaretOffset = mCaretOffset;
                    render();
                }
            }
        }

        // right arrow (ctrl-able)
        else if (chCode == KeyCode.RIGHT) {
            // ctrl + right: move a word to right
            if (mIsCtrlPressed) {
                if (mCaretOffset < mTextBuffers.get(mCaretLineIdx).length()) {
                    String identifierRegex = "[0-9A-Za-z_]";
                    String spaceRegex = "\\s";
                    String chBehind = mTextBuffers.get(mCaretLineIdx).toString().substring(mCaretOffset, mCaretOffset + 1);

                    // search to left
                    while (true) {
                        ++mCaretOffset;
                        if (mCaretOffset == mTextBuffers.get(mCaretLineIdx).length())
                            break;

                        String newChBehind = mTextBuffers.get(mCaretLineIdx).toString().substring(mCaretOffset, mCaretOffset + 1);
                        if (chBehind.matches(identifierRegex) && newChBehind.matches(identifierRegex))
                            continue;
                        if (chBehind.matches(spaceRegex) && newChBehind.matches(spaceRegex))
                            continue;
                        break;
                    }

                    // re-render
                    mOrigestCaretOffset = mCaretOffset;
                    render();
                }
            }

            // move a single character to right
            else {
                if (mCaretOffset < mTextBuffers.get(mCaretLineIdx).length()) {
                    ++mCaretOffset;
                    mOrigestCaretOffset = mCaretOffset;
                    render();
                }
            }
        }

        // up arrow
        else if (chCode == KeyCode.UP) {
            if (mCaretLineIdx > 0) {
                --mCaretLineIdx;
                mCaretOffset = Math.min(mOrigestCaretOffset, mTextBuffers.get(mCaretLineIdx).length());
                render();
            }
        }

        // down arrow
        else if (chCode == KeyCode.DOWN) {
            if (mCaretLineIdx < mTextBuffers.size() - 1) {
                ++mCaretLineIdx;
                mCaretOffset = Math.min(mOrigestCaretOffset, mTextBuffers.get(mCaretLineIdx).length());
                render();
            }
        }

        // end (ctrl-able)
        else if (chCode == KeyCode.END) {
            // ctrl + end: go to the last character of the last line
            if (mIsCtrlPressed) {
                mCaretLineIdx = mTextBuffers.size() - 1;
                mCaretOffset = mTextBuffers.get(mCaretLineIdx).length();
                mOrigestCaretOffset = mCaretOffset;
                render();
            }

            // go to the last character of the current line
            else {
                if (mCaretOffset != mTextBuffers.get(mCaretLineIdx).length()) {
                    mCaretOffset = mTextBuffers.get(mCaretLineIdx).length();
                    mOrigestCaretOffset = mCaretOffset;
                    render();
                }
            }
        }

        // home (ctrl-able)
        else if (chCode == KeyCode.HOME) {
            // ctrl + home: go to the first character of the first line
            if (mIsCtrlPressed) {
                mCaretLineIdx = 0;
                mCaretOffset = 0;
                mOrigestCaretOffset = 0;
                render();
            }

            // go to the first character of the current line
            else {
                if (mCaretOffset > 0) {
                    mCaretOffset = 0;
                    mOrigestCaretOffset = mCaretOffset;
                    render();
                }
            }
        }

        // back-space
        else if (chCode == KeyCode.BACK_SPACE) {
            // delete a single character in front of the caret
            if (mCaretOffset > 0) {
                mTextBuffers.get(mCaretLineIdx).deleteCharAt(mCaretOffset - 1);
                --mCaretOffset;
                mOrigestCaretOffset = mCaretOffset;
                render();
            }

            // delete a '\n'
            else {
                if (mCaretLineIdx > 0) {
                    mCaretOffset = mTextBuffers.get(mCaretLineIdx - 1).length();
                    mOrigestCaretOffset = mCaretOffset;
                    mTextBuffers.get(mCaretLineIdx - 1).append(mTextBuffers.get(mCaretLineIdx).toString());
                    mTextBuffers.remove(mCaretLineIdx);
                    --mCaretLineIdx;
                    render();
                }
            }
        }

        // delete
        else if (chCode == KeyCode.DELETE) {
            // delete a single character behind the caret
            if (mCaretOffset < mTextBuffers.get(mCaretLineIdx).length()) {
                mTextBuffers.get(mCaretLineIdx).deleteCharAt(mCaretOffset);
                render();
            }

            // delete a '\n'
            else {
                if (mCaretLineIdx < mTextBuffers.size() - 1) {
                    mTextBuffers.get(mCaretLineIdx).append(mTextBuffers.get(mCaretLineIdx + 1).toString());
                    mTextBuffers.remove(mCaretLineIdx + 1);
                    render();
                }
            }
        }

        // visible characters + white-space
        else if (chCode.getCode() >= 32) {
            // append to the string-buffer
            mTextBuffers.get(mCaretLineIdx).insert(mCaretOffset, getVisibleChar(ch, chCode));

            // update the caret offset
            ++mCaretOffset;
            mOrigestCaretOffset = mCaretOffset;

            // re-render
            render();
        }

        // enter, that is, a new line
        else if (chCode == KeyCode.ENTER) {
            // insert a new string-buffer for this new line
            mTextBuffers.add(
                    mCaretLineIdx + 1,
                    new StringBuffer(mTextBuffers.get(mCaretLineIdx).substring(mCaretOffset))
            );

            // move the sub-string behind the origin caret location to the new line
            mTextBuffers.get(mCaretLineIdx).delete(mCaretOffset, mTextBuffers.get(mCaretLineIdx).length());

            // update the caret offset and line index
            mCaretOffset = 0;
            mOrigestCaretOffset = mCaretOffset;
            ++mCaretLineIdx;

            // re-render
            render();
        }

        // tab
        else if (chCode == KeyCode.TAB) {
            // append to the string-buffer
            mTextBuffers.get(mCaretLineIdx).insert(mCaretOffset, "    ");

            // update the caret offset
            mCaretOffset += 4;
            mOrigestCaretOffset = mCaretOffset;

            // re-render
            render();
        }

        // ctrl
        else if (chCode == KeyCode.CONTROL) {
            mIsCtrlPressed = true;
        }

        // shift
        else if (chCode == KeyCode.SHIFT) {
            mIsShiftPressed = true;
        }
    }

    // get the visible character
    private char getVisibleChar(String ch, KeyCode chCode) {
        if (!mIsShiftPressed)
            return ch.charAt(0);
        return mShiftableCharactersMap.getOrDefault(ch, ch.charAt(0));
    }

    // render
    private void render() {
        // render the background
        mGphCxt.setFill(Paint.valueOf("#333"));
        mGphCxt.fillRect(0, 0, 1200, 600);

        // render the line hint
        mGphCxt.setFill(Paint.valueOf("#555"));
        mGphCxt.fillRect(0, mCaretLineIdx * LINE_HEIGHT, mWidth, LINE_HEIGHT);

        // render the texts
        mGphCxt.setFill(Paint.valueOf("white"));
        for (int k = 0; k < mTextBuffers.size(); ++k)
            mGphCxt.fillText(mTextBuffers.get(k).toString(), LINE_START_OFFSET, (k + 1) * LINE_HEIGHT - 5);

        // render the caret
        renderCaret();
    }

    // render the caret
    private void renderCaret() {
        double halfOfCaretWidth = CARET_WIDTH / 2.0;
        mGphCxt.fillRect(
            mCaretOffset * CHARA_WIDTH - halfOfCaretWidth + LINE_START_OFFSET,
            mCaretLineIdx * LINE_HEIGHT,
            CARET_WIDTH,
            LINE_HEIGHT
        );
    }
}
