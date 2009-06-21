/**
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
package jsattrak.utilities;

/*
 * StringTransferHandler.java is used by the 1.4
 * ExtendedDnDDemo.java example.
 */
import java.awt.datatransfer.*;
import javax.swing.*;
import java.io.IOException;

public abstract class StringTransferHandler extends TransferHandler implements java.io.Serializable
{
    
    protected abstract String exportString(JComponent c);
    protected abstract void importString(JComponent c, String str);
    protected abstract void cleanup(JComponent c, boolean remove);
    
    protected Transferable createTransferable(JComponent c)
    {
        return new StringSelection(exportString(c));
    }
    
    public int getSourceActions(JComponent c)
    {
        return COPY_OR_MOVE;
    }
    
    public boolean importData(JComponent c, Transferable t)
    {
        if (canImport(c, t.getTransferDataFlavors()))
        {
            try
            {
                
                String str = (String)t.getTransferData(DataFlavor.stringFlavor);
                importString(c, str);
                //System.out.println("HERE_last");
                return true;
            }
            catch (UnsupportedFlavorException ufe)
            {
                //ufe.printStackTrace();
            }
            catch (IOException ioe)
            {
                //ioe.printStackTrace();
            }
        }
        
        return false;
    }
    
    protected void exportDone(JComponent c, Transferable data, int action)
    {
        cleanup(c, action == MOVE);
    }
    
    public boolean canImport(JComponent c, DataFlavor[] flavors)
    {
        for (int i = 0; i < flavors.length; i++)
        {
            if (DataFlavor.stringFlavor.equals(flavors[i]))
            {
                return true;
            }
        }
        return false;
    }
}
