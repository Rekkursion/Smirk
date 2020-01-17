package rekkursion.util.tool

/* in the mutable-pair: first = line-idx, second = line-offset */
class TextInterval(start: MutablePair<Int, Int>, end: MutablePair<Int, Int>) {
    // secondary constructor
    constructor(startLineIdx: Int, startLineOffset: Int, endLineIdx: Int, endLindOffset: Int)
            : this(MutablePair(startLineIdx, startLineOffset), MutablePair(endLineIdx, endLindOffset))

    // the start location of this interval
    private val mStart = start
    val startCopied get() = mStart.copy()

    // the end location of this interval
    private val mEnd = end
    val endCopied get() = mEnd.copy()

    /* ===================================================================== */

    // set the start location
    fun setStart(lineIdx: Int = mStart.first, lineOffset: Int = mStart.second) {
        mStart.first = lineIdx
        mStart.second = lineOffset
    }

    // set the end location
    fun setEnd(lineIdx: Int = mEnd.first, lineOffset: Int = mEnd.second) {
        mEnd.first = lineIdx
        mEnd.second = lineOffset
    }

    // set the end location
    fun setEnd(pair: MutablePair<Int, Int>) { setEnd(pair.first, pair.second) }

    // copy
    fun copy(): TextInterval = TextInterval(mStart.copy(), mEnd.copy())

    // check if this interval is a reversed interval
    fun isReversed(): Boolean = (mStart.first > mEnd.first || (mStart.first == mEnd.first && mStart.second > mEnd.second))

    // check if another interval is overlapped/connected with this interval
    fun isOverlappedOrConnectedWith(rhs: TextInterval) {

    }

    // check if another interval is connected with this interval
    fun isConnectedWith(rhs: TextInterval): Boolean {
        var smallerLineIdx = 0
        var smallerLineOffset = 0
        var biggerLineIdx = 0
        var biggerLineOffset = 0
        if (mStart.first < mEnd.first || (mStart.first == mEnd.first && mStart.second < mEnd.second)) {
            smallerLineIdx = mStart.first
            smallerLineOffset = mStart.second
            biggerLineIdx = mEnd.first
            biggerLineOffset = mEnd.second
        }
        else {
            smallerLineIdx = mEnd.first
            smallerLineOffset = mEnd.second
            biggerLineIdx = mStart.first
            biggerLineOffset = mStart.second
        }

        var rhsSmallerLineIdx = 0
        var rhsSmallerLineOffset = 0
        var rhsBiggerLineIdx = 0
        var rhsBiggerLineOffset = 0
        if (rhs.mStart.first < rhs.mEnd.first || (rhs.mStart.first == rhs.mEnd.first && rhs.mStart.second < rhs.mEnd.second)) {
            rhsSmallerLineIdx = rhs.mStart.first
            rhsSmallerLineOffset = rhs.mStart.second
            rhsBiggerLineIdx = rhs.mEnd.first
            rhsBiggerLineOffset = rhs.mEnd.second
        }
        else {
            rhsSmallerLineIdx = rhs.mEnd.first
            rhsSmallerLineOffset = rhs.mEnd.second
            rhsBiggerLineIdx = rhs.mStart.first
            rhsBiggerLineOffset = rhs.mStart.second
        }

        return ((smallerLineIdx == rhsBiggerLineIdx && smallerLineOffset == rhsBiggerLineOffset) ||
                (biggerLineIdx == rhsSmallerLineIdx && biggerLineOffset == rhsSmallerLineOffset))
    }

    // merge the interval if possible
    fun mergeWith(rhs: TextInterval): Boolean {
        val isConnected = isConnectedWith(rhs)
        if (!isConnected)
            return false

        if (mStart == rhs.mStart) {

        }

        return true
    }

    // for destruction assignment, return the start line index
    operator fun component1(): Int = mStart.first

    // for destruction assignment, return the start line offset
    operator fun component2(): Int = mStart.second

    // for destruction assignment, return the end line index
    operator fun component3(): Int = mEnd.first

    // for destruction assignment, return the end line offset
    operator fun component4(): Int = mEnd.second

    /* ===================================================================== */

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextInterval

        if (mStart != other.mStart) return false
        if (mEnd != other.mEnd) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mStart.hashCode()
        result = 31 * result + mEnd.hashCode()
        return result
    }
}