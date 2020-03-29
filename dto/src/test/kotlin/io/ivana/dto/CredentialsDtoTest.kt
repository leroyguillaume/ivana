package io.ivana.dto

internal class CredentialsDtoTest : JsonTest(
    filename = "credentials.json",
    expectedValue = CredentialsDto("admin", "changeit"),
    deserializeAs = typeOf<CredentialsDto>()
)
