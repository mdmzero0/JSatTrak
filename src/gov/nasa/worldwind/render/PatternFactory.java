package gov.nasa.worldwind.render;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Static class to creates tilable patterns.
 * <p>
 * The <code>createPattern()</code> method draws a shape inside a usually square bitmap, so that it will match if tiled.
 * </p>
 * <p>
 * Each pattern supports a <code>scale</code> factor between <code>zero</code> and <code>one</code> - default is .5.
 * With a scale of <code>zero</code> no pattern will be produced. With a scale of <code>one</code> the pattern will
 * cover all the background.
 * </p>
 * @author Patrick Murris
 * @version $Id$
 */
public class PatternFactory {

    public final static String PATTERN_CIRCLE = "PatternFactory.PatternCircle";
    public final static String PATTERN_CIRCLES = "PatternFactory.PatternCircles";
    public final static String PATTERN_SQUARE = "PatternFactory.PatternSquare";
    public final static String PATTERN_SQUARES = "PatternFactory.PatternSquares";
    public final static String PATTERN_HLINE = "PatternFactory.PatternHLine";
    public final static String PATTERN_VLINE = "PatternFactory.PatternVLine";
    public final static String PATTERN_HVLINE = "PatternFactory.PatternHVLine";
    public final static String PATTERN_DIAGONAL_UP = "PatternFactory.PatternDiagonalUp";
    public final static String PATTERN_DIAGONAL_DOWN = "PatternFactory.PatternDiagonalDown";

    public final static String GRADIENT_HLINEAR = "PatternFactory.GradientHLinear";
    public final static String GRADIENT_VLINEAR = "PatternFactory.GradientVLinear";

    private static Dimension defaultDimension = new Dimension(32, 32);
    private static float defaultScale = .5f;
    private static Color defaultLineColor = Color.LIGHT_GRAY;
    private static Color defaultBackColor = new Color(0f, 0f, 0f, 0f);

    /**
     * Draws a pattern using the default scale (.5), bitmap dimensions (32x32) and colors (light grey over
     * a transparent background).
     * @param pattern the pattern to draw. See {@link PatternFactory} static constants.
     * @return the corresponding <code>BufferedImage</code>.
     */
    public static BufferedImage createPattern(String pattern)
    {
        return createPattern(pattern, defaultDimension, defaultScale, defaultLineColor, defaultBackColor);
    }

    /**
     * Draws a pattern with a given <code>Color</code> using the default scale (.5), bitmap dimensions (32x32)
     * and backgound color (transparent).
     * @param pattern the pattern to draw. See {@link PatternFactory} static constants.
     * @param lineColor the pattern <code>Color</code>.
     * @return the corresponding <code>BufferedImage</code>.
     */
    public static BufferedImage createPattern(String pattern, Color lineColor)
    {
        return createPattern(pattern, defaultDimension, defaultScale, lineColor, defaultBackColor);
    }

    /**
     * Draws a pattern with a given <code>scale</code> using the default bitmap dimensions (32x32) and colors
     * (light grey over a transparent background).
     * @param pattern the pattern to draw. See {@link PatternFactory} static constants.
     * @param scale the scale at which the pattern should be drawn (0 to 1).
     * @return the corresponding <code>BufferedImage</code>.
     */
    public static BufferedImage createPattern(String pattern, float scale)
    {
        return createPattern(pattern, defaultDimension, scale, defaultLineColor, defaultBackColor);
    }

    /**
     * Draws a pattern with a given <code>scale</code> and <code>Color</code> using the default bitmap
     * dimensions (32x32) and backgound color (transparent).
     * @param pattern the pattern to draw. See {@link PatternFactory} static constants.
     * @param scale the scale at which the pattern should be drawn (0 to 1).
     * @param lineColor the pattern <code>Color</code>.
     * @return the corresponding <code>BufferedImage</code>.
     */
    public static BufferedImage createPattern(String pattern, float scale, Color lineColor)
    {
        return createPattern(pattern, defaultDimension, scale, lineColor, defaultBackColor);
    }

    /**
     * Draws a pattern with a given <code>scale</code> and <code>Color</code>s using the default bitmap
     * dimensions (32x32).
     * @param pattern the pattern to draw. See {@link PatternFactory} static constants.
     * @param scale the scale at which the pattern should be drawn (0 to 1).
     * @param lineColor the pattern <code>Color</code>.
     * @param backColor the pattern background <code>Color</code>.
     * @return the corresponding <code>BufferedImage</code>.
     */
    public static BufferedImage createPattern(String pattern, float scale, Color lineColor, Color backColor)
    {
        return createPattern(pattern, defaultDimension, scale, lineColor, backColor);
    }

    /**
     * Draws a pattern with a given <code>scale</code>, <code>Color</code> and bitmap
     * dimensions, using the default backgound color (transparent).
     * @param pattern the pattern to draw. See {@link PatternFactory} static constants.
     * @param size the <code>Dimension</code> of the <code>BufferedImage produced</code>.
     * @param scale the scale at which the pattern should be drawn (0 to 1).
     * @param lineColor the pattern <code>Color</code>.
     * @return the corresponding <code>BufferedImage</code>.
     */
    public static BufferedImage createPattern(String pattern, Dimension size, float scale, Color lineColor)
    {
        return createPattern(pattern, size, scale, lineColor, defaultBackColor);
    }

    /**
     * Draws a pattern with the given <code>scale</code>, <code>Color</code>s and bitmap dimensions.
     * @param pattern the pattern to draw. See {@link PatternFactory} static constants.
     * @param size the <code>Dimension</code> of the <code>BufferedImage produced</code>.
     * @param scale the scale at which the pattern should be drawn (0 to 1).
     * @param lineColor the pattern <code>Color</code>.
     * @param backColor the pattern background <code>Color</code>.
     * @return the corresponding <code>BufferedImage</code>.
     */
    public static BufferedImage createPattern(String pattern, Dimension size, float scale, Color lineColor, Color backColor)
    {
        int halfWidth = size.width / 2;
        int halfHeight = size.height / 2;
        int dim = (int)(size.width * scale);
        BufferedImage image = new BufferedImage(size.width,  size.height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setPaint(backColor);
        g2.fillRect(0, 0, size.width, size.height);
        if (scale <= 0)
            return image;

        // Pattern
        g2.setPaint(lineColor);
        g2.setStroke(new BasicStroke(dim));
        if (pattern.compareTo(PATTERN_HLINE) == 0)
        {
            int y = halfHeight - 1 - dim / 2;
            g2.fillRect(0, y, size.width, dim);
        }
        else if (pattern.compareTo(PATTERN_VLINE) == 0)
        {
            int x = halfWidth - 1 - dim / 2;
            g2.fillRect(x, 0, dim, size.height);
        }
        if (pattern.compareTo(PATTERN_HVLINE) == 0)
        {
            int x = halfWidth - 1 - dim / 2;
            g2.fillRect(x, 0, dim, size.height);
            int y = halfHeight - 1 - dim / 2;
            g2.fillRect(0, y, size.width, dim);
        }
        else if (pattern.compareTo(PATTERN_SQUARE) == 0)
        {
            int x = halfWidth - 1 - dim / 2;
            int y = halfHeight - 1 - dim / 2;
            g2.fillRect(x, y, dim, dim);
        }
        else if (pattern.compareTo(PATTERN_SQUARES) == 0)
        {
            int x = halfWidth - 1 - dim / 2;
            int y = halfHeight - 1 - dim / 2;
            g2.fillRect(x, y, dim, dim);
            g2.fillRect(x - halfWidth, y - halfHeight, dim, dim);
            g2.fillRect(x - halfWidth, y + halfHeight, dim, dim);
            g2.fillRect(x + halfWidth, y - halfHeight, dim, dim);
            g2.fillRect(x + halfWidth, y + halfHeight, dim, dim);
        }
        else if (pattern.compareTo(PATTERN_CIRCLE) == 0)
        {
            int x = halfWidth - 1 - dim / 2;
            int y = halfHeight - 1 - dim / 2;
            g2.fillOval(x, y, dim, dim);
        }
        else if (pattern.compareTo(PATTERN_CIRCLES) == 0)
        {
            int x = halfWidth - 1 - dim / 2;
            int y = halfHeight - 1 - dim / 2;
            g2.fillOval(x, y, dim, dim);
            g2.fillOval(x - halfWidth, y - halfHeight, dim, dim);
            g2.fillOval(x - halfWidth, y + halfHeight, dim, dim);
            g2.fillOval(x + halfWidth, y - halfHeight, dim, dim);
            g2.fillOval(x + halfWidth, y + halfHeight, dim, dim);
        }
        else if (pattern.compareTo(PATTERN_DIAGONAL_UP) == 0 || pattern.compareTo(PATTERN_DIAGONAL_DOWN) == 0)
        {
            if (pattern.compareTo(PATTERN_DIAGONAL_DOWN) == 0)
            {
                AffineTransform at = AffineTransform.getScaleInstance(-1, 1);
                at.translate(-size.width, 0);
                g2.setTransform(at);
            }
            g2.drawLine(-dim, size.height - 1 + dim, size.width - 1 + dim, - dim);
            g2.drawLine(-dim - 1, dim, dim - 1, - dim);
            g2.drawLine(size.width - dim, size.height - 1 + dim, size.width + dim, size.height - 1 - dim);
        }
        else if (pattern.compareTo(GRADIENT_VLINEAR) == 0)
        {
            g2.setPaint(new GradientPaint((float)halfWidth, 0f, lineColor, (float)halfWidth, (float)size.height - 1, backColor));
            g2.fillRect(0, 0, size.width, size.height);
        }
        else if (pattern.compareTo(GRADIENT_HLINEAR) == 0)
        {
            g2.setPaint(new GradientPaint(0f, halfHeight, lineColor, (float)size.width - 1, halfHeight, backColor));
            g2.fillRect(0, 0, size.width, size.height);
        }

        return image;
    }
}
