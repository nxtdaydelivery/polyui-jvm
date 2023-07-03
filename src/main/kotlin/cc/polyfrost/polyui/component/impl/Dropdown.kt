/*
 * This file is part of PolyUI
 * PolyUI - Fast and lightweight UI framework
 * Copyright (C) 2023 Polyfrost and its contributors.
 *   <https://polyfrost.cc> <https://github.com/Polyfrost/polui-jvm>
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

package cc.polyfrost.polyui.component.impl

import cc.polyfrost.polyui.PolyUI
import cc.polyfrost.polyui.animate.Animation
import cc.polyfrost.polyui.color.Color
import cc.polyfrost.polyui.component.Component
import cc.polyfrost.polyui.component.ContainingComponent
import cc.polyfrost.polyui.component.Focusable
import cc.polyfrost.polyui.event.Events
import cc.polyfrost.polyui.event.FocusedEvents
import cc.polyfrost.polyui.input.PolyText
import cc.polyfrost.polyui.input.PolyTranslator.Companion.localised
import cc.polyfrost.polyui.layout.Layout.Companion.drawables
import cc.polyfrost.polyui.layout.impl.GridLayout
import cc.polyfrost.polyui.property.impl.DropdownProperties
import cc.polyfrost.polyui.renderer.Renderer
import cc.polyfrost.polyui.renderer.data.Cursor
import cc.polyfrost.polyui.renderer.data.PolyImage
import cc.polyfrost.polyui.unit.*
import cc.polyfrost.polyui.unit.Unit
import cc.polyfrost.polyui.utils.fastEach
import cc.polyfrost.polyui.utils.maxOf
import org.jetbrains.annotations.Contract
import kotlin.math.max

class Dropdown(
    properties: DropdownProperties? = null,
    at: Point<Unit>,
    size: Size<Unit>? = null,
    heightBeforeScrolls: Unit.Pixel = 300.px,
    val default: Int = 0,
    vararg entries: Entry
) : Component(properties, at, size, false, true), Focusable {
    lateinit var borderColor: Color.Mutable
    private val chevron = Image(image = PolyImage("/dropdown-arrow.svg", 16f, 16f), at = origin)

    init {
        entries.forEach {
            it.dropdown = this
            if (size != null) it.size = size.clone()
        }
    }

    override fun accept(event: FocusedEvents) {
        if (event is FocusedEvents.FocusGained) {
            active = true
            open()
        }
        if (event is FocusedEvents.FocusLost) {
            active = false
            close()
        }
    }

    override val properties: DropdownProperties
        get() = super.properties as DropdownProperties
    var selected: Entry? = null
        private set(value) {
            if (field == value) return
            layout.removeComponentNow(field)
            field = value!!.clone()
            field!!.show = true
            layout.addComponent(field!!)
            field!!.calculateBounds()
            field!!.y += properties.verticalPadding
        }
    var i = 0
        get() = field++
    private val dropdown = GridLayout(
        at.clone(),
        gap = Gap(0f.px, 0f.px),
        drawables = drawables(
            *entries
        )
    ) // .scrolling(0.px * heightBeforeScrolls) // todo this (broken lol)
    init {
        dropdown.refuseFramebuffer = true
        dropdown.preRender = {
            if (openAnimation != null) renderer.pushScissor(0f, 0f, width, height * openAnimation!!.value)
            renderer.rect(0f, 0f, width, height, this@Dropdown.properties.color, 0f, 0f, this@Dropdown.properties.cornerRadius, this@Dropdown.properties.cornerRadius)
        }
        dropdown.postRender = {
            renderer.popScissor()
        }
        dropdown.simpleName = "Dropdown@${Integer.toHexString(this.hashCode())}"
    }
    var active = false
        private set
    private var openAnimation: Animation? = null

    override fun setup(renderer: Renderer, polyui: PolyUI) {
        dropdown.y += if (size != null) size!!.height else 0f
        super.setup(renderer, polyui)
        layout.addComponents(chevron, dropdown)
        dropdown.components.fastEach {
            (it as Entry).dropdown = this
        }
        borderColor = properties.borderColor.toMutable()
    }

    override fun accept(event: Events): Boolean {
        if (event is Events.MouseEntered) {
            polyui.cursor = Cursor.Clicker
            return true
        }
        if (event is Events.MouseExited) {
            polyui.cursor = Cursor.Pointer
            return true
        }
        if (event is Events.MouseClicked) {
            if (event.button == 0 && active) {
                polyui.unfocus()
                return true
            }
        }
        return super.accept(event)
    }

    fun close() {
        openAnimation = properties.openAnimation.create(properties.openDuration, openAnimation?.value ?: 1f, 0f)
        chevron.rotateTo(0.0, properties.openAnimation, properties.openDuration)
        color.recolor(properties.color)
        borderColor.recolor(properties.borderColor)
    }

    fun open() {
        openAnimation = properties.openAnimation.create(properties.openDuration, openAnimation?.value ?: 0f, 1f)
        chevron.rotateTo(180.0, properties.openAnimation, properties.openDuration)
        color.recolor(properties.activeColor)
        borderColor.recolor(properties.activeBorderColor)
    }

    override fun render() {
        if (openAnimation != null) {
            if (openAnimation!!.isFinished && openAnimation!!.value == 0f) {
                openAnimation = null
            } else {
                openAnimation!!.update(polyui.delta)
            }
        }
        dropdown.enabled = (openAnimation?.value ?: 0f) != 0f
        if (active) selected?.recolorAll(properties.hoveredColor)
        renderer.rect(x, y, width, height, color, properties.cornerRadius)
        renderer.hollowRect(x, y, width, height, borderColor, properties.borderThickness, properties.cornerRadius)
    }

    override fun calculateSize(): Size<Unit> {
        val largest = dropdown.components.maxOf { it.calculateSize() }
        dropdown.components.fastEach {
            it.size = largest.clone()
        }
        largest.a.px = max(largest.a.px, properties.minWidth)
        largest.b.px += properties.verticalPadding * 2f
        dropdown.y += largest.height
        return largest
    }

    override fun calculateBounds() {
        dropdown.calculateBounds()
        super.calculateBounds()
    }

    override fun onInitComplete() {
        x += properties.borderThickness
        dropdown.x += properties.borderThickness
        selected = dropdown.components[default] as Entry
        dropdown.enabled = false
        chevron.layout = layout
        chevron.setup(renderer, polyui)
        chevron.calculateBounds()
        chevron.x = x + width - chevron.width - properties.verticalPadding
        chevron.y = y + height / 2f - chevron.height / 2f
    }

    fun default() {
        selected = dropdown.components[default] as Entry
    }

    class Entry @JvmOverloads constructor(private val txt: PolyText, private val icon: PolyImage? = null, private val iconSide: Side = Side.Right, properties: DropdownProperties.Entry? = null, private val onSelected: (() -> kotlin.Unit)? = null) : ContainingComponent(properties, grid(0, 0), null, false, true, arrayOf()) {
        @JvmOverloads
        constructor(text: String, icon: PolyImage? = null, iconSide: Side = Side.Right, properties: DropdownProperties.Entry? = null, onSelected: (() -> kotlin.Unit)? = null) : this(text.localised(), icon, iconSide, properties, onSelected)

        override val properties
            get() = super.properties as DropdownProperties.Entry

        private lateinit var text: Text
        private var image: Image? = null
        internal lateinit var dropdown: Dropdown
        internal var show = false

        override fun setup(renderer: Renderer, polyui: PolyUI) {
            super.setup(renderer, polyui)
            // shh
            val i = dropdown.i
            (this.at.a as Unit.Grid).row = i
            (this.at.b as Unit.Grid).row = i
        }

        override fun onInitComplete() {
            text = Text(this.properties.textProperties, txt, origin)
            image = if (icon != null) Image(this.properties.iconProperties, icon, origin) else null
            addComponents(text, image)
            recolorAll(properties.contentColor)
            super.onInitComplete()
        }

        override fun accept(event: Events): Boolean {
            if (event is Events.MouseExited) {
                recolorAll(properties.contentColor, properties.hoverAnimation, properties.hoverAnimationDuration)
                return true
            }
            if (event is Events.MouseEntered) {
                recolorAll(properties.contentHoverColor, properties.hoverAnimation, properties.hoverAnimationDuration)
                return true
            }
            if (event is Events.MousePressed) {
                return true
            }
            if (event is Events.MouseReleased) {
                return true
            }
            if (event is Events.MouseClicked) {
                if (event.button != 0) return false
                if (!show) {
                    dropdown.active = false
                    dropdown.close()
                    dropdown.selected = this
                    onSelected?.invoke()
                    return true
                }
            }
            return super.accept(event)
        }

        override fun onParentInitComplete() {
            text.x += x
            text.y += y
            if (image != null) {
                image!!.x += x
                image!!.y += y
            }
        }

        override fun placeChildren() {
            if (image != null) {
                if (iconSide == Side.Right) {
                    image!!.x += width - image!!.width - properties.lateralPadding
                    text.x += properties.lateralPadding
                } else {
                    image!!.x += properties.lateralPadding
                    text.x += image!!.width + properties.lateralPadding
                }
                image!!.y += (height - image!!.height) / 2f
            } else {
                text.x += properties.lateralPadding
            }
            text.y += (height - text.height) / 2f
        }

        // this function is only called if the dropdown is not specified with a size, and does not mutate self, hence why it is pure
        @Contract(pure = true)
        override fun calculateSize(): Size<Unit> {
            val (w, h) = renderer.textBounds(properties.textProperties.font, txt.string, properties.textProperties.fontSize.px, properties.textProperties.alignment)
            val width = w.px + properties.lateralPadding * 2f + if (image != null) image!!.width + properties.lateralPadding else 0f
            val height = h.px + properties.verticalPadding * 2f
            return Size(max(width, dropdown.properties.minWidth).px, height.px)
        }

        override fun clone() = Entry(txt, image?.image, iconSide, properties, onSelected).also {
            it.size = size!!.clone()
            it.x = dropdown.x
            it.y = dropdown.y
            it.text = text
            it.image = image
            it.dropdown = this.dropdown
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Entry) return false

            if (txt != other.txt) return false
            if (icon !== other.icon) return false
            return iconSide == other.iconSide
        }
    }

    companion object {
        /**
         * Constructs a dropdown from an enum class. This method will look at the first field in the enum entries, and if it is a String it will extract that; and use that as the name.
         *
         * Otherwise, it will use the name of the enum entry as it is declared in the source code.
         *
         * @throws IllegalArgumentException if the class is not an enum
         * @since 0.19.0
         */
        @JvmStatic
        fun from(enumClass: Class<*>): Array<out Entry> {
            require(enumClass.isEnum) { "class must be an enum to create a dropdown" }
            return enumClass.enumConstants.map {
                it as Enum<*>
                Entry(it::class.java.fields[0].get(it) as? String ?: it.name)
            }.toTypedArray()
        }

        /**
         * Constructs a dropdown from an array of values. This method will call [Any.toString] on each value to get the name.
         * @since 0.19.0
         */
        @JvmStatic
        fun from(values: Array<out Any>): Array<out Entry> = values.map { Entry(it.toString()) }.toTypedArray()
    }
}