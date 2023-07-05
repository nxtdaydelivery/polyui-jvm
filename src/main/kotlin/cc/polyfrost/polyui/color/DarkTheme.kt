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

package cc.polyfrost.polyui.color

import cc.polyfrost.polyui.utils.rgba

/**
 * The default color set used in PolyUI.
 *
 * @see Colors
 * @since 0.17.0
 */
open class DarkTheme : Colors {
    override val page = DarkPage()
    override val brand = DarkBrand()
    override val onBrand = DarkOnBrand()
    override val state = DarkState()
    override val component = DarkComponent()
    override val text = DarkText()

    open class DarkPage : Colors.Page() {
        override val bg = Colors.Palette(
            rgba(17, 23, 28),
            rgba(26, 34, 41),
            rgba(14, 19, 23),
            rgba(17, 23, 28, 0.5f)
        )
        override val bgOverlay = rgba(255, 255, 255, 0.1f)

        override val fg = Colors.Palette(
            Color.TRANSPARENT,
            rgba(26, 34, 41),
            rgba(14, 19, 23),
            rgba(26, 34, 41, 0.5f)
        )
        override val fgOverlay = rgba(255, 255, 255, 0.1f)

        override val border20 = rgba(255, 255, 255, 0.2f)
        override val border10 = rgba(255, 255, 255, 0.1f)
        override val border5 = rgba(255, 255, 255, 0.05f)
    }
    open class DarkBrand : Colors.Brand() {
        override val fg = Colors.Palette(
            rgba(43, 75, 255),
            rgba(40, 67, 221),
            rgba(57, 87, 255),
            rgba(57, 87, 255, 0.5f)
        )

        override val accent = Colors.Palette(
            rgba(15, 28, 51),
            rgba(12, 23, 41),
            rgba(26, 44, 78),
            rgba(15, 28, 51, 0.5f)
        )
    }
    open class DarkOnBrand : Colors.OnBrand() {
        override val fg = Colors.Palette(
            rgba(213, 219, 255),
            rgba(213, 219, 255, 0.85f),
            rgba(225, 229, 255),
            rgba(225, 229, 255, 0.5f)
        )

        override val accent = Colors.Palette(
            rgba(63, 124, 228),
            rgba(63, 124, 228, 0.85f),
            rgba(37, 80, 154),
            rgba(63, 124, 228, 0.5f)
        )
    }
    open class DarkState : Colors.State() {
        override val danger = Colors.Palette(
            rgba(255, 68, 68),
            rgba(214, 52, 52),
            rgba(255, 86, 86),
            rgba(255, 68, 68, 0.5f)
        )

        override val warning = Colors.Palette(
            rgba(255, 171, 29),
            rgba(233, 156, 27),
            rgba(255, 178, 49),
            rgba(255, 171, 29, 0.5f)
        )

        override val success = Colors.Palette(
            rgba(35, 154, 96),
            rgba(26, 135, 82),
            rgba(44, 172, 110),
            rgba(35, 154, 96, 0.5f)
        )
    }
    open class DarkComponent : Colors.Component() {
        override val bg = Colors.Palette(
            rgba(26, 34, 41),
            rgba(23, 31, 37, 0.85f),
            rgba(34, 44, 53),
            rgba(34, 44, 53, 0.5f)
        )
        override val bgDeselected = Color.TRANSPARENT
    }
    open class DarkText : Colors.Text() {
        override val primary = Colors.Palette(
            rgba(213, 219, 255),
            rgba(213, 219, 255, 0.85f),
            rgba(225, 229, 255),
            rgba(225, 229, 255, 0.5f)
        )
        override val secondary = Colors.Palette(
            rgba(120, 129, 141),
            rgba(95, 104, 116),
            rgba(130, 141, 155),
            rgba(120, 129, 141, 0.5f)
        )
    }
}
