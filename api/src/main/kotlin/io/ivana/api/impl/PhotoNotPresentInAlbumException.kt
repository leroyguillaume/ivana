package io.ivana.api.impl

class PhotoNotPresentInAlbumException(
    override val message: String
) : RuntimeException()
