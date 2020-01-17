package rekkursion.util

import javafx.geometry.Point2D
import rekkursion.manager.PreferenceManager

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
    fun move(offsetX: Double, offsetY: Double): Point2D
            = set(mLocX + offsetX, mLocY + offsetY)

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
}