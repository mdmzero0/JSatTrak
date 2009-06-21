/*
 * CustomSunPositionProvider.java
 * =====================================================================
 * Copyright (C) 2009 Shawn E. Gano
 *
 * This file is part of JSatTrak.
 *
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 *
 * Created on 10 June 2009
 */
package name.gano.worldwind.sunshader;

import gov.nasa.worldwind.examples.sunlight.SunPositionProvider;
import gov.nasa.worldwind.geom.LatLon;
import name.gano.astro.bodies.Sun;

/**
 *
 * @author sgano
 */
public class CustomSunPositionProvider implements SunPositionProvider
{
    private Sun sun;

    public CustomSunPositionProvider(Sun sun)
    {
        this.sun = sun;
    }

    public LatLon getPosition()
	{
		return LatLon.fromRadians(sun.getCurrentLLA()[0], sun.getCurrentLLA()[1]);
	}
}
