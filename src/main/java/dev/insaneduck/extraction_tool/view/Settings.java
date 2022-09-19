package dev.insaneduck.extraction_tool.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class Settings
{
    MainGUI mainGUI;
    private JRadioButton darkRadioButton;
    private JRadioButton lightRadioButton;
    private JButton githubButton;
    private JScrollPane logsScrollPane;
    private JTextArea logger;
    private JButton showLogsButton;
    private JPanel settingsPanel;

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    public Settings(MainGUI mainGUI)
    {
        this.mainGUI = mainGUI;
        //change to light mode
        lightRadioButton.addActionListener(actionEvent -> {
            if (lightRadioButton.isSelected())
            {
                try
                {
                    UIManager.setLookAndFeel(FlatLightLaf.class.getDeclaredConstructor().newInstance());
                }
                catch (InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException |
                       InvocationTargetException | NoSuchMethodException e)
                {
                    throw new RuntimeException(e);
                }
                SwingUtilities.updateComponentTreeUI(SwingUtilities.getWindowAncestor(settingsPanel));
            }
        });
        //change to dark mode
        darkRadioButton.addActionListener(actionEvent -> {
            if (darkRadioButton.isSelected())
            {
                try
                {
                    UIManager.setLookAndFeel(FlatOneDarkIJTheme.class.getDeclaredConstructor().newInstance());
                }
                catch (InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException |
                       InvocationTargetException | NoSuchMethodException e)
                {
                    throw new RuntimeException(e);
                }
                SwingUtilities.updateComponentTreeUI(SwingUtilities.getWindowAncestor(settingsPanel));
            }
        });
        //show logs
        showLogsButton.addActionListener(actionEvent -> {
            if (Objects.equals(showLogsButton.getText(), "Show Logs"))
            {
                logsScrollPane.setVisible(true);
                showLogsButton.setText("Hide Logs");
            }
            else if (Objects.equals(showLogsButton.getText(), "Hide Logs"))
            {
                logsScrollPane.setVisible(false);
                showLogsButton.setText("Show Logs");
            }
        });
        //open GitHub guide to use the tool
        githubButton.addActionListener(actionEvent -> {
            try
            {
                String url = "https://github.com/InsaneDuck/XML_Tool/blob/master/README.md";
                Desktop desktop = Desktop.getDesktop();
                URI uri = new URI(url);
                desktop.browse(uri.resolve(uri));
            }
            catch (URISyntaxException | IOException e)
            {
                throw new RuntimeException(e);
            }
        });
    }

    public JPanel getSettingsPanel()
    {
        return settingsPanel;
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayoutManager(4, 6, new Insets(0, 0, 0, 0), -1, -1));
        final Spacer spacer1 = new Spacer();
        settingsPanel.add(spacer1, new GridConstraints(0, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 3), new Dimension(-1, 3), new Dimension(-1, 3), 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Theme");
        settingsPanel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        darkRadioButton = new JRadioButton();
        darkRadioButton.setSelected(true);
        darkRadioButton.setText("Dark");
        settingsPanel.add(darkRadioButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lightRadioButton = new JRadioButton();
        lightRadioButton.setText("Light");
        settingsPanel.add(lightRadioButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        settingsPanel.add(spacer2, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        githubButton = new JButton();
        githubButton.setText("Github");
        settingsPanel.add(githubButton, new GridConstraints(1, 4, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showLogsButton = new JButton();
        showLogsButton.setText("Show Logs");
        settingsPanel.add(showLogsButton, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        logsScrollPane = new JScrollPane();
        logsScrollPane.setVisible(false);
        settingsPanel.add(logsScrollPane, new GridConstraints(3, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        logger = new JTextArea();
        logsScrollPane.setViewportView(logger);
        final Spacer spacer3 = new Spacer();
        settingsPanel.add(spacer3, new GridConstraints(3, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(lightRadioButton);
        buttonGroup.add(lightRadioButton);
        buttonGroup.add(darkRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {return settingsPanel;}

}
