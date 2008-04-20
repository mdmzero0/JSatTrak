/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * @author tag
 * @version $Id: Material.java 2677 2007-08-25 06:41:38Z tgaskins $
 */
public class Material
{
    public static final int SPECULAR = 0;
    public static final int DIFFUSE = 1;
    public static final int AMBIENT = 2;
    public static final int EMISSION = 3;

    public static final Material WHITE = new Material(new Color(0.9f, 0.9f, 0.9f, 0.0f), new Color(0.8f, 0.8f, 0.8f,
        0.0f), new Color(0.2f, 0.2f, 0.2f, 0.0f), new Color(0.0f, 0.0f, 0.0f, 0.0f), 20f);

    public static final Material RED = new Material(new Color(0.75f, 0.0f, 0.0f, 0.0f), new Color(0.8f, 0.0f, 0.0f,
        0.0f), new Color(0.2f, 0.0f, 0.0f, 0.0f), new Color(0.0f, 0.0f, 0.0f, 0.0f), 80f);

    public static final Material GREEN = new Material(new Color(0.0f, 0.75f, 0.0f, 0.0f), new Color(0.0f, 0.8f, 0.0f,
        0.0f), new Color(0.0f, 0.2f, 0.0f, 0.0f), new Color(0.0f, 0.0f, 0.0f, 0.0f), 20f);

    public static final Material BLUE = new Material(new Color(0.0f, 0.0f, 0.75f, 0.0f), new Color(0.0f, 0.0f, 0.8f,
        0.0f), new Color(0.0f, 0.0f, 0.2f, 0.0f), new Color(0.0f, 0.0f, 0.0f, 0.0f), 20f);

    public static final Material YELLOW = new Material(new Color(0.75f, 0.75f, 0.55f, 0.0f), new Color(0.8f, 0.8f, 0.0f,
        0.0f), new Color(0.2f, 0.2f, 0.01f, 0.0f), new Color(0.0f, 0.0f, 0.0f, 0.0f), 20f);

    private final Color specular;
    private final Color diffuse;
    private final Color ambient;
    private final Color emission;
    private final float shininess;

    /**
     * @param specular
     * @param diffuse
     * @param ambient
     * @param emission
     * @param shininess
     * @throws IllegalArgumentException if <code>specular</code>, <code>diffuse</code>, <code>ambient</code> or
     *                                  <code>emission</code> is null
     */
    public Material(Color specular, Color diffuse, Color ambient, Color emission, float shininess)
    {
        if (specular == null || diffuse == null || ambient == null || emission == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.specular = specular;
        this.diffuse = diffuse;
        this.ambient = ambient;
        this.emission = emission;
        this.shininess = shininess;
    }

    public Material(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.specular = new Color(0.75f, 0.75f, 0.55f, 0.0f);
        this.diffuse = color;
        this.ambient = new Color(0.2f, 0.2f, 0.01f, 0.0f);
        this.emission = new Color(0.0f, 0.0f, 0.0f, 0.0f);
        this.shininess = 20f;
    }

    public Color getSpecular()
    {
        return this.specular;
    }

    public Color getDiffuse()
    {
        return this.diffuse;
    }

    public Color getAmbient()
    {
        return this.ambient;
    }

    public Color getEmission()
    {
        return this.emission;
    }

    public float getShininess()
    {
        return this.shininess;
    }

    public void apply(javax.media.opengl.GL gl, int face)
    {
        float[] rgba = new float[4];

        gl.glMaterialfv(face, javax.media.opengl.GL.GL_SPECULAR, this.specular.getRGBComponents(rgba), 0);
        gl.glMaterialfv(face, javax.media.opengl.GL.GL_DIFFUSE, this.diffuse.getRGBComponents(rgba), 0);
        gl.glMaterialfv(face, javax.media.opengl.GL.GL_AMBIENT, this.ambient.getRGBComponents(rgba), 0);
        gl.glMaterialf(face, javax.media.opengl.GL.GL_SHININESS, this.shininess);
        gl.glMaterialfv(face, javax.media.opengl.GL.GL_EMISSION, this.emission.getRGBComponents(rgba), 0);
    }
}
