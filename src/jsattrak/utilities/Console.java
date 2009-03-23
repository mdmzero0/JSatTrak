/*
 * =====================================================================
 * Copyright (C) 2008 Shawn E. Gano
 * 
 * This file is part of JSatTrak.
 * 
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 * 
 * Modified version of: http://www.artima.com/forums/flat.jsp?forum=1&thread=148613
 * Modifications by: Shawn Gano
 */

package jsattrak.utilities;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Console extends FilterOutputStream
{

    private static ArrayList<ConsoleListener> registeredListeners = new ArrayList<ConsoleListener>();

    static
    {
        // On its first use. Install the Console Listener
        PrintStream printStream =
                new PrintStream(
                new Console(new ByteArrayOutputStream()));
        System.setOut(printStream);
        // send errors to the console -- 23 March 2009 -- for web start debugging
        System.setErr(printStream);
    }

    public Console(OutputStream out)
    {
        super(out);
    }

    /* Override Ancestor method */
    public void write(byte b[]) throws IOException
    {
        String str = new String(b);
        logMessage(str);
    }

    /* Override Ancestor method */
    public void write(byte b[], int off, int len) throws IOException
    {
        String str = new String(b, off, len);
        logMessage(str);
    }

    /* Override Ancestor method */
    public void write(int b) throws IOException
    {
        String str = new String(new char[]{(char) b});
        logMessage(str);
    }

    public static void registerOutputListener(ConsoleListener listener)
    {
        // we don't register null listeners
        if (listener != null)
        {
            registeredListeners.add(listener);
        }
    }

    public static void removeOutputListener(ConsoleListener listener)
    {
        if (listener != null)
        {
            registeredListeners.remove(listener);
        }
    }

    private static void logMessage(String message)
    {
        // Log output to each listener
        int count = registeredListeners.size();
        for (int i = 0; i < count; i++)
        {
            ((ConsoleListener) registeredListeners.get(i)).logMessage(message);
        }
    }
}
