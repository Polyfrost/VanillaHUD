package org.polyfrost.vanillahud.events

import org.polyfrost.oneconfig.api.event.v1.events.Event

data class RecordPlayingEvent(val message: String, val isPlaying: Boolean) : Event