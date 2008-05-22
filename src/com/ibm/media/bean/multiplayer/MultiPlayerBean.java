// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MultiPlayerBean.java

package com.ibm.media.bean.multiplayer;

import java.applet.Applet;
import java.applet.AppletContext;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EventObject;
import java.util.Vector;
import javax.media.*;
import javax.media.bean.playerbean.MediaPlayer;

// Referenced classes of package com.ibm.media.bean.multiplayer:
//            ImageButton, RelatedLink, MediaGroup, JMFUtil, 
//            ImageLabel

public class MultiPlayerBean extends Panel
    implements ActionListener, Serializable, ControllerListener, ComponentListener
{

    public MultiPlayerBean()
    {
        currentPlayer = null;
        gridbag = new GridBagLayout();
        constraint = new GridBagConstraints();
        currentU = null;
        displayURL = false;
        panelVisible = true;
        fitVideo = true;
        looping = true;
        sequential = true;
        mpCodeBase = null;
        mpAppletContext = null;
        numOfClips = 0;
        numOfMGroups = 0;
        preferredHeight = 400;
        preferredWidth = 300;
        maxMediaGroup = 10;
        loadOnInit = false;
        fixAspectRatio = true;
        startingButton = 1;
        buttonPosition = JMFUtil.getString("SOUTH");
        tabsize = 16;
        msVM = false;
        changes = new PropertyChangeSupport(this);
        setup();
    }

    private void getContext(Object parent)
    {
        if(parent == null)
            parent = getParent();
        if(mpCodeBase != null)
            return;
        if(parent instanceof Applet)
        {
            mpCodeBase = ((Applet)parent).getCodeBase();
            mpAppletContext = ((Applet)parent).getAppletContext();
        }
    }

    private void setup()
    {
        getContext(null);
        if(!JMFUtil.msVersion())
        {
            msVM = false;
            doDebug("Not Microsoft VM");
        } else
        {
            msVM = true;
            doDebug("Microsoft VM");
        }
        setLayout(new BorderLayout(0, 0));
        setBackground(getBackground());
        currentPlayer = null;
        buttonPanel = new Panel();
        buttonPanel.setLayout(new BorderLayout(0, 0));
        buttonPanel.setBackground(getBackground());
        buttonPanel.setVisible(false);
        gifPanel = new Panel();
        gifPanel.setBackground(getBackground());
        gifPanel.setVisible(false);
        gifPanel.addComponentListener(this);
        scrollPanel = new Panel();
        scrollPanel.setLayout(new BorderLayout(0, 0));
        scrollPanel.setBackground(getBackground());
        scrollPanel.add("Center", gifPanel);
        buttonPanel.add("Center", scrollPanel);
        infoButton = new Button(JMFUtil.getString("Link"));
        infoButton.setActionCommand("info");
        infoButton.addActionListener(this);
        infoButton.setEnabled(true);
        infoButton.setVisible(true);
        utilPanel = new Panel();
        utilPanel.setBackground(getBackground());
        utilPanel.setVisible(false);
        utilPanel.add("Center", infoButton);
        videoPanel = new Panel() {

            public Insets insets()
            {
                return new Insets(10, 10, 10, 10);
            }

        }
;
        videoPanel.setBounds(getBounds().x, getBounds().y, getSize().width, getSize().height - buttonPanel.getSize().height);
        videoPanel.setLayout(new BorderLayout(0, 0));
        videoPanel.setBackground(getBackground());
        videoPanel.setVisible(true);
        utilPanel.setVisible(true);
        buttonPanel.setVisible(true);
        videoPanel.validate();
        setLayout(new BorderLayout());
        position();
    }

    private void position()
    {
        remove(videoPanel);
        remove(buttonPanel);
        if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("WEST")))
        {
            add("Center", videoPanel);
            add("West", buttonPanel);
        } else
        if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("EAST")))
        {
            add("Center", videoPanel);
            add("East", buttonPanel);
        } else
        if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("SOUTH")))
        {
            add("Center", videoPanel);
            add("South", buttonPanel);
        } else
        if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("NORTH")))
        {
            add("Center", videoPanel);
            add("North", buttonPanel);
        } else
        {
            add("Center", videoPanel);
        }
        updateGifPanel();
    }

    private void createLeftRight(int height)
    {
        left = new ImageButton("<", true, tabsize, height);
        right = new ImageButton(">", true, tabsize, height);
        left.setActionCommand("left");
        right.setActionCommand("right");
        left.addActionListener(this);
        right.addActionListener(this);
        left.setBackground(Color.lightGray);
        right.setBackground(Color.lightGray);
    }

    private void createUpDown(int width)
    {
        up = new ImageButton("UP", true, width, tabsize);
        down = new ImageButton("DOWN", true, width, tabsize);
        up.setActionCommand("up");
        down.setActionCommand("down");
        up.addActionListener(this);
        down.addActionListener(this);
        up.setBackground(Color.lightGray);
        down.setBackground(Color.lightGray);
    }

    public void start()
    {
        doDebug("in start()");
        if(loadOnInit)
        {
            for(int i = 0; i < numOfMGroups; i++)
                mGroups[i].setPlayer(formPlayer(mGroups[i].mediaName));

        }
        videoPanel.setBounds(getBounds().x, getBounds().y, getSize().width, getSize().height - buttonPanel.getSize().height);
        videoPanel.setVisible(true);
        pauseCurrent();
        setPlayer(1);
        validate();
        repaint();
    }

    public void stop()
    {
        doDebug("in stop()");
        if(currentPlayer != null)
            currentPlayer.stop();
    }

    public void destroy()
    {
        doDebug("in destroy()");
        currentPlayer.stop();
        currentPlayer.close();
        numOfClips = 0;
        System.out.println("out destroy");
    }

    public synchronized void controllerUpdate(ControllerEvent evt)
    {
        if(evt instanceof EndOfMediaEvent)
        {
            if(!looping && currentPlayer != null)
                currentPlayer.setMediaTime(new Time(0L));
            if(sequential)
                nextPlayer();
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getActionCommand().equalsIgnoreCase("info") && mpAppletContext != null)
        {
            doDebug("Info: currentNum = " + currentNum);
            if(mGroups[currentNum - 1].getRelated() != null)
            {
                Vector links = mGroups[currentNum - 1].getRelated();
                int numLinks = links.size();
                double currentTime = currentPlayer.getMediaTime().getSeconds();
                for(int i = 0; i < numLinks; i++)
                {
                    RelatedLink l = (RelatedLink)links.elementAt(i);
                    if((double)l.startTime <= currentTime && (l.stopTime == 0L || (double)l.stopTime >= currentTime))
                    {
                        mpAppletContext.showDocument(l.uLink, infoWindow);
                        i = numLinks;
                    }
                }

            }
        } else
        if(e.getActionCommand().equalsIgnoreCase("left") || e.getActionCommand().equalsIgnoreCase("up"))
            shiftButtons(-1);
        if(e.getActionCommand().equalsIgnoreCase("right") || e.getActionCommand().equalsIgnoreCase("down"))
        {
            shiftButtons(1);
        } else
        {
            int buttonIndex;
            try
            {
                buttonIndex = Integer.parseInt(e.getActionCommand());
            }
            catch(NumberFormatException n)
            {
                return;
            }
            doDebug("Button " + buttonIndex + " pressed.");
            if(currentNum != buttonIndex)
            {
                pauseCurrent();
                setPlayer(buttonIndex);
            }
        }
    }

    protected URL getURL(String filename)
    {
        URL url = null;
        doDebug(filename);
        try
        {
            if((!filename.startsWith("http") && !filename.startsWith("file")) & (mpCodeBase != null))
                url = new URL(mpCodeBase, filename);
            else
                url = new URL(filename);
        }
        catch(MalformedURLException e)
        {
            System.out.println(JMFUtil.getString("InvalidURL:") + filename);
            return null;
        }
        return url;
    }

    private void videoResize()
    {
        doDebug("videoResize");
        MediaPlayer pb = currentPlayer;
        if(pb == null)
            return;
        Dimension d = getSize();
        int height = d.height;
        int width = d.width;
        int maxHeight = 10;
        buttonPanel.validate();
        Dimension buttonD = buttonPanel.getSize();
        if(maxHeight < buttonD.height)
            maxHeight = buttonD.height;
        pb.setBounds(getBounds().x, getBounds().y, width, height - maxHeight);
        while(pb.getState() != 500 && pb.getState() != 600) 
            try
            {
                Thread.sleep(200L);
            }
            catch(InterruptedException ie) { }
    }

    private MediaPlayer formPlayer(String mediaName)
    {
        try
        {
            mediaName = mediaName.trim();
            if(mediaName.length() != 0)
            {
                doDebug("Forming player: " + mediaName + "| CodeBase = " + mpCodeBase);
                ClassLoader cl = getClass().getClassLoader();
                MediaPlayer pb = (MediaPlayer)Beans.instantiate(cl, "javax.media.bean.playerbean.MediaPlayer");
                if((!mediaName.startsWith("http") && !mediaName.startsWith("file")) & (mpCodeBase != null))
                    mediaName = mpCodeBase + mediaName;
                doDebug("loading: 111" + mediaName);
                pb.setMediaLocation(mediaName);
                pb.prefetch();
                pb.waitForState(500);
                System.out.println("Wait for state done");
                pb.addControllerListener(this);
                pb.addComponentListener(this);
                pb.setVisible(false);
                pb.setBackground(getBackground());
                pb.setMediaLocationVisible(displayURL);
                pb.setPlaybackLoop(looping);
                pb.setControlPanelVisible(panelVisible);
                numOfClips++;
                System.out.println("num of Clips=" + numOfClips);
                Dimension d = getSize();
                int height = d.height;
                int width = d.width;
                int maxHeight = 0;
                int pbHeight = 0;
                int pbWidth = 0;
                Dimension buttonD = buttonPanel.getSize();
                if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("NORTH")) || buttonPosition.equalsIgnoreCase(JMFUtil.getString("SOUTH")))
                {
                    pbHeight = height - buttonD.height;
                    pbWidth = width;
                } else
                if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("WEST")) || buttonPosition.equalsIgnoreCase(JMFUtil.getString("EAST")))
                {
                    pbHeight = height;
                    pbWidth = width - buttonD.width;
                }
                if(fixAspectRatio)
                {
                    Component vc = pb.getVisualComponent();
                    float aspect = 1.0F;
                    if(vc != null)
                    {
                        int cHeight = 0;
                        if(pb.getControlPanelComponent() != null && panelVisible)
                            cHeight = pb.getControlPanelComponent().getSize().height;
                        if(displayURL)
                            cHeight += 23;
                        Dimension pbD = vc.getPreferredSize();
                        aspect = JMFUtil.aspectRatio(pbD.width, pbD.height, 0);
                        pbHeight = (int)((float)width / aspect) + cHeight;
                        if(pbHeight > height)
                        {
                            pbWidth = (int)(aspect * (float)(height - cHeight));
                            pbHeight = height - maxHeight;
                        } else
                        {
                            pbWidth = width;
                        }
                    }
                }
                pb.setBounds(getBounds().x, getBounds().y, pbWidth, pbHeight);
                doDebug("Number of Clips: " + numOfClips);
                return pb;
            } else
            {
                return null;
            }
        }
        catch(Exception e)
        {
            System.err.println(JMFUtil.getString("PlayerBeanNotGood") + e);
            e.printStackTrace();
            return null;
        }
    }

    private boolean containsButton(Component but)
    {
        Component comps[] = gifPanel.getComponents();
        for(int i = 0; i < comps.length; i++)
            if(comps[i] == but)
                return true;

        return false;
    }

    protected ImageButton formButton(URL i, int index)
    {
        doDebug("Form image button");
        ImageButton newButton = new ImageButton(i);
        newButton.setActionCommand(Integer.toString(index));
        newButton.addActionListener(this);
        newButton.setEnabled(true);
        newButton.waitForImage(true);
        return newButton;
    }

    protected ImageButton formButton(int index)
    {
        doDebug("Form Index button");
        ImageButton newButton = new ImageButton(Integer.toString(index), true, 80, 60);
        newButton.setActionCommand(Integer.toString(index));
        newButton.addActionListener(this);
        newButton.waitForImage(true);
        return newButton;
    }

    public void pauseCurrent()
    {
        if(currentNum <= numOfMGroups && currentNum > 0)
        {
            ImageButton imgB = mGroups[currentNum - 1].getButton();
            imgB.setBorderColor(new Color(160, 160, 160));
            imgB.drawBorder(true);
        }
        if(currentPlayer != null)
        {
            currentPlayer.stop();
            currentPlayer.close();
            currentPlayer.setVisible(false);
            videoPanel.remove(currentPlayer);
            doDebug("Exiting PauseCurrent.");
        }
    }

    private void changeCurrentPlayer(MediaPlayer newPB, int num)
    {
        currentNum = num;
        currentPlayer = newPB;
        doDebug("Switching current Player");
        currentPlayer.setVisible(true);
        videoPanel.add(currentPlayer);
        JMFUtil.center(videoPanel, currentPlayer, true, currentPlayer.getMediaLocationHeight() + currentPlayer.getControlPanelHeight());
        videoPanel.setVisible(true);
        try
        {
            if(currentPlayer.getState() != 600)
                currentPlayer.start();
            else
                System.out.println("Player already in started state.");
        }
        catch(ClockStartedError cse)
        {
            currentPlayer.close();
            currentPlayer.deallocate();
            currentPlayer.start();
        }
        currentPlayer.setVisible(true);
        currentPlayer.waitForState(600);
        currentPlayer.invalidate();
        videoPanel.validate();
        videoPanel.setVisible(true);
    }

    public void nextPlayer()
    {
        if(numOfMGroups <= 1)
            return;
        pauseCurrent();
        currentNum++;
        if(currentNum > numOfMGroups)
            currentNum = 1;
        for(; !containsButton(mGroups[currentNum - 1].getButton()); shiftButtons(1));
        doDebug("Next Player");
        setPlayer(currentNum);
    }

    public void previousPlayer()
    {
        if(numOfMGroups <= 1)
            return;
        pauseCurrent();
        currentNum--;
        if(currentNum < 1)
            currentNum = numOfMGroups;
        for(; !containsButton(mGroups[currentNum - 1].getButton()); shiftButtons(-1));
        doDebug("Previous Player");
        setPlayer(currentNum);
    }

    public void setPlayer(int current)
    {
        MediaGroup mGrp = null;
        ImageButton imgBut = null;
        if(current < 1 || current > numOfMGroups)
            return;
        mGrp = mGroups[current - 1];
        mGrp.setPlayer(formPlayer(mGrp.mediaName));
        if(!containsButton(mGrp.getButton()))
        {
            startingButton = currentNum;
            updateGifPanel();
        }
        if(mGrp.getRelated() != null && mpAppletContext != null)
        {
            if(mGrp.getRelated().size() > 0)
            {
                if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("SOUTH")) || buttonPosition.equalsIgnoreCase(JMFUtil.getString("NORTH")))
                {
                    buttonPanel.add("South", utilPanel);
                    validate();
                } else
                if(!buttonPosition.equalsIgnoreCase(JMFUtil.getString("NONE")))
                {
                    videoPanel.add("South", utilPanel);
                    validate();
                }
            } else
            {
                buttonPanel.remove(utilPanel);
                videoPanel.remove(utilPanel);
                validate();
            }
        } else
        {
            buttonPanel.remove(utilPanel);
            videoPanel.remove(utilPanel);
        }
        imgBut = mGrp.getButton();
        imgBut.requestFocus();
        imgBut.setBorderColor(Color.darkGray);
        imgBut.drawBorder(false);
        currentPlayer = mGrp.getPlayer();
        changeCurrentPlayer(currentPlayer, current);
    }

    public boolean isFixAspectRatio()
    {
        return fixAspectRatio;
    }

    public void setFixAspectRatio(boolean f)
    {
        boolean old = fixAspectRatio;
        if(old != f)
        {
            for(int i = 0; i < numOfClips; i++);
        }
        fixAspectRatio = f;
        changes.firePropertyChange("fixAspectRatio", new Boolean(old), new Boolean(f));
    }

    public String getButtonPosition()
    {
        return buttonPosition;
    }

    public void setButtonPosition(String p)
    {
        String old = buttonPosition;
        if(!old.equalsIgnoreCase(p))
        {
            buttonPosition = p;
            position();
            changes.firePropertyChange("buttonPosition", new String(old), new String(p));
        }
    }

    public boolean isSequentialPlay()
    {
        return sequential;
    }

    public void setSequentialPlay(boolean b)
    {
        boolean old = sequential;
        if(old != b)
        {
            sequential = b;
            changes.firePropertyChange("sequentialPlay", new Boolean(old), new Boolean(b));
        }
    }

    public boolean isLooping()
    {
        return looping;
    }

    public void setLooping(boolean l)
    {
        boolean old = looping;
        if(old != l)
        {
            for(int i = 0; i < numOfClips; i++)
                mGroups[i].getPlayer().setPlaybackLoop(l);

        }
        looping = l;
        changes.firePropertyChange("looping", new Boolean(old), new Boolean(l));
    }

    public boolean isPanelVisible()
    {
        return panelVisible;
    }

    public void setPanelVisible(boolean val)
    {
        if(panelVisible != val)
        {
            for(int i = 0; i < numOfClips; i++)
                mGroups[i].getPlayer().setControlPanelVisible(val);

            validate();
            panelVisible = val;
            changes.firePropertyChange("panelVisible", new Boolean(!val), new Boolean(val));
        }
    }

    public boolean isURLVisible()
    {
        return displayURL;
    }

    public void setURLVisible(boolean val)
    {
        if(displayURL != val)
        {
            for(int i = 0; i < numOfClips; i++)
                mGroups[i].getPlayer().setMediaLocationVisible(val);

            validate();
            displayURL = val;
            changes.firePropertyChange("URLVisible", new Boolean(!val), new Boolean(val));
        }
    }

    public boolean isFitVideo()
    {
        return fitVideo;
    }

    public void setFitVideo(boolean f)
    {
        boolean old = fitVideo;
        if(old != f)
        {
            for(int i = 0; i < numOfClips; i++);
            fitVideo = f;
        }
    }

    public boolean getLoadOnInit()
    {
        return loadOnInit;
    }

    public void setLoadOnInit(boolean b)
    {
        boolean old = loadOnInit;
        if(old != b)
        {
            loadOnInit = b;
            changes.firePropertyChange("loadOnInit", new Boolean(old), new Boolean(b));
        }
    }

    public void moveUp(int index)
    {
        MediaGroup mg = mGroups[index];
        if(index > 0)
        {
            mGroups[index] = mGroups[index - 1];
            mGroups[index - 1] = mg;
            mGroups[index].setIndex(index);
            mGroups[index - 1].setIndex(index - 1);
            mGroups[index].getButton().setActionCommand(Integer.toString(index + 1));
            mGroups[index - 1].getButton().setActionCommand(Integer.toString(index));
            if(switchLinkIndex(Integer.toString(index), Integer.toString(index + 1)))
                setLinks(links);
            doDebug("ActionCommand = " + (index + 1));
            doDebug("ActionCommand = " + index);
        }
        for(int i = index - 1; i < numOfMGroups; i++)
        {
            ImageButton b = mGroups[i].getButton();
            gifPanel.remove(b);
            gifPanel.add(b);
        }

        validate();
    }

    public void moveDown(int index)
    {
        MediaGroup mg = mGroups[index];
        if(index < numOfMGroups - 1)
        {
            mGroups[index] = mGroups[index + 1];
            mGroups[index + 1] = mg;
            mGroups[index].setIndex(index);
            mGroups[index + 1].setIndex(index + 1);
            mGroups[index].getButton().setActionCommand(Integer.toString(index + 1));
            mGroups[index + 1].getButton().setActionCommand(Integer.toString(index + 2));
            if(switchLinkIndex(Integer.toString(index + 1), Integer.toString(index + 2)))
                setLinks(links);
            doDebug("moveDown(i):ActionCommand=" + (index + 1));
            doDebug("moveDown(i):ActionCommand=" + (index + 2));
        }
        for(int i = index; i < numOfMGroups; i++)
        {
            ImageButton b = mGroups[i].getButton();
            doDebug("Moving media " + mGroups[i].mediaName);
            gifPanel.remove(b);
            gifPanel.add(b);
        }

        validate();
    }

    public void addMGroup(String mediaName, String buttonGif)
    {
        doDebug("adding media group - " + numOfMGroups);
        if(mGroups == null)
            mGroups = new MediaGroup[maxMediaGroup];
        if(numOfMGroups < maxMediaGroup)
            mGroups[numOfMGroups] = new MediaGroup(mediaName, buttonGif, "", this);
        else
            System.out.println(JMFUtil.getString("MaxMGroup"));
        doDebug("Number of next media group = " + numOfMGroups);
    }

    public void deleteMGroup(int i)
    {
        boolean changed = false;
        String index = Integer.toString(i + 1);
        doDebug("i = " + i);
        doDebug("index = " + index);
        doDebug("MGroups length = " + mGroups.length);
        String temp[] = null;
        MediaGroup mg = mGroups[i];
        int linksLength = 0;
        if(links != null)
            linksLength = links.length;
        for(int j = 0; j < linksLength; j += 4)
            if(index.equals(links[j]))
            {
                changed = true;
                temp = new String[links.length - 4];
                JMFUtil.copyShortenStringArray(links, temp, j, 4);
            }

        for(int j = i; j < numOfMGroups - 1; j++)
        {
            mGroups[j] = mGroups[j + 1];
            if(links != null)
            {
                index = Integer.toString(j + 1);
                if(changed)
                {
                    for(int k = 0; k < temp.length; k += 4)
                        if(temp[k].equals(index))
                            temp[k] = Integer.toString(j);

                } else
                {
                    temp = new String[linksLength];
                    JMFUtil.copyStringArray(links, temp);
                    for(int k = 0; k < temp.length; k += 4)
                        if(temp[k].equals(index))
                        {
                            changed = true;
                            temp[k] = Integer.toString(j);
                        }

                }
            }
        }

        if(changed)
            setLinks(temp);
        mGroups[numOfMGroups - 1] = null;
        numOfMGroups--;
        doDebug("Remove button " + mg.gifName);
        gifPanel.remove(mg.getButton());
        updateGifPanel();
        validate();
    }

    private boolean switchLinkIndex(String index1, String index2)
    {
        boolean changed = false;
        if(links != null)
        {
            for(int i = 0; i < links.length; i += 4)
                if(index1.equals(links[i]))
                {
                    doDebug("Switched " + links[i] + " with " + index2);
                    changed = true;
                    links[i] = index2;
                } else
                if(index2.equals(links[i]))
                {
                    doDebug("Switched " + links[i] + " with " + index1);
                    changed = true;
                    links[i] = index1;
                }

        }
        return changed;
    }

    public void replaceMGroup(int i, String mName, String gName)
    {
        if(i < numOfMGroups)
        {
            if(i < numOfMGroups - 1)
            {
                MediaGroup m1 = mGroups[i + 1];
                if(m1.mediaName.equals(mName) & m1.gifName.equals(gName))
                {
                    moveDown(i);
                    return;
                }
            }
            MediaGroup mg = mGroups[i];
            if(mg.getPlayer() != null)
            {
                MediaPlayer pb = mg.getPlayer();
                pb.close();
                pb.deallocate();
                mg.setPlayer(null);
            }
            doDebug("Media Name = " + mName);
            doDebug("Gif Name = " + gName);
            mg.mediaName = mName;
            mg.gifName = gName;
            if(gName != null || gName.compareTo("") != 0)
                mg.setButton(formButton(getURL(gName), i + 1));
            else
                mg.setButton(formButton(i + 1));
            doDebug("Replacing " + i);
            refreshAllButtons();
        }
    }

    private void refreshAllButtons()
    {
        gifPanel.removeAll();
        for(int i = 0; i < numOfMGroups; i++)
            gifPanel.add(mGroups[i].getButton());

        validate();
    }

    public boolean addLink(int index, String l, long start, long end)
    {
        doDebug("adding link: index = " + index);
        doDebug("Link = " + l + " start=" + start + " stop=" + end);
        try
        {
            RelatedLink link = new RelatedLink(l, start, end, this);
            mGroups[index - 1].setRelated(link);
        }
        catch(MalformedURLException e)
        {
            System.out.println(JMFUtil.getString("InvalidURL"));
            return false;
        }
        if(!duplicate(Integer.toString(index + 1), l, Long.toString(start), Long.toString(end)))
        {
            int len = 0;
            if(links != null)
                len = links.length;
            String temp[] = new String[len + 4];
            JMFUtil.copyStringArray(links, temp);
            temp[len] = Integer.toString(index + 1);
            temp[len + 1] = l;
            temp[len + 2] = Long.toString(start);
            temp[len + 3] = Long.toString(end);
        }
        return true;
    }

    private boolean duplicate(String mIndex, String url, String start, String stop)
    {
        if(links != null)
        {
            for(int i = 0; i < links.length; i += 4)
                if(links[i].equals(mIndex) & links[i + 1].equals(url) & links[i + 2].equals(start) & links[i + 3].equals(stop))
                    return true;

        }
        return false;
    }

    public void setMGroups(MediaGroup indexprop[])
    {
        MediaGroup oldValue[] = mGroups;
        mGroups = indexprop;
        changes.firePropertyChange("mGroups", oldValue, indexprop);
    }

    public void setMGroups(int index, MediaGroup indexprop)
    {
        MediaGroup oldValue[] = mGroups;
        mGroups[index] = indexprop;
        changes.firePropertyChange("mGroups", oldValue, mGroups);
    }

    public MediaGroup[] getMGroups()
    {
        return mGroups;
    }

    public MediaGroup getMGroups(int index)
    {
        return mGroups[index];
    }

    public void setMediaNames(String indexprop[])
    {
        String oldValue[] = mediaNames;
        mediaNames = indexprop;
        doDebug("setmediaNames " + mediaNames);
        updateMGroups(mediaNames);
        updateLinks(links);
        doDebug("updateMGroups done");
        updateGifPanel();
        changes.firePropertyChange("mediaNames", oldValue, indexprop);
    }

    public void setMediaNames(String indexprop[], Object t)
    {
        String oldValue[] = mediaNames;
        mediaNames = indexprop;
        doDebug("setmediaNames " + mediaNames);
        getContext(t);
        updateMGroups(mediaNames);
        updateLinks(links);
        doDebug("updateMGroups done");
        updateGifPanel();
        changes.firePropertyChange("mediaNames", oldValue, indexprop);
    }

    public void setMediaNames(int index, String indexprop)
    {
        String oldValue[] = mediaNames;
        mediaNames[index] = indexprop;
        String mediaName;
        String gifName;
        if(index % 2 == 0)
        {
            mediaName = mediaNames[index];
            gifName = mediaNames[index + 1];
        } else
        {
            mediaName = mediaNames[index - 1];
            gifName = mediaNames[index];
        }
        updateMGroups(index / 2, mediaName, gifName);
        updateGifPanel();
        changes.firePropertyChange("mediaNames", oldValue, mediaNames);
    }

    private boolean updateMGroups(String mNames[])
    {
        boolean changed = false;
        int mNamesLength = 0;
        int mindex = 0;
        if(mNames != null)
            mNamesLength = mNames.length;
        doDebug("Updating MGroups " + mNamesLength);
        for(int i = 0; i < mNamesLength; i += 2)
        {
            mindex = i / 2;
            if(changed)
                updateMGroups(mindex, mNames[i], mNames[i + 1]);
            else
                changed = updateMGroups(mindex, mNames[i], mNames[i + 1]);
        }

        if(mNamesLength / 2 < numOfMGroups)
        {
            for(int i = numOfMGroups - 1; i >= mNamesLength / 2; i--)
            {
                doDebug("Deleting mGroup " + i);
                mGroups[i].delete();
            }

        }
        return changed;
    }

    private boolean updateMGroups(int i, String mediaName, String gifName)
    {
        doDebug("Index=" + i + " Media=" + mediaName + " Gif=" + gifName);
        if(i < numOfMGroups)
        {
            MediaGroup m = mGroups[i];
            if((!m.mediaName.equals(mediaName)) | (!m.gifName.equals(gifName)))
            {
                replaceMGroup(i, mediaName, gifName);
                return true;
            } else
            {
                return false;
            }
        } else
        {
            addMGroup(mediaName, gifName);
            return true;
        }
    }

    public String[] getMediaNames()
    {
        return mediaNames;
    }

    public String getMediaNames(int index)
    {
        return mediaNames[index];
    }

    public void setLinks(String indexprop[])
    {
        String oldValue[] = links;
        boolean changed = false;
        links = indexprop;
        doDebug("Inside setLinks(String[])");
        changed = deleteLinks(links);
        doDebug("updating links...");
        if(updateLinks(links) | changed)
            changes.firePropertyChange("links", oldValue, indexprop);
    }

    public void setLinks(int index, String indexprop)
    {
        String oldValue[] = links;
        String mediaNum = "";
        String relatedName = "";
        String startString = "";
        String stopString = "";
        links[index] = indexprop;
        if(index % 4 == 0)
        {
            mediaNum = links[index];
            relatedName = links[index + 1];
            startString = links[index + 2];
            stopString = links[index + 3];
        } else
        if(index % 4 == 1)
        {
            mediaNum = links[index - 1];
            relatedName = links[index];
            startString = links[index + 1];
            stopString = links[index + 2];
        } else
        if(index % 3 == 2)
        {
            mediaNum = links[index - 2];
            relatedName = links[index - 1];
            startString = links[index];
            stopString = links[index + 1];
        } else
        if(index % 3 == 4)
        {
            mediaNum = links[index - 3];
            relatedName = links[index - 2];
            startString = links[index - 1];
            stopString = links[index];
        }
        if(updateLinks(mediaNum, relatedName, startString, stopString))
            changes.firePropertyChange("links", oldValue, links);
    }

    private boolean deleteLinks(String linksList[])
    {
        int index = 0;
        Vector rl = null;
        RelatedLink l = null;
        boolean changed = false;
        boolean match = false;
        for(int i = 0; i < numOfMGroups; i++)
        {
            rl = mGroups[i].getRelated();
            if(rl != null)
            {
                for(int j = 0; j < rl.size(); j++)
                {
                    l = (RelatedLink)rl.elementAt(j);
                    match = false;
                    if(linksList != null)
                    {
                        for(int k = 0; k < linksList.length; k += 4)
                        {
                            if(!(Integer.toString(i).equals(linksList[k]) & l.link.equals(linksList[k + 1]) & Long.toString(l.startTime).equals(linksList[k + 2]) & Long.toString(l.stopTime).equals(linksList[k + 3])))
                                continue;
                            match = true;
                            break;
                        }

                    }
                    if(!match)
                    {
                        rl.removeElementAt(j);
                        changed = true;
                    }
                }

            }
        }

        return changed;
    }

    private boolean updateLinks(String linksList[])
    {
        boolean changed = false;
        if(linksList != null)
        {
            doDebug("links length: " + linksList.length);
            if(msVM)
            {
                for(int i = 0; i < linksList.length; i++)
                {
                    String temp = linksList[i];
                }

            }
            for(int i = 0; i < linksList.length; i += 4)
                if(changed)
                    updateLinks(linksList[i], linksList[i + 1], linksList[i + 2], linksList[i + 3]);
                else
                    changed = updateLinks(linksList[i], linksList[i + 1], linksList[i + 2], linksList[i + 3]);

        }
        return changed;
    }

    private boolean updateLinks(String mediaNum, String relatedName, String startString, String stopString)
    {
        doDebug("MediaNum=" + mediaNum + " related=" + relatedName);
        doDebug("Start=" + startString + " Stop=" + stopString);
        int i = Integer.parseInt(mediaNum) - 1;
        long start = 0L;
        long stop = 0L;
        boolean dup = false;
        try
        {
            start = Long.parseLong(startString);
            stop = Long.parseLong(stopString);
        }
        catch(NumberFormatException n)
        {
            return false;
        }
        if((i < numOfMGroups) & (i >= 0))
        {
            MediaGroup m = mGroups[i];
            Vector r = m.getRelated();
            RelatedLink rl = null;
            if(r != null)
            {
                for(int j = 0; j < r.size(); j++)
                {
                    rl = (RelatedLink)r.elementAt(j);
                    if(rl.link.equals(relatedName) & (rl.startTime == start) & (rl.stopTime == stop))
                    {
                        dup = true;
                        j = r.size();
                        return false;
                    }
                }

            }
            if(!dup)
            {
                try
                {
                    m.setRelated(new RelatedLink(relatedName, start, stop, this));
                }
                catch(MalformedURLException me)
                {
                    System.out.println(JMFUtil.getBIString("BadURL") + relatedName);
                    return false;
                }
                return true;
            }
        }
        System.out.println(JMFUtil.getBIString("MEDIA_GROUP_BOUNDS"));
        return false;
    }

    public String[] getLinks()
    {
        return links;
    }

    public String getLinks(int index)
    {
        return links[index];
    }

    public void setAppletContext(AppletContext ac)
    {
        mpAppletContext = ac;
    }

    public void setCodeBase(URL cb)
    {
        mpCodeBase = cb;
    }

    public void addControllerListener(ControllerListener listener)
    {
        for(int i = 0; i < numOfClips; i++)
            mGroups[i].getPlayer().addControllerListener(listener);

    }

    public void removeControllerListener(ControllerListener listener)
    {
        for(int i = 0; i < numOfClips; i++)
            mGroups[i].getPlayer().removeControllerListener(listener);

    }

    public int getNumberOfMediaGroups()
    {
        return numOfMGroups;
    }

    public void setBounds(int x, int y, int w, int h)
    {
        super.setBounds(x, y, w, h);
        Dimension d = getSize();
        int height = d.height;
        int maxHeight = 10;
        Dimension buttonD = buttonPanel.getSize();
        if(maxHeight < buttonD.height)
            maxHeight = buttonD.height;
        doDebug("Num of Clips = " + numOfClips);
        for(int i = 0; i < numOfClips; i++)
            mGroups[i].getPlayer().setBounds(x, y, w, h - maxHeight);

        updateGifPanel();
    }

    public Dimension getPreferredSize()
    {
        if(Beans.isDesignTime())
            return new Dimension(preferredWidth, preferredHeight);
        Dimension d = getSize();
        if(d.width == 0 || d.height == 0)
            return new Dimension(preferredWidth, preferredHeight);
        else
            return d;
    }

    public void addPropertyChangeListener(PropertyChangeListener c)
    {
        changes.addPropertyChangeListener(c);
    }

    public void removePropertyChangeListener(PropertyChangeListener c)
    {
        changes.removePropertyChangeListener(c);
    }

    protected void createTabs(int length)
    {
        if(up != null)
            scrollPanel.remove(up);
        if(down != null)
            scrollPanel.remove(down);
        if(left != null)
            scrollPanel.remove(left);
        if(right != null)
            scrollPanel.remove(right);
        if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("SOUTH")) || buttonPosition.equalsIgnoreCase(JMFUtil.getString("NORTH")))
        {
            createLeftRight(length);
            scrollPanel.add("West", left);
            scrollPanel.add("East", right);
        } else
        if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("WEST")) || buttonPosition.equalsIgnoreCase(JMFUtil.getString("EAST")))
        {
            createUpDown(length);
            scrollPanel.add("North", up);
            scrollPanel.add("South", down);
        }
    }

    private void shiftButtons(int difference)
    {
        startingButton = startingButton + difference;
        if(startingButton > numOfMGroups)
            startingButton = startingButton - numOfMGroups;
        if(startingButton < 1)
            startingButton = numOfMGroups + startingButton;
        updateGifPanel();
    }

    protected void updateGifPanel()
    {
        if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("NONE")))
        {
            validate();
            return;
        }
        if(numOfMGroups <= 0)
            return;
        int numOfButtons = 0;
        int i = startingButton - 1;
        int stop = i + 1;
        if(stop == numOfMGroups)
            stop = 0;
        int tabLength = 0;
        ImageButton iButton = mGroups[i].getButton();
        int gifLength = getSize().width;
        int buttonsLength = 0;
        tabLength = ((ImageLabel) (iButton)).height;
        if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("WEST")) || buttonPosition.equalsIgnoreCase(JMFUtil.getString("EAST")))
        {
            gifLength = getSize().height;
            buttonsLength = 0;
            tabLength = ((ImageLabel) (iButton)).width;
        }
        gifLength -= 2 * tabsize;
        boolean first = true;
        scrollPanel.setVisible(false);
        scrollPanel.removeAll();
        gifPanel.setVisible(false);
        gifPanel.removeAll();
        if(++i >= numOfMGroups)
            i = 0;
        doDebug("ButtonsLength: " + buttonsLength);
        doDebug("Gif Length: " + gifLength);
        doDebug("i: " + i);
        doDebug("stop: " + stop);
        iButton = mGroups[i].getButton();
        while(first || i != stop) 
        {
            first = false;
            if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("NORTH")) || buttonPosition.equalsIgnoreCase(JMFUtil.getString("SOUTH")))
            {
                if(((ImageLabel) (iButton)).width + buttonsLength > gifLength)
                    break;
                numOfButtons++;
                buttonsLength += ((ImageLabel) (iButton)).width;
                if(((ImageLabel) (iButton)).height > tabLength)
                    tabLength = ((ImageLabel) (iButton)).height;
            } else
            if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("WEST")) || buttonPosition.equalsIgnoreCase(JMFUtil.getString("EAST")))
            {
                if(((ImageLabel) (iButton)).height + buttonsLength > gifLength)
                    break;
                numOfButtons++;
                buttonsLength += ((ImageLabel) (iButton)).height;
                if(((ImageLabel) (iButton)).width > tabLength)
                    tabLength = ((ImageLabel) (iButton)).width;
            }
            if(++i >= numOfMGroups)
                i = 0;
        }
        int gap = 0;
        if(numOfButtons > 1)
            gap = (gifLength - buttonsLength) / (numOfButtons - 1);
        System.out.println("gap=" + gap + " gifLength=" + gifLength + " buttonsLength=" + buttonsLength);
        if(numOfButtons == 1)
            gifPanel.setLayout(new FlowLayout());
        else
        if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("NORTH")) || buttonPosition.equalsIgnoreCase(JMFUtil.getString("SOUTH")))
            gifPanel.setLayout(new GridLayout(1, numOfButtons, gap, 0));
        else
        if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("WEST")) || buttonPosition.equalsIgnoreCase(JMFUtil.getString("EAST")))
            gifPanel.setLayout(new GridLayout(numOfButtons, 1, 0, gap));
        iButton = mGroups[startingButton - 1].getButton();
        buttonsLength = ((ImageLabel) (iButton)).width;
        first = true;
        if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("WEST")) || buttonPosition.equalsIgnoreCase(JMFUtil.getString("EAST")))
            buttonsLength = ((ImageLabel) (iButton)).height;
        i = startingButton;
        if(i >= numOfMGroups)
            i = 0;
        while(buttonsLength <= gifLength && (first || i != stop)) 
        {
            first = false;
            gifPanel.add(iButton);
            iButton = mGroups[i].getButton();
            if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("NORTH")) || buttonPosition.equalsIgnoreCase(JMFUtil.getString("SOUTH")))
                buttonsLength += ((ImageLabel) (iButton)).width;
            else
            if(buttonPosition.equalsIgnoreCase(JMFUtil.getString("WEST")) || buttonPosition.equalsIgnoreCase(JMFUtil.getString("EAST")))
                buttonsLength += ((ImageLabel) (iButton)).height;
            if(++i >= numOfMGroups)
                i = 0;
        }
        if(buttonsLength > gifLength && i != stop)
            createTabs(tabLength);
        scrollPanel.add("Center", gifPanel);
        gifPanel.setVisible(true);
        scrollPanel.setVisible(true);
        validate();
    }

    public void componentResized(ComponentEvent ce)
    {
        doDebug("Something is being resized.");
        doDebug("videoPanel=" + videoPanel);
        if(currentPlayer != null)
            doDebug("pb=" + currentPlayer);
        if(ce.getSource() == gifPanel)
        {
            doDebug("resize gifPanel: " + gifPanel);
            videoResize();
            if(!msVM)
                updateGifPanel();
        } else
        if(ce.getSource() == currentPlayer)
        {
            doDebug("resize currentPlayer: " + currentPlayer.getSize());
            JMFUtil.center(videoPanel, currentPlayer, true, currentPlayer.getPreferredSize().height - currentPlayer.getVisualComponent().getSize().height);
        }
    }

    public void componentMoved(ComponentEvent componentevent)
    {
    }

    public void componentHidden(ComponentEvent componentevent)
    {
    }

    public void componentShown(ComponentEvent componentevent)
    {
    }

    protected void doDebug(String s)
    {
        System.out.println(s);
    }

    transient Panel buttonPanel;
    transient Panel gifPanel;
    transient Panel scrollPanel;
    transient Panel utilPanel;
    transient Panel videoPanel;
    transient ImageButton up;
    transient ImageButton down;
    transient ImageButton left;
    transient ImageButton right;
    private MediaPlayer currentPlayer;
    transient Button infoButton;
    transient GridBagLayout gridbag;
    transient GridBagConstraints constraint;
    private final String infoWindow = JMFUtil.getString("Info");
    private URL currentU;
    private boolean displayURL;
    private boolean panelVisible;
    private boolean fitVideo;
    private boolean looping;
    private boolean sequential;
    private URL mpCodeBase;
    private AppletContext mpAppletContext;
    private int currentNum;
    private int numOfClips;
    protected int numOfMGroups;
    private int preferredHeight;
    private int preferredWidth;
    private String linksString;
    private String mediaNames[];
    private String links[];
    public MediaGroup mGroups[];
    private int maxMediaGroup;
    private boolean loadOnInit;
    private boolean fixAspectRatio;
    private int startingButton;
    private String buttonPosition;
    private int tabsize;
    private boolean msVM;
    private PropertyChangeSupport changes;
}
