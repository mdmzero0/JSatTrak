package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.*;
import java.awt.*;
import java.io.*;

/**
 * Renders a star background based on a subset of ESA Hipparcos catalog
 *
 * @author Patrick Murris
 * @version $id$
 */
public class StarsLayer extends RenderableLayer
{

    // TODO: make configurable
    protected String starsFileName = "config/Hipparcos_Stars_Mag6x5044.tsv";
    private int glListId = -1;                    // GL list id
    private float brightness = 1f;                // Brightness multiplier
    private boolean rebuild = false;            // True if need to rebuild GL list
    private double radius = 6356752 * 10;        // Earth radius x 10
    private Angle longitudeOffset = Angle.ZERO;    // Star sphere rotation longitude
    private Angle latitudeOffset = Angle.ZERO;    // Star sphere rotation latitude
    //private String layerName = WorldWind.retrieveErrMsg("layers.StarsLayer.Name");
    private String layerName = "Stars";

    /**
     * A RenderableLayer that displays a star background
     */
    public StarsLayer()
    {
        this.setName(Logging.getMessage("layers.Earth.StarsLayer.Name"));
    }

    /**
     * A RenderableLayer that displays a star background
     *
     * @param starsFileName the path and filename of the star catalog file
     */
    public StarsLayer(String starsFileName)
    {
        this.setName(Logging.getMessage("layers.Earth.StarsLayer.Name"));
        this.setStarsFileName(starsFileName);
    }

    // Public properties

    /**
     * Get the path and filename of the stars catalog file.
     */
    public String getStarsFileName()
    {
        return this.starsFileName;
    }

    /**
     * Set the path and filename of the stars catalog file.
     *
     * @param fileName the path and filename
     */
    public void setStarsFileName(String fileName)
    {
        if (fileName == null || fileName.length() == 0)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.starsFileName = fileName;
        this.rebuild = true;
    }

    /**
     * Get the actual brightness multiplier.
     *
     * @return brightness
     */
    public float getBrightness()
    {
        return this.brightness;
    }

    /**
     * Set the brightness multiplier. eg : 1.0f = no change, 0.5f = darker, 2.0f = brighter.
     *
     * @param brightness the brightness multiplier
     */
    public void setBrightness(float brightness)
    {
        this.brightness = Math.abs(brightness);
        this.rebuild = true;
    }

    /**
     * Get the star sphere radius.
     *
     * @return the star sphere radius in meter.
     */
    public double getRadius()
    {
        return this.radius;
    }

    /**
     * Set the star sphere radius in meter.
     *
     * @param radius the radius in meter.
     */
    public void setRadius(double radius)
    {
        this.radius = Math.abs(radius);
        this.rebuild = true;
    }

    /**
     * Returns the latitude offset or relative tilt for the star sphere.
     *
     * @return the latitude offset.
     */
    public Angle getLatitudeOffset()
    {
        return this.latitudeOffset;
    }

    /**
     * Sets the latitude offset or relative tilt of the star sphere.
     *
     * @param offset the latitude offset.
     */
    public void setLatitudeOffset(Angle offset)
    {
        if (offset == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.latitudeOffset = offset;
    }

    /**
     * Returns the longitude offset or rotation of the star sphere.
     *
     * @return the longitude offset.
     */
    public Angle getLongitudeOffset()
    {
        return this.longitudeOffset;
    }

    /**
     * Sets the longitude offset or rotation of the star sphere.
     *
     * @param offset the longitude offset.
     */
    public void setLongitudeOffset(Angle offset)
    {
        if (offset == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.longitudeOffset = offset;
    }

    @Override
    public void doRender(DrawContext dc)
    {
        GL gl = dc.getGL();
        boolean attribsPushed = false;
        boolean modelviewPushed = false;
        boolean projectionPushed = false;

        // Load or reload stars if needed
        if (this.glListId == -1 || this.rebuild)
        {
            if (this.glListId != -1)
                gl.glDeleteLists(this.glListId, 1);
            this.loadStars(dc); // Create glList
            this.rebuild = false;
        }
        // Still no stars to render ?
        if (this.glListId == -1)
            return;

        try
        {
            // GL set up
            // Save GL state
            gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT
                | GL.GL_POLYGON_BIT | GL.GL_TEXTURE_BIT | GL.GL_ENABLE_BIT
                | GL.GL_CURRENT_BIT);
            attribsPushed = true;
            gl.glDisable(GL.GL_TEXTURE_2D);        // no textures
            gl.glDisable(GL.GL_DEPTH_TEST);        // no depth testing

            // Set far clipping far enough - is this the right way to do it ?
            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glPushMatrix();
            projectionPushed = true;
            gl.glLoadIdentity();
            double ditanceFromOrigin = dc.getView().getEyePoint().getLength3();
            //noinspection UnnecessaryLocalVariable
            double near = ditanceFromOrigin;
            double far = this.radius + ditanceFromOrigin;
            dc.getGLU().gluPerspective(dc.getView().getFieldOfView().degrees,
                dc.getView().getViewport().getWidth() / dc.getView().getViewport().getHeight(),
                near, far);

            // Rotate sphere
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPushMatrix();
            modelviewPushed = true;
            gl.glRotatef((float) this.longitudeOffset.degrees, 0.0f, 1.0f, 0.0f);
            gl.glRotatef((float) -this.latitudeOffset.degrees, 1.0f, 0.0f, 0.0f);

            // Draw
            gl.glCallList(this.glListId);
        }
        finally
        {
            // Restore GL state
            if (modelviewPushed)
            {
                gl.glMatrixMode(GL.GL_MODELVIEW);
                gl.glPopMatrix();
            }
            if (projectionPushed)
            {
                gl.glMatrixMode(GL.GL_PROJECTION);
                gl.glPopMatrix();
            }
            if (attribsPushed)
                gl.glPopAttrib();
        }
    }

    /**
     * Read stars catalog file and draw into a glList
     *
     * @param dc the current DrawContext
     */
    private void loadStars(DrawContext dc)
    {
        GL gl = dc.getGL();
        this.glListId = gl.glGenLists(1);
        gl.glNewList(this.glListId, GL.GL_COMPILE);
        this.drawStarsFromFile(dc);
        gl.glEndList();
    }

    /**
     * Read the current star catalog file and draw GL points
     *
     * @param dc the current DrawContext
     */
    private void drawStarsFromFile(DrawContext dc)
    {
        // Read star catalog and draw
        GL gl = dc.getGL();
        try
        {
            InputStream starsStream = this.getClass().getResourceAsStream("/" + this.starsFileName);
            if (starsStream == null)
            {
                File starsFile = new File(this.starsFileName);
                if (starsFile.exists())
                {
                    starsStream = new FileInputStream(starsFile);
                }
            }
            if (starsStream == null)
                // TODO: logger error
                return;
//            BufferedInputStream bis = new BufferedInputStream(starsStream);
//            DataInputStream starsReader = new DataInputStream(bis);
            BufferedReader starsReader = new BufferedReader(new InputStreamReader(starsStream));

            String line;
            int idxRAhms = 2;        // Catalog field indices
            int idxDEdms = 3;
            int idxVmag = 4;
            int idxBV = 5;
            double longitude;
            double latitude;
            boolean isData = false;

            gl.glBegin(GL.GL_POINTS);
            while ((line = starsReader.readLine()) != null)
            {
                if (line.length() < 3)
                    continue;
                if (line.substring(0, 1).compareTo("#") == 0)
                    continue;
                if (isData) // Star data here
                {
                    // Split data in ';' separated values
                    String[] starData = line.trim().split(";");
                    String RAhms, DEdms, Vmag, BV;
                    RAhms = starData[idxRAhms];    // Right Asc in H, min, sec 	"00 01 35.85"
                    DEdms = starData[idxDEdms];    // Declinaison Degre min sec	"-77 03 55.1"
                    Vmag = starData[idxVmag];    // Apparent magnitude	" 4.78"
                    // B-V spectral color " 1.254" (may be missing)
                    BV = idxBV < starData.length ? starData[idxBV] : "";

                    // compute RAhms into longitude
                    double RAh = Double.parseDouble(RAhms.substring(0, 2));
                    double RAm = Double.parseDouble(RAhms.substring(3, 5));
                    double RAs = Double.parseDouble(RAhms.substring(6));
                    longitude = (RAh * 15) + (RAm * .25) + (RAs * 0.0041666) - 180;
                    // compute DEdms into latitude
                    String DEsign = DEdms.substring(0, 1);
                    double DEd = Double.parseDouble(DEdms.substring(1, 3));
                    double DEm = Double.parseDouble(DEdms.substring(4, 6));
                    double DEs = Double.parseDouble(DEdms.substring(7));
                    latitude = DEd + (DEm / 60) + (DEs / 3600);
                    if (DEsign.compareTo("-") == 0) latitude *= -1;
                    // compute aparent magnitude -1.5 - 10 to grayscale 0 - 255
                    double VM = Double.parseDouble(Vmag);
                    double Vdec = 255 - ((VM + 1.5) * 255 / 10);
                    Vdec *= this.brightness; // boost luminosity
                    if (Vdec > 255) Vdec = 255;
                    Vdec /= 255;    // scale back to 0.0 - 1.0
                    // convert B-V  -0.5 - 4 for rgb color select
                    double BVdec = 0;
                    try
                    {
                        BVdec = Double.parseDouble(BV);
                    }
                    catch (Exception e)
                    {
                        BVdec = 0;
                    }

                    // Star color
                    Color color = BVColor(BVdec);
                    //gl.glColor3f((float)Vdec, (float)Vdec, (float)Vdec); // grayscale
                    gl.glColor3f((float) color.getRed() / 255f * (float) Vdec,
                        (float) color.getGreen() / 255f * (float) Vdec,
                        (float) color.getBlue() / 255f * (float) Vdec); // B-V color
                    // Place vertex for point star
                    Vec4 pos = SphericalToCartesian(latitude, longitude, this.radius);
                    gl.glVertex3d(pos.getX(), pos.getY(), pos.getZ());
                }
                // Data starting next line
                if (line.substring(0, 3).compareTo("---") == 0)
                    isData = true;
            }
            gl.glEnd();
            starsReader.close();

        }
        catch (IOException e)
        {
            // TODO: Log proper message
            //String message = WorldWind.retrieveErrMsg("generic.IOExceptionWhileLoadingData");
            String message = "IOException while loading stars data from " + this.starsFileName;
            Logging.logger().severe(message);
        }
        catch (Exception e)
        {
            String message = "Error while loading stars data from " + this.starsFileName;
            Logging.logger().severe(message);
        }
    }

    /**
     * Converts position in spherical coordinates (lat/lon/radius) to cartesian (XYZ) coordinates.
     *
     * @param latitude  Latitude in decimal degrees
     * @param longitude Longitude in decimal degrees
     * @param radius    Radius
     * @return the corresponding Point
     */
    private static Vec4 SphericalToCartesian(double latitude, double longitude, double radius)
    {
        latitude *= Math.PI / 180.0f;
        longitude *= Math.PI / 180.0f;

        double radCosLat = radius * Math.cos(latitude);

        return new Vec4(
            radCosLat * Math.sin(longitude),
            radius * Math.sin(latitude),
            radCosLat * Math.cos(longitude));
    }

    /**
     * Returns the corresponding B-V color
     *
     * @param BV the star B-V decimal value (-.5 .. 4)
     * @return the corresponding Color
     */
    private static Color BVColor(double BV)
    {
        // TODO: interpolate between values
        if (BV < 0) return new Color(.635f, .764f, .929f);            // Light blue
        else if (BV < .5) return new Color(1f, 1f, 1f);                // White
        else if (BV < 1) return new Color(1f, .984f, .266f);            // Yellow
        else if (BV < 1.5) return new Color(.964f, .725f, .0784f);    // Orange
        else return new Color(.921f, .376f, .0392f);                // Redish
    }

    public void dispose()
    {
        if (this.glListId < 0)
            return;

        GLContext glc = GLContext.getCurrent();
        if (glc == null)
            return;

        glc.getGL().glDeleteLists(this.glListId, 1);

        this.glListId = -1;
    }

    @Override
    public String toString()
    {
        return this.getName();
    }

}
