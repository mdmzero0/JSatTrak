/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.layers.Earth.*;
import gov.nasa.worldwind.view.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author tag
 * @version $Id: TerrainProfilePanel.java 3430 2007-11-02 05:39:47Z tgaskins $
 */
public class TerrainProfilePanel extends JPanel
{
    private String follow;
    private boolean showEyePosition;
    private boolean keepProportions;
    private boolean zeroBased;
    private Dimension graphDimension;
    private double profileLengthFactor;
    private JLabel helpLabel;
    private JSlider lengthSlider;
    private JCheckBox showEyeCheck;
    private TerrainProfileLayer tpl;
    private WorldWindow wwd;

    public TerrainProfilePanel(WorldWindow wwd)
    {
        super(new BorderLayout());

        this.wwd = wwd;
        this.tpl = new TerrainProfileLayer();
        this.tpl.setShowEyePosition(true);
        this.tpl.setZeroBased(false);
        this.tpl.setSize(new Dimension(250, 100));
        this.tpl.setFollow(TerrainProfileLayer.FOLLOW_EYE);
        
        this.tpl.setEventSource(wwd);

        this.follow = this.tpl.getFollow();
        this.showEyePosition = this.tpl.getShowEyePosition();
        this.keepProportions = this.tpl.getKeepProportions();
        this.zeroBased = this.tpl.getZeroBased();
        this.graphDimension = tpl.getSize();
        this.profileLengthFactor = tpl.getProfileLenghtFactor();

        this.add(this.makeProfilePanel(), BorderLayout.CENTER);
    }

    public TerrainProfileLayer getLayer()
    {
        return tpl;
    }

    public void setLayer(TerrainProfileLayer tpl)
    {
        this.tpl = tpl;
    }

    private JPanel makeProfilePanel()
    {
        JPanel controlPanel = new JPanel(new GridLayout(0, 1, 0, 4));

        // Show eye position check box
        JPanel buttonsPanel = new JPanel(new GridLayout(0, 2, 0, 0));
        this.showEyeCheck = new JCheckBox("Show eye");
        this.showEyeCheck.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                showEyePosition = ((JCheckBox) actionEvent.getSource()).isSelected();
                updateTerrainProfile();
            }
        });
        this.showEyeCheck.setSelected(this.showEyePosition);
        this.showEyeCheck.setEnabled(this.follow.compareToIgnoreCase(TerrainProfileLayer.FOLLOW_EYE) == 0);
        buttonsPanel.add(this.showEyeCheck);
        // Keep proportions check box
        JCheckBox cbKeepProportions = new JCheckBox("Keep proportions");
        cbKeepProportions.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                keepProportions = ((JCheckBox) actionEvent.getSource()).isSelected();
                updateTerrainProfile();
            }
        });
        cbKeepProportions.setSelected(this.keepProportions);
        buttonsPanel.add(cbKeepProportions);

        // Zero based graph check box
        JPanel buttonsPanel2 = new JPanel(new GridLayout(0, 2, 0, 0));
        JCheckBox cb = new JCheckBox("Zero based");
        cb.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                zeroBased = ((JCheckBox) actionEvent.getSource()).isSelected();
                updateTerrainProfile();
            }
        });
        cb.setSelected(this.zeroBased);
        buttonsPanel2.add(new JLabel("")); // Dummy
        buttonsPanel2.add(cb);

        // Dimension combo
        JPanel dimensionPanel = new JPanel(new GridLayout(0, 2, 0, 0));
        dimensionPanel.add(new JLabel("  Dimension:"));
        final JComboBox cbDimension = new JComboBox(new String[] {"Small", "Medium", "Large"});
        cbDimension.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                String size = (String) cbDimension.getSelectedItem();
                if (size.compareToIgnoreCase("Small") == 0)
                {
                    graphDimension = new Dimension(250, 100);
                }
                else if (size.compareToIgnoreCase("Medium") == 0)
                {
                    graphDimension = new Dimension(450, 140);
                }
                else if (size.compareToIgnoreCase("Large") == 0)
                {
                    graphDimension = new Dimension(655, 240);
                }
                updateTerrainProfile();
            }
        });
        cbDimension.setSelectedItem("Small");
        dimensionPanel.add(cbDimension);

        // Profile length factor slider
        JPanel sliderPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        JSlider s = new JSlider(JSlider.HORIZONTAL, 0, 30, (int) (this.profileLengthFactor * 10));  // -5 - 5 in tenth
        s.setMajorTickSpacing(10);
        s.setMinorTickSpacing(1);
        //s.setPaintTicks(true);
        //s.setPaintLabels(true);
        s.setToolTipText("Profile length");
        s.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent event)
            {
                JSlider s = (JSlider) event.getSource();
                if (!s.getValueIsAdjusting())
                {
                    profileLengthFactor = (double) s.getValue() / 10;
                    updateTerrainProfile();
                }
            }

        });
        sliderPanel.add(s);
        this.lengthSlider = s;

        // Help label
        JPanel textPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        this.helpLabel = new JLabel("Tip: move mouse over the graph.");
        this.helpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        textPanel.add(this.helpLabel);

        // Follow behavior combo
        JPanel followPanel = new JPanel(new GridLayout(0, 2, 0, 0));
        followPanel.add(new JLabel("  Follow:"));
        final JComboBox cbFollow = new JComboBox(new String[] {"View", "Cursor", "Eye", "Object", "None"});
        cbFollow.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                String size = (String) cbFollow.getSelectedItem();
                if (size.compareToIgnoreCase("View") == 0)
                {
                    follow = TerrainProfileLayer.FOLLOW_VIEW;
                    helpLabel.setEnabled(true);
                    showEyeCheck.setEnabled(false);
                    lengthSlider.setEnabled(true);
                }
                else if (size.compareToIgnoreCase("Cursor") == 0)
                {
                    follow = TerrainProfileLayer.FOLLOW_CURSOR;
                    helpLabel.setEnabled(false);
                    showEyeCheck.setEnabled(false);
                    lengthSlider.setEnabled(true);
                }
                else if (size.compareToIgnoreCase("Eye") == 0)
                {
                    follow = TerrainProfileLayer.FOLLOW_EYE;
                    helpLabel.setEnabled(true);
                    showEyeCheck.setEnabled(true);
                    lengthSlider.setEnabled(true);
                }
                else if (size.compareToIgnoreCase("Object") == 0)
                {
                    follow = TerrainProfileLayer.FOLLOW_OBJECT;
                    helpLabel.setEnabled(true);
                    showEyeCheck.setEnabled(true);
                    lengthSlider.setEnabled(true);
                    tpl.setObjectPosition(wwd.getView().getEyePosition());
                    tpl.setObjectHeading(((OrbitView) wwd.getView()).getHeading());
                }
                else if (size.compareToIgnoreCase("None") == 0)
                {
                    follow = TerrainProfileLayer.FOLLOW_NONE;
                    helpLabel.setEnabled(true);
                    showEyeCheck.setEnabled(false);
                    lengthSlider.setEnabled(false);
                }
                updateTerrainProfile();
            }
        });
        cbFollow.setSelectedItem("View");
        followPanel.add(cbFollow);

        // Assembly
        controlPanel.add(dimensionPanel);
        controlPanel.add(followPanel);
        controlPanel.add(buttonsPanel);
        controlPanel.add(buttonsPanel2);
        controlPanel.add(sliderPanel);
        controlPanel.add(textPanel);
        controlPanel.setBorder(
            new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Terrain profile")));
        controlPanel.setToolTipText("Terrain profile controls");
        return controlPanel;
    }

    // Update terrain profile layer
    private void updateTerrainProfile()
    {
        this.tpl.setFollow(this.follow);
        this.tpl.setKeepProportions(this.keepProportions);
        this.tpl.setZeroBased(this.zeroBased);
        this.tpl.setSize(this.graphDimension);
        this.tpl.setShowEyePosition(this.showEyePosition);
        this.tpl.setProfileLengthFactor(this.profileLengthFactor);

        firePropertyChange(this.getClass().getName(), -1, 1);
    }
}
