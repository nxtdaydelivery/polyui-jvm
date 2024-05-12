/*
 * This file is part of PolyUI
 * PolyUI - Fast and lightweight UI framework
 * Copyright (C) 2024 Polyfrost and its contributors.
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

package org.polyfrost.polyui

import org.polyfrost.polyui.component.impl.*
import org.polyfrost.polyui.component.onClick
import org.polyfrost.polyui.component.withStates
import org.polyfrost.polyui.dsl.polyUI
import org.polyfrost.polyui.renderer.impl.GLFWWindow
import org.polyfrost.polyui.renderer.impl.NVGRenderer
import org.polyfrost.polyui.unit.by
import org.polyfrost.polyui.utils.image
import org.polyfrost.polyui.utils.open

fun main() {
    val window = GLFWWindow("PolyUI Test v2 (DSL)", 800, 500)
    polyUI {
        size = 800f by 500f
        renderer = NVGRenderer
        image("polyfrost.png")
        text("text.dark") {
            fontSize = 20f
        }
        group {
            Button("moon.svg".image()).add()
            Button("face-wink.svg".image(), "button.text").add()
            Switch(size = 28f).add()
            Checkbox(size = 28f).add()
        }
        Dropdown("tomato", "orange", "banana", "lime").add()
        BoxedTextInput(pre = "Title:", post = "px").add()
        group {
            repeat(30) {
                block(size = (32f + (Math.random().toFloat() * 100f)) by 32f).withStates()
            }
            it.visibleSize = 350f by 120f
        }
        group {
            Button("shuffle.svg".image(), "button.randomize").onClick {
                val box = parent.parent[5]
                box.children?.shuffle()
                box.repositionChildren()
            }.add()
            Button("minus.svg".image()).onClick {
                val box = parent.parent[5]
                box.removeChild(box.children!!.lastIndex)
            }.add()
            Button("plus.svg".image()).onClick {
                parent.parent[5].addChild(Block(size = 32f + (Math.random().toFloat() * 100f) by 32f).withStates())
            }.add()
            group {
                Radiobutton("hello", "goodbye").add()
                Slider().add()
                text("blink three times when u feel it kicking in")
            }
        }
    }.open(window)
}
