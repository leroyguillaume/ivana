package io.ivana.dto

internal class PasswordUpdateDtoTest : JsonTest(
    filename = "password-update.json",
    expectedValue = PasswordUpdateDto("changeit"),
    deserializeAs = typeOf<PasswordUpdateDto>()
)
