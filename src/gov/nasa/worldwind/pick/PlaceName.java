/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.pick;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.WWIcon;

import java.awt.*;

/**
 * @author dcollins
 * @version $Id: PlaceName.java 3318 2007-10-17 18:33:07Z dcollins $
 */
public interface PlaceName
{
    CharSequence getText();

    void setText(CharSequence text);

    Position getPosition();

    void setPosition(Position position);

    Font getFont();

    void setFont(Font font);

    Color getColor();

    void setColor(Color color);

    boolean isVisible();

    void setVisible(boolean visible);

    WWIcon getIcon();

    void setIcon(WWIcon icon);
}
