package io.ivana.core

sealed class Transform {
    data class Rotation(
        val direction: Direction
    ) : Transform() {
        enum class Direction(
            val angle: Double
        ) {
            Clockwise(90.0),
            Counterclockwise(-90.0)
        }
    }
}
