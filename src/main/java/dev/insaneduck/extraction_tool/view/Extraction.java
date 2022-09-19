package dev.insaneduck.extraction_tool.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import dev.insaneduck.extraction_tool.controller.Logic;
import dev.insaneduck.extraction_tool.controller.UiLogic;
import dev.insaneduck.extraction_tool.controller.extraction.CSVHandler;
import dev.insaneduck.extraction_tool.controller.extraction.Extractor;
import dev.insaneduck.extraction_tool.modal.*;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Extraction
{
    List<FileNameAndFile> xmlFileNameAndFile = new ArrayList<>();
    List<Parameter> parameterListInput = new ArrayList<>();
    UiLogic uiLogic = new UiLogic();

    MainGUI mainGUI;
    private JPanel extractDataPanel;
    private JPanel xmlFilesPanel;
    private JCheckBox includeParentParametersForCheckBox;
    private JCheckBox mergeAllOutputXLSCheckBox;
    private JScrollPane filesScrollPane;
    private JList<String> listOfFiles;
    private JLabel workingDirectory;
    private JPanel depthPanel;
    private JButton decreaseDepth;
    private JLabel depth;
    private JButton increaseDepth;
    private JButton selectXMLFolderButton;
    private JButton selectTemplateToExtractButton;
    private JButton extractDataButton;
    private JButton clearButton;
    private JRadioButton XLSRadioButton;
    private JRadioButton CSVRadioButton;
    private JPanel previewPanel;
    private JTabbedPane csvTabs;
    private JPanel parentPanel;

    public Extraction()
    {
        selectXMLFolderButton.addActionListener(actionEvent -> selectXMLFiles());
        selectTemplateToExtractButton.addActionListener(actionEvent -> selectTemplateToExtract());
        extractDataButton.addActionListener(actionEvent -> extractData());
        depthPanel.setVisible(false);

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
        extractDataButton.setToolTipText("select a template before clicking extract");
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
            //todo pack
        });
    }

    public JPanel getExtractDataPanel()
    {
        return extractDataPanel;
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
            mainGUI.getStatus().setText("aborted");
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
            //todo pack
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
            mainGUI.getProgressBar().setIndeterminate(true);
            mainGUI.getStatus().setText("Checking if input xml files match template schema");
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
                    mainGUI.getStatus().setText("Ready");
                    mainGUI.getProgressBar().setIndeterminate(false);
                    extractDataButton.setEnabled(true);
                }
            }.execute();
        }
        else
        {
            mainGUI.getStatus().setText("no template file selected");
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
            mainGUI.getStatus().setText("Processing...this may take a while");
            mainGUI.getProgressBar().setIndeterminate(true);
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
                    mainGUI.getStatus().setText("Done");
                    mainGUI.getProgressBar().setIndeterminate(false);
                }
            }.execute();
        }
        else
        {
            //todo multi thread this
            mainGUI.getStatus().setText("Extracting data..");
            mainGUI.getProgressBar().setIndeterminate(true);
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
                    mainGUI.getStatus().setText("Done");
                    mainGUI.getProgressBar().setIndeterminate(false);
                }
            }.execute();
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
        extractDataPanel = new JPanel();
        extractDataPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        final Spacer spacer1 = new Spacer();
        extractDataPanel.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 3), new Dimension(-1, 3), new Dimension(-1, 3), 0, false));
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
        depthPanel.setVisible(true);
        xmlFilesPanel.add(depthPanel, new GridConstraints(5, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        depthPanel.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(20, -1), null, new Dimension(20, -1), 0, false));
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
        final Spacer spacer3 = new Spacer();
        depthPanel.add(spacer3, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
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
        final Spacer spacer4 = new Spacer();
        xmlFilesPanel.add(spacer4, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
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
        previewPanel.setVisible(false);
        extractDataPanel.add(previewPanel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        csvTabs = new JTabbedPane();
        previewPanel.add(csvTabs, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {return extractDataPanel;}

}
