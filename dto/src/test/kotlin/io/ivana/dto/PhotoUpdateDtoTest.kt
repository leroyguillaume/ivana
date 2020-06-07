package io.ivana.dto

import org.junit.jupiter.api.Nested
import java.time.LocalDate

internal class PhotoUpdateDtoTest {
    @Nested
    inner class Default : JsonTest(
        filename = "photo-update/default.json",
        expectedValue = PhotoUpdateDto(),
        deserializeAs = typeOf<PhotoUpdateDto>()
    )

    @Nested
    inner class Complete : JsonTest(
        filename = "photo-update/complete.json",
        expectedValue = PhotoUpdateDto(
            shootingDate = LocalDate.parse("2020-06-07")
        ),
        deserializeAs = typeOf<PhotoUpdateDto>()
    )
}
