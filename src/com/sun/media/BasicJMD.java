// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicJMD.java

package com.sun.media;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Vector;
import javax.media.*;

// Referenced classes of package com.sun.media:
//            BasicModule, BasicSourceModule, BasicFilterModule, BasicRendererModule, 
//            BasicMuxModule, JMD, OutputConnector, Connector

public final class BasicJMD extends Panel
    implements JMD, WindowListener
{
    class Con extends Button
    {

        public void flash(Color c)
        {
            Graphics g = getGraphics();
            if(g == null)
            {
                return;
            } else
            {
                g.setColor(c);
                g.fillRect(1, 1, cSize - 2, cSize - 2);
                return;
            }
        }

        public Graphics getGraphics()
        {
            g = super.getGraphics();
            return g;
        }

        public void paint(Graphics g)
        {
            g.setColor(Color.black);
            g.drawRect(0, 0, cSize - 1, cSize - 1);
            g.setColor(Color.gray);
            g.fillRect(1, 1, cSize - 2, cSize - 2);
        }

        public void setData(Buffer d)
        {
            if(mouseHere)
                updateStatus();
            data = d;
        }

        void updateStatus()
        {
            Format f = data.getFormat();
            String s;
            if(f == null)
                s = "null";
            else
                s = f.toString();
            status.setText(s + ", Length = " + data.getLength());
        }

        Graphics g;
        Buffer data;
        boolean mouseHere;

        public Con()
        {
            g = null;
            data = null;
            mouseHere = false;
            addMouseListener(new MouseAdapter() {

                public void mouseEntered(MouseEvent me)
                {
                    updateStatus();
                    mouseHere = true;
                }

                public void mouseExited(MouseEvent me)
                {
                    mouseHere = false;
                }

            }
);
        }
    }

    class ModButton extends Button
    {

        public String cropName(String name)
        {
            int box_width = 120;
            FontMetrics fm = getFontMetrics(new Font("Dialog", 0, 11));
            String cropped = name;
            int width = fm.stringWidth(cropped);
            boolean appendDots;
            if(width > box_width)
                appendDots = true;
            else
                appendDots = false;
            for(; width > box_width; width = fm.stringWidth(cropped))
            {
                int length = cropped.length();
                cropped = name.substring(0, length - 1);
            }

            if(appendDots)
                cropped = cropped + "...";
            return cropped;
        }

        public void updateStatus()
        {
            status.setText(plugin.getClass().getName() + " , " + plugin.getName());
        }

        BasicModule module;
        boolean mouseHere;
        PlugIn plugin;

        public ModButton(String name, BasicModule m, PlugIn p)
        {
            mouseHere = false;
            name = cropName(name);
            super.setLabel(name);
            module = m;
            plugin = p;
            addMouseListener(new MouseAdapter() {

                public void mouseEntered(MouseEvent me)
                {
                    updateStatus();
                    mouseHere = true;
                }

                public void mouseExited(MouseEvent me)
                {
                    mouseHere = false;
                }

            }
);
        }
    }


    public BasicJMD(String title)
    {
        modList = new Vector();
        conList = new Vector();
        graphic = true;
        frame = null;
        activated = false;
        button = null;
        preferredSize = new Dimension(512, 140);
        colMax = 1;
        roMax = 1;
        wrapWidth = 200;
        wrapHeight = 50;
        offX = 0;
        offY = 0;
        fill = 10;
        cSize = 10;
        setLayout(new BorderLayout());
        setBackground(Color.lightGray);
        center = new Panel() {

            public Dimension getPreferredSize()
            {
                return preferredSize;
            }

        }
;
        center.setLayout(null);
        add("North", center);
        status = new Label();
        add("South", status);
        setSize(512, 200);
    }

    public Component getControlComponent()
    {
        if(button == null)
        {
            button = new Button("PlugIn Viewer") {

                public void removeNotify()
                {
                    super.removeNotify();
                    dispose();
                }

            }
;
            button.setName("PlugIns");
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae)
                {
                    setVisible(true);
                }

            }
);
        }
        return button;
    }

    public synchronized void dispose()
    {
        if(frame != null)
        {
            frame.dispose();
            frame = null;
        }
    }

    public synchronized void setVisible(boolean visible)
    {
        if(getParent() == null)
        {
            if(visible && frame == null)
            {
                frame = new Frame("PlugIn Viewer");
                frame.setLayout(new BorderLayout());
                frame.add("Center", this);
                frame.addWindowListener(this);
                frame.pack();
                frame.setVisible(true);
            }
        } else
        if(getParent() == frame)
            frame.setVisible(visible);
        else
            super.setVisible(visible);
    }

    public void initGraph(BasicModule source)
    {
        center.removeAll();
        modList = new Vector();
        conList = new Vector();
        drawGraph(source);
        ro = 0;
        col = 0;
        preferredSize = new Dimension((colMax + 1) * wrapWidth + offX * 2, roMax * wrapHeight + offY * 2);
    }

    public void drawGraph(BasicModule source)
    {
        String names[] = source.getOutputConnectorNames();
        int height = names.length;
        if(height == 0)
            height = 1;
        createModuleWrap(source, ro, col, height);
        if(roMax < names.length)
            roMax = names.length;
        for(int i = 0; i < names.length; i++)
        {
            OutputConnector oc = source.getOutputConnector(names[i]);
            Module m;
            InputConnector ic;
            if((ic = oc.getInputConnector()) == null)
            {
                if(col == 0)
                    ro++;
            } else
            if((m = ic.getModule()) == null)
            {
                if(col == 0)
                    ro++;
            } else
            {
                col++;
                if(col > colMax)
                    colMax = col;
                drawGraph((BasicModule)m);
                col--;
                if(col == 0)
                    ro++;
            }
        }

    }

    public void createModuleWrap(BasicModule m, int row, int column, int h)
    {
        Object plugin = m;
        if(m instanceof BasicSourceModule)
            plugin = ((BasicSourceModule)m).getDemultiplexer();
        else
        if(m instanceof BasicFilterModule)
            plugin = ((BasicFilterModule)m).getCodec();
        else
        if(m instanceof BasicRendererModule)
            plugin = ((BasicRendererModule)m).getRenderer();
        else
        if(m instanceof BasicMuxModule)
            plugin = ((BasicMuxModule)m).getMultiplexer();
        String name = ((PlugIn)plugin).getName();
        Button b = new ModButton(name, m, (PlugIn)plugin);
        b.setName("M" + m.hashCode());
        modList.addElement(b);
        b.setBackground(new Color(192, 192, 128));
        b.setForeground(Color.black);
        center.add(b);
        b.setBounds(offX + column * wrapWidth + fill, offY + row * wrapHeight + fill, wrapWidth - fill * 2, h * wrapHeight - fill * 2);
        b.setVisible(true);
        center.invalidate();
    }

    public void moduleIn(BasicModule bm, int index, Buffer d, boolean here)
    {
        updateConnector(bm, index, d, here, 0);
    }

    public void updateConnector(BasicModule bm, int index, Buffer d, boolean here, int inOut)
    {
        if(!activated)
            return;
        Con c = findConnector(bm, index, inOut);
        if(c == null)
            return;
        c.setData(d);
        if(here)
        {
            if(d.isEOM())
                c.flash(Color.red);
            else
            if(d.isDiscard())
                c.flash(Color.yellow);
            else
                c.flash(Color.green);
        } else
        {
            c.flash(Color.gray);
        }
    }

    public void moduleOut(BasicModule bm, int index, Buffer d, boolean here)
    {
        updateConnector(bm, index, d, here, 1);
    }

    public Con findConnector(BasicModule bm, int index, int inOut)
    {
        String name = "C" + bm.hashCode() + index + inOut;
        for(Enumeration e = conList.elements(); e.hasMoreElements();)
        {
            Con c = (Con)e.nextElement();
            if(c.getName().equals(name))
                return c;
        }

        Component m = findModule(bm);
        if(m == null)
        {
            return null;
        } else
        {
            Point p = m.getLocation();
            Con c = new Con();
            center.add(c);
            c.setBounds((p.x - fill) + (wrapWidth - fill) * inOut, p.y + (wrapHeight - 2 * fill - cSize) / 2 + wrapHeight * index, cSize, cSize);
            c.setName(name);
            conList.addElement(c);
            return c;
        }
    }

    public Component findModule(BasicModule bm)
    {
        String name = "M" + bm.hashCode();
        for(Enumeration e = modList.elements(); e.hasMoreElements();)
        {
            Component c = (Component)e.nextElement();
            if(c.getName().equals(name))
                return c;
        }

        return null;
    }

    public void windowActivated(WindowEvent we)
    {
        activated = true;
    }

    public void windowOpened(WindowEvent windowevent)
    {
    }

    public void windowIconified(WindowEvent windowevent)
    {
    }

    public void windowDeiconified(WindowEvent windowevent)
    {
    }

    public void windowClosing(WindowEvent we)
    {
        setVisible(false);
    }

    public void windowClosed(WindowEvent windowevent)
    {
    }

    public void windowDeactivated(WindowEvent we)
    {
        activated = false;
    }

    Vector modList;
    Vector conList;
    boolean graphic;
    Panel center;
    Label status;
    Frame frame;
    boolean activated;
    Button button;
    Dimension preferredSize;
    int ro;
    int col;
    int colMax;
    int roMax;
    int wrapWidth;
    int wrapHeight;
    int offX;
    int offY;
    int fill;
    int cSize;
}
