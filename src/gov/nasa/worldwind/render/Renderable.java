/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

/**
 * @author Tom Gaskins
 * @version $Id: Renderable.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public interface Renderable
{
    /**
     * Causes this <code>Renderable</code> to render itself using the <code>DrawContext</code> provided. The
     * <code>DrawContext</code> provides the elevation model, openGl instance, globe and other information required for
     * drawing. It is recommended that the <code>DrawContext</code> is non-null as most implementations do not support
     * null <code>DrawContext</code>s.
     *
     * @param dc the <code>DrawContext</code> to be used
     * @see DrawContext
     */
    public void render(DrawContext dc);
}
