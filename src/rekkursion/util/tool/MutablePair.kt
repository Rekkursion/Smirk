package rekkursion.util.tool

class MutablePair<T, S>(first: T, second: S) {
    // the first value of this pair
    private var mFirst = first
    var first get() = mFirst
    set(value) { mFirst = value }

    // the second value of this pair
    private var mSecond = second
    var second get() = mSecond
    set(value) { mSecond = value }

    /* ===================================================================== */

    // return the swapped mutable-pair
    fun swap(): MutablePair<S, T> = MutablePair(mSecond, mFirst)

    // copy into a new pair
    fun copy(): MutablePair<T, S> = MutablePair(mFirst, mSecond)

    // for destruction assignment, return the first value
    operator fun component1(): T = mFirst

    // for destruction assignment, return the second value
    operator fun component2(): S = mSecond

    /* ===================================================================== */

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MutablePair<*, *>

        if (mFirst != other.mFirst) return false
        if (mSecond != other.mSecond) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mFirst?.hashCode() ?: 0
        result = 31 * result + (mSecond?.hashCode() ?: 0)
        return result
    }
}