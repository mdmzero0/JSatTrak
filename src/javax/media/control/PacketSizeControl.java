package javax.media.control;

import javax.media.Control;

public interface PacketSizeControl
    extends Control
{

    public abstract int setPacketSize(int i);

    public abstract int getPacketSize();
}
