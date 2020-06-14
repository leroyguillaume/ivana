@file:Suppress("ClassName")

package io.ivana.dto

import java.util.*

internal class PersonDtoTest : JsonTest(
    filename = "person.json",
    expectedValue = PersonDto(
        id = UUID.fromString("61f11547-a340-441c-bce7-551234d5d361"),
        lastName = "Leroy",
        firstName = "Guillaume"
    ),
    deserializeAs = typeOf<PersonDto>()
)
