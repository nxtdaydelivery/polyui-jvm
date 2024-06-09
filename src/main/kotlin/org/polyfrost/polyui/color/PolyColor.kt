/*
 * This file is part of PolyUI
 * PolyUI - Fast and lightweight UI framework
 * Copyright (C) 2023-2024 Polyfrost and its contributors.
 *   <https://polyfrost.org> <https://github.com/Polyfrost/polui-jvm>
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *     PolyUI is licensed under the terms of version 3 of the GNU Lesser
 * General Public License as published by the Free Software Foundation,
 * AND the simple request that you adequately accredit us if you use PolyUI.
 * See details here <https://github.com/Polyfrost/polyui-jvm/ACCREDITATION.md>.
 *     This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 * License.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.polyfrost.polyui.color

import org.polyfrost.polyui.PolyUI
import org.polyfrost.polyui.animate.Animation
import org.polyfrost.polyui.utils.HSBtoRGB
import org.polyfrost.polyui.utils.RGBtoHSB
import org.polyfrost.polyui.utils.rgba

/**
 * # Color
 *
 * *(revised 1.3)* The color used by PolyUI. It stores the color in the HSBA format, and can be converted to ARGB.
 *
 * @see [PolyColor.Animatable]
 * @see [PolyColor.Gradient]
 */
@Suppress("INAPPLICABLE_JVM_NAME")
interface PolyColor {
    /**
     * The hue of this color. Can be any value, but is `mod 360`, so values are always between 0 and 360.
     */
    val hue: Float

    /**
     * The saturation of this color. Clamped between `0f..1f`.
     */
    val saturation: Float

    /**
     * The brightness of this color. Clamped between `0f..1f`.
     */
    val brightness: Float

    /**
     * The alpha of this color. Clamped between `0f..1f`.
     */
    val alpha: Float

    /** return an integer representation of this color.
     * Utilizes bit-shifts to store the color as one 32-bit integer, like so:
     *
     * `0bAAAAAAAARRRRRRRRGGGGGGGGBBBBBBBB`
     * aka `0xAARRGGBB`
     *
     * @see org.polyfrost.polyui.utils.toColor
     */
    val argb: Int

    /**
     * @return true if the color is transparent (`alpha == 0f`)
     */
    open val transparent get() = alpha == 0f

    /** red value of this color, from 0 to 255 */
    @get:JvmName("red")
    val r get() = argb shr 16 and 0xFF

    /** green value of this color, from 0 to 255 */
    @get:JvmName("green")
    val g get() = argb shr 8 and 0xFF

    /** blue value of this color, from 0 to 255 */
    @get:JvmName("blue")
    val b get() = argb and 0xFF

    /** alpha value of this color, from 0 to 255 */
    @get:JvmName("alpha")
    val a get() = argb shr 24 and 0xFF

    open class Static(hue: Float, saturation: Float, brightness: Float, alpha: Float) : PolyColor {
        final override val hue = hue % 360f
        final override val saturation = saturation.coerceIn(0f, 1f)
        final override val brightness = brightness.coerceIn(0f, 1f)
        final override val alpha = alpha.coerceIn(0f, 1f)

        @Transient
        final override val argb = HSBtoRGB(this.hue, this.saturation, this.brightness, this.alpha)

        override fun toString() = "Color($hue, $saturation, $brightness, $alpha)"

        override fun equals(other: Any?) = other is PolyColor && other.argb == this.argb

        override fun hashCode() = argb.hashCode()
    }


    open class Mutable(hue: Float, saturation: Float, brightness: Float, alpha: Float) : PolyColor {
        final override var hue = hue % 360f
            set(value) {
                dirty = true
                field = value % 360f
            }
        final override var saturation = saturation.coerceIn(0f, 1f)
            set(value) {
                dirty = true
                field = value.coerceIn(0f, 1f)
            }
        final override var brightness = brightness.coerceIn(0f, 1f)
            set(value) {
                dirty = true
                field = value.coerceIn(0f, 1f)
            }
        final override var alpha = alpha.coerceIn(0f, 1f)
            set(value) {
                dirty = true
                field = value.coerceIn(0f, 1f)
            }

        @Transient
        final override var argb = HSBtoRGB(this.hue, this.saturation, this.brightness, alpha)
            protected set
            get() {
                if (dirty) {
                    field = HSBtoRGB(hue, saturation, brightness, alpha)
                    dirty = false
                }
                return field
            }

        fun recolor(to: PolyColor) {
            this.hue = to.hue
            this.saturation = to.saturation
            this.brightness = to.brightness
            this.alpha = to.alpha
        }

        @Transient
        protected var dirty = false

        open fun update(deltaTimeNanos: Long) = false

        override fun toString() = "Color($hue, $saturation, $brightness, $alpha)"

        override fun equals(other: Any?) = other is PolyColor && other.argb == this.argb

        override fun hashCode() = argb.hashCode()
    }

    class Chroma(hue: Float, saturation: Float, brightness: Float, alpha: Float, var speedNanos: Long) : Mutable(hue, saturation, brightness, alpha) {
        @Transient
        private var time = (this.hue * speedNanos.toFloat()).toLong()

        override fun update(deltaTimeNanos: Long): Boolean {
            time += deltaTimeNanos
            hue = (time % speedNanos) / speedNanos.toFloat()
            return false
        }
    }

    open class Animatable(hue: Float, saturation: Float, brightness: Float, alpha: Float) : Mutable(hue, saturation, brightness, alpha) {
        @Transient
        protected var animation: Animation? = null

        /**
         * animation color data.
         * ```
         * hue, saturation, brightness
         * fromR, fromG, fromB, fromA
         * toR, toG, toB, toA
         * ```
         * @see recolor
         */
        @Transient
        protected var cdata: FloatArray? = null

        /**
         * recolor this color to the target color, with the given animation type and duration.
         *
         * **make sure to check if the color is a gradient, as this method may not have the expected result!**
         * @param animation animation to use. if it is null, the color will be set to the target color immediately.
         * @see [Gradient]
         */
        open fun recolor(to: PolyColor, animation: Animation? = null) {
            if (to == this) return
            // clear old animation
            this.animation = null
            if (animation == null) {
                return super.recolor(to)
            }
            this.animation = animation
            val fr = this.r.toFloat()
            val fg = this.g.toFloat()
            val fb = this.b.toFloat()
            this.cdata = floatArrayOf(
                0f, 0f, 0f,
                fr, fg, fb, this.alpha,
                to.r.toFloat() - fr, to.g.toFloat() - fg, to.b.toFloat() - fb, to.alpha - this.alpha
            )
        }


        /**
         * update the color animation, if present.
         * After, the animation is cleared, and the color becomes static again.
         *
         * @return true if the animation finished on this tick, false if otherwise
         * */
        override fun update(deltaTimeNanos: Long): Boolean {
            if (animation != null) {
                dirty = true
                val animation = this.animation ?: return false
                val c = this.cdata ?: return false

                val progress = animation.update(deltaTimeNanos)
                RGBtoHSB(
                    (c[3] + c[7] * progress).toInt(),
                    (c[4] + c[8] * progress).toInt(),
                    (c[5] + c[9] * progress).toInt(),
                    c,
                )
                this.alpha = (c[6] + c[10] * progress)
                this.hue = c[0]
                this.saturation = c[1]
                this.brightness = c[2]

                if (animation.isFinished) {
                    this.animation = null
                    this.cdata = null
                    return true
                }
                return false
            }
            return false
        }
    }


    open class Gradient(color1: PolyColor, open val color2: PolyColor, val type: Type) : PolyColor by color1 {
        operator fun get(index: Int) = when (index) {
            0 -> this
            1 -> color2
            else -> throw IndexOutOfBoundsException("Invalid index $index: must be 0 or 1")
        }

        override val transparent: Boolean
            get() = super.transparent && color2.transparent

        val argb2 get() = color2.argb

        @get:JvmName("red2")
        val r2 get() = color2.r

        @get:JvmName("green2")
        val g2 get() = color2.g

        @get:JvmName("blue2")
        val b2 get() = color2.b

        @get:JvmName("alpha2")
        val a2 get() = color2.a

        override fun toString() = "${type}Gradient(Color($hue, $saturation, $brightness, $alpha) -> $color2)"

        override fun equals(other: Any?) = other is Gradient && other.argb == this.argb && other.argb2 == this.argb2 && other.type == this.type

        override fun hashCode() = (super.hashCode() * 31) + (color2.hashCode() * 31) + type.hashCode()

        open class Mutable(@Transient protected open val color1: PolyColor.Mutable, color2: PolyColor.Mutable, type: Type) : Gradient(color1, color2, type) {
            override val color2: PolyColor.Mutable
                get() = super.color2 as PolyColor.Mutable

            operator fun set(index: Int, to: PolyColor) {
                when (index) {
                    0 -> color1.recolor(to)
                    1 -> color2.recolor(to)
                    else -> throw IndexOutOfBoundsException("Invalid index $index: must be 0 or 1")
                }
            }

            open fun update(deltaTimeNanos: Long) = color1.update(deltaTimeNanos) and color2.update(deltaTimeNanos)
        }

        open class Animatable(color1: PolyColor.Animatable, color2: PolyColor.Animatable, type: Type) : Mutable(color1, color2, type) {
            override val color1: PolyColor.Animatable
                // Trust Me Bro:tm:
                get() = super.color1 as PolyColor.Animatable

            override val color2: PolyColor.Animatable
                get() = super.color2 as PolyColor.Animatable

            open fun recolor(index: Int, to: PolyColor, animation: Animation? = null) {
                when (index) {
                    0 -> color1.recolor(to, animation)
                    1 -> color2.recolor(to, animation)
                    else -> throw IndexOutOfBoundsException("Invalid index $index: must be 0 or 1")
                }
            }
        }

        @Suppress("ConvertObjectToDataObject")
        sealed interface Type {
            object TopToBottom : Type

            object LeftToRight : Type

            object TopLeftToBottomRight : Type

            object BottomLeftToTopRight : Type

            /**
             * Radial gradient.
             *
             * Note that if your radii are very close together, you may just get a circle in a box.
             * @param innerRadius how much of the shape should be filled entirely with color1
             * @param outerRadius how much of the shape should be filled entirely with color2
             * @param centerX the center of the gradient, in the x-axis. if it is -1, it will be the center of the shape
             * @param centerY the center of the gradient, in the y-axis. if it is -1, it will be the center of the shape
             *
             * @throws IllegalArgumentException if innerRadius1 is larger than outerRadius2
             */
            data class Radial @JvmOverloads constructor(
                val innerRadius: Float,
                val outerRadius: Float,
                val centerX: Float = -1f,
                val centerY: Float = -1f,
            ) : Type {
                init {
                    require(innerRadius < outerRadius) { "innerRadius must be smaller than outerRadius! ($innerRadius < $outerRadius)" }
                    if (innerRadius + 5f > outerRadius) PolyUI.LOGGER.warn("[Gradient] innerRadius and outerRadius are very close together, you may just get a circle in a box.")
                }
            }

            data class Box(val radius: Float, val feather: Float) : Type
        }
    }

    companion object Constants {
        /** Transparent color. This should be used for checking in draw calls to prevent drawing of empty objects, e.g.
         *
         * `if (color == TRANSPARENT) return`
         */
        @JvmField
        val TRANSPARENT = rgba(0, 0, 0, 0f)

        @JvmField
        val WHITE = rgba(255, 255, 255, 1f)

        @JvmField
        val BLACK = rgba(0, 0, 0, 1f)
    }
}

typealias Color = PolyColor
