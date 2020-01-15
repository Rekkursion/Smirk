package rekkursion.util.statemachine.state

import rekkursion.util.statemachine.edge.Edge
import rekkursion.util.statemachine.edge.EdgeType

class StateMachine {
    // state-machine builder
    class Builder(sm: StateMachine = StateMachine()) {
        private val mStateMachine = sm

        // create the state-machine
        fun create(): StateMachine = mStateMachine

        // add the state into this state-machine
        fun addState(state: State): Builder {
            if (state.type != StateType.START || !mStateMachine.mStates.any { it.type == StateType.START })
                mStateMachine.mStates.add(state)
            return this
        }

        // add the edge into this state-machine
        fun addEdge(srcText: String, edgeText: String, dstText: String, edgeType: EdgeType = EdgeType.GENERAL): Builder {
            // find the source & destination states in this state-machine
            val src = mStateMachine.mStates.find { it.text == srcText }
            val dst = mStateMachine.mStates.find { it.text == dstText }

            if (src != null && dst != null) {
                // create the edge based on source & destination states
                val edge = Edge(src, dst, edgeText, edgeType)

                // add this edge
                if (!src.hasEdge(edge, checkOutgoing = true, checkIngoing = false) &&
                        !dst.hasEdge(edge, checkOutgoing = false, checkIngoing = true)) {
                    src.addEdge(edge, true)
                    dst.addEdge(edge, false)
                }
            }

            return this
        }
    }

    /* ===================================================================== */

    // the stored states of this state-machine
    private val mStates = HashSet<State>()

    // for primary constructor
    init {
        // initially add the START state
        mStates.add(State("START", StateType.START))
    }

    /* ===================================================================== */

    // get the builder to modify this state-machine
    fun getBuilder() = Builder(this)

    /* ===================================================================== */

    override fun toString(): String {
        return "Num of states = ${mStates.size}"
    }
}