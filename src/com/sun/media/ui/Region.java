// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Region.java

package com.sun.media.ui;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

class Region
    implements Cloneable, Serializable
{

    public Region()
    {
        rects = new Vector();
    }

    public Region(Rectangle r)
    {
        rects = new Vector();
        addRectangle(r);
    }

    public Region(Rectangle r1, Rectangle r2)
    {
        rects = new Vector();
        addRectangle(r1);
        addRectangle(r2);
    }

    public boolean isEmpty()
    {
        return rects.isEmpty();
    }

    public int getNumRectangles()
    {
        return rects.size();
    }

    public Enumeration rectangles()
    {
        return rects.elements();
    }

    public Object clone()
    {
        Region r = new Region();
        r.rects = (Vector)rects.clone();
        return r;
    }

    public Rectangle getBounds()
    {
        Rectangle r = new Rectangle();
        for(int i = 0; i < rects.size(); i++)
            r = r.union((Rectangle)rects.elementAt(i));

        return r;
    }

    public void addRectangle(Rectangle r)
    {
        for(int position = 0; position < rects.size();)
        {
            Rectangle current = (Rectangle)rects.elementAt(position);
            if(r.x > current.x && r.y > current.y && right(r) <= right(current) && bottom(r) <= bottom(current))
                return;
            if(r.intersects(current))
            {
                r = r.union(current);
                rects.removeElementAt(position);
            } else
            {
                position++;
            }
        }

        rects.addElement(r);
    }

    public boolean intersects(Rectangle r)
    {
        for(int position = 0; position < rects.size(); position++)
        {
            Rectangle rect = (Rectangle)rects.elementAt(position);
            if(rect.intersects(r))
                return true;
        }

        return false;
    }

    public void intersect(Rectangle r)
    {
        for(int position = 0; position < rects.size();)
        {
            Rectangle rect = (Rectangle)rects.elementAt(position);
            rect = rect.intersection(r);
            if(rect.isEmpty())
            {
                rects.removeElementAt(position);
            } else
            {
                rects.setElementAt(rect, position);
                position++;
            }
        }

    }

    public void addRegion(Region r)
    {
        for(Enumeration e = r.rectangles(); e.hasMoreElements(); addRectangle((Rectangle)e.nextElement()));
    }

    public void translate(int dx, int dy)
    {
        for(int p = 0; p < rects.size(); p++)
        {
            Rectangle r = (Rectangle)rects.elementAt(p);
            r.translate(dx, dy);
        }

    }

    public String toString()
    {
        String s = getClass().getName() + " = [\n";
        for(Enumeration e = rectangles(); e.hasMoreElements();)
            s = s + "(" + (Rectangle)e.nextElement() + ")\n";

        return s + "]";
    }

    public static int right(Rectangle r)
    {
        return (r.x + r.width) - 1;
    }

    public static int bottom(Rectangle r)
    {
        return (r.y + r.height) - 1;
    }

    protected Vector rects;
}
