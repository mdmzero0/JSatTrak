/*
 *  A Thread to handle a socket connection - all input stream from client is sent to a
 *  beanshell interperator.
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
 */

package jguiserver;

import bsh.Interpreter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import jguiserver.GuiServer.CommandServerThread;

/**
 *
 * @author sgano
 */
public class CommandMultiServerThread extends Thread   
{
    private Socket socket = null;
    Interpreter bsh;
    
    private int connectionID;
    
    private String iniDate;
    
    CommandServerThread server; // used to handle disconnects (etc)

    public CommandMultiServerThread(Socket socket, Interpreter bsh, int ID, CommandServerThread server)
    {
        super("CommandMultiServerThread");
        this.socket = socket;
        this.bsh = bsh;
        this.connectionID = ID;
        this.server = server;
        
        iniDate = now();
    }
    
    // method to get current time
    public static final String DATE_FORMAT_NOW = "HH:mm:ss dd-MMM-yyyy";
    public static String now()
    {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());

    }

    public void run()
    {

        try
        {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    socket.getInputStream()));

            String inputLine, outputLine;
            
            out.println("Connection Successful");

            while ((inputLine = in.readLine()) != null)
            {
                //outputLine = cp.processInput(inputLine);
                
                // check for an exit 
                if (inputLine.trim().equalsIgnoreCase("exit"))
                {
                    break;
                }
                
                // process command
                try
                {
                    Object rst = bsh.eval(inputLine); // run command in interperator
                    out.println(rst.toString()); // return result as a string
                }
                catch (Exception e)
                {
                    // if there is an error send this: (must send something back)
                    //out.println("ERROR: Check command syntax");
                    out.println("null"); // hmm might want to send more info, such as if the command worked or if it was a bad command? Maybe the tostring is causing problems
                }
                      
            } // read from client
            
            out.close();
            in.close();
            socket.close();
            // tell server we are closing
            server.processConnectionClose(this);

        }
        catch (IOException e)
        {
            e.printStackTrace();
            
             // tell server we are closing
            server.processConnectionClose(this);
        }
    }

    public

    int getConnectionID()
    {
        return connectionID;
    }
    
    public void close() throws Exception
    {
        socket.close();
    }
    
    public String getClientIP()
    {
        return socket.getInetAddress().toString();
    }

    public

    String getIniDate()
    {
        return iniDate;
    }
}
