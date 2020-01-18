package rekkursion.util.tool

class ShortcutCommand(isCtrlPressed: Boolean, isShiftPressed: Boolean, isAltPressed: Boolean, primaryKeyCode: Int) {
    // store the information of key code, ctrl, shift, & alt pressed or not
    private var mValue: Int

    // for primary constructor
    init {
        mValue = primaryKeyCode
        if (isCtrlPressed)
            mValue = mValue.or(1 shl 31)
        if (isShiftPressed)
            mValue = mValue.or(1 shl 30)
        if (isAltPressed)
            mValue = mValue.or(1 shl 29)
    }

    /* ===================================================================== */

    // check if ctrl is pressed
    fun isCtrlPressed(): Boolean = ((mValue shr 31) and 1) == 1

    // check if shift is pressed
    fun isShiftPressed(): Boolean = ((mValue shr 30) and 1) == 1

    // check if alt is pressed
    fun isAltPressed(): Boolean = ((mValue shr 29) and 1) == 1

    // get the key code
    fun getKeyCode(): Int = mValue and (7 shl 29).inv()

    /* ===================================================================== */

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShortcutCommand

        if (mValue != other.mValue) return false

        return true
    }

    override fun hashCode(): Int {
        return mValue
    }

    @ExperimentalUnsignedTypes
    override fun toString(): String {
        return "|value in binary format = ${mValue.toUInt().toString(2)}|"
    }
}