/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import java.net.*;
import java.io.*;
import java.util.Scanner;

/**
 * @author tag
 * @version $Id$
 */
public class PostTest
{
    static final String HOST_NAME = "geossregistries.info:1090/GEOSSCSW202/discovery";
    static final String PATH = "/GEOSSCSW202/discovery";
    static final String SERVER_URL = "http://" + HOST_NAME;

    public static void main(String[] args)
    {
        try
        {
            StringBuilder query = new StringBuilder();
            Scanner qin = new Scanner(new File("query1.xml"));
            while (qin.hasNextLine())
            {
                query.append(qin.nextLine());
                query.append("\n");
            }

            URL url = new URL(SERVER_URL);
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            PrintWriter out = new PrintWriter(connection.getOutputStream());

            out.write("POST " + PATH + " HTTP/1.1\r\n");
            out.write("Host: " + HOST_NAME + "\r\n");
            out.write("Content-Length: " + query.length() + "\r\n");
            out.write("Content-Type: text/xml; charset=\"utf-8\"\r\n");
            out.write("\r\n");
            out.print(query.toString());
            out.flush();

            StringBuilder sb = new StringBuilder();
            Scanner in = new Scanner(connection.getInputStream());
            while (in.hasNextLine())
            {
                sb.append(in.nextLine());
                sb.append("\n");
            }
            out.close();

            System.out.println(sb.toString());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
