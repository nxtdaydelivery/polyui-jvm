package org.polyfrost.polyui.markdown.elements

import dev.dediamondpro.minemark.LayoutStyle
import dev.dediamondpro.minemark.elements.Element
import dev.dediamondpro.minemark.elements.impl.BlockQuoteElement
import dev.dediamondpro.minemark.elements.impl.CodeBlockElement
import dev.dediamondpro.minemark.elements.impl.HorizontalRuleElement
import org.polyfrost.polyui.color.PolyColor
import org.polyfrost.polyui.markdown.MarkdownStyle
import org.polyfrost.polyui.renderer.Renderer
import org.xml.sax.Attributes
import java.awt.Color

class MarkdownBlockquoteElement(
    style: MarkdownStyle,
    layoutStyle: LayoutStyle,
    parent: Element<MarkdownStyle, Renderer>?,
    qName: String,
    attributes: Attributes?
) : BlockQuoteElement<MarkdownStyle, Renderer>(style, layoutStyle, parent, qName, attributes) {
    override fun drawBlock(x: Float, y: Float, width: Float, height: Float, color: Color, renderer: Renderer) {
        renderer.rect(x, y, width, height, PolyColor.from(color))
    }
}
