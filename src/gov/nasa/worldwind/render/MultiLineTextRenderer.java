/*
Copyright (C) 2001, 2006, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import com.sun.opengl.util.j2d.TextRenderer;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.avlist.AVKey;

import javax.media.opengl.GL;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.util.ArrayList;

    /**
     * Multi line, rectangle bound text renderer with (very) minimal html support.
     *<p>
     * The {@link MultiLineTextRenderer} (MLTR) handles wrapping, measuring and drawing
     * of multiline text strings using Sun's JOGL {@link TextRenderer}.
     *</p>
     * <p>
     * A multiline text string is a character string containing new line characters
     * in between lines.
     * </p>
     * <p>
     * MLTR can handle both regular text with new line seprators and a very minimal
     * implementation of HTML. Each type of text has its own methods though.
     *</p>
     *
     * <p><b>Usage:</b></p>
     *
     * <p>Instantiation:</p>
     * <p>
     * The MLTR needs a Font or a TextRenderer to be instanciated. This will be
     * the font used for text drawing, wrapping and measuring. For HTML methods
     * this font will be considered as the document default font.
     * </p>
     * <pre>
     * Font font = Font.decode("Arial-PLAIN-12");
     * MultiLineTextRenderer mltr = new MultiLineTextRenderer(font);
     * </pre>
     * or
     * <pre>
     * TextRenderer tr = new TextRenderer(Font.decode("Arial-PLAIN-10"));
     * MultiLineTextRenderer mltr = new MultiLineTextRenderer(tr);
     * </pre>
     *
     * <p>Drawing regular text:</p>
     * <pre>
     * String text = "Line one.\nLine two.\nLine three...";
     * int x = 10;             // Upper left corner of text rectangle.
     * int y = 200;            // Origin at bottom left of screen.
     * int lineHeight = 14;    // Line height in pixels.
     * Color color = Color.RED;
     *
     * mltr.setTextColor(color);
     * mltr.getTextRenderer().begin3DRendering();
     * mltr.draw(text, x, y, lineHeight);
     * mltr.getTextRenderer().end3DRendering();
     * </pre>
     *
     * <p>Wrapping text to fit inside a width and optionaly a height</p>
     * <p>
     * The MLTR wrap method will insert new line characters inside the text so that
     * it fits a given width in pixels.
     * </p>
     * <p>
     * If a height dimension above zero is specified too, the text will be truncated
     * if needed, and a continuation string will be appended to the last line. The
     * continuation string can be set with mltr.setContinuationString();
     * </p>
     * <pre>
     * // Fit inside 300 pixels, no height constraint
     * String wrappedText = mltr.wrap(text, new Dimension(300, 0));
     *
     * // Fit inside 300x400 pixels, text may be truncated
     * String wrappedText = mltr.wrap(text, new Dimension(300, 400));
     * </pre>
     *
     * <p>Measuring text</p>
     * <pre>
     * Rectangle2D textBounds = mltr.getBounds(text);
     * </pre>
     * <p>
     * The textBounds rectangle returned contains the width and height of the text
     * as it would be drawn with the current font.
     * </p>
     * <p>
     * Note that textBounds.minX is the number of lines found and textBounds.minY
     * is the maximum line height for the font used. This value can be safely used
     * as the lineHeight argument when drawing - or can even be ommited after a
     * getBounds: draw(text, x, y);
     * ...
     * </p>
     *
     * <p><b>HTML support</b></p>
     * <p>
     * Supported tags are:
     * <ul>
     * <li>&lt;p&gt;&lt;/p&gt;, &lt;br&gt; &lt;br /&gt;</li>
     * <li>&lt;b&gt;&lt;/b&gt;</li>
     * <li>&lt;i&gt;&lt;/i&gt;</li>
     * <li>&lt;a href="..."&gt;&lt;/a&gt;</li>
     * <li>&lt;font color="#ffffff"&gt;&lt;/font&gt;</li>
     * </ul>
     * </p>
     * ...
     *
     *
     * 
     * <p>
     * See {@link AbstractAnnotation}.drawAnnotation() for more usage details.
     * </p>
     *
     * @author: Patrick Murris
     * @version $Id$
     */
    public class MultiLineTextRenderer
    {
        public final static int ALIGN_LEFT = 0;
        public final static int ALIGN_CENTER = 1;
        public final static int ALIGN_RIGHT = 2;

        public static final String EFFECT_NONE = "render.MultiLineTextRenderer.EffectNone";
        public static final String EFFECT_SHADOW = "render.MultiLineTextRenderer.EffectShadow";
        public static final String EFFECT_OUTLINE = "render.MultiLineTextRenderer.EffectOutline";

        private TextRenderer textRenderer;
        private int lineSpacing = 0;            // Inter line spacing in pixels
        private int lineHeight = 14;            // Will be set by getBounds() or by application
        private int textAlign = ALIGN_LEFT;     // Text alignement
        private String continuationString = "...";
        private Color textColor = Color.DARK_GRAY;
        private Color backColor = Color.LIGHT_GRAY;
        private Color linkColor = Color.BLUE;

        public MultiLineTextRenderer(TextRenderer textRenderer)
        {
            if(textRenderer == null)
            {
                String msg = Logging.getMessage("nullValue.TextRendererIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }
            this.textRenderer = textRenderer;
        }

        public MultiLineTextRenderer(Font font)
        {
            if(font == null)
            {
                String msg = Logging.getMessage("nullValue.FontIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }
            this.textRenderer = new TextRenderer(font, true, true);
        }

        /**
         * Get the current TextRenderer.
         * @return the current TextRenderer.
         */
        public TextRenderer getTextRenderer()
        {
            return this.textRenderer;
        }

        /**
         * Get the current line spacing height in pixels.
         * @return the current line spacing height in pixels.
         */
        public int getLineSpacing()
        {
            return this.lineSpacing;
        }

        /**
         * Set the current line spacing height in pixels.
         * @param height the line spacing height in pixels.
         */
        public void setLineSpacing(int height)
        {
            this.lineSpacing = height;
        }

        /**
         * Get the current line height in pixels.
         * @return the current line height in pixels.
         */
        public int getLineHeight()
        {
            return this.lineHeight;
        }

        /**
         * Set the current line height in pixels.
         * @param height the current line height in pixels.
         */
        public void setLineHeight(int height)
        {
            this.lineHeight = height;
        }

        /**
         * Get the current text alignment. Can be one of {@link #ALIGN_LEFT} the default,
         * {@link #ALIGN_CENTER} or {@link #ALIGN_RIGHT}.
         * @return the current text alignment.
         */
        public int getTextAlign()
        {
            return this.textAlign;
        }

        /**
         * Set the current text alignment. Can be one of {@link #ALIGN_LEFT} the default,
         * {@link #ALIGN_CENTER} or {@link #ALIGN_RIGHT}.
         * @param align the current text alignment.
         */
        public void setTextAlign(int align)
        {
            this.textAlign = align;
        }

        /**
         * Get the current text color.
         * @return the current text color.
         */
        public Color getTextColor()
        {
            return this.textColor;
        }

        /**
         * Set the text renderer color.
         * @param color the color to use when drawing text.
         */
        public void setTextColor(Color color)
        {
            if(color != null)
            {
                this.textColor = color;
                this.textRenderer.setColor(color);
            }
        }

        /**
         * Get the background color used for EFFECT_SHADOW and EFFECT_OUTLINE.
         * @return the current background color used when drawing shadow or outline..
         */
        public Color getBackColor()
        {
            return this.backColor;
        }

        /**
         * Set the background color used for EFFECT_SHADOW and EFFECT_OUTLINE.
         * @param color the color to use when drawing shadow or outline.
         */
        public void setBackColor(Color color)
        {
            if(color != null)
            {
                this.backColor = color;
            }
        }

        /**
         * Get the current link color.
         * @return the current link color.
         */
        public Color getLinkColor()
        {
            return this.linkColor;
        }

        /**
         * Set the link color.
         * @param color the color to use when drawing hyperlinks.
         */
        public void setLinkColor(Color color)
        {
            if(color != null)
            {
                this.linkColor = color;
            }
        }

        /**
         * Set the character string appended at the end of text truncated during
         * a wrap operation when exceeding the given height limit.
         * @param s the continuation character string.
         */
        public void setContinuationString(String s)
        {
            this.continuationString = s;
        }

        /**
         * Get the maximum line height for the given text renderer.
         * @param tr the TextRenderer.
         * @return the maximum line height.
         */
        public double getMaxLineHeight(TextRenderer tr)
        {
            // Check underscore + capital E with acute accent
            return tr.getBounds("_\u00c9").getHeight();
        }
        

        //** Plain text support ******************************************************
        //****************************************************************************

        /**
         * Returns the bounding rectangle for a multi-line string.
         * Note that the X component of the rectangle is the number of lines found in the text
         * and the Y component of the rectangle is the max line height encountered.
         * Note too that this method will automatically set the current line height to the max height found.
         * @param text the multi-line text to evaluate.
         * @return the bounding rectangle for the string.
         */
        public Rectangle getBounds(String text)
        {
            int width = 0;
            int maxLineHeight = 0;
            String[] lines = text.split("\n");
            for(int i = 0; i < lines.length; i++)
            {
                Rectangle2D lineBounds = this.textRenderer.getBounds(lines[i]);
                width = (int)Math.max(lineBounds.getWidth(), width);
                maxLineHeight = (int)Math.max(lineBounds.getHeight(), lineHeight);
            }
            // Make sure we have the highest line height
            maxLineHeight = (int)Math.max(getMaxLineHeight(this.textRenderer), maxLineHeight);
            // Set current line height for future draw
            this.lineHeight = maxLineHeight;
            // Compute final height using maxLineHeight and number of lines
            return new Rectangle(lines.length, lineHeight, width,
                    lines.length * maxLineHeight + (lines.length - 1) * this.lineSpacing);
        }

        /**
         * Draw a multi-line text string with bounding rectangle top starting at the y position.
         * Depending on the current textAlign, the x position is either the rectangle left side,
         * middle or right side.
         * Uses the current line height.
         * @param text the multi-line text to draw.
         * @param x the x position for top left corner of text rectangle.
         * @param y the y position for top left corner of the text rectangle.
         */
        public void draw(String text, int x, int y)
        {
            this.draw(text, x, y, this.lineHeight);
        }

        public void draw(String text, int x, int y, String effect)
        {
            this.draw(text, x, y, this.lineHeight, effect);
        }

        public void draw(String text, int x, int y,  int textLineHeight, String effect)
        {
            if (effect.compareToIgnoreCase(EFFECT_SHADOW) == 0)
            {
                this.textRenderer.setColor(backColor);
                this.draw(text, x + 1, y - 1, textLineHeight);
                this.textRenderer.setColor(textColor);
            }
            else if (effect.compareToIgnoreCase(EFFECT_OUTLINE) == 0)
            {
                this.textRenderer.setColor(backColor);
                this.draw(text, x, y + 1, textLineHeight);
                this.draw(text, x + 1, y, textLineHeight);
                this.draw(text, x, y - 1, textLineHeight);
                this.draw(text, x - 1, y, textLineHeight);
                this.textRenderer.setColor(textColor);
            }
            // Draw normal text
            this.draw(text, x, y, textLineHeight);
        }

        /**
         * Draw a multi-line text string with bounding rectangle top starting at the y position.
         * Depending on the current textAlign, the x position is either the rectangle left side,
         * middle or right side.
         * Uses the given line height.
         * @param text the multi-line text to draw.
         * @param x the x position for top left corner of text rectangle.
         * @param y the y position for top left corner of the text rectangle.
         * @param textLineHeight the line height in pixels.
         */
        public void draw(String text, int x, int y, int textLineHeight)
        {
            String[] lines = text.split("\n");
            for(int i = 0; i < lines.length; i++)
            {
                int xAligned = x;
                if(this.textAlign == ALIGN_CENTER)
                    xAligned = x - (int)(this.textRenderer.getBounds(lines[i]).getWidth() / 2);
                else if(this.textAlign == ALIGN_RIGHT)
                    xAligned = x - (int)(this.textRenderer.getBounds(lines[i]).getWidth());
                y -= textLineHeight;
                this.textRenderer.draw(lines[i], xAligned, y);
                y -= this.lineSpacing;
            }
        }

        /**
         * Draw text with unique colors word bounding rectangles and add each as a pickable object
         * to the provided PickSupport instance.
         * @param text the multi-line text to draw.
         * @param x the x position for top left corner of text rectangle.
         * @param y the y position for top left corner of the text rectangle.
         * @param textLineHeight the line height in pixels.
         * @param dc the current DrawContext.
         * @param pickSupport the PickSupport instance to be used.
         * @param refObject the user reference object associated with every picked word.
         * @param refPosition the user reference Position associated with every picked word.
         */
        public void pick(String text, int x, int y, int textLineHeight,
                         DrawContext dc, PickSupport pickSupport, Object refObject, Position refPosition)
        {
            String[] lines = text.split("\n");
            for(int i = 0; i < lines.length; i++)
            {
                int xAligned = x;
                if(this.textAlign == ALIGN_CENTER)
                    xAligned = x - (int)(this.textRenderer.getBounds(lines[i]).getWidth() / 2);
                else if(this.textAlign == ALIGN_RIGHT)
                    xAligned = x - (int)(this.textRenderer.getBounds(lines[i]).getWidth());
                y -= textLineHeight;
                drawLineWithUniqueColors(lines[i], xAligned, y, dc, pickSupport, refObject, refPosition);
                y -= this.lineSpacing;
            }
        }

        private void drawLineWithUniqueColors(String text, int x, int y,
                          DrawContext dc, PickSupport pickSupport, Object refObject, Position refPosition)
        {
            float spaceWidth = this.textRenderer.getCharWidth(' ');
            float drawX = x;
            float drawY = y;
            String source = text.trim();
            int start = 0;
            int end = source.indexOf(' ', start + 1);
            while(start < source.length())
            {
                if(end == -1)
                    end = source.length();   // last word
                // Extract a 'word' which is in fact a space and a word except for first word
                String word = source.substring(start, end);
                // Measure word and already draw line part - from line beginning
                Rectangle2D wordBounds = this.textRenderer.getBounds(word);
                Rectangle2D drawnBounds = this.textRenderer.getBounds(source.substring(0, start));
                float space = word.charAt(0) == ' ' ? spaceWidth : 0f;
                drawX = x + (start > 0 ? (float)drawnBounds.getWidth() + (float)drawnBounds.getX() : 0);
                // Add pickable object
                Color color = dc.getUniquePickColor();
                int colorCode = color.getRGB();
                PickedObject po = new PickedObject(colorCode, refObject, refPosition, false);
                po.setValue(AVKey.TEXT, word.trim());
                pickSupport.addPickableObject(colorCode, po);
                // Draw word rectangle
                dc.getGL().glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
                drawFilledRectangle(dc, drawX + wordBounds.getX(), drawY - wordBounds.getHeight() - wordBounds.getY(),
                        wordBounds.getWidth(), wordBounds.getHeight());
                // Move forward in source string
                start = end;
                if(start < source.length() - 1)
                {
                    end = source.indexOf(' ', start + 1);
                }
            }
        }

        /**
         * Add 'new line' characters inside a string so that it's bounding rectangle
         * tries not to exceed the given dimension width.
         * If the dimension height is more than zero, the text will be truncated accordingly and
         * the continuation string will be appended to the last line.
         * Note that words will not be split and at least one word will be used per line
         * so the longest word defines the final width of the bounding rectangle.
         * Each line is trimmed of leading and trailing spaces.
         * @param text the text string to wrap
         * @param dimension the maximum dimension in pixels
         * @return the wrapped string
         */
        public String wrap(String text, Dimension dimension)
        {
            int width = (int)dimension.getWidth();
            int height = (int)dimension.getHeight();
            String[] lines = text.split("\n");
            StringBuffer wrappedText = new StringBuffer();
            // Wrap each line
            for(int i = 0; i < lines.length; i++)
            {
                lines[i] = this.wrapLine(lines[i], width);
            }
            // Concatenate all lines in one string with new line separators
            // between lines - not at the end
            // Checks for height limit.
            int currentHeight = 0;
            boolean heightExceeded = false;
            double maxLineHeight = getMaxLineHeight(this.textRenderer);
            for(int i = 0; i < lines.length && !heightExceeded; i++)
            {
                String[] subLines = lines[i].split("\n");
                for(int j = 0; j < subLines.length && !heightExceeded; j++)
                {
                    if(height <= 0 || currentHeight + maxLineHeight <= height)
                    {
                        wrappedText.append(subLines[j]);
                        currentHeight += maxLineHeight + this.lineSpacing;
                        if(j < subLines.length - 1)
                            wrappedText.append('\n');
                    }
                    else
                    {
                        heightExceeded = true;
                    }
                }
                if(i < lines.length - 1 && !heightExceeded)
                    wrappedText.append('\n');
            }
            // Add continuation string if text truncated
            if(heightExceeded)
            {
                if(wrappedText.length() > 0)
                    wrappedText.deleteCharAt(wrappedText.length() - 1); // Remove excess new line
                wrappedText.append(this.continuationString);
            }
            return wrappedText.toString();
        }

        // Wrap one line to fit the given width
        private String wrapLine(String text, int width)
        {
            StringBuffer wrappedText = new StringBuffer();
            // Single line - trim leading and trailing spaces
            String source = text.trim();
            Rectangle2D lineBounds = this.textRenderer.getBounds(source);
            if(lineBounds.getWidth() > width)
            {
                // Split single line to fit preferred width
                StringBuffer line = new StringBuffer();
                int start = 0;
                int end = source.indexOf(' ', start + 1);
                while(start < source.length())
                {
                    if(end == -1)
                        end = source.length();   // last word
                    // Extract a 'word' which is in fact a space and a word
                    String word = source.substring(start, end);
                    String linePlusWord = line + word;
                    if(this.textRenderer.getBounds(linePlusWord).getWidth() <= width)
                    {
                        // Keep adding to the current line
                        line.append(word);
                    }
                    else
                    {
                        // Width exceeded
                        if(line.length() != 0 )
                        {
                            // Finish current line and start new one
                            wrappedText.append(line);
                            wrappedText.append('\n');
                            line.delete(0, line.length());
                            line.append(word.trim());  // get read of leading space(s)
                        }
                        else
                        {
                            // Line is empty, force at least one word
                            line.append(word.trim());
                        }
                    }
                    // Move forward in source string
                    start = end;
                    if(start < source.length() - 1)
                    {
                        end = source.indexOf(' ', start + 1);
                    }
                }
                // Gather last line
                wrappedText.append(line);
            }
            else
            {
                // Line doesnt need to be wrapped
                wrappedText.append(source);
            }
            return wrappedText.toString();
        }

        //** Very very simple html support *******************************************
        // Handles <P></P>, <BR /> or <BR>, <B></B>, <I></I>, <A HREF="..."></A>
        // and <font color="#ffffff"></font>.
        //****************************************************************************


        /**
         * Return true if the text contains some sgml tags.
         * @param text The text string to evaluate.
         * @return true if the string contains sgml or html tags
         */
        public static boolean containsHTML(String text)
        {
            Pattern pattern =  Pattern.compile("<[^\\s].*?>");  // Match any sgml tag
            Matcher matcher = pattern.matcher(text);
            return matcher.find(); 
        }

        /**
         * Remove new line characters then replace BR and P tags with appropriate new lines
         * @param text The html text string to process.
         * @return The processed text string.
         */
        public static String processLineBreaksHTML(String text)
        {
            text = text.replaceAll("\n", ""); // Remove all new line characters
            text = text.replaceAll("(?i)<br\\s?.*?>", "\n"); // Replace <br ...> with one new line
            text = text.replaceAll("(?i)<p\\s?.*?>", ""); // Replace <p ...> with nothing
            text = text.replaceAll("(?i)</p>", "\n\n"); // Replace </p> with two new line
            return text;
        }

        /**
         * Remove all HTML tags from a text string.
         * @param text the string to filter.
         * @return the filtered string.
         */
        public static String removeTagsHTML(String text)
        {
            return text.replaceAll("<[^\\s].*?>", "");
        }

        /**
         * Extract an attribute value from a HTML tag string. The attribute is expected to be formed
         * on the pattern: name="...". Other variants will likely fail.
         * @param text the HTML tage string.
         * @param attributeName the attribute name.
         * @return the attribute value found. Null if empty or not found.
         */
        public static String getAttributeFromTagHTML(String text, String attributeName)
        {
            // Look for name="..." - will not work for other variants
            Pattern pattern =  Pattern.compile("(?i)" + attributeName.toLowerCase() + "=\"([^\"].*?)\"");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find())
                return matcher.group(1);

            return null;
        }

        /**
         * Returns the bounding rectangle for a multi-line html string.
         * Note that the X component of the rectangle is the number of lines found in the text
         * and the Y component of the rectangle is the average line height encountered.
         * @param text the multi-line html text to evaluate.
         * @param renderers A HashMap of fonts and shared text renderers.
         * @return the bounding rectangle for the rendered text.
         */
        public Rectangle2D getBoundsHTML(String text, TextRendererCache renderers)
        {
            DrawState ds = new DrawState(renderers, this.textRenderer.getFont(), null, this.textColor);
            return getBoundsHTML(text, renderers, ds);
        }

        /**
         * Returns the bounding rectangle for a multi-line html string.
         * Note that the X component of the rectangle is the number of lines found in the text
         * and the Y component of the rectangle is the average line height encountered.
         * @param text the multi-line html text to evaluate.
         * @param renderers A HashMap of fonts and shared text renderers.
         * @param dsCurrent The current DrawState.
         * @return the bounding rectangle for the rendered text.
         */
        public Rectangle2D getBoundsHTML(String text, TextRendererCache renderers,
                                         DrawState dsCurrent)
        {
            String regex = "(<[^\\s].*?>)|(\\s)"; // Find sgml tags or spaces
            Pattern pattern =  Pattern.compile(regex);

            // Use a copy of DrawState - do not alter original
            DrawState ds = new DrawState(dsCurrent);

            // Spilt string
            double width = 0;
            double height = 0;
            String[] lines = text.split("\n");
            StringBuffer linePart = new StringBuffer();
            for(int i = 0; i < lines.length; i++)
            {
                // Measure each line
                int start = 0;
                double lineWidth = 0;
                double maxLineHeight = getMaxLineHeight(ds.textRenderer);
                linePart.delete(0, linePart.length());
                Matcher matcher = pattern.matcher(lines[i]);
                while (matcher.find()) {
                    if(matcher.group().compareTo(" ") == 0)
                    {
                        // Space found, concatenate and keep going
                        linePart.append(lines[i].substring(start, matcher.start()));
                        start = matcher.start();               // move on
                    }
                    else
                    {
                        // Html tag found

                        // Process current line part and measure - use counterTrim() workaround
                        linePart.append(lines[i].substring(start, matcher.start()));
                        if(linePart.length() > 0)
                        {
                            Rectangle2D partBounds = ds.textRenderer.getBounds(counterTrim(linePart));
                            //Rectangle2D partBounds = currentTextRenderer.getBounds(linePart);
                            lineWidth += partBounds.getWidth() + partBounds.getX();
                            linePart.delete(0, linePart.length()); // clear part
                        }
                        start = matcher.end();                 // move on

                        // Process html tag and update draw attributes
                        ds.update(matcher.group(), false);

                        // Keep track of max line height
                        maxLineHeight = (int)Math.max(getMaxLineHeight(ds.textRenderer), maxLineHeight);

                    }
                }
                // Gather and measure end of line
                if(start < lines[i].length())
                {
                    linePart.append(lines[i].substring(start));
                    if(linePart.length() > 0)
                    {
                        //Rectangle2D partBounds = currentTextRenderer.getBounds(counterTrim(linePart));
                        Rectangle2D partBounds = ds.textRenderer.getBounds(linePart);
                        lineWidth += partBounds.getWidth() + partBounds.getX();
                        maxLineHeight = (int)Math.max(partBounds.getHeight(), maxLineHeight);
                    }
                }

                // Accumulate dimensions
                width = Math.max(width, lineWidth);
                height += maxLineHeight + this.lineSpacing;
            }

            height -= this.lineSpacing; // subtract last line spacing
            // Return bounds - Note that minX is the number of lines and minY is the line height average
            return new Rectangle(lines.length, (int)(height / lines.length),
                    (int)Math.round(width), (int)Math.round(height));
        }

        /**
         * Draw a multi-line html text string with bounding rectangle top starting at the y position. The x
         * position is eiher the rectangle left side, middle or right side depending on the current text alignement.
         * @param text the multi-line text to draw
         * @param x the x position for top left corner of text rectangle
         * @param y the y position for top left corner of the text rectangle
         * @param renderers A HashMap of fonts and shared text renderers.
         */
        public void drawHTML(String text, int x, int y, TextRendererCache renderers)
        {
            String regex = "(<[^\\s].*?>)|(\\s)"; // Find sgml tags or spaces
            Pattern pattern =  Pattern.compile(regex);

            // Draw attributes
            DrawState ds = new DrawState(renderers, this.textRenderer.getFont(), null, this.textColor);

            // Draw string
            int baseX = x;
            double drawY = y;
            ds.textRenderer.begin3DRendering();
            ds.textRenderer.setColor(this.textColor);
            String[] lines = text.split("\n");
            StringBuffer linePart = new StringBuffer();
            for(int i = 0; i < lines.length; i++)
            {
                // Set line start x
                double drawX = baseX;
                Rectangle2D lineBounds = getBoundsHTML(lines[i], renderers,  ds);
                if(this.textAlign == ALIGN_CENTER)
                    drawX = x - (int)(lineBounds.getWidth() / 2);
                else if(this.textAlign == ALIGN_RIGHT)
                    drawX = x - (int)(lineBounds.getWidth());
                // Skip line height
                drawY -= lineBounds.getHeight();

                // Draw one line
                int start = 0;
                linePart.delete(0, linePart.length());
                Matcher matcher = pattern.matcher(lines[i]);
                while (matcher.find()) {
                    if(matcher.group().compareTo(" ") == 0)
                    {
                        // Space found, concatenate and keep going
                        linePart.append(lines[i].substring(start, matcher.start()));
                        start = matcher.start();               // move on
                    }
                    else
                    {
                        // Html tag found

                        // Process current line part and draw
                        linePart.append(lines[i].substring(start, matcher.start()));
                        if(linePart.length() > 0)
                        {
                            // Draw
                            ds.textRenderer.draw(linePart, (int)Math.round(drawX), (int)Math.round(drawY));

                            // Move x - use antiTrim() workaround
                            Rectangle2D partBounds = ds.textRenderer.getBounds(counterTrim(linePart));
                            //Rectangle2D partBounds = currentTextRenderer.getBounds(linePart);
                            drawX += partBounds.getWidth() + partBounds.getX();

                            linePart.delete(0, linePart.length()); // clear part
                        }
                        start = matcher.end();                 // move on

                        // Process html tag and update draw attributes
                        ds.update(matcher.group(), true);

                    }
                }
                // Gather and draw end of line
                if(start < lines[i].length())
                {
                    linePart.append(lines[i].substring(start));
                    if(linePart.length() > 0)
                        ds.textRenderer.draw(linePart, (int)Math.round(drawX), (int)Math.round(drawY));
                }
                // Skip line spacing
                drawY -= this.lineSpacing;
            }
            ds.textRenderer.end3DRendering();
        }

        /**
         * Draw text with unique colors word bounding rectangles and add each as a pickable object
         * to the provided PickSupport instance.
         * @param text the multi-line text to draw.
         * @param x the x position for top left corner of text rectangle.
         * @param y the y position for top left corner of the text rectangle.
         * @param renderers A HashMap of fonts and shared text renderers.
         * @param dc the current DrawContext.
         * @param pickSupport the PickSupport instance to be used.
         * @param refObject the user reference object associated with every picked word.
         * @param refPosition the user reference Position associated with every picked word.
         */
        public void pickHTML(String text, int x, int y, TextRendererCache renderers,
                             DrawContext dc, PickSupport pickSupport, Object refObject, Position refPosition)
        {
            String regex = "(<[^\\s].*?>)|(\\s)"; // Find sgml tags or spaces
            Pattern pattern =  Pattern.compile(regex);

            // Draw attributes
            DrawState ds = new DrawState(renderers, this.textRenderer.getFont(), null, this.textColor);

            // Draw string
            double drawX = x;
            double drawY = y;
            String[] lines = text.split("\n");
            StringBuffer linePart = new StringBuffer();
            for(int i = 0; i < lines.length; i++)
            {
                // Set line start x
                double baseX = x;
                Rectangle2D lineBounds = getBoundsHTML(lines[i], renderers,  ds);
                if(this.textAlign == ALIGN_CENTER)
                    baseX = x - (int)(lineBounds.getWidth() / 2);
                else if(this.textAlign == ALIGN_RIGHT)
                    baseX = x - (int)(lineBounds.getWidth());
                // Skip line height
                drawY -= lineBounds.getHeight();

                // Save draw state at beginning of line and word
                DrawState dsLine = new DrawState(ds);
                DrawState dsWord = new DrawState(ds);

                // Draw one line
                int wordStart = -1;
                int start = 0;
                linePart.delete(0, linePart.length());
                Matcher matcher = pattern.matcher(lines[i]);
                while (matcher.find()) {
                    if(matcher.group().compareTo(" ") == 0)
                    {
                        // Space found - get and measure new word and already drawn part
                        String word = wordStart == -1 ? lines[i].substring(start, matcher.start())
                                : lines[i].substring(wordStart, matcher.start());
                        String drawn = wordStart == -1 ? lines[i].substring(0, start)
                                : lines[i].substring(0, wordStart);
                        Rectangle2D wordBounds = getBoundsHTML(word, renderers, dsWord);
                        Rectangle2D drawnBounds = getBoundsHTML(drawn, renderers, dsLine);

                        // get current hyperlink
                        String hyperlink = dsWord.getDrawAttributes().hyperlink != null ? dsWord.getDrawAttributes().hyperlink : ds.getDrawAttributes().hyperlink;
                        // Draw word bounding rectangle
                        drawX = baseX + (start > 0 ? (float)drawnBounds.getWidth() + (float)drawnBounds.getX() : 0);
                        pickWord( word, hyperlink, drawX, drawY, wordBounds, dc, pickSupport, refObject, refPosition);

                        // Save draw state for next word
                        dsWord = new DrawState(ds);

                        start = matcher.start();               // move on from space found
                        wordStart = -1;
                    }
                    else
                    {
                        // Html tag found
                        
                        wordStart = wordStart == -1 ? start : wordStart;
                        start = matcher.end();                 // move on from after tag

                        // Process html tag and update draw attributes
                        ds.update(matcher.group(), false);

                    }
                }
                // Gather and draw end of line
                if(start < lines[i].length() || wordStart != -1)
                {
                    String word = wordStart == -1 ? lines[i].substring(start) : lines[i].substring(wordStart);
                    String drawn = wordStart == -1 ? lines[i].substring(0, start) : lines[i].substring(0, wordStart);
                    Rectangle2D wordBounds = getBoundsHTML(word, renderers, dsWord);
                    Rectangle2D drawnBounds = getBoundsHTML(drawn, renderers, dsLine);

                    // get current hyperlink
                    String hyperlink = dsWord.getDrawAttributes().hyperlink != null ? dsWord.getDrawAttributes().hyperlink : ds.getDrawAttributes().hyperlink;
                    // Draw word bounding rectangle
                    drawX = baseX + (start > 0 ? (float)drawnBounds.getWidth() + (float)drawnBounds.getX() : 0);
                    pickWord( word, hyperlink, drawX, drawY, wordBounds, dc, pickSupport, refObject, refPosition);
                }
                // Skip line spacing
                drawY -= this.lineSpacing;
            }
        }

        private void pickWord(String word, String hyperlink, double drawX, double drawY, Rectangle2D wordBounds,
                              DrawContext dc, PickSupport pickSupport, Object refObject, Position refPosition)
        {
            // Add pickable object
            Color color = dc.getUniquePickColor();
            int colorCode = color.getRGB();
            PickedObject po = new PickedObject(colorCode, refObject, refPosition, false);
            po.setValue(AVKey.TEXT, removeTagsHTML(word.trim()));
            if(hyperlink != null)
                po.setValue(AVKey.URL, hyperlink);
            pickSupport.addPickableObject(colorCode, po);
            // Draw word rectangle
            dc.getGL().glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
            drawFilledRectangle(dc, drawX, drawY - wordBounds.getHeight() / 5,
                    wordBounds.getWidth(), wordBounds.getHeight());
        }


        /**
         * Add 'new line' characters inside an html text string so that it's bounding rectangle
         * tries not to exceed the given dimension width.
         * If the dimension height is more than zero, the text will be truncated accordingly and
         * the continuation string will be appended to the last line.
         * Note that words will not be split and at least one word will be used per line
         * so the longest word defines the final width of the bounding rectangle.
         * Each line is trimmed of leading and trailing spaces.
         * @param text the html text string to wrap
         * @param dimension the maximum dimension in pixels
         * @param renderers A HashMap of fonts and shared text renderers.
         * @return the wrapped html string
         */
        public String wrapHTML(String text, Dimension dimension, TextRendererCache renderers)
        {
            String regex = "(<[^\\s].*?>)|(\\s)"; // Find sgml tags or spaces
            Pattern pattern =  Pattern.compile(regex);

            // Draw attributes
            DrawState ds = new DrawState(renderers, this.textRenderer.getFont(), null, this.textColor);
            int width = (int)dimension.getWidth();
            int height = (int)dimension.getHeight();

            // Split string
            String[] lines = text.split("\n");
            StringBuffer wrappedText = new StringBuffer();
            int currentHeight = 0;
            int lineCount = 0;
            boolean heightExceeded = false;
            for(int i = 0; i < lines.length && !heightExceeded; i++)
            {
                // Single line - trim leading and trailing spaces
                String source = lines[i].trim();
                double maxLineHeight = getMaxLineHeight(ds.textRenderer);
                Rectangle2D lineBounds = getBoundsHTML(source, renderers, ds);
                if(lineBounds.getWidth() > width)
                {
                    // Split single line to fit preferred width

                    StringBuffer line = new StringBuffer();
                    double lineWidth = 0;
                    double wordWidth = 0;
                    int wordStart = -1;
                    int start = 0;
                    Matcher matcher = pattern.matcher(source);
                    while (matcher.find() && !heightExceeded)
                    {
                        if(matcher.group().compareTo(" ") == 0)
                        {
                            // Space found - check new word length and line total
                            String word = source.substring(start, matcher.start());
                            Rectangle2D wordBounds = getBoundsHTML(word, renderers, ds);
                            wordWidth += wordBounds.getWidth() + wordBounds.getX();
                            // If word already started earlier, gather the full word
                            if(wordStart != -1)
                                word = source.substring(wordStart, matcher.start());
                            if(lineWidth + wordWidth <= width)
                            {
                                // Keep adding to the current line
                                line.append(word);
                                lineWidth += wordWidth;
                            }
                            else
                            {
                                // Width exceeded
                                word = word.trim(); // get read of leading space(s)
                                wordBounds = getBoundsHTML(word, renderers, ds);
                                wordWidth = wordBounds.getWidth() + wordBounds.getX();
                                if(line.length() != 0 )
                                {
                                    // Finish current line and start new one
                                    if(height <= 0 || currentHeight + maxLineHeight <= height)
                                    {
                                        wrappedText.append(line);
                                        wrappedText.append('\n');
                                        currentHeight += maxLineHeight + this.lineSpacing;
                                        lineCount++;
                                        line.delete(0, line.length());
                                        line.append(word);
                                        lineWidth = wordWidth;
                                        // Keep track of max line height
                                        maxLineHeight = getMaxLineHeight(ds.textRenderer);
                                    }
                                    else
                                    {
                                        heightExceeded = true;
                                    }
                                }
                                else
                                {
                                    // Line is empty, force at least one word
                                    line.append(word);
                                    lineWidth = wordWidth;
                                }
                            }
                            // Move on from space found
                            start = matcher.start();
                            wordWidth = 0;
                            wordStart = -1;
                        }
                        else
                        {
                            // Html tag found

                            // Process line part and measure - use counterTrim() workaround
                            // Accumulate wordWidth and set wordStart to decide latter whether this is going on the current line
                            if(matcher.start() > start)
                            {
                                String word = source.substring(start, matcher.start());
                                Rectangle2D wordBounds = getBoundsHTML(counterTrim(word), renderers, ds);
                                wordWidth += wordBounds.getWidth() + wordBounds.getX();
                            }
                            wordStart = wordStart == -1 ? start : wordStart;
                            start = matcher.end(); // move on

                            // Process html tag and update draw attributes
                            ds.update(matcher.group(), false);
                            // Keep track of max line height
                            maxLineHeight = (int)Math.max(getMaxLineHeight(ds.textRenderer), maxLineHeight);
                        }
                    }
                    // Gather and measure end of line if any
                    if((start < source.length() || wordStart != -1)  && !heightExceeded)
                    {
                        String word = "";
                        if(start < source.length())
                        {
                            // Gather last bit and add to wordWidth
                            word = source.substring(start);
                            Rectangle2D wordBounds = getBoundsHTML(word, renderers, ds);
                            wordWidth += wordBounds.getWidth() + wordBounds.getX();
                        }
                        // If word already started earlier, gather the full word
                        if(wordStart != -1)
                            word = source.substring(wordStart);
                        if(lineWidth + wordWidth <= width)
                        {
                            // Keep adding to the current line
                            line.append(word);
                        }
                        else
                        {
                            // Width exceeded
                            word = word.trim(); // get read of leading space(s)
                            if(line.length() != 0 )
                            {
                                // Finish current line and start new one
                                if(height <= 0 || currentHeight + maxLineHeight <= height)
                                {
                                    wrappedText.append(line);
                                    wrappedText.append('\n');
                                    currentHeight += maxLineHeight + this.lineSpacing;
                                    lineCount++;
                                    line.delete(0, line.length());
                                    line.append(word);
                                    // Keep track of max line height
                                    maxLineHeight = getMaxLineHeight(ds.textRenderer);
                                }
                                else
                                {
                                    heightExceeded = true;
                                }
                            }
                            else
                            {
                                // Line is empty, force at least one word
                                line.append(word);
                            }
                        }
                        if(height <= 0 || currentHeight + maxLineHeight <= height)
                        {
                            wrappedText.append(line);
                            currentHeight += maxLineHeight + this.lineSpacing;
                            lineCount++;
                        }
                        else
                        {
                            heightExceeded = true;
                        }
                    }
                }
                else
                {
                    // line doesnt need to be wrapped
                    if(height <= 0 || currentHeight + maxLineHeight <= height)
                    {
                        wrappedText.append(source);
                        currentHeight += maxLineHeight + this.lineSpacing;
                        lineCount++;
                    }
                    else
                    {
                        heightExceeded = true;
                    }
                }
                // Add new line between lines - not after the last one.
                if(i < lines.length - 1 && !heightExceeded)
                    wrappedText.append('\n');
            }
            // Add continuation string if text truncated
            if(heightExceeded)
            {
                if(wrappedText.length() > 0)
                    wrappedText.deleteCharAt(wrappedText.length() - 1); // Remove excess new line
                wrappedText.append(this.continuationString);
            }
            return wrappedText.toString();
        }

        // Replace first leading space and last trailing space with the character 't'.
        // This is a workaround for TextRenderer.getBounds() which ignores leading and trailing spaces.
        private String counterTrim(StringBuffer s)
        {
            if(s.length() == 0)
                return "";

            StringBuffer sbOut = new StringBuffer(s);
            if(sbOut.substring(sbOut.length() - 1).compareTo(" ") == 0)
                sbOut.setCharAt(s.length() - 1, 't');  // use a 't' to fillup last space
            if(sbOut.substring(0, 1).compareTo(" ") == 0)
                sbOut.setCharAt(0, 't');  // use a 't' to fillup leading space

            return sbOut.toString();
        }

        private String counterTrim(String  s)
        {
            if(s.length() == 0)
                return "";

            StringBuffer sbOut = new StringBuffer(s);
            if(sbOut.substring(sbOut.length() - 1).compareTo(" ") == 0)
                sbOut.setCharAt(s.length() - 1, 't');  // use a 't' to fillup last space
            if(sbOut.substring(0, 1).compareTo(" ") == 0)
                sbOut.setCharAt(0, 't');  // use a 't' to fillup leading space

            return sbOut.toString();
        }

        // Draw a filled rectangle
        private void drawFilledRectangle(DrawContext dc, double x, double y, double width, double height)
        {
            GL gl = dc.getGL();
            gl.glBegin(GL.GL_POLYGON);
            gl.glVertex3d(x, y, 0);
            gl.glVertex3d(x + width - 1, y, 0);
            gl.glVertex3d(x + width - 1, y + height - 1, 0);
            gl.glVertex3d(x, y + height - 1, 0);
            gl.glVertex3d(x, y, 0);
            gl.glEnd();
        }

        private Color applyTextAlpha(Color color)
        {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() * textColor.getAlpha() / 255 );
        }

        // -- Draw state handling -----------------------------------

        private class DrawState
        {
            private class DrawAttributes
            {
                private final Font font;
                private final String hyperlink;
                private final Color color;

                public DrawAttributes(Font font, String hyperlink, Color color)
                {
                    this.font = font;
                    this.hyperlink = hyperlink;
                    this.color = color;
                }
            }

            private ArrayList<DrawAttributes> stack = new ArrayList<DrawAttributes>();
            private TextRendererCache renderers;
            public TextRenderer textRenderer;

            public DrawState(TextRendererCache renderers, Font font, String hyperlink, Color color)
            {
                this.stack.add(new DrawAttributes(font, hyperlink, color));
                this.renderers = renderers;
                this.textRenderer = getTextRenderer(font);
            }

            public DrawState(DrawState ds)
            {
                this.stack.addAll(ds.stack);
                this.renderers = ds.renderers;
                this.textRenderer = ds.textRenderer;
            }

            public DrawAttributes getDrawAttributes()
            {
                if (this.stack.size() < 1)
                    return null;
                return this.stack.get(this.stack.size() - 1);
            }

            private TextRenderer getTextRenderer(Font font)
            {
                TextRenderer tr = this.renderers.get(font);
                if(tr == null)
                {
                    tr = new TextRenderer(font, true, true);
                    renderers.add(font, tr);
                }
                return tr;
            }

            private Font getFont(Font font, boolean isBold, boolean isItalic)
            {
                int fontStyle = isBold ? (isItalic ? Font.BOLD | Font.ITALIC : Font.BOLD)
                        : (isItalic ? Font.ITALIC : Font.PLAIN);
                return font.deriveFont(fontStyle);
            }

            // Update DrawState from html tag
            public TextRenderer update(String tag, boolean startStopRendering)
            {
                DrawAttributes da = getDrawAttributes();
                boolean fontChanged = false;

                if(tag.compareToIgnoreCase("<b>") == 0)
                {
                    this.stack.add(new DrawAttributes(getFont(da.font, true, da.font.isItalic()), da.hyperlink, da.color));
                    fontChanged = true;
                }
                else if(tag.compareToIgnoreCase("</b>") == 0)
                {
                    if (this.stack.size() > 1)
                        this.stack.remove(this.stack.size() - 1);
                    fontChanged = true;
                }
                else if(tag.compareToIgnoreCase("<i>") == 0)
                {
                    this.stack.add(new DrawAttributes(getFont(da.font, da.font.isBold(), true), da.hyperlink, da.color));
                    fontChanged = true;
                }
                else if(tag.compareToIgnoreCase("</i>") == 0)
                {
                    if (this.stack.size() > 1)
                        this.stack.remove(this.stack.size() - 1);
                    fontChanged = true;
                }
                else if(tag.toLowerCase().startsWith("<a "))
                {
                    this.stack.add(new DrawAttributes(da.font, MultiLineTextRenderer.getAttributeFromTagHTML(tag, "href"), applyTextAlpha(linkColor)));
                    if(startStopRendering)
                        this.textRenderer.setColor(applyTextAlpha(linkColor));
                }
                else if(tag.compareToIgnoreCase("</a>") == 0)
                {
                    if (this.stack.size() > 1)
                        this.stack.remove(this.stack.size() - 1);
                    if(startStopRendering)
                        this.textRenderer.setColor(getDrawAttributes().color);
                }
                else if(tag.toLowerCase().startsWith("<font "))
                {
                    String colorCode = MultiLineTextRenderer.getAttributeFromTagHTML(tag, "color");
                    if (colorCode != null)
                    {
                        Color color = da.color;
                        try
                        {
                            color = applyTextAlpha(Color.decode(colorCode));
                        }
                        catch (Exception e) {}
                        this.stack.add(new DrawAttributes(da.font, da.hyperlink, color));
                        if(startStopRendering)
                            this.textRenderer.setColor(color);
                    }
                }
                else if(tag.compareToIgnoreCase("</font>") == 0)
                {
                    if (this.stack.size() > 1)
                        this.stack.remove(this.stack.size() - 1);
                    if(startStopRendering)
                        this.textRenderer.setColor(getDrawAttributes().color);
                }

                if(fontChanged)
                {
                    // Terminate current rendering
                    if(startStopRendering)
                        this.textRenderer.end3DRendering();
                    // Get new text renderer
                    da = getDrawAttributes();
                    this.textRenderer = getTextRenderer(da.font);
                    // Resume rendering
                    if(startStopRendering)
                    {
                        this.textRenderer.begin3DRendering();
                        this.textRenderer.setColor(da.color);
                    }
                }

                return this.textRenderer;
            }
        }


    }
