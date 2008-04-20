/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: TrackTraversalControlPanel.java 3438 2007-11-06 05:28:53Z tgaskins $
 */
public class TrackTraversalControlPanel extends JPanel
{
    private RenderableLayer layer;
    private JSpinner positionSpinner;
    private JSlider segmentSlider;
    private JLabel lat;
    private JLabel lon;
    private JLabel ele;
    private JLabel heading;
    private int currentPositionNumber = 0;
    private int numPoints = 0;

    public TrackTraversalControlPanel()
    {
        super(new BorderLayout());
        JPanel p = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Position");
        label.setBorder(new EmptyBorder(5, 5, 5, 15));
        p.add(label, BorderLayout.WEST);
        this.positionSpinner = new JSpinner(new SpinnerListModel(new String[] {"0"}));
        p.add(this.positionSpinner, BorderLayout.CENTER);
        positionSpinner.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent changeEvent)
            {
                currentPositionNumber = Integer.parseInt((String) ((JSpinner) changeEvent.getSource()).getValue());
                segmentSlider.setValue(0);
                Position p = getAdjustedPosition();
                lat.setText("Lat " + Double.toString((scale(p.getLatitude().getDegrees(), 2))));
                lon.setText("Lon " + Double.toString((scale(p.getLongitude().getDegrees() ,2))));
                ele.setText("Alt " + Double.toString((scale(p.getElevation(), 3))));
                firePropertyChange("PositionNumber", -1, currentPositionNumber);
            }
        });
        this.add(p, BorderLayout.WEST);

        this.segmentSlider = new JSlider(0, 2000, 0);
        this.segmentSlider.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent changeEvent)
            {
                Position p = getAdjustedPosition();
                lat.setText("Lat " + Double.toString((scale(p.getLatitude().getDegrees(), 2))));
                lon.setText("Lon " + Double.toString((scale(p.getLongitude().getDegrees() ,2))));
                ele.setText("Alt " + Double.toString((scale(p.getElevation(), 3))));
                firePropertyChange("InteriorPosition", -1, currentPositionNumber);
            }
        });
        this.add(this.segmentSlider, BorderLayout.CENTER);

        JPanel readoutPanel = new JPanel(new GridLayout(1, 0, 5, 5));
        readoutPanel.add(this.lat = new JLabel("000.00"));
        readoutPanel.add(this.lon = new JLabel("000.00"));
        readoutPanel.add(this.ele = new JLabel("000.00"));
        this.add(readoutPanel, BorderLayout.SOUTH);
    }
    
    public RenderableLayer getTrackLayer()
    {
        return this.layer;
    }

    public void setTrackLayer(RenderableLayer layer)
    {
        this.numPoints = 0;
        Polyline line = (Polyline) layer.getRenderables().iterator().next();
        for (Position p: line.getPositions())
            ++this.numPoints;
        this.layer = layer;
        this.currentPositionNumber = 0;
        this.updateTrackInfo();
        firePropertyChange("Layer", -1, currentPositionNumber);
    }

    private boolean isLastPosition(int n)
    {
        return n == this.numPoints - 1;
    }

    public void setPositionNumber(int n)
    {
        this.currentPositionNumber = n;
        firePropertyChange("PositionNumber", -1, currentPositionNumber);
    }

    public Position getPosition()
    {
        if (layer == null)
            return null;
        
        int i = 0;
        for (Position p: ((Polyline) layer.getRenderables().iterator().next()).getPositions())
        {
            if (i++ == currentPositionNumber)
                return p;
        }

        return null;
    }

    public Position getPosition(int n)
    {
        if (layer == null)
            return null;

        int i = 0;
        for (Position p: ((Polyline) layer.getRenderables().iterator().next()).getPositions())
        {
            if (i++ == n)
                return p;
        }

        return null;
    }

    public Position getAdjustedPosition()
    {
        if (layer == null)
            return null;

        int i = this.segmentSlider.getValue();
        int min = this.segmentSlider.getMinimum();
        int max = this.segmentSlider.getMaximum();
        double t = (double) i / ((double) max - (double) min);

        return this.getPositionAlongSegment(t);
    }

    public Position getNextPosition()
    {
        return isLastPosition(this.currentPositionNumber) ? null : this.getPosition(this.currentPositionNumber + 1);
    }

    public Position getPositionAlongSegment(double t)
    {
        Position pa = this.getPosition();
        Position pb = this.getNextPosition();
        if (pa == null || pb == null)
            return null;

        LatLon ll = LatLon.interpolate(t, pa.getLatLon(), pb.getLatLon());
        double e = (1d - t) * pa.getElevation() + t * pa.getElevation();
        return new Position(ll, e);
    }

    public Angle getHeading()
    {
        if (layer == null)
            return null;

        Position pA;
        Position pB;

        if (this.currentPositionNumber > 0)
        {
            pA = this.getPosition(this.currentPositionNumber - 1);
            pB = this.getPosition(this.currentPositionNumber);
        }
        else
        {
            pA = this.getPosition(0);
            pB = this.getPosition(1);
        }

        return this.computeHeading(pA, pB);
    }

    private Angle computeHeading(Position pa, Position pb)
    {
        if (layer == null)
            return null;

        return LatLon.azimuth(pa.getLatLon(), pb.getLatLon());
    }

    private void updateTrackInfo()
    {
        ArrayList<String> strings = new ArrayList<String>();
        int i = 0;
        for (Position p: ((Polyline) layer.getRenderables().iterator().next()).getPositions())
            strings.add(Integer.toString(i++));

        String[] ptNames = strings.toArray(new String[1]);

        this.positionSpinner.setModel(new SpinnerListModel(ptNames));
        positionSpinner.setValue("0");
        segmentSlider.setValue(0);
        Position p = this.getAdjustedPosition();
        lat.setText("Lat " + Double.toString((scale(p.getLatitude().getDegrees(), 2))));
        lon.setText("Lon " + Double.toString((scale(p.getLongitude().getDegrees() ,2))));
        ele.setText("Alt " + Double.toString((scale(p.getElevation(), 3))));
    }

    private double scale(double v, int n)
    {
        return (((int)(v * Math.pow(10, n))) / Math.pow(10, n));
    }
}
