package io.ivana.dto

internal class CredentialsDtoTest : JsonTest(
    filename = "credentials.json",
    expectedValue = CredentialsDto("admin", "foo123"),
    deserializeAs = typeOf<CredentialsDto>()
)
