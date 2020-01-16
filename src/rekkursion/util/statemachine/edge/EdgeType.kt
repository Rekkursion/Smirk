package rekkursion.util.statemachine.edge

enum class EdgeType {
    GENERAL,
    OTHERS_AND_CONSUMED,
    OTHERS_AND_NOT_CONSUMED,
    // \\s+, operators
    ACCEPTABLE_SYMBOLS
}