package rekkursion.util.statemachine.state

import rekkursion.util.statemachine.edge.Edge
import rekkursion.util.statemachine.edge.EdgeType
import java.util.*
import kotlin.collections.HashSet

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

        // build the state-machine by literal symbols
        fun buildStateMachineByLiteralSymbols(symbols: Array<String>) {
            // clear the whole state-machine
            clear(true)

            // iterate every symbol
            for (symbol in symbols) {
                var curState: State? = mStateMachine.getState("START", StateType.START) ?: break

                // iterate every character
                symbol.forEachIndexed { index, ch ->
                    // TODO: character pre-processing
                    val str = ch.toString()

                    // try to find out the out-going edge we need
                    val outgoingEdge = curState!!.findEdgeByText(str, true)

                    // check if it's the last character of this symbol
                    val isLastChar = index == symbol.length - 1

                    // doesn't exist -> create the state and the edge
                    curState = if (outgoingEdge == null) {
                        val newState = State(UUID.randomUUID().toString(), if (isLastChar) StateType.END else StateType.INTERMEDIATE)
                        addState(newState)
                        addEdge(curState!!.text, str, newState.text)
                        newState
                    }
                    // exists -> translate to the destination state of that edge
                    else
                        outgoingEdge.dstState
                }
            }
        }

        // clear the whole state-machine
        private fun clear(shouldRemainStart: Boolean): Builder {
            mStateMachine.mStates.removeIf { !shouldRemainStart || it.type != StateType.START }
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

    // get a certain state by the content text and the type
    fun getState(text: String, type: StateType): State? = mStates.find { it.text == text && it.type == type }

    // get START state
    fun getStartState(): State? = getState("START", StateType.START)

    // for debugging
    fun print() {
        mStates.forEach { cur ->
            println("current = ${cur.text}\n==================")
            cur.outgoingEdges.forEach { edge ->
                println("${edge.srcState.text} -- ${edge.edgeText} -> ${edge.dstState.text}")
            }
            println()
        }
    }

    /* ===================================================================== */

    override fun toString(): String {
        return "Num of states = ${mStates.size}"
    }
}