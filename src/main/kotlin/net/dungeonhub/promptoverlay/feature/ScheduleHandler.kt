package net.dungeonhub.promptoverlay.feature

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import net.dungeonhub.promptoverlay.api.render.Overlay
import net.dungeonhub.promptoverlay.config.categories.FeaturesCategory
import net.dungeonhub.promptoverlay.config.categories.FeaturesToggle
import net.dungeonhub.promptoverlay.overlays.DarkAuctionWarpOverlay
import net.dungeonhub.promptoverlay.overlays.TravelingZooWarpOverlay
import net.minecraft.client.Minecraft
import java.time.Duration as JavaDuration
import java.time.LocalDateTime as JavaLocalDateTime
import java.time.Instant
import java.util.concurrent.Executors
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

object ScheduleHandler {
    private val supervisor = SupervisorJob()
    private val dispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

    val scheduler = CoroutineScope(supervisor + dispatcher)

    val firstTravelingZoo: Instant = Instant.parse("2020-02-26T08:55:00Z")

    fun init() {
        launchDarkAuctionPrompt()
        launchTravelingZooPrompt()
    }

    private fun launchScheduledPrompt(timeUntil: () -> Duration, announceBefore: () -> Duration, featureToggle: () -> Boolean, overlayBuilder: (Duration) -> Overlay) {
        scheduler.launch {
            delay(timeUntil() - announceBefore())

            if(Minecraft.getInstance().level != null && featureToggle()) {
                OverlayFeature.setOverlay(overlayBuilder(announceBefore()))
            }

            delay(announceBefore())

            launchScheduledPrompt(timeUntil, announceBefore, featureToggle, overlayBuilder)
        }
    }

    private fun launchDarkAuctionPrompt() {
        launchScheduledPrompt(::timeUntilDarkAuction, { FeaturesCategory.darkAuctionTime.seconds }, FeaturesToggle::darkAuctionWarp) { DarkAuctionWarpOverlay(it) }
    }

    private fun timeUntilDarkAuction(): Duration {
        return JavaDuration.between(JavaLocalDateTime.now(), nextDarkAuctionTime().toJavaLocalDateTime()).toKotlinDuration()
    }

    private fun nextDarkAuctionTime(): LocalDateTime {
        val now = JavaLocalDateTime.now()
        return if(now.minute >= 55) {
            now.withMinute(55).withSecond(0).withNano(0).plusHours(1)
        } else {
            now.withMinute(55).withSecond(0).withNano(0)
        }.toKotlinLocalDateTime()
    }

    private fun launchTravelingZooPrompt() {
        launchScheduledPrompt(::timeUntilTravelingZoo, { FeaturesCategory.travelingZooTime.seconds }, FeaturesToggle::travelingZoo) { TravelingZooWarpOverlay(it) }
    }

    private fun timeUntilTravelingZoo(): Duration {
        return JavaDuration.between(Instant.now(), nextTravelingZoo()).toKotlinDuration()
    }

    internal fun nextTravelingZoo(now: Instant = Instant.now()): Instant {
        val interval = JavaDuration.ofHours(62)

        if(now.isBefore(firstTravelingZoo)) {
            return firstTravelingZoo
        }

        val elapsed = JavaDuration.between(firstTravelingZoo, now)
        val intervalsUntilNext = elapsed.dividedBy(interval) + 1
        return firstTravelingZoo.plus(interval.multipliedBy(intervalsUntilNext))
    }
}
