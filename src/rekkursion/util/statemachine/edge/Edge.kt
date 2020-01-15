package rekkursion.util.statemachine.edge

import rekkursion.util.statemachine.state.State
import java.util.*

class Edge(srcState: State, dstState: State, edgeText: String, edgeType: EdgeType = EdgeType.GENERAL) {
    // the unique id of every instance
    private val mId = UUID.randomUUID().toString()

    // the edge type
    private val mType = edgeType
    val type get() = mType

    // the source state of this edge
    private val mSrcState = srcState
    val srcState get() = mSrcState

    // the destination state of this edge
    private val mDstState = dstState
    val dstState get() = mDstState

    // the text (could be regular expression) of this edge
    private val mEdgeText = edgeText

    /* ===================================================================== */

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Edge

        if (mId != other.mId) return false

        return true
    }

    override fun hashCode(): Int {
        return mId.hashCode()
    }
}