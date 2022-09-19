package dev.insaneduck.extraction_tool.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import dev.insaneduck.extraction_tool.controller.Logic;
import dev.insaneduck.extraction_tool.controller.UiLogic;
import dev.insaneduck.extraction_tool.controller.Zip;
import dev.insaneduck.extraction_tool.controller.template.TreeBuilder;
import dev.insaneduck.extraction_tool.modal.Constants;
import dev.insaneduck.extraction_tool.modal.FileNameAndFile;
import dev.insaneduck.extraction_tool.modal.JCheckBoxTree;
import dev.insaneduck.extraction_tool.modal.Parameter;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainGUI extends JFrame
{
    JCheckBoxTree tree = new JCheckBoxTree();
    UiLogic uiLogic = new UiLogic();

    List<Parameter> parameterOutputList = new ArrayList<>();

    private JPanel main;
    private JButton generateTemplate;
    private JScrollPane treePane;
    private JPanel generateTemplatePanel;
    private JButton exportTemplateButton;
    private JTabbedPane tabsPanel;
    private JLabel status;
    private JProgressBar progressBar;

    public MainGUI()
    {
        initialiseUI();
    }

    public JLabel getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status.setText(status);
    }

    public JProgressBar getProgressBar()
    {
        return progressBar;
    }

    void initialiseUI()
    {
        setTitle("Data Extraction Tool");
        setLayout(new GridLayout());
        setIconImage(Toolkit.getDefaultToolkit().getImage(MainGUI.class.getResource("/logo.jpg")));
        setContentPane(main);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(550, 700));
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
        generateTemplate.addActionListener(actionEvent -> generateTree());
        exportTemplateButton.addActionListener(actionEvent -> exportTemplate());
        tabsPanel.addTab("Extract Data", new Extraction().getExtractDataPanel());
        tabsPanel.addTab("Settings", new Settings().getSettingsPanel());
    }

    public void generateTree()
    {
        try
        {
            //cleaning temp directory of previous files
            FileUtils.cleanDirectory(new File(Constants.TEMPLATE_GENERATION));
            File inputFile = uiLogic.filePicker("Select for generating template", "XML File", "xml");
            if (inputFile == null)
            {
                status.setText("no file selected");
            }
            else
            {
                TreeBuilder treeBuilder = new TreeBuilder(inputFile);
                DefaultMutableTreeNode defaultMutableTreeNode = treeBuilder.getTree();
                DefaultTreeModel model = new DefaultTreeModel(defaultMutableTreeNode);
                tree.setModel(model);
                for (int i = 0; i < tree.getRowCount(); i++)
                {
                    tree.expandRow(i);
                }
                treePane.setViewportView(tree);
                //todo make this global
                parameterOutputList = treeBuilder.getParameterAltList();
            }

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void exportTemplate()
    {
        File templateSaveLocation = uiLogic.filePicker("Select directory to save template file", "Template", "template");
        //getting list of items checked from JCheckBoxTree
        TreePath[] treePaths = tree.getCheckedPaths();
        for (TreePath treePath : treePaths)
        {
            Parameter parameterAlt = (Parameter) ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject();

            for (Parameter parameter : parameterOutputList)
            {
                String parameterName = parameterAlt.getParameterName();
                String parameterType = parameterAlt.getParameterType();
                String parameterClass = parameterAlt.getParameterClass();
                if (Objects.equals(parameter.getParameterName(), parameterName) &&
                        Objects.equals(parameter.getParameterType(), parameterType) &&
                        Objects.equals(parameter.getParameterClass(), parameterClass))
                {
                    parameter.setSelected(true);
                }
            }
        }
        if (templateSaveLocation != null)
        {
            File templateJSON = new File(Constants.TEMPLATE_GENERATION + "/template.json");
            if (templateJSON.exists())
            {
                templateJSON.delete();
            }
            Logic.writeTextToFile(templateJSON, Logic.beautify(Parameter.getJson(parameterOutputList)));
            List<String> templateFiles = new ArrayList<>();
            templateFiles.add(templateJSON.toString());
            templateFiles.add(Constants.TEMPLATE_GENERATION + "/schema.json");
            Zip.zipFiles(templateFiles, templateSaveLocation.toString());
            status.setText("template saved to " + templateSaveLocation);
        }
        else
        {
            status.setText("No template output location selected");
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
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
        main = new JPanel();
        main.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        tabsPanel = new JTabbedPane();
        main.add(tabsPanel, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        generateTemplatePanel = new JPanel();
        generateTemplatePanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabsPanel.addTab("Generate Template", generateTemplatePanel);
        generateTemplate = new JButton();
        generateTemplate.setText("1. Load XML for template");
        generateTemplatePanel.add(generateTemplate, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        treePane = new JScrollPane();
        generateTemplatePanel.add(treePane, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        generateTemplatePanel.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 3), new Dimension(-1, 3), new Dimension(-1, 3), 0, false));
        exportTemplateButton = new JButton();
        exportTemplateButton.setText("2. Export Template");
        generateTemplatePanel.add(exportTemplateButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        main.add(spacer2, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(5, 5), new Dimension(5, 5), new Dimension(5, 5), 0, false));
        final Spacer spacer3 = new Spacer();
        main.add(spacer3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(5, 5), new Dimension(5, 5), new Dimension(5, 5), 0, false));
        status = new JLabel();
        status.setText("Status");
        main.add(status, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar = new JProgressBar();
        main.add(progressBar, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {return main;}

}
