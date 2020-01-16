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
    fun swap() = MutablePair(mSecond, mFirst)

    // for destruction assignment, return the first value
    operator fun component1(): T = mFirst

    // for destruction assignment, return the second value
    operator fun component2(): S = mSecond
}