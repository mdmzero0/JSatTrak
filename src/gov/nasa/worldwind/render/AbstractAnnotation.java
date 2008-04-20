/*
Copyright (C) 2001, 2006, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.geom.*;
import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

import javax.media.opengl.GL;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.nio.DoubleBuffer;
import java.util.logging.Level;

/**
 * An {@link Annotation} represent a text label and its rendering attributes. Annotations must be attached either to
 * a globe <code>Position</code> ({@link GlobeAnnotation}) or a viewport <code>Point</code> (ScreenAnnotation).
 *
 * <pre>
 * GlobaAnnotation ga = new  GlobeAnnotation("Lat-Lon zero", Position.fromDegrees(0, 0, 0)));
 * ScreenAnnotation sa = new ScreenAnnotation("Message...", new Point(10,10));
 * </pre>
 * <p>
 * Each Annotation refers to an {@link AnnotationAttributes} object which defines how the text will be rendered.
 * </p>
 * Rendering attributes allow to set:
 * <ul>
 * <li>the size of the bounding rectangle into which the text will be displayed</li>
 * <li>its frame shape, border color, width and stippling pattern</li>
 * <li>the text font, size, style and color</li>
 * <li>the background color or image</li>
 * <li>how much an annotation scales and fades with distance</li>
 * </ul>
 * <pre>
 * ga.getAttributes().setTextColor(Color.WHITE);
 * ga.getAttributes().setFont(Font.decode("Arial-BOLD-24");
 * ...
 * </pre>
 *
 * Annotations are usually handled by an {@link gov.nasa.worldwind.layers.AnnotationLayer}. Although they also implement the {@link Renderable}
 * and {@link Pickable} interfaces and thus can be handled by a {@link gov.nasa.worldwind.layers.RenderableLayer} too.
 *
 * <pre>
 * AnnotationLayer layer = new AnnotationLayer();
 * layer.addAnnotation(new GlobeAnnotation("Text...", Position.fromDegrees(0, 0, 0)));
 * </pre>
 *
 * Each Annotation starts its life with a fresh attribute set that can be altered to produce the desired effect.
 * However, <code>AnnotationAttributes</code> can be set and shared between annotations allowing to control the rendering attributes
 * of many annotations from a single <code>AnnotationAttributes</code> object.
 *
 * <pre>
 * AnnotationAttributes attr = new AnnotationAttributes();
 * attr.setTextColor(Color.WHITE);
 * attr.setFont(Font.decode("Arial-BOLD-24");
 * ga.setAttributes(attr);
 * </pre>
 *
 * In the above example changing the text color of the attributes set will affect all annotations refering it. However,
 * changing the text color of one of those annotations will also affect all others since it will in fact change the
 * common attributes set.
 * <p>
 * To use an attributes object only as default values for a serie of annotations use:
 * </p>
 * <pre>
 * ga.getAttributes()setDefaults(attr);
 * </pre>
 *
 * which can also be done in the Annotation constructor:
 *
 * <pre>
 * GlobeAnnotation ga = new GlobeAnnotation(text, position, attr);
 * </pre>
 *
 * Finer control over attributes inheritence can be achieved using default or fallback attributes set.
 * <p>
 * Most attributes can be set to a 'use default' value which is minus one for numeric values and <code>null</code> for attributes
 * refering objects (colors, dimensions, insets..). In such a case the value of an attribute will be that of the
 * default attribute set. New annotations have all their attributes set to use default values.
 * </p>
 * 
 * Each <code>AnnotationAttributes</code> object points to a default static attributes set which is the fallback source for
 * attributes with  <code>null</code> or <code>-1</code> values. This default attributes set can be set to any attributes object other than the
 * static one.
 *
 * <pre>
 * AnnotationAttributes geoFeature = new AnnotationAttributes();
 * geoFeature.setFrameShape(FrameFactory.SHAPE_ELLIPSE);
 * geoFeature.setInsets(new Insets(12, 12, 12, 12));
 *
 * AnnotationAttributes waterBody = new AnnotationAttributes();
 * waterBody.setTextColor(Color.BLUE);
 * waterBoby.setDefaults(geoFeature);
 *
 * AnnotationAttributes mountain = new AnnotationAttributes();
 * mountain.setTextColor(Color.GREEN);
 * mountain.setDefaults(geoFeature);
 *
 * layer.addAnnotation(new GlobeAnnotation("Spirit Lake", Position.fromDegrees(46.26, -122.15), waterBody);
 * layer.addAnnotation(new GlobeAnnotation("Mt St-Helens", Position.fromDegrees(46.20, -122.19), mountain);
 * </pre>
 * 
 * In the above example all geographic features have an ellipse shape, water bodies and mountains use that attributes
 * set has defaults and have their own text colors. They are in turn used as defaults by the two annotations. Mount
 * Saint Helens attributes could be changed without affecting other mountains. However, changes on the geoFeatures
 * attributes would affect all mountains and lakes.
 *
 *
 *
 * @author Patrick Murris
 * @version $Id$
 * @see AnnotationAttributes
 * @see AnnotationRenderer
 */
public abstract class AbstractAnnotation implements Annotation
{
    protected String text;
    protected AnnotationAttributes attributes = new AnnotationAttributes();

    protected RenderInfo renderInfo;

    abstract protected void doDraw(DrawContext dc);

    private class RenderInfo
    {
        // Reference values
        private final String text;
        private final Dimension size;
        private final Insets insets;
        private final Font font;
        private final double borderWidth;
        private final Texture texture;
        private final String shape;
        private final String leader;
        private final int cornerRadius;
        private final Point offset;
        private final String adjustWidthToText;

        // Cached values
        private final String drawText;
        private final Rectangle textBounds;
        private final DoubleBuffer verts;
        private final DoubleBuffer coords;

        public RenderInfo(String text, Dimension size, Insets insets, Font font, double borderWidth, Texture texture,
                          String shape, String leader, int cornerRadius, Point offset, String adjustWidthToText,
                          String drawText, Rectangle textBounds, DoubleBuffer verts, DoubleBuffer coords)
        {
            // Key values
            this.text = text;
            this.size = size;
            this.insets = insets;
            this.font = font;
            this.borderWidth = borderWidth;
            this.texture = texture;
            this.shape = shape;
            this.leader = leader;
            this.cornerRadius = cornerRadius;
            this.offset = offset;
            this.adjustWidthToText = adjustWidthToText;

            // Cached values
            this.drawText = drawText;
            this.textBounds = textBounds;
            this.verts = verts;
            this.coords = coords;
        }

        public String getDrawText()
        {
            return this.drawText;
        }

        public Rectangle getTextBounds()
        {
            return this.textBounds;
        }

        public DoubleBuffer getVertices()
        {
            return this.verts;
        }

        public DoubleBuffer getTextureCoordinates()
        {
            return this.coords;
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            RenderInfo ri = (RenderInfo) o;
            // Compare key values
            if (text == null ^ ri.text == null)
                return false;
            if (text != null && ri.text != null)
                if (text.compareTo(ri.text) != 0)
                    return false;

            if (size == null ^ ri.size == null)
                return false;
            if (size != null && ri.size != null)
                if (!size.equals(ri.size))
                    return false;

            if (insets == null ^ ri.insets == null)
                return false;
            if (insets != null && ri.insets != null)
                if (!insets.equals(ri.insets))
                    return false;

            if (font == null ^ ri.font == null)
                return false;
            if (font != null && ri.font != null)
                if (!font.equals(ri.font))
                    return false;

            if (borderWidth != ri.borderWidth)
                return false;

            if (texture == null ^ ri.texture == null)
                return false;
            if (texture != null && ri.texture != null)
                if (!texture.equals(ri.texture))
                    return false;

            if (shape == null ^ ri.shape == null)
                return false;
            if (shape != null && ri.shape != null)
                if (shape.compareTo(ri.shape) != 0)
                    return false;

            if (leader == null ^ ri.leader == null)
                return false;
            if (leader != null && ri.leader != null)
                if (leader.compareTo(ri.leader) != 0)
                    return false;

            if (cornerRadius != ri.cornerRadius)
                return false;

            if (offset == null ^ ri.offset == null)
                return false;
            if (offset != null && ri.offset != null)
                if (offset.distance(ri.offset) != 0)
                    return false;

            if (adjustWidthToText == null ^ ri.adjustWidthToText == null)
                return false;
            if (adjustWidthToText != null && ri.adjustWidthToText != null)
                if (adjustWidthToText.compareTo(ri.adjustWidthToText) != 0)
                    return false;

            return true;
        }
    }

    public String getText()
    {
        return this.text;
    }

    public void setText(String text)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.text = text;
    }

    public AnnotationAttributes getAttributes()
    {
        return this.attributes;
    }

    public void setAttributes(AnnotationAttributes attrs)
    {
        if (attrs == null)
        {
            String message = Logging.getMessage("nullValue.AnnotationAttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.attributes = attrs;
    }

    /**
     * Render the annotation. Called as a Renderable.
     * @param dc the current DrawContext.
     */
    public void render(DrawContext dc)
    {
        dc.getAnnotationRenderer().render(dc, this, null);
    }

    /**
     * Draw the annotation. Called as an Annotation by an AnnotationRenderer while batch rendering.
     * The gl context is ready to draw with the model view set as an identity ortho projection.
     * @param dc the current DrawContext.
     */
    public void draw(DrawContext dc)
    {
        this.doDraw(dc);
    }

    /**
     * Pick at the annotation. Called as a Pickable.
     * @param dc the current DrawContext.
     */
    public void pick(DrawContext dc, Point pickPoint)
    {
        dc.getAnnotationRenderer().pick(dc, this, null, pickPoint, null);
    }


    public void dispose()
    {
    }

    protected TextRenderer getTextRenderer(DrawContext dc, Font font)
    {
        TextRenderer tr = dc.getTextRendererCache().get(font);
        if (tr == null)
        {
            tr = new TextRenderer(font, true, true);
            dc.getTextRendererCache().add(font, tr);
        }
        return tr;
    }

    protected RenderInfo getRenderInfo(DrawContext dc, Annotation annotation)
    {
        return this.renderInfo;
    }

    protected void cacheRenderInfo(Annotation annotation, RenderInfo renderInfo)
    {
        this.renderInfo = renderInfo;
    }

    public PickSupport getPickSupport()
    {
        return this.pickSupport;
    }

    public void setPickSupport(PickSupport pickSupport)
    {
        if (pickSupport == null)
        {
            String message = Logging.getMessage("nullValue.PickSupportIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.pickSupport = pickSupport;
    }

    // -- Rendering ------------------------------------------------------------------------

    private float[] compArray;
    private Dimension minSize = new Dimension(20, 20);
    private PickSupport pickSupport;

    /**
     * Values used for drawing.
     * <p>
     * Should be used by post drawing code.
     * </p>
     */

    protected double scaleFactor;          // Scale factor used for distance and highlight - 1 = no change
    protected double alphaFactor;          // Opacity factor - 1 = fully opaque, 0 = transparent
    protected Rectangle drawRectangle;     // Viewport rectangle where annotation content was drawn
    protected Rectangle freeRectangle;     // Free space inside the draw rectangle, below the text


    /**
     * Draws an annotation at a screen point. Current GL state has ortho identity model
     * view active with origin at the screen point.
     * @param dc the current DrawContext.
     * @param screenPoint the annotation position projected location on the viewport.
     * @param pickPosition the <code>Position</code> that will be associated with any <code>PickedObject</code>
     * produced during picking.
     */
    protected void drawAnnotation(DrawContext dc, Point screenPoint, double drawScale,
                                  double drawAlpha, Position pickPosition)
    {
        // Get TextRenderer
        TextRenderer tr = getTextRenderer(dc, attributes.getFont());
        MultiLineTextRenderer mltr = new MultiLineTextRenderer(tr);
        mltr.setLineSpacing(-2); // Tighten lines together a bit
        mltr.setTextAlign(attributes.getTextAlign());

        // Get texture if any
        Texture annotationTexture = null;
        if(attributes.getImageSource() != null)
        {
            annotationTexture = dc.getTextureCache().get(attributes.getImageSource());
            if (annotationTexture == null)
                annotationTexture = initializeTexture(dc, this);
        }

        // Get colors
        Color textColor = attributes.getTextColor();
        Color backColor = attributes.getBackgroundColor();
        Color borderColor = attributes.getBorderColor();

        // Get shape, insets, cornerRadius, offset and border width
        String shape = attributes.getFrameShape();
        String leader = attributes.getLeader();
        String adjustWidthToText = attributes.getAdjustWidthToText();
        Insets insets = attributes.getInsets();
        double borderWidth = attributes.getBorderWidth();
        Point offset = attributes.getDrawOffset();
        int cornerRadius = attributes.getCornerRadius();

        // Scaling and fading factors
        this.scaleFactor = attributes.getScale() * drawScale;
        this.alphaFactor = attributes.getOpacity() * drawAlpha;
        // Highlight
        if (attributes.isHighlighted())
        {
            // Factor in highlight scale and remove transparency if highlighted
            this.scaleFactor *= attributes.getHighlightScale();
            this.alphaFactor = 1;
        }

        // Find call out preferred dimension
        Dimension size = attributes.getSize();
        // Clamp size
        size.setSize(Math.max(size.getWidth(), minSize.getWidth()), size.getHeight() > 0 ?
                Math.max(size.getHeight(), minSize.getHeight()) : 0);
        // Draw area dimension - TODO: factor in border width
        Dimension drawSize = new Dimension(
                (int)size.getWidth() - insets.left - insets.right,
                size.getHeight() > 0 ? Math.max((int)size.getHeight() - insets.top - insets.bottom, 1) : 0);

        // Render values
        String wrappedText;         // Draw text
        Rectangle2D textBounds;     // Rendered text bounds
        Double width, height;       // Final callout dimension
        DoubleBuffer verts = null;  // Callout shape vertices
        DoubleBuffer coords = null; // Callout shape texture coordinates

        // Get render info and values
        RenderInfo oldRI = getRenderInfo(dc, this);
        RenderInfo newRI = new RenderInfo(getText(), size, insets,
                attributes.getFont(), borderWidth, annotationTexture, shape, leader, cornerRadius, offset, adjustWidthToText,
                null, null, null, null);
        if(newRI.equals(oldRI))
        {
            // Use old render info
            wrappedText = oldRI.getDrawText();
            textBounds = oldRI.getTextBounds();
            verts = oldRI.getVertices();
            coords = oldRI.getTextureCoordinates();
            if(adjustWidthToText.compareTo(Annotation.SIZE_FIT_TEXT) == 0
                    && getText().length() > 0)
                width = textBounds.getWidth() + insets.left + insets.right;
            else
                width = size.getWidth();
            height = size.getHeight() > 0 ? size.getHeight() : textBounds.getHeight() + insets.top + insets.bottom;
            // Compute placement offset
            //offset = computeOffset(new Point((int)screenPoint.x,  (int)screenPoint.y), new Dimension((int)width, (int)height));
        }
        else
        {
            // Compute new render info

            // Wrap text to max available width
            if(MultiLineTextRenderer.containsHTML(getText()))
            {
                // Simple HTML
                wrappedText = MultiLineTextRenderer.processLineBreaksHTML(getText());
                wrappedText = mltr.wrapHTML(wrappedText, drawSize, dc.getTextRendererCache());
                textBounds = mltr.getBoundsHTML(wrappedText, dc.getTextRendererCache());
            }
            else
            {
                // Regular text
                wrappedText = mltr.wrap(getText(), drawSize);
                textBounds = mltr.getBounds(wrappedText);
            }
            // Final call out dimension - use size height if not zero and size width if no text
            if(adjustWidthToText.compareTo(Annotation.SIZE_FIT_TEXT) == 0
                    && getText().length() > 0)
                width = textBounds.getWidth() + insets.left + insets.right;
            else
                width = size.getWidth();
            height = size.getHeight() > 0 ? size.getHeight() : textBounds.getHeight() + insets.top + insets.bottom;
            // Compute placement offset
            //offset = computeOffset(new Point((int)screenPoint.x,  (int)screenPoint.y), new Dimension((int)width, (int)height));
            // Get shape vertices and texture coordinates
            if(shape.compareTo(FrameFactory.SHAPE_NONE) != 0)
            {
                Point shapeLeaderOffset = new Point((int)(width / 2 - offset.x),  -offset.y);
                verts = attributes.getLeader().compareTo(FrameFactory.LEADER_TRIANGLE) == 0 ?
                        FrameFactory.createShapeWithLeaderBuffer(shape, width, height, shapeLeaderOffset, cornerRadius)
                        : FrameFactory.createShapeBuffer(shape, width, height, cornerRadius);
                if(annotationTexture != null && verts != null)
                    coords = FrameFactory.getTextureCoordinates(verts, width, height, annotationTexture.getWidth(),
                            annotationTexture.getHeight());
                else
                    coords = null;
            }
            // Save render info
            newRI = new RenderInfo(getText(), size, insets, attributes.getFont(),
                    borderWidth, annotationTexture, shape, leader, cornerRadius, offset, adjustWidthToText,
                    wrappedText, (Rectangle)textBounds, verts, coords);
            cacheRenderInfo(this, newRI);
        }

        // Update current draw rectangle and free rectangle
        this.drawRectangle = new Rectangle((int)(screenPoint.x + offset.x - width / 2 + insets.left),
                (int)(screenPoint.y + offset.y + insets.bottom),
                (int)(width - insets.left - insets.right - 1),
                (int)(height - insets.bottom - insets.top - 1));
        this.freeRectangle = new Rectangle(drawRectangle.x,  drawRectangle.y,
                drawRectangle.width, drawRectangle.height - (int)textBounds.getHeight());

        // Apply scale factor and translate to callout lower left corner
        GL gl = dc.getGL();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        // Translate to screenpoint
        gl.glTranslated(screenPoint.x, screenPoint.y, 0d);        
        gl.glScaled(this.scaleFactor, this.scaleFactor, 1d);
        gl.glTranslated(- width / 2 + offset.x, offset.y, 0d);

        if (dc.isPickingMode())
        {
            // Picking
            Color color = dc.getUniquePickColor();
            int colorCode = color.getRGB();
            this.pickSupport.addPickableObject(colorCode, this, pickPosition, false);
            gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
        }
        else
        {
            // Set background color
            setDrawColor(dc, new Color(backColor.getRed(), backColor.getGreen(), backColor.getBlue(),
                    (int)((float)backColor.getAlpha() * this.alphaFactor)));
        }

        // Draw callout background color
        if (verts != null && (backColor.getAlpha() > 10 || dc.isPickingMode()))
        {
            gl.glDisable(GL.GL_TEXTURE_2D);
            FrameFactory.drawBuffer(dc, GL.GL_TRIANGLE_FAN, verts);
        }

        // Draw callout texture
        if(!dc.isPickingMode() && annotationTexture != null && verts != null)
        {
            annotationTexture.bind();
            // Texture repeat and transform
            if(attributes.getImageRepeat().compareTo(Annotation.IMAGE_REPEAT_X) == 0 ||
                    attributes.getImageRepeat().compareTo(Annotation.IMAGE_REPEAT_XY) == 0)
                annotationTexture.setTexParameteri(GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
            else
                annotationTexture.setTexParameteri(GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_BORDER);
            if(attributes.getImageRepeat().compareTo(Annotation.IMAGE_REPEAT_Y) == 0 ||
                    attributes.getImageRepeat().compareTo(Annotation.IMAGE_REPEAT_XY) == 0)
                annotationTexture.setTexParameteri(GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
            else
                annotationTexture.setTexParameteri(GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_BORDER);
            gl.glMatrixMode(GL.GL_TEXTURE);
            gl.glLoadIdentity();
            gl.glScaled(1 / attributes.getImageScale(), 1 / attributes.getImageScale(), 1d);
            if(attributes.getImageOffset() != null)
                gl.glTranslated(-(double)attributes.getImageOffset().x / annotationTexture.getWidth(),
                        -(double)attributes.getImageOffset().y / annotationTexture.getHeight(), 0d);

            gl.glEnable(GL.GL_TEXTURE_2D);
            // Set opacity
            byte textureOpacity = (byte)(attributes.getImageOpacity() * this.alphaFactor * 255);
            gl.glColor4ub(textureOpacity, textureOpacity, textureOpacity, textureOpacity);
            // Draw
            FrameFactory.drawBuffer(dc, GL.GL_TRIANGLE_FAN, verts, coords);
            gl.glDisable(GL.GL_TEXTURE_2D);

            gl.glLoadIdentity();
            gl.glMatrixMode(GL.GL_MODELVIEW);
        }

        // Draw call out border
        if(!dc.isPickingMode() && borderWidth > 0 && verts != null)
        {
            // Set border color, line width and stipple if any
            gl.glLineWidth((float)(borderWidth * this.scaleFactor));
            setDrawColor(dc, new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(),
                    (int)((float)borderColor.getAlpha() * alphaFactor)));
            if (attributes.getBorderStippleFactor() > 0)
            {
                gl.glEnable(GL.GL_LINE_STIPPLE);
                gl.glLineStipple(attributes.getBorderStippleFactor(), attributes.getBorderStipplePattern());
            }
            else
            {
                gl.glDisable(GL.GL_LINE_STIPPLE);
            }
            if(attributes.getAntiAliasHint() == Annotation.ANTIALIAS_NICEST)
            {
                gl.glEnable(GL.GL_LINE_SMOOTH);
                gl.glHint(GL.GL_LINE_SMOOTH_HINT, attributes.getAntiAliasHint());
            }
            else
                gl.glDisable(GL.GL_LINE_SMOOTH);
            // Draw
            FrameFactory.drawBuffer(dc, GL.GL_LINE_STRIP, verts);
            gl.glLineWidth(1f);  // reset line width
        }

        // Draw multi-line text
        double baseLineOffset = textBounds.getMinY() / 6; // Max line height / 6
        gl.glLoadIdentity();
        // Translate and scale to screen point
        gl.glTranslated(screenPoint.x, screenPoint.y, 0d);
        gl.glScaled(this.scaleFactor, this.scaleFactor, 1d);
        // Compute text draw start point - depending on text align
        //Point drawPointBottom = new Point((int)( - width / 2 + insets.left + offset.x),
        //        (int)( insets.bottom + offset.y + baseLineOffset + textBounds.getHeight()) + 1);
        Point drawPoint = new Point((int)( - width / 2 + insets.left + offset.x),
                (int)( offset.y + insets.bottom + (drawSize.getHeight() > 0 ?
                        drawSize.getHeight() : textBounds.getHeight()) + baseLineOffset) + 2 );
        if(attributes.getTextAlign() == MultiLineTextRenderer.ALIGN_CENTER)
                drawPoint.setLocation(offset.x + (insets.left - insets.right) / 2, drawPoint.y);
        if(attributes.getTextAlign() == MultiLineTextRenderer.ALIGN_RIGHT)
                drawPoint.setLocation((int)( width / 2 - insets.right + offset.x), drawPoint.y);
        // Draw
        if(MultiLineTextRenderer.containsHTML(getText()))
        {
            // Simple html
            if(!dc.isPickingMode())
            {
                mltr.setTextColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(),
                        (int)((float)textColor.getAlpha() * this.alphaFactor)));
                mltr.drawHTML(wrappedText, drawPoint.x, drawPoint.y, dc.getTextRendererCache());
            }
            else
            {
                // Draw text with unique colors for each word - only if cursor is inside our draw rectangle
                if (dc.getPickPoint() != null)
                    if (getRectangleInViewportCoordinates(dc, this.drawRectangle, screenPoint, scaleFactor)
                        .contains(dc.getPickPoint()))
                            mltr.pickHTML(wrappedText, drawPoint.x, drawPoint.y, dc.getTextRendererCache(),
                                dc, this.pickSupport, this, pickPosition);
            }

        }
        else
        {
            // Regular text
            if(!dc.isPickingMode())
            {
                mltr.setTextColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(),
                        (int)(textColor.getAlpha() * this.alphaFactor)));
                mltr.setBackColor(new Color(backColor.getRed(), backColor.getGreen(), backColor.getBlue(),
                        (int)(backColor.getAlpha() * this.alphaFactor)));
                mltr.getTextRenderer().begin3DRendering();
                mltr.draw(wrappedText, drawPoint.x, drawPoint.y, (int)textBounds.getMinY(), attributes.getEffect());
                mltr.getTextRenderer().end3DRendering();
            }
            else
            {
                // Draw text with unique colors for each word - only if cursor is inside our draw rectangle
                if (dc.getPickPoint() != null)
                    if (getRectangleInViewportCoordinates(dc, this.drawRectangle, screenPoint, scaleFactor)
                        .contains(dc.getPickPoint()))
                            mltr.pick(wrappedText, drawPoint.x, drawPoint.y, (int)textBounds.getMinY(),
                                dc, this.pickSupport, this, pickPosition);
            }

        }

    }
    // -- end drawAnnotation() --------------------------------------------------------------

    // Compute distance from eye to the position in the middle of the screen
    protected double computeLookAtDistance(DrawContext dc)
    {
        View view = dc.getView();
        // Get point in the middle of the screen
        Position groundPos = view.computePositionFromScreenPoint(
                view.getViewport().getWidth() / 2, view.getViewport().getHeight() / 2);

        // Return eye altitude if no point found
        if (groundPos == null)
            return view.getEyePosition().getElevation();

        return view.getEyePoint().distanceTo3(dc.getGlobe().computePointFromPosition(groundPos));
    }

    // Compute viewport rectangle coordinates after scaling from screen point.
    protected Rectangle getRectangleInViewportCoordinates(DrawContext dc, Rectangle r, Point screenPoint,
                                                          double scaleFactor)
    {
        Rectangle r2 = new Rectangle(r);
        r2.translate(-screenPoint.x, -screenPoint.y);
        r2.setRect(r2.x * scaleFactor, r2.y * scaleFactor, r2.width * scaleFactor, r2.height * scaleFactor);
        r2.translate(screenPoint.x, screenPoint.y);
        return new Rectangle(r2.x,  dc.getView().getViewport().height - r2.y - r2.height, r2.width, r2.height);
    }

    // Initialize texture
    protected Texture initializeTexture(DrawContext dc, Annotation annotation)
    {
        try
        {
            Texture annotationTexture = null;
            Object imageSource = annotation.getAttributes().getImageSource();

            if (imageSource instanceof String)
            {
                String path = (String) imageSource;
                java.io.InputStream textureStream = this.getClass().getResourceAsStream("/" + path);
                if (textureStream == null)
                {
                    java.io.File textureFile = new java.io.File(path);
                    if (textureFile.exists())
                    {
                        textureStream = new java.io.FileInputStream(textureFile);
                    }
                }
                annotationTexture = TextureIO.newTexture(textureStream, true, null);
            }
            else if (imageSource instanceof BufferedImage)
            {
                annotationTexture = TextureIO.newTexture((BufferedImage) imageSource, true);
            }
            else
            {
                // TODO: Log case of unknown image-source type.
            }

            if (annotationTexture == null)
            {
                // TODO: Log case.
                return null;
            }

            // Annotations with the same path are assumed to be identical textures, so key the texture id off the path.
            dc.getTextureCache().put(imageSource, annotationTexture);
            annotationTexture.bind();

            GL gl = dc.getGL();
            gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);

            return annotationTexture;
        }
        catch (java.io.IOException e)
        {
            String msg = Logging.getMessage("generic.IOExceptionDuringTextureInitialization");
            Logging.logger().log(Level.SEVERE, msg, e);
            throw new WWRuntimeException(msg, e);
        }
    }

    // Set draw color using pre multipied alpha rgb values.
    protected void setDrawColor(DrawContext dc, float r, float g, float b, float a)
    {
        GL gl = dc.getGL();
        dc.getGL().glColor4f(r * a, g * a, b * a, a);
    }

    // Set draw color using pre multipied alpha rgb values.
    protected void setDrawColor(DrawContext dc, Color color)
    {
        if (this.compArray == null)
            this.compArray = new float[4];
        color.getRGBComponents(this.compArray);
        this.setDrawColor(dc, this.compArray[0], this.compArray[1], this.compArray[2], this.compArray[3]);
    }

    protected void setDepthFunc(DrawContext dc, Vec4 screenPoint)
    {
        GL gl = dc.getGL();

        Position eyePos = dc.getView().getEyePosition();
        if (eyePos == null)
        {
            gl.glDepthFunc(GL.GL_ALWAYS);
            return;
        }

        double altitude = eyePos.getElevation();
        if (altitude < (dc.getGlobe().getMaxElevation() * dc.getVerticalExaggeration()))
        {
            double depth = screenPoint.z - (8d * 0.00048875809d);
            depth = depth < 0d ? 0d : (depth > 1d ? 1d : depth);
            gl.glDepthFunc(GL.GL_LESS);
            gl.glDepthRange(depth, depth);
        }
        else if (screenPoint.z >= 1d)
        {
            gl.glDepthFunc(GL.GL_EQUAL);
            gl.glDepthRange(1d, 1d);
        }
        else
        {
            gl.glDepthFunc(GL.GL_ALWAYS);
        }
    }
    

}
