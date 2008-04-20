/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.util.Logging;

import java.util.logging.Level;

/**
 * @author Tom Gaskins
 * @version $Id: BasicModel.java 3489 2007-11-13 00:36:18Z tgaskins $
 */
public class BasicModel extends WWObjectImpl implements Model
{
    private Globe globe;
    private LayerList layers;
    private boolean showWireframeInterior = false;
    private boolean showWireframeExterior = false;
    private boolean showTessellationBoundingVolumes = false;

    public BasicModel() // TODO: Derive from configuration descriptor
    {
        // Create a default globe
        Globe globe = (Globe) WorldWind.createConfigurationComponent(AVKey.GLOBE_CLASS_NAME);
        this.setGlobe(globe);
        this.createLayers();
    }

    private void createLayers()
    {
        LayerList layers = new LayerList();
        String layerNames = Configuration.getStringValue(AVKey.LAYERS_CLASS_NAMES,
            "gov.nasa.worldwind.layers.Earth.BMNGSurfaceLayer");
        if (layerNames == null)
            return;

        String[] names = layerNames.split(",");
        for (String name : names)
        {
            try
            {
                if (name.length() > 0)
                {
                    Layer l = (Layer) WorldWind.createComponent(name);
                    layers.add(l);
                }
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE, Logging.getMessage("BasicModel.LayerNotFound", name), e);
            }
        }

        this.setLayers(layers);
    }

    public void setGlobe(Globe globe)
    {
        // don't raise an exception if globe == null. In that case, we are disassociating the model from any globe

        //remove property change listener "this" from the current globe.
        if (this.globe != null)
            this.globe.removePropertyChangeListener(this);

        // if the new globe is not null, add "this" as a property change listener.
        if (globe != null)
            globe.addPropertyChangeListener(this);

        Globe old = this.globe;
        this.globe = globe;
        this.firePropertyChange(AVKey.GLOBE, old, this.globe);
    }

    public void setLayers(LayerList layers)
    {
        // don't raise an exception if layers == null. In that case, we are disassociating the model from any layer set

        if (this.layers != null)
            this.layers.removePropertyChangeListener(this);
        if (layers != null)
            layers.addPropertyChangeListener(this);

        LayerList old = this.layers;
        this.layers = layers;
        this.firePropertyChange(AVKey.LAYERS, old, this.layers);
    }

    public Globe getGlobe()
    {
        return this.globe;
    }

    public LayerList getLayers()
    {
        return this.layers;
    }

    public void setShowWireframeInterior(boolean show)
    {
        this.showWireframeInterior = show;
    }

    public void setShowWireframeExterior(boolean show)
    {
        this.showWireframeExterior = show;
    }

    public boolean isShowWireframeInterior()
    {
        return this.showWireframeInterior;
    }

    public boolean isShowWireframeExterior()
    {
        return this.showWireframeExterior;
    }

    public boolean isShowTessellationBoundingVolumes()
    {
        return showTessellationBoundingVolumes;
    }

    public void setShowTessellationBoundingVolumes(boolean showTessellationBoundingVolumes)
    {
        this.showTessellationBoundingVolumes = showTessellationBoundingVolumes;
    }

    public Extent getExtent()
    {
        // See if the layers have it.
        LayerList layers = BasicModel.this.getLayers();
        if (layers != null)
        {
            for (Object layer1 : layers)
            {
                Layer layer = (Layer) layer1;
                Extent e = (Extent) layer.getValue(AVKey.EXTENT);
                if (e != null)
                    return e;
            }
        }

        // See if the Globe has it.
        Globe globe = this.getGlobe();
        if (globe != null)
        {
            Extent e = globe.getExtent();
            if (e != null)
                return e;
        }

        return null;
    }
}
