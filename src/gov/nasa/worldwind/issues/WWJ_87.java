/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.issues;

import gov.nasa.worldwind.examples.ApplicationTemplate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author tag
 * @version $Id: WWJ_87.java 2989 2007-09-22 17:45:45Z tgaskins $
 */
public class WWJ_87 extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            final JPanel spacer = new JPanel(new BorderLayout());
            spacer.add(new JLabel("Spacer"), BorderLayout.CENTER);
            this.getContentPane().add(spacer, BorderLayout.SOUTH);

            JPanel controls = new JPanel(new GridLayout(0, 1));

            JButton b = new JButton("Status Invisible");
            b.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    getStatusBar().setVisible(false);
                }
            });
            controls.add(b);

            b = new JButton("Status Visible");
            b.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    getStatusBar().setVisible(true);
                }
            });
            controls.add(b);

            b = new JButton("Remove Status");
            b.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    getWwjPanel().remove(getStatusBar());
                    getWwjPanel().validate();
                }
            });
            controls.add(b);

            b = new JButton("Add Status");
            b.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    getWwjPanel().add(getStatusBar(), BorderLayout.SOUTH);
                    getWwjPanel().validate();
                }
            });
            controls.add(b);

            b = new JButton("Remove Spacer");
            b.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    AppFrame.this.getContentPane().remove(spacer);
                    AppFrame.this.validate();
                }
            });
            controls.add(b);

            b = new JButton("Add Spacer");
            b.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    AppFrame.this.getContentPane().add(spacer, BorderLayout.SOUTH);
                    AppFrame.this.validate();
                }
            });
            controls.add(b);

            this.getContentPane().add(controls, BorderLayout.EAST);
            this.pack();
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind WMS-87", AppFrame.class);
    }
}
