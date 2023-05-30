/*
 * This file is part of PolyUI
 * PolyUI - Fast and lightweight UI framework
 * Copyright (C) 2023 Polyfrost and its contributors. All rights reserved.
 *   <https://polyfrost.cc> <https://github.com/Polyfrost/polui-jvm>
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */

package cc.polyfrost.polyui.property.impl

import cc.polyfrost.polyui.animate.Animations
import cc.polyfrost.polyui.color.Color
import cc.polyfrost.polyui.event.Events
import cc.polyfrost.polyui.property.Properties
import cc.polyfrost.polyui.unit.seconds
import cc.polyfrost.polyui.utils.radii

/**
 * @param cornerRadii The corner radii of the block. The order is top-left, top-right, bottom-right, bottom-left.
 * @param lineThickness The thickness of this component. If you set it to something other than 0, it will become hollow.
 */
open class BlockProperties @JvmOverloads constructor(
    override val color: Color = Color.BLACK,
    open val cornerRadii: FloatArray = 0f.radii(),
    open val lineThickness: Float = 0f
) : Properties() {
    open val hoverColor = Color(12, 48, 255)
    open val clickColor = Color(15, 60, 0)
    open val clickAnimation: Animations? = Animations.EaseOutExpo
    open val hoverAnimation: Animations? = Animations.EaseOutExpo
    open val clickAnimationDuration: Long = 0.25.seconds
    open val hoverAnimationDuration: Long = 0.7.seconds
    override val padding: Float = 0F

    init {
        addEventHandlers(
            Events.MousePressed(0) to {
                recolor(clickColor, clickAnimation, clickAnimationDuration)
                false
            },
            Events.MouseReleased(0) to {
                recolor(hoverColor, clickAnimation, clickAnimationDuration)
                false
            },
            Events.MouseEntered to {
                recolor(hoverColor, hoverAnimation, hoverAnimationDuration)
                false
            },
            Events.MouseExited to {
                recolor(properties.color, hoverAnimation, hoverAnimationDuration)
                false
            }
        )
    }
}
