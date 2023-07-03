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

package cc.polyfrost.polyui.property.impl

import cc.polyfrost.polyui.animate.Animations
import cc.polyfrost.polyui.color.Color
import cc.polyfrost.polyui.property.Properties
import cc.polyfrost.polyui.unit.seconds

open class DropdownProperties : Properties() {

    open class Entry : Properties() {
        val textProperties = TextProperties()
        val iconProperties = ImageProperties()
        override val color: Color
            get() = colors.component.bg

        open val contentColor: Color
            get() = colors.text.secondary
        open val contentHoverColor: Color
            get() = colors.text.primaryHovered

        open val hoverAnimation = Animations.EaseOutExpo
        open val hoverAnimationDuration = 0.5.seconds

        open val verticalPadding = 4f
        open val lateralPadding = 12f
    }
    override val color: Color
        get() = colors.component.bg

    open val minWidth = 120f
    open val verticalPadding = 2f

    open val activeColor: Color
        get() = colors.brand.accent

    open val hoveredColor: Color
        get() = colors.text.primaryHovered

    open val borderColor: Color
        get() = colors.page.border5
    open val activeBorderColor: Color
        get() = colors.brand.fg

    open val openAnimation = Animations.EaseOutExpo
    open val openDuration = 0.5.seconds

    open val borderThickness = 1f

    open val cornerRadius = 8f
}