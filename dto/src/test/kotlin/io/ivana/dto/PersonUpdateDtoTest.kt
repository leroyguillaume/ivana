@file:Suppress("ClassName")

package io.ivana.dto

internal class PersonUpdateDtoTest : JsonTest(
    filename = "person-update.json",
    expectedValue = PersonUpdateDto(
        lastName = "Leroy",
        firstName = "Guillaume"
    ),
    deserializeAs = typeOf<PersonUpdateDto>()
)
