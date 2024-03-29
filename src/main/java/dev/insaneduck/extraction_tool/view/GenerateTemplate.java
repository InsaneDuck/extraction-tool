package dev.insaneduck.extraction_tool.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import dev.insaneduck.extraction_tool.controller.Logic;
import dev.insaneduck.extraction_tool.controller.UiLogic;
import dev.insaneduck.extraction_tool.controller.Zip;
import dev.insaneduck.extraction_tool.controller.template.TreeBuilder;
import dev.insaneduck.extraction_tool.modal.Constants;
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

public class GenerateTemplate
{
    MainGUI mainGUI;
    JCheckBoxTree tree = new JCheckBoxTree();
    UiLogic uiLogic = new UiLogic();
    List<Parameter> parameterOutputList = new ArrayList<>();
    private JPanel generateTemplatePanel;
    private JButton generateTemplate;
    private JScrollPane treePane;
    private JButton exportTemplateButton;

    public GenerateTemplate(MainGUI mainGUI)
    {
        this.mainGUI = mainGUI;
        generateTemplate.addActionListener(actionEvent -> generateTree());
        exportTemplateButton.addActionListener(actionEvent -> exportTemplate());
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
                mainGUI.getStatus().setText("no file selected");
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
                parameterOutputList = treeBuilder.getParameterList();
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
            mainGUI.getStatus().setText("template saved to " + templateSaveLocation);
        }
        else
        {
            mainGUI.getStatus().setText("No template output location selected");
        }
    }

    public JPanel getGenerateTemplatePanel()
    {
        return generateTemplatePanel;
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
        generateTemplatePanel = new JPanel();
        generateTemplatePanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        generateTemplate = new JButton();
        generateTemplate.setText("1. Load XML for template");
        generateTemplatePanel.add(generateTemplate, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        treePane = new JScrollPane();
        generateTemplatePanel.add(treePane, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        generateTemplatePanel.add(spacer1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 3), new Dimension(-1, 3), new Dimension(-1, 3), 0, false));
        exportTemplateButton = new JButton();
        exportTemplateButton.setText("2. Export Template");
        generateTemplatePanel.add(exportTemplateButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {return generateTemplatePanel;}

}
