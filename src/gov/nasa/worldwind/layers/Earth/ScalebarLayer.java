/*
Copyright (C) 2001, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Earth;

import com.sun.opengl.util.j2d.TextRenderer;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import java.awt.*;
import java.awt.geom.*;

/**
 * Renders a scalebar graphic in a screen corner
 * @author Patrick Murris
 * @version $Id$
 */
public class ScalebarLayer extends RenderableLayer {

	// Positionning constants
    public final static String NORTHWEST = "gov.nasa.worldwind.ScalebarLayer.NorthWest";
    public final static String SOUTHWEST = "gov.nasa.worldwind.ScalebarLayer.SouthWest";
    public final static String NORTHEAST = "gov.nasa.worldwind.ScalebarLayer.NorthEast";
    public final static String SOUTHEAST = "gov.nasa.worldwind.ScalebarLayer.SouthEast";
	// Stretching behavior constants
	public final static String RESIZE_STRETCH = "gov.nasa.worldwind.ScalebarLayer.Stretch";
	public final static String RESIZE_SHRINK_ONLY = "gov.nasa.worldwind.ScalebarLayer.ShrinkOnly";
	public final static String RESIZE_KEEP_FIXED_SIZE = "gov.nasa.worldwind.ScalebarLayer.FixedSize";
	// Units constants
	public final static String UNIT_METRIC = "gov.nasa.worldwind.ScalebarLayer.Metric";
	public final static String UNIT_IMPERIAL = "gov.nasa.worldwind.ScalebarLayer.Imperial";

	// Display parameters - TODO: make configurable
	private Dimension size = new Dimension(150, 10);
	private Color color = Color.white;
	private int borderWidth = 20;
	private String position = SOUTHEAST;
	private String resizeBehavior = RESIZE_SHRINK_ONLY;
	private String unit = UNIT_METRIC;
	private Font defaultFont = Font.decode("Arial-12-PLAIN");
	private double toViewportScale = 0.2;

	private Vec4 locationCenter = null;
	private TextRenderer textRenderer = null;

    // Draw it as ordered with an eye distance of 0 so that it shows up in front of most other things.
    // TODO: Add general support for this common pattern.
    private OrderedIcon orderedImage = new OrderedIcon();

    private class OrderedIcon implements OrderedRenderable
    {
        public double getDistanceFromEye()
        {
            return 0;
        }

        public void pick(DrawContext dc, Point pickPoint)
        {
        }

        public void render(DrawContext dc)
        {
            ScalebarLayer.this.draw(dc);
        }
    }

    /**
     * Renders a scalebar graphic in a screen corner
     */
	public ScalebarLayer()
    {
		this.setName(Logging.getMessage("layers.Earth.ScalebarLayer.Name"));
	}

	// Public properties

    /**
     * Get the scalebar graphic Dimension (in pixels)
     * @return the scalebar graphic Dimension
     */
    public Dimension getSize()
    {
		return this.size;
	}

    /**
     * Set the scalebar graphic Dimenion (in pixels)
     * @param size the scalebar graphic Dimension
     */
    public void setSize(Dimension size)
    {
        if (size == null)
        {
            String message = Logging.getMessage("nullValue.DimensionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
		this.size = size;
	}

    /**
     * Get the scalebar color
     * @return  the scalebar Color
     */
    public Color getColor()
    {
		return this.color;
	}

    /**
     * Set the scalbar Color
     * @param color the scalebar Color
     */
    public void setColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
		this.color = color;
	}

	/** Returns the scalebar-to-viewport scale factor.
	 *
	 * @return the scalebar-to-viewport scale factor
	 */
	public double getToViewportScale()
	{
		return toViewportScale;
	}

	/**
	 * Sets the scale factor applied to the viewport size to determine the displayed size of the scalebar. This
	 * scale factor is used only when the layer's resize behavior is {@link #RESIZE_STRETCH} or {@link
	 * #RESIZE_SHRINK_ONLY}. The scalebar's width is adjusted to occupy the proportion of the viewport's width indicated by
	 * this factor. The scalebar's height is adjusted to maintain the scalebar's Dimension aspect ratio.
	 *
	 * @param toViewportScale the scalebar to viewport scale factor
	 */
	public void setToViewportScale(double toViewportScale)
	{
		this.toViewportScale = toViewportScale;
	}

	public String getPosition()
    {
		return this.position;
	}

    /**
     * Sets the relative viewport location to display the scalebar. Can be one of {@link #NORTHEAST} (the default),
     * {@link #NORTHWEST}, {@link #SOUTHEAST}, or {@link #SOUTHWEST}. These indicate the corner of the viewport.
     *
     * @param position the desired scalebar position
     */
	public void setPosition(String position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
		this.position = position;
	}

    /**
     * Returns the layer's resize behavior.
     *
     * @return the layer's resize behavior
     */
	public String getResizeBehavior()
	{
		return resizeBehavior;
	}

    /**
     * Sets the behavior the layer uses to size the scalebar when the viewport size changes, typically when the
     * World Wind window is resized. If the value is {@link #RESIZE_KEEP_FIXED_SIZE}, the scalebar size is kept to the size
     * specified in its Dimension scaled by the layer's current icon scale. If the value is {@link #RESIZE_STRETCH},
     * the scalebar is resized to have a constant size relative to the current viewport size. If the viewport shrinks the
     * scalebar size decreases; if it expands then the scalebar enlarges. If the value is
     * {@link #RESIZE_SHRINK_ONLY} (the default), scalebar sizing behaves as for {@link #RESIZE_STRETCH} but it will
     * not grow larger than the size specified in its Dimension.
     *
     * @param resizeBehavior the desired resize behavior
     */
	public void setResizeBehavior(String resizeBehavior)
	{
		this.resizeBehavior = resizeBehavior;
	}

	public int getBorderWidth()
	{
		return borderWidth;
	}

    /**
     * Sets the scalebar offset from the viewport border.
     *
     * @param borderWidth the number of pixels to offset the scalebar from the borders indicated by {@link
     * #setPosition(String)}.
     */
	public void setBorderWidth(int borderWidth)
	{
		this.borderWidth = borderWidth;
	}

	public String getUnit()
    {
		return this.unit;
	}

    /**
     * Sets the unit the scalebar uses to display distances.
     * Can be one of {@link #UNIT_METRIC} (the default),
     * or {@link #UNIT_IMPERIAL}.
     *
     * @param unit the desired unit
     */
	public void setUnit(String unit)
    {
		this.unit = unit;
	}

    /**
     * Get the scalebar legend Fon
     * @return the scalebar legend Font
     */
    public Font getFont()
    {
		return this.defaultFont;
	}

    /**
     * Set the scalebar legend Fon
     * @param font the scalebar legend Font
     */
    public void setFont(Font font)
    {
        if (font == null)
        {
            String msg = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
		this.defaultFont = font;
	}

    // Rendering
    @Override
    public void doRender(DrawContext dc)
    {
        dc.addOrderedRenderable(this.orderedImage);
    }

	// Rendering
	public void draw(DrawContext dc)
	{
		GL gl = dc.getGL();

		boolean attribsPushed = false;
		boolean modelviewPushed = false;
		boolean projectionPushed = false;

		try
		{
			gl.glPushAttrib(GL.GL_DEPTH_BUFFER_BIT
					| GL.GL_COLOR_BUFFER_BIT
					| GL.GL_ENABLE_BIT
					| GL.GL_TEXTURE_BIT
					| GL.GL_TRANSFORM_BIT
					| GL.GL_VIEWPORT_BIT
					| GL.GL_CURRENT_BIT);
			attribsPushed = true;

			gl.glDisable(GL.GL_TEXTURE_2D);		// no textures

			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glDisable(GL.GL_DEPTH_TEST);

			double width = this.size.width;
			double height = this.size.height;

			// Load a parallel projection with xy dimensions (viewportWidth, viewportHeight)
			// into the GL projection matrix.
			java.awt.Rectangle viewport = dc.getView().getViewport();
			gl.glMatrixMode(javax.media.opengl.GL.GL_PROJECTION);
			gl.glPushMatrix();
			projectionPushed = true;
			gl.glLoadIdentity();
			double maxwh = width > height ? width : height;
			gl.glOrtho(0d, viewport.width, 0d, viewport.height, -0.6 * maxwh, 0.6 * maxwh);

			gl.glMatrixMode(GL.GL_MODELVIEW);
			gl.glPushMatrix();
			modelviewPushed = true;
			gl.glLoadIdentity();

			// Scale to a width x height space
			// located at the proper position on screen
			double scale = this.computeScale(viewport);
			Vec4 locationSW = this.computeLocation(viewport, scale);
			gl.glTranslated(locationSW.x(), locationSW.y(), locationSW.z());
			gl.glScaled(scale, scale, 1);

            // Compute scale size in real world
            Position groundPos = this.computeGroundPosition(dc, dc.getView());
            if(groundPos != null)
            {
                Vec4 groundTarget = dc.getGlobe().computePointFromPosition(groundPos);
                Double distance = dc.getView().getEyePoint().distanceTo3(groundTarget);
                Double pixelSize = dc.getView().computePixelSizeAtDistance(distance);
                Double scaleSize = pixelSize * width * scale;  // meter
                String unitLabel = "m";
                if(this.unit.equals(UNIT_METRIC)) {
                    if(scaleSize > 10000) {
                        scaleSize /= 1000;
                        unitLabel = "Km";
                    }
                } else if(this.unit.equals(UNIT_IMPERIAL)) {
                    scaleSize *= 3.280839895; // feet
                    unitLabel = "ft";
                    if(scaleSize > 5280) {
                        scaleSize /= 5280;
                        unitLabel = "mile(s)";
                    }
                }

                // Rounded division size
                int pot =  (int)Math.floor(Math.log10(scaleSize));
                int digit = Integer.parseInt(scaleSize.toString().substring(0, 1));
                double divSize = digit * Math.pow(10, pot);
                if(digit >= 5)
                    divSize = 5 * Math.pow(10, pot);
                else if (digit >= 2)
                    divSize = 2 * Math.pow(10, pot);
                double divWidth = width * divSize / scaleSize;

                // Draw scale
                // Set color using current layer opacity
                Color backColor = this.getBackgroundColor(this.color);
                float[] colorRGB = backColor.getRGBColorComponents(null);
                gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], (double)backColor.getAlpha() / 255d * this.getOpacity());
                gl.glTranslated((width - divWidth) / 2, 0d, 0d);
                this.drawScale(dc, divWidth, height);

                colorRGB = this.color.getRGBColorComponents(null);
                gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], this.getOpacity());
                gl.glTranslated(-1d / scale, 1d / scale, 0d);
                this.drawScale(dc, divWidth, height);

                // Draw label
                String label = String.format("%.0f ", divSize) + unitLabel;
                gl.glLoadIdentity();
                gl.glDisable(GL.GL_CULL_FACE);
                drawLabel(label,
                        locationSW.add3(new Vec4(divWidth * scale / 2 + (width - divWidth) / 2, height * scale, 0)));

            }
        }
		finally
		{
			if (projectionPushed)
			{
				gl.glMatrixMode(GL.GL_PROJECTION);
				gl.glPopMatrix();
			}
			if (modelviewPushed)
			{
				gl.glMatrixMode(GL.GL_MODELVIEW);
				gl.glPopMatrix();
			}
			if (attribsPushed)
				gl.glPopAttrib();
		}
	}

    // Draw scale graphic
    private void drawScale(DrawContext dc, double width, double height)
    {
        GL gl = dc.getGL();
        gl.glBegin(GL.GL_LINE_STRIP);
        gl.glVertex3d(0, height ,0);
        gl.glVertex3d(0, 0 ,0);
        gl.glVertex3d(width, 0 ,0);
        gl.glVertex3d(width, height ,0);
        gl.glEnd();
        gl.glBegin(GL.GL_LINE_STRIP);
        gl.glVertex3d(width / 2, 0 ,0);
        gl.glVertex3d(width / 2, height / 2 ,0);
        gl.glEnd();
    }

    // Draw the scale label
	private void drawLabel(String text, Vec4 screenPoint)
    {
		if (this.textRenderer == null) {
			this.textRenderer =  new TextRenderer(this.defaultFont, true, true);
		}

		Rectangle2D nameBound = this.textRenderer.getBounds(text);
		int x = (int) (screenPoint.x() - nameBound.getWidth() / 2d);
		int y = (int) screenPoint.y();

		this.textRenderer.begin3DRendering();

        this.textRenderer.setColor(this.getBackgroundColor(this.color));
        this.textRenderer.draw(text, x + 1, y - 1);
		this.textRenderer.setColor(this.color);
		this.textRenderer.draw(text, x, y);

		this.textRenderer.end3DRendering();

	}

	private final float[] compArray = new float[4];    
    // Compute background color for best contrast
    private Color getBackgroundColor(Color color)
    {
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), compArray);
        if (compArray[2] > 0.5)
            return new Color(0, 0, 0, 0.7f);
        else
            return new Color(1, 1, 1, 0.7f);
    }

	private double computeScale(java.awt.Rectangle viewport)
	{
		if (this.resizeBehavior.equals(RESIZE_SHRINK_ONLY))
		{
			return Math.min(1d, (this.toViewportScale) * viewport.width / this.size.width);
		}
		else if (this.resizeBehavior.equals(RESIZE_STRETCH))
		{
			return (this.toViewportScale) * viewport.width / this.size.width;
		}
		else if (this.resizeBehavior.equals(RESIZE_KEEP_FIXED_SIZE))
		{
			return 1d;
		}
		else
		{
			return 1d;
		}
	}

	private Vec4 computeLocation(java.awt.Rectangle viewport, double scale)
	{
		double scaledWidth = scale * this.size.width;
		double scaledHeight = scale * this.size.height;

		double x;
		double y;

		if (this.locationCenter != null)
		{
			x = viewport.getWidth() - scaledWidth / 2 - this.borderWidth;
			y = viewport.getHeight() - scaledHeight / 2 - this.borderWidth;
		}
		else if (this.position.equals(NORTHEAST))
		{
			x = viewport.getWidth() - scaledWidth - this.borderWidth;
			y = viewport.getHeight() - scaledHeight - this.borderWidth;
		}
		else if (this.position.equals(SOUTHEAST))
		{
			x = viewport.getWidth() - scaledWidth - this.borderWidth;
			y = 0d + this.borderWidth;
		}
		else if (this.position.equals(NORTHWEST))
		{
			x = 0d + this.borderWidth;
			y = viewport.getHeight() - scaledHeight - this.borderWidth;
		}
		else if (this.position.equals(SOUTHWEST))
		{
			x = 0d + this.borderWidth;
			y = 0d + this.borderWidth;
		}
		else // use North East
		{
			x = viewport.getWidth() - scaledWidth / 2 - this.borderWidth;
			y = viewport.getHeight() - scaledHeight / 2 - this.borderWidth;
		}

		return new Vec4(x, y, 0);
	}

    private Position computeGroundPosition(DrawContext dc, View view)
    {
        if (view == null)
            return null;

        Position groundPos = view.computePositionFromScreenPoint(
                view.getViewport().getWidth() / 2, view.getViewport().getHeight() / 2);
        if (groundPos == null)
            return null;

        double elevation = dc.getGlobe().getElevation(groundPos.getLatitude(), groundPos.getLongitude());
        return new Position(
                groundPos.getLatitude(),
                groundPos.getLongitude(),
                elevation * dc.getVerticalExaggeration());
    }

    public void dispose()
    {
        if (this.textRenderer != null)
        {
            this.textRenderer.dispose();
            this.textRenderer = null;
        }
    }

	@Override
	public String toString()
	{
		return this.getName();
	}

}	
