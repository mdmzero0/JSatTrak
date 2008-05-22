// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Blitter.java

package com.sun.media.renderer.video;

import java.awt.*;
import java.util.Vector;
import javax.media.Buffer;

public interface Blitter
{

    public abstract int newData(Buffer buffer, Vector vector, Vector vector1, Vector vector2);

    public abstract Image process(Buffer buffer, Object obj, Object obj1, Dimension dimension);

    public abstract void draw(Graphics g, Component component, Image image, int i, int j, int k, int l, 
            int i1, int j1, int k1, int l1);

    public abstract void resized(Component component);
}
