package dev.vjcbs.bunqcli

data class Summary(
    val incoming: Double = 0.0,
    val outgoing: Double = 0.0
) {
    val delta: Double
        get() = this.incoming + this.outgoing

    operator fun plus(other: Summary) =
        Summary(
            incoming = this.incoming + other.incoming,
            outgoing = this.outgoing + other.outgoing
        )
}
