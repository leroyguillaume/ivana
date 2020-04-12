package io.ivana.api.web

import io.ivana.core.Transform
import io.ivana.dto.TransformDto

const val RootApiEndpoint = "/api"

internal fun TransformDto.toTransform() = when (this) {
    is TransformDto.Rotation -> toTransform()
}

private fun TransformDto.Rotation.Direction.toDirection() = when (this) {
    TransformDto.Rotation.Direction.Clockwise -> Transform.Rotation.Direction.Clockwise
    TransformDto.Rotation.Direction.Counterclockwise -> Transform.Rotation.Direction.Counterclockwise
}

private fun TransformDto.Rotation.toTransform() = Transform.Rotation(direction.toDirection())
