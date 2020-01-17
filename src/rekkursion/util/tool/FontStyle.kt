package rekkursion.util.tool

import javafx.scene.paint.Color

class FontStyle {
    // font-style builder
    class Builder {
        private val mFontStyle: FontStyle = FontStyle()

        // primary constructor
        constructor()

        // second constructor w/ an old font-style
        constructor(fontStyle: FontStyle) {
            mFontStyle.mSizeRatio = fontStyle.mSizeRatio
            mFontStyle.mFontColor = fontStyle.mFontColor
            mFontStyle.mIsBold = fontStyle.mIsBold
            mFontStyle.mIsItalic = fontStyle.mIsItalic
            mFontStyle.mIsUnderlined = fontStyle.mIsUnderlined
            mFontStyle.mUnderlineColor = fontStyle.mUnderlineColor
            mFontStyle.mBgColor = fontStyle.mBgColor
        }

        // create the building font-style
        fun create(): FontStyle = mFontStyle

        // set size ratio
        fun setSizeRatio(sizeRatio: Double): Builder {
            mFontStyle.mSizeRatio = sizeRatio
            return this
        }
        // set font color
        fun setFontColor(fontColor: Color): Builder {
            mFontStyle.mFontColor = fontColor
            return this
        }
        // set is bold, is italic, and is underlined
        fun setStyle(isBold: Boolean = false, isItalic: Boolean = false, isUnderlined: Boolean = false, underlineColor: Color = Color.TRANSPARENT): Builder {
            mFontStyle.mIsBold = isBold
            mFontStyle.mIsItalic = isItalic
            mFontStyle.mIsUnderlined = isUnderlined
            mFontStyle.mUnderlineColor = underlineColor
            return this
        }
        // set background color
        fun setBgColor(bgColor: Color): Builder {
            mFontStyle.mBgColor = bgColor
            return this
        }
    }

    /* ===================================================================== */

    // size ratio
    private var mSizeRatio: Double = 1.0
    val sizeRatio get() = mSizeRatio

    // font color
    private var mFontColor: Color = Color.WHITE
    val fontColor get() = mFontColor

    // is bold
    private var mIsBold: Boolean = false
    val isBold get() = mIsBold

    // is italic
    private var mIsItalic: Boolean = false
    val isItalic get() = mIsItalic

    // is underlined
    private var mIsUnderlined: Boolean = false
    val isUnderlined get() = mIsUnderlined

    // underline color
    private var mUnderlineColor: Color = Color.TRANSPARENT
    val underlineColor get() = mUnderlineColor

    // background color
    private var mBgColor: Color = Color.TRANSPARENT
    val bgColor get() = mBgColor
}