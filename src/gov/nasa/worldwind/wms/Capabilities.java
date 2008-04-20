/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.wms;

import gov.nasa.worldwind.util.Logging;
import org.w3c.dom.*;

import javax.xml.XMLConstants;
import javax.xml.xpath.*;
import java.util.*;
import java.util.logging.Level;

/**
 * @author tag
 * @version $Id: Capabilities.java 3601 2007-11-21 03:23:23Z tgaskins $
 */
public abstract class Capabilities
{
    protected Document doc;
    protected Element service;
    protected Element capability;
    protected XPath xpath;

    public static Capabilities parse(Document doc)
    {
        XPathFactory xpFactory = XPathFactory.newInstance();
        XPath xpath = xpFactory.newXPath();

        SimpleNamespaceContext nsc = new SimpleNamespaceContext();
        nsc.addNamespace(XMLConstants.DEFAULT_NS_PREFIX, "http://www.opengis.net/wms");
        xpath.setNamespaceContext(new SimpleNamespaceContext());

        try
        {
            String version = xpath.evaluate(altPaths("*/@wms:version"), doc);
            if (version == null || version.length() == 0)
                return null;

            if (version.compareTo("1.3") < 0)
                return new CapabilitiesV111(doc, xpath);
            else
                return new CapabilitiesV130(doc, xpath);
        }
        catch (XPathExpressionException e)
        {
            Logging.logger().log(Level.SEVERE, "WMS.ParsingError", e);
            return null;
        }
    }

    protected Capabilities(Document doc, XPath xpath)
    {
        this.doc = doc;
        this.xpath = xpath;

        try
        {
            this.service = (Element) this.xpath.evaluate(altPaths("*/wms:Service"), doc, XPathConstants.NODE);
            if (this.service == null)
            {
                Logging.logger().severe("WMS.NoServiceElement");
                throw new IllegalArgumentException(Logging.getMessage("WMS.NoServiceElement"));
            }

            this.capability = (Element) this.xpath.evaluate(altPaths("*/wms:Capability"), doc, XPathConstants.NODE);
            if (this.capability == null)
            {
                Logging.logger().severe("WMS.NoCapabilityElement");
                throw new IllegalArgumentException(Logging.getMessage("WMS.NoCapabilityElement"));
            }
        }
        catch (XPathExpressionException e)
        {
            Logging.logger().log(Level.SEVERE, "WMS.ParsingError", e);
        }
    }

    private static String altPaths(String path)
    {
        return path != null ? path + "|" + path.replaceAll("wms:", "") : null;
    }

    protected String getText(String path)
    {
        return this.getText(null, path);
    }

    protected String getText(Element context, String path)
    {
        try
        {
            return this.xpath.evaluate(altPaths(path), context != null ? context : doc);
        }
        catch (XPathExpressionException e)
        {
            return null;
        }
    }

    protected String[] getTextArray(Element context, String path)
    {
        try
        {
            NodeList nodes = (NodeList) this.xpath.evaluate(altPaths(path), context != null ? context : doc,
                XPathConstants.NODESET);
            if (nodes == null || nodes.getLength() == 0)
                return null;

            String[] strings = new String[nodes.getLength()];
            for (int i = 0; i < nodes.getLength(); i++)
            {
                strings[i] = nodes.item(i).getTextContent();
            }
            return strings;
        }
        catch (XPathExpressionException e)
        {
            return null;
        }
    }

    protected String[] getUniqueText(Element context, String path)
    {
        String[] strings = this.getTextArray(context, path);
        if (strings == null)
            return null;

        ArrayList<String> sarl = new ArrayList<String>();
        for (String s : strings)
        {
            if (!sarl.contains(s))
                sarl.add(s);
        }

        return sarl.toArray(new String[1]);
    }

    protected Element getElement(Element context, String path)
    {
        try
        {
            Node node = (Node) this.xpath.evaluate(altPaths(path), context != null ? context : doc,
                XPathConstants.NODE);
            if (node == null)
                return null;

            return node instanceof Element ? (Element) node : null;
        }
        catch (XPathExpressionException e)
        {
            return null;
        }
    }

    protected Element[] getElements(Element context, String path)
    {
        try
        {
            NodeList nodes = (NodeList) this.xpath.evaluate(altPaths(path), context != null ? context : doc,
                XPathConstants.NODESET);
            if (nodes == null || nodes.getLength() == 0)
                return null;

            Element[] elements = new Element[nodes.getLength()];
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node node = nodes.item(i);
                if (node instanceof Element)
                    elements[i] = (Element) node;
            }
            return elements;
        }
        catch (XPathExpressionException e)
        {
            return null;
        }
    }

    public Element[] getUniqueElements(Element context, String path, String uniqueTag)
    {
        Element[] elements = this.getElements(context, path);
        if (elements == null)
            return null;

        HashMap<String, Element> styles = new HashMap<String, Element>();
        for (Element e : elements)
        {
            String name = this.getText(e, uniqueTag);
            if (name != null)
                styles.put(name, e);
        }

        return styles.values().toArray(new Element[1]);
    }

    private HashMap<Element, Layer> namedLayerElements = new HashMap<Element, Layer>();
    private HashMap<String, Layer> namedLayers = new HashMap<String, Layer>();

    private void fillLayerList()
    {
        if (this.namedLayers.size() == 0)
        {
            Element[] nels = this.getElements(this.capability, "descendant::wms:Layer[wms:Name]");
            if (nels == null || nels.length == 0)
                return;

            for (Element le : nels)
            {
                String name = this.getLayerName(le);
                if (name != null)
                {
                    Layer layer = new Layer(le);
                    this.namedLayers.put(name, layer);
                    this.namedLayerElements.put(le, layer);
                }
            }
        }
    }

    public Element[] getNamedLayers()
    {
        if (this.namedLayerElements.size() == 0)
            this.fillLayerList();

        return this.namedLayerElements.keySet().toArray(new Element[this.namedLayerElements.size()]);
    }

    public Element getLayerByName(String layerName)
    {
        if (this.namedLayers.size() == 0)
            this.fillLayerList();

        Layer l = this.namedLayers.get(layerName);
        return l != null ? l.element : null;
    }

    // ********* Document Items ********* //

    public String getVersion()
    {
        return this.getText("*/@wms:version");
    }

    public String getUpdateSequence()
    {
        return this.getText("*/@wms:updateSequence");
    }

    // ********* Service Items ********* //

    public String getAbstract()
    {
        return this.getText(this.service, "wms:Abstract");
    }

    public String getAccessConstraints()
    {
        return this.getText(this.service, "wms:AccessConstraints");
    }

    public String getContactOrganization()
    {
        return this.getText(
            this.service, "wms:ContactInformation/wms:ContactPersonPrimary/wms:ContactOrganization");
    }

    public String getContactPerson()
    {
        return this.getText(
            this.service, "wms:ContactInformation/wms:ContactPersonPrimary/wms:ContactPerson");
    }

    public String getFees()
    {
        return this.getText(this.service, "wms:Fees");
    }

    public String[] getKeywordList()
    {
        return this.getTextArray(this.service, "wms:KeywordList/wms:Keyword");
    }

    public String getLayerLimit()
    {
        return this.getText(this.service, "wms:LayerLimit");
    }

    public String getMaxWidth()
    {
        return this.getText(this.service, "wms:MaxWidth");
    }

    public String getMaxHeight()
    {
        return this.getText(this.service, "wms:MaxHeight");
    }

    public String getName()
    {
        return this.getText(this.service, "wms:Name");
    }

    public String getTitle()
    {
        return this.getText(this.service, "wms:Title");
    }

    // ********* Capability Items ********* //

    public String getOnlineResource()
    {
        return this.getText(this.capability, "wms:OnlineResource/@xlink:href");
    }

    public String[] getGetCapabilitiesFormats()
    {
        return this.getTextArray(this.capability,
            "wms:Request/wms:GetCapabilities/wms:Format");
    }

    public String getGetCapabilitiesRequestGetURL()
    {
        return this.getText(this.capability,
            "wms:Request/wms:GetCapabilities/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href");
    }

    public String getGetCapabilitiesRequestPostURL()
    {
        return this.getText(this.capability,
            "wms:Request/wms:GetCapabilities/wms:DCPType/wms:HTTP/wms:Post/wms:OnlineResource/@xlink:href");
    }

    public String[] getExceptionFormats()
    {
        return this.getTextArray(this.capability, "wms:Exception/wms:Format");
    }

    public String getFeatureInfoRequestGetURL()
    {
        return this.getText(this.capability,
            "wms:Request/wms:GetFeatureInfo/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href");
    }

    public String getFeatureInfoRequestPostURL()
    {
        return this.getText(this.capability,
            "wms:Request/wms:GetFeatureInfo/wms:DCPType/wms:HTTP/wms:Post/wms:OnlineResource/@xlink:href");
    }

    public String[] getGetMapFormats()
    {
        return this.getTextArray(this.capability,
            "wms:Request/wms:GetMap/wms:Format");
    }

    public String getGetMapRequestGetURL()
    {
        return this.getText(this.capability,
            "wms:Request/wms:GetMap/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href");
    }

    public String getGetMapRequestPostURL()
    {
        return this.getText(this.capability,
            "wms:Request/wms:GetMap/wms:DCPType/wms:HTTP/wms:Post/wms:OnlineResource/@xlink:href");
    }

    public String getVendorSpecificCapabilities()
    {
        return this.getText(this.capability, "wms:VendorSpecificCapabilities");
    }

    public Element getLayer()
    {
        return this.getElement(this.capability, "wms:Layer");
    }

    // ********* Layer Items ********* //

    protected static class Layer
    {
        protected HashMap<Element, Style> styleElements = new HashMap<Element, Style>();
        protected final Element element;
        protected Layer layer;
        protected String name;
        protected String title;

        public Layer(Element element)
        {
            this.element = element;
        }
    }

    public String getLayerAbstract(Element layer)
    {
        return this.getText(layer, "wms:Abstract");
    }

    public String getLayerAttributionTitle(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/wms:Attribution/wms:Title");
    }

    public String getLayerAttributionURL(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/wms:Attribution/wms:OnlineResource/@xlink:href");
    }

    public String getLayerAttributionLogoFormat(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/wms:Attribution/wms:LogoURL/wms:Format");
    }

    public String getLayerAttributionLogoHeight(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/wms:Attribution/wms:LogoURL/@wms:height");
    }

    public String getLayerAttributionLogoURL(Element layer)
    {
        return this.getText(layer,
            "ancestor-or-self::wms:Layer/wms:Attribution/wms:LogoURL/wms:OnlineResource/@xlink:href");
    }

    public String getLayerAttributionLogoWidth(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/wms:Attribution/wms:LogoURL/@wms:width");
    }

    public Element[] getLayerAuthorityURLs(Element layer)
    {
        return this.getUniqueElements(layer, "ancestor-or-self::wms:Layer/wms:AuthorityURL", "@wms:type");
    }

    public abstract BoundingBox[] getLayerBoundingBoxes(Element layer);

    public String getLayerCascaded(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/@cascaded");
    }

    public String[] getLayerCRS(Element layer)
    {
        return this.getUniqueText(layer, "ancestor-or-self::wms:Layer/wms:CRS");
    }

    public String getLayerDataURLFormat(Element layer)
    {
        return this.getText(layer, "wms:DataURL/wms:Format");
    }

    public String getLayerDataURL(Element layer)
    {
        return this.getText(layer, "wms:DataURL/wms:OnlineResource/@xlink:href");
    }

    public Element[] getLayerDimensions(Element layer)
    {
        Element[] dims = this.getElements(layer, "ancestor-or-self::wms:Layer/wms:Dimension");

        if (dims == null || dims.length == 0)
            return null;

        ArrayList<Element> uniqueDims = new ArrayList<Element>();
        ArrayList<String> dimNames = new ArrayList<String>();
        for (Element e : dims)
        {
            // Filter out dimensions with same name.
            // Keep all those with a null name, even though wms says they're invalid. Let the app decide.
            String name = this.getDimensionName(e);
            if (name != null && dimNames.contains(name))
                continue;

            uniqueDims.add(e);
            dimNames.add(name);
        }

        return uniqueDims.toArray(new Element[uniqueDims.size()]);
    }

    public Element[] getLayerExtents(Element layer)
    {
        Element[] extents = this.getElements(layer, "ancestor-or-self::wms:Layer/wms:Extent");

        if (extents == null || extents.length == 0)
            return null;

        ArrayList<Element> uniqueExtents = new ArrayList<Element>();
        ArrayList<String> extentNames = new ArrayList<String>();
        for (Element e : extents)
        {
            // Filter out dimensions with same name.
            // Keep all those with a null name, even though wms says they're invalid. Let the app decide.
            String name = this.getDimensionName(e);
            if (name != null && extentNames.contains(name))
                continue;

            uniqueExtents.add(e);
            extentNames.add(name);
        }

        return uniqueExtents.toArray(new Element[uniqueExtents.size()]);
    }

    public abstract BoundingBox getLayerGeographicBoundingBox(Element layer);

    public String getLayerFeatureListFormat(Element layer)
    {
        return this.getText(layer, "wms:FeatureListURL/wms:Format");
    }

    public String getLayerFeatureListURL(Element layer)
    {
        return this.getText(layer, "wms:FeatureListURL/wms:OnlineResource/@xlink:href");
    }

    public String getLayerFixedHeight(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/@fixedHeight");
    }

    public String getLayerFixedWidth(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/@fixedWidth");
    }

    public Element[] getLayerIdentifiers(Element layer)
    {
        return this.getUniqueElements(layer, "wms:Identifier", "wms:authority");
    }

    public String[] getLayerKeywordList(Element layer)
    {
        return this.getTextArray(layer, "wms:KeywordList/wms:Keyword");
    }

    public abstract String getLayerMaxScaleDenominator(Element layer);

    public Element[] getLayerMetadataURLs(Element layer)
    {
        return this.getElements(layer, "wms:MetadataURL");
    }

    public abstract String getLayerMinScaleDenominator(Element layer);

    public String getLayerName(Element layerElement)
    {
        Layer layer = this.namedLayerElements.get(layerElement);
        return layer != null && layer.name != null ? layer.name : this.getText(layerElement, "wms:Name");
    }

    public String getLayerNoSubsets(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/@noSubsets");
    }

    public String getLayerOpaque(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/@opaque");
    }

    public String getLayerQueryable(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/@queryable");
    }

    public String[] getLayerSRS(Element layer)
    {
        return this.getUniqueText(layer, "ancestor-or-self::wms:Layer/wms:SRS");
    }

    public Element[] getLayerStyles(Element layerElement)
    {
        Layer layer = this.namedLayerElements.get(layerElement);
        if (layer == null)
            return null;

        if (layer.styleElements != null)
            return layer.styleElements.keySet().toArray(new Element[1]);

        Element[] styleElements = this.getUniqueElements(layerElement, "ancestor-or-self::wms:Layer/wms:Style", "Name");
        if (styleElements == null)
            return null;

        layer.styleElements = new HashMap<Element, Style>();
        for (Element se : styleElements)
        {
            Style style = new Style(se, layer);
            layer.styleElements.put(se, style);
            this.styleElements.put(se, style);
        }

        return layer.styleElements.keySet().toArray(new Element[1]);
    }

    public Element[] getLayerSubLayers(Element layer)
    {
        return this.getElements(layer, "wms:Layer");
    }

    public String getLayerTitle(Element layerElement)
    {
        Layer layer = this.namedLayerElements.get(layerElement);
        if (layer == null)
            return null;

        return layer.title != null ? layer.title : (layer.title = this.getText(layerElement, "wms:Title"));
    }

    public Element getLayerStyleByName(Element layerElement, String styleName)
    {
        Layer layer = this.namedLayerElements.get(layerElement);
        if (layer == null)
            return null;

        if (layer.styleElements == null || layer.styleElements.size() == 0)
        {
            // Initialize the layer's style list.
            this.getLayerStyles(layerElement);
            if (layer.styleElements == null || layer.styleElements.size() == 0)
                return null;
        }

        Collection<Style> styles = layer.styleElements.values();
        for (Style s : styles)
        {
            if (s != null && s.equals(styleName))
                return s.element;
        }

        return null;
    }

    // ********* Style Items ********* //

    protected HashMap<Element, Style> styleElements = new HashMap<Element, Style>();

    protected static class Style
    {
        protected final Layer layer;
        protected final Element element;
        protected String name;
        protected String title;

        public Style(Element element, Layer layer)
        {
            this.element = element;
            this.layer = layer;
        }
    }

    public String getStyleAbstract(Element styleElement)
    {
        return this.getText(styleElement, "wms:Abstract");
    }

    public String getStyleLegendFormat(Element styleElement)
    {
        return this.getText(styleElement, "wms:LegendURL/wms:Format");
    }

    public String getStyleLegendHeight(Element styleElement)
    {
        return this.getText(styleElement, "wms:LegendURL/@height");
    }

    public String getStyleLegendURL(Element styleElement)
    {
        return this.getText(styleElement, "wms:LegendURL/wms:OnlineResource/@xlink:href");
    }

    public String getStyleLegendWidth(Element styleElement)
    {
        return this.getText(styleElement, "wms:LegendURL/@width");
    }

    public String getStyleName(Element styleElement)
    {
        Style style = this.styleElements.get(styleElement);
        return style != null && style.title != null ? style.title : this.getText(styleElement, "wms:Name");
    }

    public String getStyleName(Element layerElement, Element styleElement)
    {
        Layer layer = this.namedLayerElements.get(layerElement);
        if (layer == null || layer.styleElements == null)
            return this.getStyleName(layerElement, styleElement);

        Style style = layer.styleElements.get(styleElement);

        return style != null && style.name != null ? style.title : this.getText(styleElement, "wms:Name");
    }

    public String getStyleSheetURLFormat(Element styleElement)
    {
        return this.getText(styleElement, "wms:StyleSheetURL/wms:Format");
    }

    public String getStyleSheetURL(Element styleElement)
    {
        return this.getText(styleElement, "wms:StyleSheetURL/wms:OnlineResource/@xlink:href");
    }

    public String getStyleTitle(Element styleElement)
    {
        Style style = this.styleElements.get(styleElement);
        return style != null && style.title != null ? style.title : this.getText(styleElement, "wms:Title");
    }

    public String getStyleTitle(Element layerElement, Element styleElement)
    {
        Layer layer = this.namedLayerElements.get(layerElement);
        if (layer == null || layer.styleElements == null)
            return this.getStyleTitle(styleElement);

        Style style = this.styleElements.get(styleElement);
        return style != null && style.title != null ? style.title : this.getText(styleElement, "wms:Title");
    }

    public String getStyleURL(Element styleElement)
    {
        return this.getText(styleElement, "wms:StyleURL/wms:OnlineResource/@xlink:href");
    }

    public String getStyleURLFormat(Element styleElement)
    {
        return this.getText(styleElement, "wms:StyleURL/wms:Format");
    }

    // ********* Authority Items ********* //

    public String getAuthorityName(Element authority)
    {
        return this.getText(authority, "@wms:name");
    }

    public String getAuthorityURL(Element authority)
    {
        return this.getText(authority, "wms:OnlineResource/@xlink:href");
    }

    // ********* Identifier Items ********* //

    public String getIdentifier(Element identifier)
    {
        return this.getText(identifier, ".");
    }

    public String getIdentifierAuthority(Element identifier)
    {
        return this.getText(identifier, "@wms:authority");
    }

    // ********* Metadata Items ********* //

    public String getMetadataFormat(Element metadata)
    {
        return this.getText(metadata, "wms:Format");
    }

    public String getMetadataURL(Element metadata)
    {
        return this.getText(metadata, "wms:OnlineResource/@xlink:href");
    }

    public String getMetadataType(Element metadata)
    {
        return this.getText(metadata, "@wms:type");
    }

    // ********* EX_GeographicBoundingBox Items ********* //

    public String getWestBoundLongitude(Element bbox)
    {
        return this.getText(bbox, "wms:westBoundLongitude");
    }

    public String getEastBoundLongitude(Element bbox)
    {
        return this.getText(bbox, "wms:eastBoundLongitude");
    }

    public String getSouthBoundLatitude(Element bbox)
    {
        return this.getText(bbox, "wms:southBoundLatitude");
    }

    public String getNorthBoundLatitude(Element bbox)
    {
        return this.getText(bbox, "wms:northBoundLatitude");
    }

    // ********* BoundingBox Items ********* //

    public String getBoundingBoxCRS(Element bbox)
    {
        return this.getText(bbox, "@wms:CRS");
    }

    public String getBoundingBoxMinx(Element bbox)
    {
        return this.getText(bbox, "@wms:minx");
    }

    public String getBoundingBoxMiny(Element bbox)
    {
        return this.getText(bbox, "@wms:miny");
    }

    public String getBoundingBoxMaxx(Element bbox)
    {
        return this.getText(bbox, "@wms:maxx");
    }

    public String getBoundingBoxMaxy(Element bbox)
    {
        return this.getText(bbox, "@wms:maxy");
    }

    public String getBoundingBoxResx(Element bbox)
    {
        return this.getText(bbox, "@wms:resx");
    }

    public String getBoundingBoxResy(Element bbox)
    {
        return this.getText(bbox, "@wms:resy");
    }

    public String getBoundingBoxSRS(Element bbox)
    {
        return this.getText(bbox, "@wms:SRS");
    }

    // ********* Dimension Items ********* //

    public String getDimensionName(Element dimension)
    {
        return this.getText(dimension, "@wms:name");
    }

    public String getDimensionUnits(Element dimension)
    {
        return this.getText(dimension, "@wms:units");
    }

    public String getDimensionUnitSymbol(Element dimension)
    {
        return this.getText(dimension, "@wms:unitSymbol");
    }

    public String getDimensionDefault(Element dimension)
    {
        return this.getText(dimension, "@wms:default");
    }

    public String getDimensionMultipleValues(Element dimension)
    {
        return this.getText(dimension, "@wms:multipleValues");
    }

    public String getDimensionNearestValue(Element dimension)
    {
        return this.getText(dimension, "@wms:nearestValue");
    }

    public String getDimensionCurrent(Element dimension)
    {
        return this.getText(dimension, "@wms:current");
    }

    public String getDimensionExtent(Element dimension)
    {
        return this.getText(dimension, ".");
    }

    // ********* Extent Items, wms 1.1 only ********* //

    public String getExtentName(Element dimension)
    {
        return this.getText(dimension, "@wms:name");
    }

    public String getExtentDefault(Element dimension)
    {
        return this.getText(dimension, "@wms:default");
    }

    public String getExtentMultipleValues(Element dimension)
    {
        return this.getText(dimension, "@wms:multipleValues");
    }

    public String getExtentNearestValue(Element dimension)
    {
        return this.getText(dimension, "@wms:nearestValue");
    }

    public String getExtentCurrent(Element dimension)
    {
        return this.getText(dimension, "@wms:current");
    }

    public String getExtentText(Element dimension)
    {
        return this.getText(dimension, ".");
    }
}
