/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.wms.*;
import org.w3c.dom.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.xml.parsers.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: WMSLayersPanel.java 3601 2007-11-21 03:23:23Z tgaskins $
 */
public class WMSLayersPanel extends JPanel
{
    private static class LayerInfo
    {
        private Capabilities caps;
        private AVListImpl params = new AVListImpl();

        private String getTitle()
        {
            return params.getStringValue(AVKey.TITLE);
        }
    }

    private final WorldWindow wwd;
    private final URI serverURI;
    private final Dimension size;
    private final Thread loadingThread;
    private final TreeSet<LayerInfo> layerInfos = new TreeSet<LayerInfo>(new Comparator<LayerInfo>()
    {
        public int compare(LayerInfo infoA, LayerInfo infoB)
        {
            String nameA = infoA.getTitle();
            String nameB = infoB.getTitle();
            return nameA.compareTo(nameB);
        }
    });

    public WMSLayersPanel(WorldWindow wwd, String server, Dimension size) throws URISyntaxException
    {
        super(new BorderLayout());

        // See if the server name is a valid URI. Throw an exception if not.
        this.serverURI = new URI(server.trim()); // throws an exception if server name is not a valid uri.

        this.wwd = wwd;
        this.size = size;
        this.setPreferredSize(this.size);

        this.makeProgressPanel();

        // Thread off a retrieval of the server's capabilities document and update of this panel.
        this.loadingThread = new Thread(new Runnable()
        {
            public void run()
            {
                load();
            }
        });
        this.loadingThread.setPriority(Thread.MIN_PRIORITY);
        this.loadingThread.start();
    }

    private void load()
    {
        Capabilities caps;
        try
        {
            // Retrieve the server's capabilities document and parse it into a DOM.
            // Set up the DOM.
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc;

            // Request the capabilities document from the server.
            CapabilitiesRequest req = new CapabilitiesRequest(serverURI);
            doc = docBuilder.parse(req.toString());

            // Parse the DOM as a capabilities document.
            caps = Capabilities.parse(doc);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }

        // Gather up all the named layers and make a world wind layer for each.
        final Element[] namedLayerCaps = caps.getNamedLayers();
        if (namedLayerCaps == null)
            return;

        try
        {
            for (Element layerCaps : namedLayerCaps)
            {
                Element[] styles = caps.getLayerStyles(layerCaps);
                if (styles == null)
                {
                    LayerInfo layerInfo = createLayerInfo(caps, layerCaps, null);
                    WMSLayersPanel.this.layerInfos.add(layerInfo);
                }
                else
                {
                    for (Element style : styles)
                    {
                        LayerInfo layerInfo = createLayerInfo(caps, layerCaps, style);
                        WMSLayersPanel.this.layerInfos.add(layerInfo);
                    }
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }

        // Fill the panel with the layer titles.
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                WMSLayersPanel.this.removeAll();
                makeLayerInfosPanel(layerInfos);
            }
        });
    }

    public String getServerDisplayString()
    {
        return this.serverURI.getHost();
    }

    private LayerInfo createLayerInfo(Capabilities caps, Element layerCaps, Element style)
    {
        // Create the layer info specified by the layer's capabilities entry and the selected style.

        LayerInfo linfo = new LayerInfo();
        linfo.caps = caps;
        linfo.params = new AVListImpl();
        linfo.params.setValue(AVKey.LAYER_NAMES, caps.getLayerName(layerCaps));
        if (style != null)
            linfo.params.setValue(AVKey.STYLE_NAMES, caps.getStyleName(layerCaps, style));

        linfo.params.setValue(AVKey.TITLE, makeTitle(linfo));

        // Some wms servers are slow, so increase the timeouts and limits used by world wind's retrievers.
        linfo.params.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000);
        linfo.params.setValue(AVKey.URL_READ_TIMEOUT, 30000);
        linfo.params.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);

        return linfo;
    }

    private void makeLayerInfosPanel(Collection<LayerInfo> layerInfos)
    {
        // Create the panel holding the layer names.
        JPanel layersPanel = new JPanel(new GridLayout(0, 1, 0, 15));
        layersPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Add the server's layers to the panel.
        for (LayerInfo layerInfo : layerInfos)
        {
            addLayerInfoPanel(layersPanel, WMSLayersPanel.this.wwd, layerInfo);
        }

        // Put the name panel in a scroll bar.
        JScrollPane scrollPane = new JScrollPane(layersPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollPane.setPreferredSize(size);

        // Add the scroll bar and name panel to a titled panel that will resize with the main window.
        JPanel westPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        westPanel.setBorder(
            new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Layers")));
        westPanel.add(scrollPane);
        this.add(westPanel, BorderLayout.CENTER);

        this.revalidate();
    }

    private void addLayerInfoPanel(JPanel layersPanel, WorldWindow wwd, LayerInfo linfo)
    {
        // Give a layer a button and label and add it to the layer names panel.

        LayerInfoAction action = new LayerInfoAction(linfo, wwd);
        JCheckBox jcb = new JCheckBox(action);
        jcb.setSelected(false);
        layersPanel.add(jcb);
    }

    private class LayerInfoAction extends AbstractAction
    {
        private WorldWindow wwd;
        private LayerInfo layerInfo;
        private Layer layer;

        public LayerInfoAction(LayerInfo linfo, WorldWindow wwd)
        {
            super(linfo.getTitle());

            // Capture info we'll need later to control the layer.
            this.wwd = wwd;
            this.layerInfo = linfo;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            // If the layer is selected, add it to the world window's current model, else remove it from the model.
            if (((JCheckBox) actionEvent.getSource()).isSelected())
            {
                this.layer = WMSLayerFactory.newLayer(layerInfo.caps, layerInfo.params);
                this.layer.setEnabled(false);
                this.layer.setEnabled(true);
                ApplicationTemplate.insertBeforePlacenames(this.wwd, layer);
                WMSLayersPanel.this.firePropertyChange("LayersPanelUpdated", null, layer);
            }
            else
            {
                this.layer.setEnabled(false);
                this.wwd.getModel().getLayers().remove(layer);
                WMSLayersPanel.this.firePropertyChange("LayersPanelUpdated", layer, null);
            }

            // Tell the world window to update.
            wwd.redraw();
        }
    }

    private static String makeTitle(LayerInfo layerInfo)
    {
        String layerNames = layerInfo.params.getStringValue(AVKey.LAYER_NAMES);
        String styleNames = layerInfo.params.getStringValue(AVKey.STYLE_NAMES);
        String[] lNames = layerNames.split(",");
        String[] sNames = styleNames != null ? styleNames.split(",") : null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lNames.length; i++)
        {
            if (sb.length() > 0)
                sb.append(", ");

            String layerName = lNames[i];
            Element layer = layerInfo.caps.getLayerByName(layerName);
            String layerTitle = layerInfo.caps.getLayerTitle(layer);
            sb.append(layerTitle != null ? layerTitle : layerName);

            if (sNames == null || sNames.length <= i)
                continue;

            String styleName = sNames[i];
            Element style = layerInfo.caps.getLayerStyleByName(layer, styleName);
            if (style == null)
                continue;

            sb.append(" : ");
            String styleTitle = layerInfo.caps.getStyleTitle(layer, style);
            sb.append(styleTitle != null ? styleTitle : styleName);
        }

        return sb.toString();
    }

    private void makeProgressPanel()
    {
        // Create the panel holding the progress bar during loading.

        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        outerPanel.setPreferredSize(this.size);

        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        innerPanel.add(progressBar, BorderLayout.CENTER);

        JButton cancelButton = new JButton("Cancel");
        innerPanel.add(cancelButton, BorderLayout.EAST);
        cancelButton.addActionListener(new AbstractAction()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                if (loadingThread.isAlive())
                    loadingThread.interrupt();

                Container c = WMSLayersPanel.this.getParent();
                c.remove(WMSLayersPanel.this);
            }
        });

        outerPanel.add(innerPanel, BorderLayout.NORTH);
        this.add(outerPanel, BorderLayout.CENTER);
        this.revalidate();
    }
}
