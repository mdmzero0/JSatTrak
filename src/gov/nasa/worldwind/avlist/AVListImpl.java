/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.avlist;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.Logging;

import java.util.*;
import java.util.logging.Level;

/**
 * An implementation class for the {@link AVList} interface. Classes implementing <code>AVList</code> can subclass or
 * aggreate this class to provide default <code>AVList</code> functionality. This class maintains a hash table of
 * attribute-value pairs.
 * <p/>
 * This class implements a notification mechanism for attribute-value changes. The mechanism provides a means for
 * objects to observe attribute changes or queries for certain keys without explicitly monitoring all keys. See {@link
 * java.beans.PropertyChangeSupport}.
 *
 * @author Tom Gaskins
 * @version $Id: AVListImpl.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public class AVListImpl implements AVList, java.beans.PropertyChangeListener
{
    // TODO: Make thread-safe
    /**
     * Available to sub-classes for further exposure of property-change functionality.
     */
    protected final java.beans.PropertyChangeSupport changeSupport;// = new java.beans.PropertyChangeSupport(this);

    // To avoid unnecessary overhead, this object's hash map is created only if needed.
    private Map<String, Object> avList;

    /**
     * Creates an empty attribute-value list.
     */
    public AVListImpl()
    {
        this.changeSupport = new java.beans.PropertyChangeSupport(this);
    }

    /**
     * Constructor enabling aggregation
     * @param sourceBean The bean to be given as the soruce for any events.
     */
    public AVListImpl(Object sourceBean)
    {
        // TODO: check arg for non-null
        this.changeSupport = new java.beans.PropertyChangeSupport(sourceBean);
    }

    private boolean hasAvList()
    {
        return this.avList != null;
    }

    private Map<String, Object> createAvList()
    {
        if (!this.hasAvList())
        {
            this.avList = new java.util.HashMap<String, Object>();
        }

        return this.avList;
    }

    private Map<String, Object> avList(boolean createIfNone)
    {
        if (createIfNone && !this.hasAvList())
            this.createAvList();

        return this.avList;
    }

    public final Object getValue(String key)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.AttributeKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.hasAvList())
            return this.avList.get(key);

        return null;
    }

    public final Collection<Object> getValues()
    {
        return this.hasAvList() ? this.avList.values() : this.createAvList().values();
    }

    public Set<Map.Entry<String, Object>> getEntries()
    {
        return this.hasAvList() ? this.avList.entrySet() : this.createAvList().entrySet();
    }

    public final String getStringValue(String key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.AttributeKeyIsNull");
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }
        try
        {
            return (String) this.getValue(key);
        }
        catch (ClassCastException e)
        {
            String msg = Logging.getMessage("AVAAccessibleImpl.AttributeValueForKeyIsNotAString", key);
            Logging.logger().severe(msg);
            throw new WWRuntimeException(msg, e);
        }
    }

    public final void setValue(String key, Object value)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.AttributeKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        // Capture the existing value if there is one, then set the new value.
        this.avList(true).put(key, value);
    }

    public final void setValues(AVList list)
    {
        if (list == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Set<Map.Entry<String, Object>> entries = list.getEntries();
        for (Map.Entry<String, Object> entry : entries)
            this.setValue(entry.getKey(), entry.getValue());
    }

    public final boolean hasKey(String key)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.hasAvList() && this.avList.containsKey(key);
    }

    public final void removeKey(String key)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.hasKey(key))
            this.avList.remove(key);
    }

    public AVList copy()
    {
        AVListImpl clone = new AVListImpl();

        clone.createAvList();
        clone.avList.putAll(this.avList);

        return clone;
    }

    public final AVList clearList()
    {
        if (this.hasAvList())
            this.avList.clear();
        return this;
    }

    public void addPropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener)
    {
        if (propertyName == null)
        {
            String msg = Logging.getMessage("nullValue.PropertyNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (listener == null)
        {
            String msg = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener)
    {
        if (propertyName == null)
        {
            String msg = Logging.getMessage("nullValue.PropertyNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (listener == null)
        {
            String msg = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener listener)
    {
        if (listener == null)
        {
            String msg = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener listener)
    {
        if (listener == null)
        {
            String msg = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.changeSupport.removePropertyChangeListener(listener);
    }

    public void firePropertyChange(java.beans.PropertyChangeEvent propertyChangeEvent)
    {
        if (propertyChangeEvent == null)
        {
            String msg = Logging.getMessage("nullValue.PropertyChangeEventIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.changeSupport.firePropertyChange(propertyChangeEvent);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        if (propertyName == null)
        {
            String msg = Logging.getMessage("nullValue.PropertyNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * The property change listener for <em>this</em> instance.
     * Recieves property change notifications that this instance has registered with other proprty change notifiers.
     * @param propertyChangeEvent the event
     * @throws IllegalArgumentException if <code>propertyChangeEvent</code> is null
     */
    public void propertyChange(java.beans.PropertyChangeEvent propertyChangeEvent)
    {
        if (propertyChangeEvent == null)
        {
            String msg = Logging.getMessage("nullValue.PropertyChangeEventIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Notify all *my* listeners of the change that I caught
        this.changeSupport.firePropertyChange(propertyChangeEvent);
    }

    
    // Static AVList utilities.
    public static String getStringValue(AVList avList, String key, String defaultValue)
    {
        String v = getStringValue(avList, key);
        return v != null ? v : defaultValue;
    }

    public static String getStringValue(AVList avList, String key)
    {
        try
        {
            return avList.getStringValue(key);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static Integer getIntegerValue(AVList avList, String key, Integer defaultValue)
    {
        Integer v = getIntegerValue(avList, key);
        return v != null ? v : defaultValue;
    }

    public static Integer getIntegerValue(AVList avList, String key)
    {
        Object o = avList.getValue(key);
        if (o == null)
            return null;

        if (o instanceof Integer)
            return (Integer) o;

        String v = getStringValue(avList, key);
        if (v == null)
            return null;

        try
        {
            return Integer.parseInt(v);
        }
        catch (NumberFormatException e)
        {
            Logging.logger().log(Level.SEVERE, "Configuration.ConversionError", v);
            return null;
        }
    }

    public static Long getLongValue(AVList avList, String key, Long defaultValue)
    {
        Long v = getLongValue(avList, key);
        return v != null ? v : defaultValue;
    }

    public static Long getLongValue(AVList avList, String key)
    {
        Object o = avList.getValue(key);
        if (o == null)
            return null;

        if (o instanceof Long)
            return (Long) o;

        String v = getStringValue(avList, key);
        if (v == null)
            return null;

        try
        {
            return Long.parseLong(v);
        }
        catch (NumberFormatException e)
        {
            Logging.logger().log(Level.SEVERE, "Configuration.ConversionError", v);
            return null;
        }
    }

    public static Double getDoubleValue(AVList avList, String key, Double defaultValue)
    {
        Double v = getDoubleValue(avList, key);
        return v != null ? v : defaultValue;
    }

    public static Double getDoubleValue(AVList avList, String key)
    {
        Object o = avList.getValue(key);
        if (o == null)
            return null;

        if (o instanceof Double)
            return (Double) o;

        String v = getStringValue(avList, key);
        if (v == null)
            return null;

        try
        {
            return Double.parseDouble(v);
        }
        catch (NumberFormatException e)
        {
            Logging.logger().log(Level.SEVERE, "Configuration.ConversionError", v);
            return null;
        }
    }
}
