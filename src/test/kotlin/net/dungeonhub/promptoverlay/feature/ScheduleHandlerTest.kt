package net.dungeonhub.promptoverlay.feature

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduleHandlerTest {
    @Test
    fun `next traveling zoo uses the first known UTC start before the schedule begins`() {
        assertEquals(
            Instant.parse("2020-02-26T08:55:00Z"),
            ScheduleHandler.nextTravelingZoo(Instant.parse("2019-02-25T12:00:00Z")),
        )
    }

    @Test
    fun `UTC schedule produces the known Berlin event times`() {
        val berlin = ZoneId.of("Europe/Berlin")
        val first = ScheduleHandler.nextTravelingZoo(Instant.parse("2026-08-19T16:54:00Z"))
        val second = ScheduleHandler.nextTravelingZoo(Instant.parse("2026-08-19T16:56:00Z"))

        assertEquals(
            LocalDateTime.of(2026, 8, 19, 18, 55),
            LocalDateTime.ofInstant(first, berlin),
        )
        assertEquals(
            LocalDateTime.of(2026, 8, 22, 8, 55),
            LocalDateTime.ofInstant(second, berlin),
        )
    }

    @Test
    fun `an event starting now returns the following event`() {
        assertEquals(
            Instant.parse("2026-08-19T16:55:00Z"),
            ScheduleHandler.nextTravelingZoo(Instant.parse("2026-08-18T21:50:00Z")),
        )
        assertEquals(
            Instant.parse("2026-08-19T16:55:00Z"),
            ScheduleHandler.nextTravelingZoo(Instant.parse("2026-08-19T16:54:59Z")),
        )
        assertEquals(
            Instant.parse("2026-08-22T06:55:00Z"),
            ScheduleHandler.nextTravelingZoo(Instant.parse("2026-08-19T16:55:00Z")),
        )
        assertEquals(
            Instant.parse("2026-08-24T20:55:00Z"),
            ScheduleHandler.nextTravelingZoo(Instant.parse("2026-08-22T06:55:00Z")),
        )
    }
}
