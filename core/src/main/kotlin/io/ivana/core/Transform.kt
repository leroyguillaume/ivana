package io.ivana.core

sealed class Transform {
    data class Rotation(
        val degrees: Double
    ) : Transform()
}
