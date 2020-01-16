package rekkursion.util.statemachine.state

import rekkursion.util.statemachine.edge.Edge
import kotlin.collections.ArrayList

class State(contentText: String, type: StateType = StateType.INTERMEDIATE) {
    // the type of this state
    private val mType = type
    val type get() = mType

    // the content text
    private var mContentText = contentText
    var text get() = mContentText
    set(value) { mContentText = value }

    // the port which is used to store out-going edges
    private val mOutgoingPort = ArrayList<Edge>()
    val outgoingEdges = mOutgoingPort

    // the port which is used to store in-going edges
    private val mIngoingPort = ArrayList<Edge>()

    /* ===================================================================== */

    // add edge
    fun addEdge(edge: Edge, isOutgoing: Boolean) {
        if (isOutgoing)
            addOutgoingEdge(edge)
        else
            addIngoingEdge(edge)
    }

    // check if this state has a certain edge or not
    fun hasEdge(edge: Edge, checkOutgoing: Boolean, checkIngoing: Boolean): Boolean {
        if (checkOutgoing && mOutgoingPort.contains(edge))
            return true
        if (checkIngoing && mIngoingPort.contains(edge))
            return true
        return false
    }

    // find a certain edge by the edge text
    fun findEdgeByText(edgeText: String, isOutgoing: Boolean): Edge? {
        return if (isOutgoing)
            mOutgoingPort.find { it.edgeText == edgeText }
        else
            mIngoingPort.find { it.edgeText == edgeText }
    }

    // add out-going edge
    private fun addOutgoingEdge(edge: Edge) {
        mOutgoingPort.add(edge)
    }

    // add in-going edge
    private fun addIngoingEdge(edge: Edge) {
        mIngoingPort.add(edge)
    }

    /* ===================================================================== */

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as State

        if (mType != other.mType) return false
        if (mContentText != other.mContentText) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mType.hashCode()
        result = 31 * result + mContentText.hashCode()
        return result
    }
}