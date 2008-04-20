/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Position;

import java.awt.*;

/**
 * @author tag
 * @version $Id$
 */
public interface WWIcon // extends gov.nasa.worldwind.AVList
{
    void setImageSource(Object imageSource);
    
    Object getImageSource();

    Position getPosition();

    void setPosition(Position iconPosition);

    boolean isHighlighted();

    void setHighlighted(boolean highlighted);

    Dimension getSize();

    void setSize(Dimension size);

    boolean isVisible();

    void setVisible(boolean visible);

    double getHighlightScale();

    void setHighlightScale(double highlightScale);

    String getToolTipText();

    void setToolTipText(String toolTipText);

    Font getToolTipFont();

    void setToolTipFont(Font toolTipFont);

    boolean isShowToolTip();

    void setShowToolTip(boolean showToolTip);

    Color getToolTipTextColor();

    void setToolTipTextColor(Color textColor);

    boolean isAlwaysOnTop();

    void setAlwaysOnTop(boolean alwaysOnTop);
}
