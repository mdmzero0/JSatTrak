/*
Copyright (C) 2001, 2006, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * {@link Annotation} attributes set. All {@link AbstractAnnotation} objects start life
 * referencing a new instance of this object.
 * <p>
 * This class also defines a static <b>default</b> attributes bundle containing default values for all attributes.
 * New <code>AnnotationAttributes</code> refer this static bundle as their default values source when an
 * attribute has not been set.
 * </p>
 * <p>
 * New <code>AnnotationAttributes</code> set have all their attributes pointing to the default values until
 * they are set by the application. Most attributes refer to the default value by using minus one (<code>-1</code>)
 * for munerics and <code>null</code> for objects.
 * </p>
 * <p>
 * The default attributes set can be changed for a non static one under the application control. The process
 * can be extended or cascaded to handle multiple levels of inheritance for default attributes.
 * </p>
 * @author Patrick Murris
 * @version $Id$
 * @see AbstractAnnotation
 * @see FrameFactory
 * @see MultiLineTextRenderer
 */
public class AnnotationAttributes
{
    private static final AnnotationAttributes defaults = new AnnotationAttributes();

    static
    {
        defaults.setFrameShape(FrameFactory.SHAPE_RECTANGLE);
        defaults.setSize(new Dimension(160, 0));
        defaults.setScale(1);
        defaults.setOpacity(1);
        defaults.setLeader(FrameFactory.LEADER_TRIANGLE);
        defaults.setCornerRadius(6);
        defaults.setAdjustWidthToText(Annotation.SIZE_FIT_TEXT);
        defaults.setDrawOffset(new Point(-10, 20));
        defaults.setHighlightScale(1.2);
        defaults.setInsets(new Insets(6, 6, 6, 6));
        defaults.setFont(Font.decode("Arial-PLAIN-12"));
        defaults.setTextAlign(MultiLineTextRenderer.ALIGN_LEFT);
        defaults.setTextColor(new Color(1f, 1f, 1f, .8f));
        defaults.setBackgroundColor(new Color(0f, 0f, 0f, .4f));
        defaults.setBorderColor(new Color(1f, 1f, 1f, .7f));
        defaults.setBorderWidth(1);
        defaults.setBorderStippleFactor(0);
        defaults.setBorderStipplePattern((short)0xAAAA);
        defaults.setAntiAliasHint(Annotation.ANTIALIAS_FASTEST);
        defaults.setImageScale(1);
        defaults.setImageOffset(new Point(0, 0));
        defaults.setImageOpacity(.7);
        defaults.setImageRepeat(Annotation.IMAGE_REPEAT_XY);
        defaults.setDistanceMinScale(.5);
        defaults.setDistanceMaxScale(2);
        defaults.setDistanceMinOpacity(.3);
        defaults.setEffect(MultiLineTextRenderer.EFFECT_NONE);
    }

    private AnnotationAttributes defaultAttributes = defaults;

    private String frameShape;                              // Use default (null)
    private Dimension size;                                 // Use default (null)
    private double scale = -1;                              // Use default (-1)
    private double opacity = -1;                            // Use default (-1)
    private String leader;                                  // Use default (null)
    private int cornerRadius = -1;                          // Use default (-1)
    private String adjustWidthToText;                       // Use default (null)
    private Point drawOffset;                               // Use default (null)
    private boolean isHighlighted = false;
    private boolean isVisible = true;
    private double highlightScale = -1;                     // Use default (-1)
    private Font font;                                      // Use default (null)
    private int textAlign = -1;                             // Use default (-1)
    private Color textColor;                                // Use default (null)
    private Color backgroundColor;                          // Use default (null)
    private Color borderColor;                              // Use default (null)
    private double borderWidth = -1;                        // Use default (-1)
    private int borderStippleFactor = -1;                   // Use default (-1)
    private short borderStipplePattern = (short) 0x0000;    // Use default (zero)
    private int antiAliasHint = -1;                         // Use default (-1)
    private Insets insets;                                  // Use default (null)
    private Object imageSource;
    private double imageScale = -1;                         // Use default (-1)
    private Point imageOffset;                              // Use default (null)
    private double imageOpacity = -1;                       // Use default (-1)
    private String imageRepeat;                             // Use default (null)
    private double distanceMinScale = -1;                   // Use default (-1)
    private double distanceMaxScale = -1;                   // Use default (-1)
    private double distanceMinOpacity = -1;                 // Use default (-1)
    private String effect;                                  // Use default (null)


    //** Public properties **********************************************************************

    /**
     * Set the fallback default attributes set.
     * @param attr the default attributes set.
     */
    public void setDefaults(AnnotationAttributes attr)
    {
        if (attr == null)
        {
            String message = Logging.getMessage("nullValue.AnnotationAttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.defaultAttributes = attr;
    }


    /**
     * Get the callout frame shape. Can be one of {@link FrameFactory}.SHAPE_RECTANGLE (default), SHAPE_ELLIPSE
     * or SHAPE_NONE.
     * @return the callout frame shape.
     */
    public String getFrameShape()
    {
        return this.frameShape != null ? this.frameShape : defaultAttributes.getFrameShape();
    }

    /**
     * Set the callout frame shape. Can be one of {@link FrameFactory}.SHAPE_RECTANGLE (default), SHAPE_ELLIPSE
     * or SHAPE_NONE. Set to <code>null</code> to use the default shape.
     * <p>
     * Note that SHAPE_ELLIPSE draws an ellipse <u>inside</u> the callout bounding rectangle set by its
     * size (see setSize()) or its text bounding rectangle (see setAdjustWidthToText() and setSize() with height
     * set to zero). It is often necessary to have larger Insets dimensions (see setInsets()) to avoid having
     * the text drawn outside the shape border.
     * </p>
     * @param shape the callout frame shape.
     */
    public void setFrameShape(String shape)
    {
        this.frameShape = shape;
    }

    /**
     * Get whether the <code>Annotation</code> is highlighted and should be drawn bigger - see setHighlightScale().
     * @return true if highlighted.
     */
    public boolean isHighlighted()
    {
        return isHighlighted;
    }

    /**
     * Set whether the <code>Annotation</code> is highlighted and should be drawn bigger - see setHighlightScale().
     * @param highlighted true if highlighted.
     */
    public void setHighlighted(boolean highlighted)
    {
        isHighlighted = highlighted;
    }

    /**
     * Get the scaling factor applied to highlighted <code>Annotations</code>.
     * @return the scaling factor applied to highlighted <code>Annotations</code>.
     */
    public double getHighlightScale()
    {
        return highlightScale > 0 ? this.highlightScale : defaultAttributes.getHighlightScale();
    }

    /**
     * Set the scaling factor applied to highlighted <code>Annotations</code>. Set to minus one (<code>-1</code>)
     * to use the default value.
     * @param highlightScale the scaling factor applied to highlighted <code>Annotations</code>.
     */
    public void setHighlightScale(double highlightScale)
    {
        this.highlightScale = highlightScale;
    }

    /**
     * Get the annotation callout preferred total dimension in pixels.
     * @return the callout preferred total dimension in pixels.
     */
    public Dimension getSize()
    {
        return this.size != null ? this.size : defaultAttributes.getSize();
    }

    /**
     * Set the annotation callout preferred total dimension in pixels.
     * <p>
     * If necessary, the text will be wraped into several lines so as not to exceed the callout preferred
     * <code><b>width</b></code> (minus the <code>Insets</code> <code>left</code> and <code>right</code> dimensions
     * - see setInsets()).
     * However, if setAdjustWidthToText() is set to true, the final callout width will follow that of the final
     * text bounding rectangle.
     * </p>
     * <p>
     * If necessary, the text will also be truncated so as not to exceed the given <code><b>height</b></code>.
     * A <code>zero</code> value (default) will have the callout follow the final text bounding rectangle height
     * (including the <code>Insets</code> <code>top</code> and <code>bottom</code>).
     * </p>
     * Set to <code>null</code> to use the default size.
     * @param size the callout preferred total dimension in pixels.
     */
    public void setSize(Dimension size)
    {
        this.size = size;
    }

    /**
     * Get the scaling factor applied to the annotation. Default is 1.
     * @return the scaling factor applied to the annotation
     */
    public double getScale()
    {
        return this.scale >= 0 ? this.scale : defaultAttributes.getScale();
    }

    /**
     * Set the scaling factor to apply to the annotation. Default is 1.
     * Set to minus one (<code>-1</code>) to use the default value.
     * @param scale the scaling factor to apply to the annotation
     */
    public void setScale(double scale)
    {
        this.scale = scale;
    }

    /**
     * Get the opacity factor applied to the annotation. Default is 1.
     * @return the opacity factor applied to the annotation
     */
    public double getOpacity()
    {
        return this.opacity >= 0 ? this.opacity : defaultAttributes.getOpacity();
    }

    /**
     * Set the opacity factor to apply to the annotation. Default is 1.
     * Set to minus one (<code>-1</code>) to use the default value.
     * @param opacity the opacity factor to apply to the annotation
     */
    public void setOpacity(double opacity)
    {
        this.opacity = opacity;
    }

    /**
     * Get the callout shape leader type. Can be one of {@link FrameFactory}.LEADER_TRIANGLE (default) or LEADER_NONE.
     * @return the callout shape leader type.
     */
    public String getLeader()
    {
        return this.leader != null ? this.leader : defaultAttributes.getLeader();
    }

    /**
     * Set the callout shape leader type. Can be one of {@link FrameFactory}.LEADER_TRIANGLE (default) or LEADER_NONE.
     * @param leader the callout shape leader type.
     */
    public void setLeader(String leader)
    {
        this.leader = leader;
    }

    /**
     * Get the callout shape rounded corners radius in pixels. A value of <code>zero</code> means no rounded corners.
     * @return the callout shape rounded corners radius in pixels.
     */
    public int getCornerRadius()
    {
        return this.cornerRadius >= 0 ? this.cornerRadius : defaultAttributes.getCornerRadius();
    }

    /**
     * Set the callout shape rounded corners radius in pixels. A value of <code>zero</code> means no rounded corners.
     * Set this attribute to minus one (<code>-1</code>) to use the default value.
     * @param radius the callout shape rounded corners radius in pixels.
     */
    public void setCornerRadius(int radius)
    {
        this.cornerRadius = radius;
    }

    /**
     * Get whether the callout width should adjust to follow the wrapped text bounding rectangle width,
     * which may be smaller or larger then the preferred size depending on the text. Can be one
     * of {@link Annotation}.SIZE_FIXED or SIZE_FIT_TEXT.
     * @return whether the callout width is adjusted to follow the text bounding rectangle width.
     */
    public String getAdjustWidthToText()
    {
        return this.adjustWidthToText != null ? this.adjustWidthToText : defaultAttributes.getAdjustWidthToText();
    }

    /**
     * Set whether the callout width should adjust to follow the wrapped text bounding rectangle width
     * which may be smaller or larger then the preferred size depending on the text. Can be one
     * of {@link Annotation}.SIZE_FIXED (default) or SIZE_FIT_TEXT.
     * Setting this attribute to <code>SIZE_FIT_TEXT</code> would have the callout drawn at its exact width (see setSize()).
     * @param state whether the callout width should adjust to follow the text bounding rectangle width.
     */
    public void setAdjustWidthToText(String state)
    {
        this.adjustWidthToText = state;
    }

    /**
     * Get the callout displacement offset in pixels from the globe Position or screen point at which it is associated.
     * When the callout has a leader (see setLeader(String leader)), it will lead to the original point.
     * In the actual implementation, the callout is drawn above its associated point and the leader connects at
     * the bottom of the frame, in the middle. Positive X increases toward the right and positive Y in the up direction.
     * @return the callout displacement offset in pixels
     */
    public Point getDrawOffset()
    {
        return this.drawOffset != null ? this.drawOffset : defaultAttributes.getDrawOffset();
    }

    /**
     * Set the callout displacement offset in pixels from the globe Position or screen point at which it is associated.
     * When the callout has a leader (see setLeader(String leader)), it will lead to the original point.
     * In the actual implementation, the callout is drawn above its associated point and the leader connects at
     * the bottom of the frame, in the middle. Positive X increases toward the right and positive Y in the up direction.
     * Set to <code>null</code> to use the default offset.
     * @param offset the callout displacement offset in pixels
     */
    public void setDrawOffset(Point offset)
    {
        this.drawOffset = offset;
    }

    /**
     * Get the callout <code>Insets</code> dimensions in pixels. The text is drawn inside the callout frame
     * while keeping a distance from the callout border defined in the Insets.
     * @return the callout <code>Insets</code> dimensions in pixels.
     */
    public Insets getInsets()
    {
        return this.insets != null ? this.insets : defaultAttributes.getInsets();
    }

    /**
     * Set the callout <code>Insets</code> dimensions in pixels. The text will be drawn inside the callout frame
     * while keeping a distance from the callout border defined in the Insets. Set to <code>null</code> to use the
     * default Insets.
     * @param insets the callout <code>Insets</code> dimensions in pixels.
     */
    public void setInsets(Insets insets)
    {
        this.insets = insets;
    }

    /**
     * Get the callout border line width. A value of <code>zero</code> means no border is being drawn.
     * @return the callout border line width.
     */
    public double getBorderWidth()
    {
        return this.borderWidth >= 0 ? this.borderWidth : defaultAttributes.getBorderWidth();
    }

    /**
     * Set the callout border line width. A value of <code>zero</code> means no border
     * will is drawn. Set to minus one (<code>-1</code>) to use the default value.
     * @param width the callout border line width.
     */
    public void setBorderWidth(double width)
    {
        this.borderWidth = width;
    }

    /**
     * Get the stipple factor used for the callout border line. A value of <code>zero</code> (default) means no pattern
     * is applied.
     * @return the stipple factor used for the callout border line.
     */
    public int getBorderStippleFactor()
    {
        return this.borderStippleFactor >= 0 ? this.borderStippleFactor : defaultAttributes.getBorderStippleFactor();
    }

    /**
     * Set the stipple factor used for the callout border line. A value of <code>zero</code> (default) means no pattern
     * will be applied. Set to minus one (<code>-1</code>) to use the default value.
     * @param factor the stipple factor used for the callout border line.
     */
    public void setBorderStippleFactor(int factor)
    {
        this.borderStippleFactor = factor;
    }

    /**
     * Get the stipple pattern used for the callout border line.
     * @return the stipple pattern used for the callout border line.
     */
    public short getBorderStipplePattern()
    {
        return this.borderStipplePattern != 0x0000 ? this.borderStipplePattern : defaultAttributes.getBorderStipplePattern();
    }

    /**
     * Set the stipple pattern used for the callout border line. Set to <code>0x0000</code> to use the default value.
     * @param pattern the stipple pattern used for the callout border line.
     */
    public void setBorderStipplePattern(short pattern)
    {
        this.borderStipplePattern = pattern;
    }

    /**
     * Get the <code>GL</code> antialias hint used for rendering the callout border line. Can be one of
     * {@link Annotation}.ANTIALIAS_DONT_CARE, ANTIALIAS_FASTEST (default) or ANTIALIAS_NICEST.
     * @return the <code>GL</code> antialias hint used for rendering the callout border line.
     */
    public int getAntiAliasHint()
    {
        return this.antiAliasHint >=0 ? this.antiAliasHint : defaultAttributes.getAntiAliasHint();
    }

    /**
     * Set the <code>GL</code> antialias hint used for rendering the callout border line. Can be one of
     * {@link Annotation}.ANTIALIAS_DONT_CARE, ANTIALIAS_FASTEST (default) or ANTIALIAS_NICEST.
     * Set to minus one (<code>-1</code>) to use the default value.
     * @param hint the <code>GL</code> antialias hint used for rendering the callout border line.
     */
    public void setAntiAliasHint(int hint)
    {
        this.antiAliasHint = hint;
    }

    /**
     * Get whether the annotation is visible and should be rendered.
     * @return true if the annotation is visible and should be rendered.
     */
    public boolean isVisible()
    {
        return isVisible;
    }

    /**
     * Set whether the annotation is visible and should be rendered.
     * @param visible true if the annotation is visible and should be rendered.
     */
    public void setVisible(boolean visible)
    {
        isVisible = visible;
    }

    /**
     * Get the <code>Font</code> used for text rendering.
     * @return the <code>Font</code> used for text rendering.
     */
    public Font getFont()
    {
        return this.font != null ? this.font : defaultAttributes.getFont();
    }

    /**
     * Set the <code>Font</code> used for text rendering. Set to <code>null</code> to use the default value.
     * @param font the <code>Font</code> used for text rendering.
     */
    public void setFont(Font font)
    {
        this.font = font;
    }

    /**
     * Get the text alignement. Can be one of {@link MultiLineTextRenderer}.ALIGN_LEFT (default), ALIGN_CENTER
     * or ALIGN_RIGHT.
     * @return align the text alignement. Can be one of MultiLineTextRenderer.ALIGN_LEFT, ALIGN_CENTER or ALIGN_RIGHT.
     */
    public int getTextAlign()
    {
        return this.textAlign >= 0 ? this.textAlign : defaultAttributes.getTextAlign();
    }

    /**
     * Set the text alignement. Can be one of {@link MultiLineTextRenderer}.ALIGN_LEFT (default), ALIGN_CENTER
     * or ALIGN_RIGHT. Set to <code>null</code> to use the default value.
     * @param align the text alignement.
     */
    public void setTextAlign(int align)
    {
        this.textAlign = align;
    }

    /**
     * Get the text <code>Color</code>.
     * @return the text <code>Color</code>.
     */
    public Color getTextColor()
    {
        return this.textColor != null ? this.textColor : defaultAttributes.getTextColor();
    }

    /**
     * Set the text <code>Color</code>. Set to <code>null</code> to use the default value.
     * @param color the text <code>Color</code>.
     */
    public void setTextColor(Color color)
    {
        this.textColor = color;
    }

    /**
     * Get the callout background <code>Color</code>.
     * @return the callout background <code>Color</code>.
     */
    public Color getBackgroundColor()
    {
        return this.backgroundColor != null ? this.backgroundColor : defaultAttributes.getBackgroundColor();
    }

    /**
     * Set the callout background <code>Color</code>. Set to <code>null</code> to use the default value.
     * @param color the callout background <code>Color</code>.
     */
    public void setBackgroundColor(Color color)
    {
        this.backgroundColor = color;
    }

    /**
     * Get the callout border <code>Color</code>.
     * @return the callout border <code>Color</code>.
     */
    public Color getBorderColor()
    {
        return this.borderColor != null ? this.borderColor : defaultAttributes.getBorderColor();
    }

    /**
     * Set the callout border <code>Color</code>. Set to <code>null</code> to use the default value.
     * @param color the callout border <code>Color</code>.
     */
    public void setBorderColor(Color color)
    {
        this.borderColor = color;
    }

    /**
     * Get the background image source. Can be a <code>String</code> providing the path to a local image,
     * a {@link java.awt.image.BufferedImage} or <code>null</code>.
     * @return the background image source.
     */
    public Object getImageSource()
    {
        return this.imageSource;
    }

    /**
     * Set the background image source. Can be a <code>String</code> providing the path to a local image
     * or a {@link java.awt.image.BufferedImage}. Set to null for no background image rendering.
     * @param imageSource the background image source.
     */
    public void setImageSource(Object imageSource)
    {
        this.imageSource = imageSource;
    }

    /**
     * Get the background image scaling factor.
     * @return the background image scaling factor.
     */
    public double getImageScale()
    {
        return this.imageScale >= 0 ? this.imageScale : defaultAttributes.getImageScale();
    }

    /**
     * Set the background image scaling factor. Set to minus one (<code>-1</code>) to use the default value.
     * @param scale the background image scaling factor.
     */
    public void setImageScale(double scale)
    {
        this.imageScale = scale;
    }

    /**
     * Get the background image offset in pixels (before background scaling).
     * @return the background image offset in pixels
     */
    public Point getImageOffset()
    {
        return this.imageOffset != null ? this.imageOffset : defaultAttributes.getImageOffset();
    }

    /**
     * Set the background image offset in pixels (before background scaling). Set to <code>null</code> to use the
     * default value.
     * @param offset the background image offset in pixels
     */
    public void setImageOffset(Point offset)
    {
        this.imageOffset = offset;
    }

    /**
     * Get the opacity of the background image (0 to 1).
     * @return the opacity of the background image (0 to 1).
     */
    public double getImageOpacity()
    {
        return this.imageOpacity >= 0 ? this.imageOpacity : defaultAttributes.getImageOpacity();
    }

    /**
     * Set the opacity of the background image (0 to 1). Set to minus one (<code>-1</code>) to use the default value.
     * @param opacity the opacity of the background image (0 to 1).
     */
    public void setImageOpacity(double opacity)
    {
        this.imageOpacity = opacity;
    }

    /**
      * Get the repeat behavior or the background image. Can be one of {@link Annotation}.IMAGE_REPEAT_X,
      * IMAGE_REPEAT_Y, IMAGE_REPEAT_XY (default) or IMAGE_REPEAT_NONE.
      * @return the repeat behavior or the background image.
      */
     public String getImageRepeat()
    {
        return this.imageRepeat != null ? this.imageRepeat : defaultAttributes.getImageRepeat();
    }

    /**
     * Set the repeat behavior or the background image. Can be one of {@link Annotation}.IMAGE_REPEAT_X,
     * IMAGE_REPEAT_Y, IMAGE_REPEAT_XY (default) or IMAGE_REPEAT_NONE. Set to <code>null</code> to use
     * the default value.
     * @param repeat the repeat behavior or the background image.
     */
    public void setImageRepeat(String repeat)
    {
        this.imageRepeat = repeat;
    }

    /**
     * Get the path to the image used for background image. Returns <code>null</code> if the image source is null
     * or a memory BufferedImage.
     * @return the path to the image used for background image.
     */
    public String getPath()
    {
        return this.imageSource instanceof String ? (String) this.imageSource : null;
    }

    /**
     * Get the minimum scale that can be applied to an annotation when it gets farther away from the eye than the view
     * lookat point.
     * @return the minimum scale that can be applied to an annotation when it gets away from the eye
     */
    public double getDistanceMinScale()
    {
        return this.distanceMinScale >= 0 ? this.distanceMinScale : defaultAttributes.getDistanceMinScale();
    }

    /**
     * Set the minimum scale that can be applied to an annotation when it gets farther away from the eye than the view
     * lookat point. Set to minus one (<code>-1</code>) to use the default value.
     * @param scale the minimum scale that can be applied to an annotation when it gets away from the eye
     */
    public void setDistanceMinScale(double scale)
    {
        this.distanceMinScale = scale;
    }

    /**
     * Get the maximum scale that can be applied to an annotation when it gets closer to the eye than the view
     * lookat point.
     * @return the maximum scale that can be applied to an annotation when it gets closer to the eye
     */
    public double getDistanceMaxScale()
    {
        return this.distanceMaxScale >= 0 ? this.distanceMaxScale : defaultAttributes.getDistanceMaxScale();
    }

    /**
     * Set the maximum scale that can be applied to an annotation when it gets closer to the eye than the view
     * lookat point. Set to minus one (<code>-1</code>) to use the default value.
     * @param scale the maximum scale that can be applied to an annotation when it gets closer to the eye
     */
    public void setDistanceMaxScale(double scale)
    {
        this.distanceMaxScale = scale;
    }

    /**
     * Get the minimum opacity an annotation can have when fading away from the eye (0 to 1).
     * @return the minimum opacity an annotation can have when fading away from the eye.
     */
    public double getDistanceMinOpacity()
    {
        return this.distanceMinOpacity >= 0 ? this.distanceMinOpacity : defaultAttributes.getDistanceMinOpacity();
    }

    /**
     * Set the minimum opacity an annotation can have when fading away from the eye (0 to 1). Set to minus one
     * (<code>-1</code>) to use the default value.
     * @param opacity the minimum opacity an annotation can have when fading away from the eye.
     */
    public void setDistanceMinOpacity(double opacity)
    {
        this.distanceMinOpacity = opacity;
    }

     /**
      * Get the effect used to decorate the text. Can be one of {@link MultiLineTextRenderer}.EFFECT_SHADOW,
      * EFFECT_OUTLINE or EFFECT_NONE (default).
      * @return the effect used for text rendering
      */
     public String getEffect()
    {
        return this.effect != null ? this.effect : defaultAttributes.getEffect();
    }

    /**
     * Set the effect used to decorate the text. Can be one of {@link MultiLineTextRenderer}.EFFECT_SHADOW,
     * EFFECT_OUTLINE or EFFECT_NONE (default). Set to <code>null</code> to use the default value.
     * @param effect the effect to use for text rendering
     */
    public void setEffect(String effect)
    {
        this.effect = effect;
    }
    

}
