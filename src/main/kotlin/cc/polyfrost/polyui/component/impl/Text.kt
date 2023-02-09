/*
 * This file is part of PolyUI
 * PolyUI - Fast and lightweight UI framework
 * Copyright (C) 2023 Polyfrost and its contributors. All rights reserved.
 *   <https://polyfrost.cc> <https://github.com/Polyfrost/polui-jvm>
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */

package cc.polyfrost.polyui.component.impl

import cc.polyfrost.polyui.component.Component
import cc.polyfrost.polyui.event.ComponentEvent
import cc.polyfrost.polyui.property.Properties
import cc.polyfrost.polyui.property.impl.TextProperties
import cc.polyfrost.polyui.unit.*
import cc.polyfrost.polyui.unit.Unit

open class Text @JvmOverloads constructor(
    properties: Properties = Properties.get("cc.polyfrost.polyui.component.impl.Text"),
    acceptInput: Boolean = false,
    text: String, fontSize: Unit.Pixel = 12.px,
    textAlign: TextAlign? = null,
    at: Vec2<Unit>, size: Size<Unit>? = null,
    vararg events: ComponentEvent.Handler,
) : Component(properties, at, size, acceptInput, *events) {
    private val props: TextProperties = properties as TextProperties
    private var autoSized = false
    var text = text
        set(value) {
            field = value
            sized = renderer.textBounds(props.font, text, fontSize.get(), textAlign) as Vec2<Unit>
            wantRecalculation()
        }
    var textAlign = textAlign ?: props.textAlignment
        set(value) {
            field = value
            wantRecalculation()
        }
    var fontSize = fontSize
        set(value) {
            field = value
            wantRecalculation()
        }

    constructor(
        text: String,
        fontSize: Unit.Pixel,
        at: Vec2<Unit>,
        vararg events: ComponentEvent.Handler
    ) : this(text = text, fontSize = fontSize, at = at, size = null, events = events)


    override fun render() {
        renderer.drawText(props.font, x, y, if (autoSized) 0f else width, text, props.color, fontSize.get(), textAlign)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getSize(): Vec2<Unit> {
        autoSized = true
        return renderer.textBounds(props.font, text, fontSize.get(), textAlign) as Vec2<Unit>
    }
}