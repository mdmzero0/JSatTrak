/*
 * ProgressStatus.java
 *
 * An Object to hold the current progress and status for a JProgressBar
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
 *
 * Created on 22 March 2009
 */
package jsattrak.utilities;

/**
 *
 * @author Shawn E. Gano
 */
public class ProgressStatus
{
    private int percentComplete = 0;
    private String statusText = "";

    /**
     * Default ProgressStatus object, intialized to 0 percent and no statusText
     */
    public ProgressStatus()
    {
    }

    /**
     * Creates a Progress Status initialized to the input parameters
     * @param percentComplete
     * @param statusText
     */
    public ProgressStatus(int percentComplete, String statusText)
    {
        this.percentComplete = percentComplete;
        this.statusText = statusText;
    }

    /**
     * @return the percentComplete
     */
    public int getPercentComplete()
    {
        return percentComplete;
    }

    /**
     * @param percentComplete the percentComplete to set
     */
    public void setPercentComplete(int percentComplete)
    {
        this.percentComplete = percentComplete;
    }

    /**
     * @return the statusText
     */
    public String getStatusText()
    {
        return statusText;
    }

    /**
     * @param statusText the statusText to set
     */
    public void setStatusText(String statusText)
    {
        this.statusText = statusText;
    }

}
