package dev.insaneduck.extraction_tool.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import dev.insaneduck.extraction_tool.controller.Logic;
import dev.insaneduck.extraction_tool.controller.UiLogic;
import dev.insaneduck.extraction_tool.controller.Zip;
import dev.insaneduck.extraction_tool.controller.extraction.CSVHandler;
import dev.insaneduck.extraction_tool.controller.extraction.Extractor;
import dev.insaneduck.extraction_tool.controller.template.TreeBuilder;
import dev.insaneduck.extraction_tool.modal.*;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainGUI extends JFrame
{
    JCheckBoxTree tree = new JCheckBoxTree();
    UiLogic uiLogic = new UiLogic();
    List<FileNameAndFile> xmlFileNameAndFile = new ArrayList<>();
    List<Parameter> parameterOutputList = new ArrayList<>();
    List<Parameter> parameterListInput = new ArrayList<>();
    private JPanel main;
    private JButton selectTemplateToExtractButton;
    private JButton generateTemplate;
    private JScrollPane treePane;
    private JPanel generateTemplatePanel;
    private JButton exportTemplateButton;
    private JTabbedPane tabsPanel;
    private JButton selectXMLFolderButton;
    private JLabel workingDirectory;
    private JScrollPane filesScrollPane;
    private JList<String> listOfFiles;
    private JPanel extractDataPanel;
    private JButton extractDataButton;
    private JLabel status;
    private JProgressBar progressBar;
    private JPanel previewPanel;
    private JTabbedPane csvTabs;
    private JCheckBox mergeAllOutputXLSCheckBox;
    private JCheckBox includeParentParametersForCheckBox;
    private JPanel xmlFilesPanel;
    private JButton increaseDepth;
    private JButton decreaseDepth;
    private JButton clearButton;
    private JPanel depthPanel;
    private JLabel depth;
    private JRadioButton XLSRadioButton;
    private JRadioButton CSVRadioButton;

    public MainGUI()
    {
        initialiseUI();
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
        selectXMLFolderButton.addActionListener(actionEvent -> selectXMLFiles());
        selectTemplateToExtractButton.addActionListener(actionEvent -> selectTemplateToExtract());
        extractDataButton.addActionListener(actionEvent -> extractData());

        status.addPropertyChangeListener(propertyChangeEvent -> updateStatus());
        depthPanel.setVisible(false);
        //previewButton.addActionListener(actionEvent -> previewCSV());
        listOfFiles.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent mouseEvent)
            {
                if (mouseEvent.getClickCount() == 2)
                {
                    previewCSV();
                }
            }
        });
        //listOfFiles.addListSelectionListener(listSelectionEvent -> previewCSV());
        extractDataButton.setToolTipText("select a template before clicking extract");
        previewPanel.setVisible(false);
        includeParentParametersForCheckBox.addChangeListener(changeEvent -> depthPanel.setVisible(includeParentParametersForCheckBox.isSelected()));
        increaseDepth.addActionListener(actionEvent -> {
            int i = Integer.parseInt(depth.getText());
            ++i;
            if (i == 0)
            {
                ++i;
            }
            depth.setText(String.valueOf(i));
        });
        decreaseDepth.addActionListener(actionEvent -> {
            int i = Integer.parseInt(depth.getText());
            if (i > 1)
            {
                --i;
                depth.setText(String.valueOf(i));
            }
        });
        clearButton.addActionListener(actionEvent -> {
            listOfFiles.setListData(new String[0]);
            listOfFiles.revalidate();
            clearButton.setEnabled(false);
            workingDirectory.setText(null);
            extractDataButton.setEnabled(false);
            previewPanel.setVisible(false);
            pack();
        });
        tabsPanel.addTab("Settings", new Settings().$$$getRootComponent$$$());
        //settingsPanel.add(new Settings());
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

    public static void updateUI()
    {
        try
        {
            SwingUtilities.updateComponentTreeUI(MainGUI.class.newInstance());
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }

    }

    private void selectXMLFiles()
    {
        xmlFileNameAndFile.clear();
        //getting list of files from selected folder and filtering them by extension xml
        File[] files = uiLogic.getFilesFromFolderPicker("Choose Folder Containing XML Files", "XML Files", "xml", workingDirectory);
        if (files != null)
        {
            for (File file : files)
            {
                //key and value pair for associating file name with file
                //to string is avoided here to not display full path
                xmlFileNameAndFile.add(new FileNameAndFile(file.getName(), file));
            }
            //setting list of file names to JList
            List<String> fileNames = new ArrayList<>();
            xmlFileNameAndFile.forEach(item -> fileNames.add(item.getFileName()));
            listOfFiles.setListData(fileNames.toArray(new String[0]));
            clearButton.setEnabled(true);
        }
        else
        {
            status.setText("aborted");
        }
    }

    private void previewCSV()
    {
        File file = null;
        for (FileNameAndFile fileNameAndFile : xmlFileNameAndFile)
        {
            if (Objects.equals(fileNameAndFile.getFileName(), listOfFiles.getSelectedValue()))
            {
                file = fileNameAndFile.getFile();
            }
        }
        if (file != null)
        {
            if (includeParentParametersForCheckBox.isSelected())
            {
                Extractor extractor = new Extractor(file, parameterListInput, Integer.parseInt(depth.getText()));
                Logic logic = new Logic();
                new CSVHandler(extractor.getObjectCSV(), extractor.getArrayCSV(), logic.getCsvDirectoryFromFileName(file), null, false, false);
            }
            else
            {
                Extractor extractor = new Extractor(file, parameterListInput);
                Logic logic = new Logic();
                new CSVHandler(extractor.getObjectCSV(), extractor.getArrayCSV(), logic.getCsvDirectoryFromFileName(file), null, false, false);
            }
            String tempFolderPath = Constants.EXTRACTION_FILES + listOfFiles.getSelectedValue().replace(".xml", "") + "/";
            File[] csvFiles = new File(tempFolderPath).listFiles((dir, name) -> name.endsWith("." + "csv"));
            csvTabs.removeAll();
            if (csvFiles != null)
            {
                for (File csvFile : csvFiles)
                {
                    try
                    {
                        CSVReader csvReader = new CSVReader(new FileReader(csvFile));
                        List<String[]> csvData = csvReader.readAll();
                        //first column as column names
                        Object[] columns = csvData.get(0);
                        DefaultTableModel tableModel = new DefaultTableModel(columns, csvData.size() - 1);
                        int rowcount = tableModel.getRowCount();
                        for (int x = 0; x < rowcount + 1; x++)
                        {
                            int column = 0;
                            //ignoring column names
                            if (x > 0)
                            {
                                for (String value : csvData.get(x))
                                {
                                    tableModel.setValueAt(value, x - 1, column);
                                    column++;
                                }
                            }
                        }
                        csvTabs.addTab(csvFile.getName(), new PreviewTable(tableModel));
                    }
                    catch (IOException | CsvException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
            previewPanel.setMinimumSize(new Dimension(700, -1));
            previewPanel.setVisible(true);
            pack();
        }
    }

    public void selectTemplateToExtract()
    {
        Logic.setupTempFolder();
        parameterListInput.clear();
        File template = uiLogic.filePicker("Select Template", "Template File", "template");
        if (template != null)
        {
            Logic.getParameterListFromTemplate(template, parameterListInput);
            progressBar.setIndeterminate(true);
            status.setText("Checking if input xml files match template schema");
            //todo update progress, future work
            new SwingWorker<>()
            {
                @Override
                protected Object doInBackground()
                {
                    xmlFileNameAndFile.parallelStream().forEach(fileNameAndFile -> {
                        File file = fileNameAndFile.getFile();
                        if (Logic.validateSchema(file))
                        {
                            //todo s out
                            System.out.println("template matches");
                        }
                        else
                        {
                            //todo s out
                            System.out.println(" template doesn't match");
                        }
                    });
                    return null;
                }

                protected void done()
                {
                    status.setText("Ready");
                    progressBar.setIndeterminate(false);
                    extractDataButton.setEnabled(true);
                }
            }.execute();
        }
        else
        {
            status.setText("no template file selected");
        }

    }

    public void extractData()
    {
        //new File("/home/satya/Downloads/out");
        File outputFolder = uiLogic.folderPicker("Select folder to save XLS output");
        try
        {
            FileUtils.cleanDirectory(new File(Constants.TEMPORARY_FOLDER + "/extraction/files/"));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        if (mergeAllOutputXLSCheckBox.isSelected())
        {
            status.setText("Processing...this may take a while");
            progressBar.setIndeterminate(true);
            new SwingWorker<>()
            {
                @Override
                protected Object doInBackground()
                {
                    List<NodeValue> mergedNodeValues = new ArrayList<>();
                    for (FileNameAndFile fileNameAndFile : xmlFileNameAndFile)
                    {
                        File file = fileNameAndFile.getFile();
                        if (includeParentParametersForCheckBox.isSelected())
                        {
                            Extractor extractor = new Extractor(file, parameterListInput, Integer.parseInt(depth.getText()));
                            mergedNodeValues.addAll(extractor.getArrayCSV());
                            mergedNodeValues.addAll(extractor.getObjectCSV());
                        }
                        else
                        {
                            Extractor extractor = new Extractor(file, parameterListInput);
                            mergedNodeValues.addAll(extractor.getArrayCSV());
                            mergedNodeValues.addAll(extractor.getObjectCSV());
                        }
                    }
                    String filesPath = Constants.TEMPORARY_FOLDER + "/extraction/merged/";
                    File csvDirectory = new File(filesPath);
                    try
                    {
                        if (csvDirectory.exists())
                        {
                            FileUtils.cleanDirectory(csvDirectory);
                        }
                        else
                        {
                            FileUtils.forceMkdir(csvDirectory);

                        }
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                    new CSVHandler(new ArrayList<>(), mergedNodeValues, filesPath, outputFolder, true, CSVRadioButton.isSelected());
                    return null;
                }

                protected void done()
                {
                    status.setText("Done");
                    progressBar.setIndeterminate(false);
                }
            }.execute();
        }
        else
        {
            //todo multi thread this
            status.setText("Extracting data..");
            progressBar.setIndeterminate(true);
            new SwingWorker<>()
            {
                @Override
                protected Object doInBackground()
                {
                    for (FileNameAndFile fileNameAndFile : xmlFileNameAndFile)
                    {
                        File file = fileNameAndFile.getFile();
                        if (includeParentParametersForCheckBox.isSelected())
                        {
                            Extractor extractor = new Extractor(file, parameterListInput, Integer.parseInt(depth.getText()));
                            Logic logic = new Logic();
                            new CSVHandler(extractor.getObjectCSV(), extractor.getArrayCSV(), logic.getCsvDirectoryFromFileName(file), outputFolder, false, CSVRadioButton.isSelected());
                        }
                        else
                        {
                            Extractor extractor = new Extractor(file, parameterListInput);
                            Logic logic = new Logic();
                            new CSVHandler(extractor.getObjectCSV(), extractor.getArrayCSV(), logic.getCsvDirectoryFromFileName(file), outputFolder, false, CSVRadioButton.isSelected());
                        }
                    }
                    return null;
                }

                protected void done()
                {
                    status.setText("Done");
                    progressBar.setIndeterminate(false);
                }
            }.execute();
        }
    }

    private void updateStatus()
    {

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
        extractDataPanel = new JPanel();
        extractDataPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabsPanel.addTab("Extract Data", extractDataPanel);
        final Spacer spacer2 = new Spacer();
        extractDataPanel.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 3), new Dimension(-1, 3), new Dimension(-1, 3), 0, false));
        xmlFilesPanel = new JPanel();
        xmlFilesPanel.setLayout(new GridLayoutManager(6, 7, new Insets(0, 0, 0, 0), -1, -1));
        extractDataPanel.add(xmlFilesPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, new Dimension(560, -1), 0, false));
        includeParentParametersForCheckBox = new JCheckBox();
        includeParentParametersForCheckBox.setText("Include parent parameters for arrays");
        xmlFilesPanel.add(includeParentParametersForCheckBox, new GridConstraints(4, 0, 1, 7, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mergeAllOutputXLSCheckBox = new JCheckBox();
        mergeAllOutputXLSCheckBox.setText("Merge extracted data");
        xmlFilesPanel.add(mergeAllOutputXLSCheckBox, new GridConstraints(3, 0, 1, 7, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        filesScrollPane = new JScrollPane();
        xmlFilesPanel.add(filesScrollPane, new GridConstraints(1, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, new Dimension(550, -1), 0, false));
        listOfFiles = new JList();
        listOfFiles.setDragEnabled(true);
        listOfFiles.setMaximumSize(new Dimension(-1, -1));
        filesScrollPane.setViewportView(listOfFiles);
        workingDirectory = new JLabel();
        workingDirectory.setText("Folder");
        xmlFilesPanel.add(workingDirectory, new GridConstraints(0, 2, 1, 5, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        depthPanel = new JPanel();
        depthPanel.setLayout(new GridLayoutManager(1, 6, new Insets(0, 0, 0, 0), -1, -1));
        xmlFilesPanel.add(depthPanel, new GridConstraints(5, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        depthPanel.add(spacer3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(20, -1), null, new Dimension(20, -1), 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Depth");
        depthPanel.add(label1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        decreaseDepth = new JButton();
        decreaseDepth.setText("-");
        depthPanel.add(decreaseDepth, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(30, -1), 0, false));
        depth = new JLabel();
        depth.setText("1");
        depthPanel.add(depth, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        increaseDepth = new JButton();
        increaseDepth.setText("+");
        depthPanel.add(increaseDepth, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(30, -1), 0, false));
        final Spacer spacer4 = new Spacer();
        depthPanel.add(spacer4, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        selectXMLFolderButton = new JButton();
        selectXMLFolderButton.setText("1. Load XML Folder");
        xmlFilesPanel.add(selectXMLFolderButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Directory: ");
        xmlFilesPanel.add(label2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        selectTemplateToExtractButton = new JButton();
        selectTemplateToExtractButton.setText("2. Select Template to extract data");
        xmlFilesPanel.add(selectTemplateToExtractButton, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        extractDataButton = new JButton();
        extractDataButton.setEnabled(false);
        extractDataButton.setText("3. Extract Data");
        xmlFilesPanel.add(extractDataButton, new GridConstraints(5, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        xmlFilesPanel.add(spacer5, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        clearButton = new JButton();
        clearButton.setEnabled(false);
        clearButton.setText("Clear");
        xmlFilesPanel.add(clearButton, new GridConstraints(2, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        XLSRadioButton = new JRadioButton();
        XLSRadioButton.setSelected(true);
        XLSRadioButton.setText("XLS");
        xmlFilesPanel.add(XLSRadioButton, new GridConstraints(5, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CSVRadioButton = new JRadioButton();
        CSVRadioButton.setText("CSV");
        xmlFilesPanel.add(CSVRadioButton, new GridConstraints(5, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        previewPanel = new JPanel();
        previewPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        extractDataPanel.add(previewPanel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        csvTabs = new JTabbedPane();
        previewPanel.add(csvTabs, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final Spacer spacer6 = new Spacer();
        main.add(spacer6, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(5, 5), new Dimension(5, 5), new Dimension(5, 5), 0, false));
        final Spacer spacer7 = new Spacer();
        main.add(spacer7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(5, 5), new Dimension(5, 5), new Dimension(5, 5), 0, false));
        status = new JLabel();
        status.setText("Status");
        main.add(status, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar = new JProgressBar();
        main.add(progressBar, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(XLSRadioButton);
        buttonGroup.add(XLSRadioButton);
        buttonGroup.add(CSVRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {return main;}

}
