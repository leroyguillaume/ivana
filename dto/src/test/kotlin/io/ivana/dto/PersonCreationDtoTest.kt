@file:Suppress("ClassName")

package io.ivana.dto

internal class PersonCreationDtoTest : JsonTest(
    filename = "person-creation.json",
    expectedValue = PersonCreationDto(
        lastName = "Leroy",
        firstName = "Guillaume"
    ),
    deserializeAs = typeOf<PersonCreationDto>()
)
