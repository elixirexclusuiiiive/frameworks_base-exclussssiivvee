/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.systemui.shared.clocks

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.icu.text.NumberFormat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import com.android.systemui.customization.R
import com.android.systemui.plugins.ClockAnimations
import com.android.systemui.plugins.ClockController
import com.android.systemui.plugins.ClockEvents
import com.android.systemui.plugins.ClockFaceController
import com.android.systemui.plugins.ClockFaceEvents
import com.android.systemui.plugins.ClockSettings
import com.android.systemui.plugins.log.LogBuffer
import java.io.PrintWriter
import java.util.Locale
import java.util.TimeZone

private val TAG = CustomClockController::class.simpleName

/**
 * Controls the custom clock visuals.
 *
 * This serves as an adapter between the clock interface and the ClockView used by the
 * existing lockscreen clock.
 */
class CustomClockController(
    ctx: Context,
    private val layoutInflater: LayoutInflater,
    private val resources: Resources,
    private val settings: ClockSettings?,
) : ClockController {
    override val smallClock: CustomClockFaceController
    override val largeClock: LargeClockFaceController
    private val clocks: List<View>

    override val events: CustomClockEvents
    override lateinit var animations: ClockAnimations
        private set

    init {
        val parent = FrameLayout(ctx)
        
        smallClock =
            CustomClockFaceController(
                layoutInflater.inflate(R.layout.elixir_default_small, parent, false)
                    as ClockLayout,
                    settings?.seedColor
            )
        largeClock =
            LargeClockFaceController(
                layoutInflater.inflate(R.layout.elixir_default_large, parent, false)
                    as ClockLayout,
                    settings?.seedColor
            )

        clocks = listOf(smallClock.view, largeClock.view)

        events = CustomClockEvents()
        events.onLocaleChanged(Locale.getDefault())
    }

    override fun initialize(resources: Resources, dozeFraction: Float, foldFraction: Float) {
        events.onColorPaletteChanged(resources)
        events.onTimeZoneChanged(TimeZone.getDefault())
    }

    open inner class CustomClockFaceController(
        override val view: View,
        val seedColor: Int?,
    ) : ClockFaceController {

        // MAGENTA is a placeholder, and will be assigned correctly in initialize
        private var currentColor = Color.MAGENTA
        private var isRegionDark = false
        protected var targetRegion: Rect? = null

        override var logBuffer: LogBuffer?
            get() = null
            set(value) {
                
            }

        init {
            if (seedColor != null) {
                currentColor = seedColor
            }
        }

        override val events =
            object : ClockFaceEvents {
                override fun onRegionDarknessChanged(isRegionDark: Boolean) {

                }

                override fun onTargetRegionChanged(targetRegion: Rect?) {

                }

                override fun onFontSettingChanged(fontSizePx: Float) {

                }
            }

        open fun recomputePadding(targetRegion: Rect?) {}
    }

    inner class LargeClockFaceController(
        view: View,
        seedColor: Int?,
    ) : CustomClockFaceController(view, seedColor) {
        override fun recomputePadding(targetRegion: Rect?) {
            // We center the view within the targetRegion instead of within the parent
            // view by computing the difference and adding that to the padding.
            val parent = view.parent
            val yDiff =
                if (targetRegion != null && parent is View && parent.isLaidOut())
                    targetRegion.centerY() - parent.height / 2f
                else 0f
            val lp = view.getLayoutParams() as FrameLayout.LayoutParams
            lp.topMargin = (-0.5f * view.bottom + yDiff).toInt()
            view.setLayoutParams(lp)
        }

    }

    inner class CustomClockEvents() : ClockEvents {
        override fun onTimeFormatChanged(is24Hr: Boolean) =
            clocks.forEach {  }

        override fun onTimeZoneChanged(timeZone: TimeZone) =
            clocks.forEach { }

        override fun onColorPaletteChanged(resources: Resources) {

        }

        override fun onLocaleChanged(locale: Locale) {
            clocks.forEach { }
        }
    }

    override fun dump(pw: PrintWriter) {
        pw.print("smallClock=")
        pw.print("largeClock=")
    }

    companion object {
        @VisibleForTesting const val DOZE_COLOR = Color.WHITE
        private const val FORMAT_NUMBER = 1234567890
    }
}
