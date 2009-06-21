/*
 * TimeDepRenderable.java
 * 
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
 * Created 20 June 2009
 */

package name.gano.worldwind.objects;

import gov.nasa.worldwind.render.Renderable;

/**
 * Makes and object tim dependant and provides and interface for updating the currnet time
 * @author Shawn Gano
 */
public interface TimeDepRenderable extends Renderable
{
    public void updateMJD(double currentMJD);
}
