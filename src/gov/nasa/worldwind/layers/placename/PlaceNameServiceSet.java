/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.placename;

import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * @author Paul Collins
 * @version $Id: PlaceNameServiceSet.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public class PlaceNameServiceSet
{
    private final List<PlaceNameService> serviceList = new LinkedList<PlaceNameService>();

    public PlaceNameServiceSet()
    {
    }

    /**
     * @param placeNameService
     * @param replace
     * @return
     * @throws IllegalArgumentException if <code>placeNameService</code> is null
     */
    public boolean addService(PlaceNameService placeNameService, boolean replace)
    {
        if (placeNameService == null)
        {
            String message = Logging.getMessage("nullValue.PlaceNameServiceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (int i = 0; i < this.serviceList.size(); i++)
        {
            final PlaceNameService other = this.serviceList.get(i);
            if (placeNameService.getService().equals(other.getService()) && placeNameService.getDataset().equals(
                other.getDataset()))
            {
                if (replace)
                {
                    this.serviceList.set(i, placeNameService);
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }

        this.serviceList.add(placeNameService);
        return true;
    }

    public final PlaceNameServiceSet deepCopy()
    {
        PlaceNameServiceSet copy = new PlaceNameServiceSet();

        // Creates a deep copy of this.serviceList in copy.serviceList.
        for (int i = 0; i < this.serviceList.size(); i++)
        {
            copy.serviceList.add(i, this.serviceList.get(i).deepCopy());
        }

        return copy;
    }

    public final int getServiceCount()
    {
        return this.serviceList.size();
    }

    public final PlaceNameService getService(int index)
    {
        return this.serviceList.get(index);
    }
}
