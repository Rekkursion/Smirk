package rekkursion.util

import javafx.geometry.Point2D
import rekkursion.manager.PreferenceManager
import rekkursion.util.tool.MutablePair

class Camera(
        locX: Double = 0.0,
        locY: Double = 0.0,
        width: Double = PreferenceManager.codeCvsWidth,
        height: Double = PreferenceManager.codeCvsHeight) {

    /* ===================================================================== */

    // x-location
    private var mLocX = locX
    val locX get() = mLocX

    // y-location
    private var mLocY = locY
    val locY get() = mLocY

    // width
    private var mWidth = width
    val width = mWidth

    // height
    private var mHeight = height
    val height = mHeight

    /* ===================================================================== */

    // move the camera by the offset
    fun move(
            offsetX: Double = 0.0,
            offsetY: Double = 0.0,
            maxX: Double = Double.POSITIVE_INFINITY,
            maxY: Double = Double.POSITIVE_INFINITY,
            minX: Double = 0.0,
            minY: Double = 0.0): Point2D {

        val newLocX = when {
                    mLocX + offsetX < minX -> minX
                    mLocX + offsetX > maxX -> maxX
                    else -> mLocX + offsetX
                }
        val newLocY = when {
                    mLocY + offsetY < minY -> minY
                    mLocY + offsetY > maxY -> maxY
                    else -> mLocY + offsetY
                }

        return set(newLocX, newLocY)
    }

    // change the camera size
    fun changeSize(newWidth: Double, newHeight: Double) {
        mWidth = newWidth
        mHeight = newHeight
    }

    // set the camera location and the size
    fun set(newLocX: Double, newLocY: Double, newWidth: Double = mWidth, newHeight: Double = mHeight): Point2D {
        mLocX = newLocX
        mLocY = newLocY
        changeSize(newWidth, newHeight)

        return Point2D(mLocX, mLocY)
    }

    // get line-indices of bounds of covered lines (lower & upper bounds)
    fun getCameraCoveredBoundsLinesIndices(): MutablePair<Int, Int> {
        // get the safe lower bound
        var lowerBoundIdx: Int = (mLocY / PreferenceManager.EditorPref.lineH).toInt() - 3
        if (lowerBoundIdx < 0) lowerBoundIdx = 0

        // get the safe upper bound
        var upperBoundIdx: Int = lowerBoundIdx + (mHeight / PreferenceManager.EditorPref.lineH).toInt() + 6
        if (upperBoundIdx < 0) upperBoundIdx = 0

        // return them as a mutable-pair
        return MutablePair(lowerBoundIdx, upperBoundIdx)
    }
}