package io.ivana.api.impl

import java.util.*

sealed class ResourcesNotFoundException : RuntimeException() {
    class Album(
        override val ids: Set<UUID>
    ) : ResourcesNotFoundException()

    class Photo(
        override val ids: Set<UUID>
    ) : ResourcesNotFoundException()

    class User(
        override val ids: Set<UUID>
    ) : ResourcesNotFoundException()

    abstract val ids: Set<UUID>
}
