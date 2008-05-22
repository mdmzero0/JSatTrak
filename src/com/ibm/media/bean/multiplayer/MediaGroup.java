// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MediaGroup.java

package com.ibm.media.bean.multiplayer;

import java.util.Vector;
import javax.media.bean.playerbean.MediaPlayer;

// Referenced classes of package com.ibm.media.bean.multiplayer:
//            RelatedLink, MultiPlayerBean, ImageLabel, ImageButton

public class MediaGroup
{

    public MediaGroup(String media, String gif, String caption, MultiPlayerBean o)
    {
        isButtonGif = true;
        player = null;
        button = null;
        related = null;
        owner = null;
        mediaName = media;
        gifName = gif;
        owner = o;
        index = owner.numOfMGroups++;
        owner.doDebug("Creating " + media + ", " + gif);
        if(gif == null || gif.compareTo("") == 0)
            button = owner.formButton(owner.numOfMGroups);
        else
            button = owner.formButton(owner.getURL(gif), owner.numOfMGroups);
        buttonWidth = ((ImageLabel) (button)).width;
        buttonHeight = ((ImageLabel) (button)).height;
        this.caption = caption;
        owner.doDebug("Created: " + media + "\nNext Index: " + owner.numOfMGroups);
    }

    public MediaGroup(String media, String gif, boolean isGif, String caption, MultiPlayerBean o)
    {
        isButtonGif = true;
        player = null;
        button = null;
        related = null;
        owner = null;
        mediaName = media;
        gifName = gif;
        this.caption = caption;
        isButtonGif = isGif;
        owner = o;
        index = owner.numOfMGroups++;
    }

    protected void finalize()
        throws Throwable
    {
        super.finalize();
        button = null;
        owner = null;
        player = null;
        related = null;
    }

    public void setPlayer(MediaPlayer pb)
    {
        player = pb;
    }

    public MediaPlayer getPlayer()
    {
        return player;
    }

    public void setButton(ImageButton b)
    {
        button = b;
    }

    public ImageButton getButton()
    {
        return button;
    }

    public void setIsButtonGif(boolean b)
    {
        isButtonGif = b;
    }

    public boolean isButtonGif()
    {
        return isButtonGif;
    }

    public void delete()
    {
        owner.deleteMGroup(index);
        try
        {
            finalize();
        }
        catch(Throwable e) { }
    }

    public void setRelated(RelatedLink l)
    {
        owner.doDebug("setRelated: link=" + l.link);
        if(related == null)
            related = new Vector();
        int i = related.size() - 1;
        int j = 0;
        for(boolean insert = false; j < i && !insert;)
            if(l.startTime > ((RelatedLink)related.elementAt(j)).startTime)
                insert = true;

        related.insertElementAt(l, j);
    }

    public void setIndex(int i)
    {
        index = i;
        if(button != null)
            button.setText(Integer.toString(i + 1));
    }

    public int getIndex()
    {
        return index;
    }

    public Vector getRelated()
    {
        return related;
    }

    public String mediaName;
    public String gifName;
    boolean isButtonGif;
    int index;
    String caption;
    MediaPlayer player;
    ImageButton button;
    Vector related;
    private MultiPlayerBean owner;
    int buttonWidth;
    int buttonHeight;
}
