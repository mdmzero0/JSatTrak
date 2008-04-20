/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * @author tag
 * @version $Id$
 */
public class UserFacingIcon extends AVListImpl implements WWIcon, Movable
{
    //    private final String iconPath;
    private Position iconPosition; // may be null because placement may be relative
    private Dimension iconSize; // may be null to indicate "use native image size"
    private boolean isHighlighted = false;
    private boolean isVisible = true;
    private double highlightScale = 1.2; // TODO: make configurable
    private String toolTipText;
    private Font toolTipFont;
    private boolean showToolTip = false;
    private boolean alwaysOnTop = false;
    private java.awt.Color textColor;
    private Object imageSource;

    public UserFacingIcon(String iconPath, Position iconPosition)
    {
        if (iconPath == null)
        {
            String message = Logging.getMessage("nullValue.IconFilePath");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.imageSource = iconPath;
        this.iconPosition = iconPosition;
    }

    public UserFacingIcon(Object imageSource, Position iconPosition)
    {
        if (imageSource == null)
        {
            String message = Logging.getMessage("nullValue.IconFilePath");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.imageSource = imageSource;
        this.iconPosition = iconPosition;
    }

    public Object getImageSource()
    {
        return imageSource;
    }

    public void setImageSource(Object imageSource)
    {
        this.imageSource = imageSource;
    }

    public String getPath()
    {
        return this.imageSource instanceof String ? (String) this.imageSource : null;
    }

    public Position getPosition()
    {
        return this.iconPosition;
    }

    public void setPosition(Position iconPosition)
    {
        this.iconPosition = iconPosition;
    }

    public boolean isHighlighted()
    {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted)
    {
        isHighlighted = highlighted;
    }

    public double getHighlightScale()
    {
        return highlightScale;
    }

    public void setHighlightScale(double highlightScale)
    {
        this.highlightScale = highlightScale;
    }

    public Dimension getSize()
    {
        return this.iconSize;
    }

    public void setSize(Dimension size)
    {
        this.iconSize = size;
    }

    public boolean isVisible()
    {
        return isVisible;
    }

    public void setVisible(boolean visible)
    {
        isVisible = visible;
    }

    public String getToolTipText()
    {
        return toolTipText;
    }

    public void setToolTipText(String toolTipText)
    {
        this.toolTipText = toolTipText;
    }

    public Font getToolTipFont()
    {
        return toolTipFont;
    }

    public void setToolTipFont(Font toolTipFont)
    {
        this.toolTipFont = toolTipFont;
    }

    public boolean isShowToolTip()
    {
        return showToolTip;
    }

    public void setShowToolTip(boolean showToolTip)
    {
        this.showToolTip = showToolTip;
    }

    public Color getToolTipTextColor()
    {
        return textColor;
    }

    public void setToolTipTextColor(Color textColor)
    {
        this.textColor = textColor;
    }

    public boolean isAlwaysOnTop()
    {
        return alwaysOnTop;
    }

    public void setAlwaysOnTop(boolean alwaysOnTop)
    {
        this.alwaysOnTop = alwaysOnTop;
    }

    public void move(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.iconPosition = this.iconPosition.add(position);
    }

    public void moveTo(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.iconPosition = position;
    }

    public Position getReferencePosition()
    {
        return this.iconPosition;
    }

    public String toString()
    {
        return this.imageSource != null ? this.imageSource.toString() : this.getClass().getName();
    }
}
