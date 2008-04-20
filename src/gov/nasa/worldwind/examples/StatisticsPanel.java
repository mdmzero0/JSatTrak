package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.util.PerformanceStatistic;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Arrays;

public class StatisticsPanel extends JPanel
{
    private JPanel statsPanel;
    private JPanel outerPanel;
    private JScrollPane scrollPane;

    public StatisticsPanel(WorldWindow wwd)
    {
        // Make a panel at a default size.
        super(new BorderLayout());
        this.makePanel(new Dimension(200, 400));
    }

    public StatisticsPanel(WorldWindow wwd, Dimension size)
    {
        // Make a panel at a specified size.
        super(new BorderLayout());
        this.makePanel(size);
        wwd.setPerFrameStatisticsKeys(PerformanceStatistic.ALL_STATISTICS_SET);
    }

    private void makePanel(Dimension size)
    {
        // Make and fill the panel holding the statistics.
        this.statsPanel = new JPanel(new GridLayout(0, 1, 0, 15));
        this.statsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Must put the grid in a container to prevent scroll panel from stretching their vertical spacing.
        JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.add(this.statsPanel, BorderLayout.NORTH);

        // Put the name panel in a scroll bar.
        this.scrollPane = new JScrollPane(dummyPanel);
        this.scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        if (size != null)
            this.scrollPane.setPreferredSize(size);

        // Add the scroll bar and stats panel to a titled panel that will resize with the main window.
        outerPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        outerPanel
            .setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Statistics")));
        outerPanel.setToolTipText("Runtime Statistics");
        outerPanel.add(scrollPane);
        this.add(outerPanel, BorderLayout.CENTER);
    }

    private void fill(WorldWindow wwd)
    {
        // Fill the layers panel with the titles of all layers in the world window's current model.
        if (wwd.getSceneController().getPerFrameStatistics().size() < 1)
            return;

        PerformanceStatistic[] pfs = new PerformanceStatistic[wwd.getPerFrameStatistics().size()];
        pfs = wwd.getSceneController().getPerFrameStatistics().toArray(pfs);
        Arrays.sort(pfs);
        for (PerformanceStatistic stat : pfs)
        {
            JLabel jcb = new JLabel(stat.toString());
            this.statsPanel.add(jcb);
        }
    }

    public void update(WorldWindow wwd)
    {
        // Replace all the statistics in the panel with the current ones.
        this.statsPanel.removeAll();
        this.fill(wwd);
        this.outerPanel.revalidate();
        this.outerPanel.repaint();
    }

    @Override
    public void setToolTipText(String string)
    {
        this.scrollPane.setToolTipText(string);
    }
}
