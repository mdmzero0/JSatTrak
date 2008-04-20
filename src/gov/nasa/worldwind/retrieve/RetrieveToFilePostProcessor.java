/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.retrieve;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.Logging;

import java.util.logging.Level;

/**
 * @author Tom Gaskins
 * @version $Id: RetrieveToFilePostProcessor.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public final class RetrieveToFilePostProcessor implements RetrievalPostProcessor
{
    java.io.File destination;

    /**
     * @param destination
     * @throws IllegalArgumentException if <code>destination</code> is null
     */
    public RetrieveToFilePostProcessor(java.io.File destination)
    {
        if (destination == null)
        {
            String message = Logging.getMessage("nullValue.DestNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.destination = destination;
    }

    /**
     * @param retriever
     * @return
     * @throws IllegalArgumentException if <code>retriever</code> is null
     */
    public java.nio.ByteBuffer run(Retriever retriever)
    {
        if (retriever == null)
        {
            String message = Logging.getMessage("nullValue.RetrieverIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            java.nio.ByteBuffer buffer = retriever.getBuffer();
            if (buffer == null)
            {
                Logging.logger().log(Level.SEVERE, "RetrieveToFilePostProcessor.NullBufferPostprocessing",
                    retriever.getName());
                return null;
            }

            java.io.FileOutputStream fos = null;
            try
            {
                fos = new java.io.FileOutputStream(this.destination);
                fos.getChannel().write(buffer);
                return null;
            }
            catch (java.io.IOException e)
            {
                throw e;
            }
            finally
            {
                if (fos != null)
                    fos.close();
            }
        }
        catch (java.io.IOException e)
        {
            String message = Logging.getMessage("RetrieveToFilePostProcessor.ErrorPostprocessing", retriever.getName());
            Logging.logger().severe(message);
            throw new WWRuntimeException(message, e);
        }
    }
}
