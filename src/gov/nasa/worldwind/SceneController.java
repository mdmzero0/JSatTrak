/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind;

import gov.nasa.worldwind.cache.TextureCache;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.util.PerformanceStatistic;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.globes.SectorGeometryList;

import java.util.*;

/**
 * @author Tom Gaskins
 * @version $Id: SceneController.java 3459 2007-11-08 18:48:01Z dcollins $
 */
public interface SceneController extends WWObject
{
    public Model getModel();

    public void setModel(Model model);

    public View getView();

    public void setView(View view);

    public void repaint();

    void setVerticalExaggeration(double verticalExaggeration);

    double getVerticalExaggeration();

    PickedObjectList getPickedObjectList();

    double getFramesPerSecond();

    double getFrameTime();

    void setPickPoint(java.awt.Point pickPoint);

    java.awt.Point getPickPoint();

    void setTextureCache(TextureCache textureCache);

    Collection<PerformanceStatistic> getPerFrameStatistics();

    void setPerFrameStatisticsKeys(Set<String> keys);

    SectorGeometryList getTerrain();
}
