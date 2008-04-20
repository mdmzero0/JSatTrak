package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.util.Logging;

import java.beans.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

/**
 * @author Tom Gaskins
 * @version $Id: LayerList.java 3481 2007-11-09 23:48:53Z tgaskins $
 */
public class LayerList extends CopyOnWriteArrayList<Layer> implements WWObject
{
    private WWObjectImpl wwo = new WWObjectImpl(this);

    public LayerList()
    {
    }

    public LayerList(Layer[] layers)
    {
        if (layers == null)
        {
            String message = Logging.getMessage("nullValue.LayersIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        for (Layer layer : layers)
        {
            this.add(layer);
        }
    }

    @Override
    public Object clone()
    {
        LayerList newList = (LayerList) super.clone();
        newList.wwo = new WWObjectImpl(newList);
        for (Layer l : newList)
            l.removePropertyChangeListener(this);

        return newList;
    }

    public boolean add(Layer layer)
    {
        if (layer == null)
        {
            String message = Logging.getMessage("nullValue.LayerIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        super.add(layer);
        layer.addPropertyChangeListener(this);
        this.firePropertyChange(AVKey.LAYERS, null, this);

        return true;
    }

    public void add(int index, Layer layer)
    {
        if (layer == null)
        {
            String message = Logging.getMessage("nullValue.LayerIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        super.add(index, layer);
        layer.addPropertyChangeListener(this);
        this.firePropertyChange(AVKey.LAYERS, null, this);
    }

    public void remove(Layer layer)
    {
        if (layer == null)
        {
            String msg = Logging.getMessage("nullValue.LayerIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.contains(layer))
            return;

        layer.removePropertyChangeListener(this);
        super.remove(layer);
        this.firePropertyChange(AVKey.LAYERS, null, this);
    }

    public Layer remove(int index)
    {
        Layer layer = get(index);
        if (layer == null)
            return null;

        layer.removePropertyChangeListener(this);
        super.remove(index);
        this.firePropertyChange(AVKey.LAYERS, null, this);

        return layer;
    }

    public Layer set(int index, Layer layer)
    {
        if (layer == null)
        {
            String message = Logging.getMessage("nullValue.LayerIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Layer oldLayer = this.get(index);
        if (oldLayer != null)
            oldLayer.removePropertyChangeListener(this);

        super.set(index, layer);
        layer.addPropertyChangeListener(this);
        this.firePropertyChange(AVKey.LAYERS, null, this);

        return oldLayer;
    }

    public boolean remove(Object o)
    {
        for (Layer layer : this)
        {
            if (layer.equals(o))
                layer.removePropertyChangeListener(this);
        }

        boolean removed = super.remove(o);
        if (removed)
            this.firePropertyChange(AVKey.LAYERS, null, this);

        return removed;
    }

    public boolean addIfAbsent(Layer layer)
    {
        for (Layer l : this)
        {
            if (l.equals(layer))
                return false;
        }

        layer.addPropertyChangeListener(this);

        boolean added = super.addIfAbsent(layer);
        if (added)
            this.firePropertyChange(AVKey.LAYERS, null, this);

        return added;
    }

    public boolean removeAll(Collection<?> objects)
    {
        for (Layer layer : this)
            layer.removePropertyChangeListener(this);

        boolean removed = super.removeAll(objects);
        if (removed)
            this.firePropertyChange(AVKey.LAYERS, null, this);

        return removed;
    }

    public int addAllAbsent(Collection<? extends Layer> layers)
    {
        for (Layer layer : layers)
        {
            if (!this.contains(layer))
                layer.addPropertyChangeListener(this);
        }

        int numAdded = super.addAllAbsent(layers);
        if (numAdded > 0)
            this.firePropertyChange(AVKey.LAYERS, null, this);

        return numAdded;
    }

    public boolean addAll(Collection<? extends Layer> layers)
    {
        boolean added = super.addAll(layers);
        if (added)
            this.firePropertyChange(AVKey.LAYERS, null, this);

        return added;
    }

    public boolean addAll(int i, Collection<? extends Layer> layers)
    {
        for (Layer layer : layers)
            layer.addPropertyChangeListener(this);
        
        boolean added = super.addAll(i, layers);
        if (added)
            this.firePropertyChange(AVKey.LAYERS, null, this);

        return added;
    }

    public boolean retainAll(Collection<?> objects)
    {
        for (Layer layer : this)
        {
            if (!objects.contains(layer))
                layer.removePropertyChangeListener(this);
        }

        boolean added = super.retainAll(objects);
        if (added)
            this.firePropertyChange(AVKey.LAYERS, null, this);

        return added;
    }

    public Object getValue(String key)
    {
        return wwo.getValue(key);
    }

    public Collection<Object> getValues()
    {
        return wwo.getValues();
    }

    public Set<Map.Entry<String, Object>> getEntries()
    {
        return wwo.getEntries();
    }

    public String getStringValue(String key)
    {
        return wwo.getStringValue(key);
    }

    public void setValue(String key, Object value)
    {
        wwo.setValue(key, value);
    }

    public void setValues(AVList avList)
    {
        wwo.setValues(avList);
    }

    public boolean hasKey(String key)
    {
        return wwo.hasKey(key);
    }

    public void removeKey(String key)
    {
        wwo.removeKey(key);
    }

    public AVList copy()
    {
        return wwo.copy();
    }

    public AVList clearList()
    {
        return this.wwo.clearList();
    }

    public LayerList sort()
    {
        if (this.size() <= 0)
            return this;

        Layer[] array = new Layer[this.size()];
        this.toArray(array);
        Arrays.sort(array, new Comparator<Layer>()
        {
            public int compare(Layer layer, Layer layer1)
            {
                return layer.getName().compareTo(layer1.getName());
            }
        });

        this.clear();
        for (Layer l : array)
            super.add(l);

        return this;
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        wwo.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        wwo.removePropertyChangeListener(propertyName, listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        wwo.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        wwo.removePropertyChangeListener(listener);
    }

    public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
    {
        wwo.firePropertyChange(propertyChangeEvent);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        wwo.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void propertyChange(PropertyChangeEvent propertyChangeEvent)
    {
        wwo.propertyChange(propertyChangeEvent);
    }

    @Override
    public String toString()
    {
        String r = "";
        for (Layer l : this)
        {
            r += l.toString() + ", ";
        }
        return r;
    }
}
