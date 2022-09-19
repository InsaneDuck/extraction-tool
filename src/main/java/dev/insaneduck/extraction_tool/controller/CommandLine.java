package dev.insaneduck.extraction_tool.controller;

import dev.insaneduck.extraction_tool.controller.extraction.CSVHandler;
import dev.insaneduck.extraction_tool.controller.extraction.Extractor;
import dev.insaneduck.extraction_tool.modal.Constants;
import dev.insaneduck.extraction_tool.modal.NodeValue;
import dev.insaneduck.extraction_tool.modal.Parameter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//class that implements functions when tool is used as command line
public class CommandLine
{
    private final List<Parameter> parameterListInput = new ArrayList<>();
    private boolean merge = false;
    private int depth = 0;
    private String templatePath = "";
    private String inputFolder = "";
    private String outputFolder = "";

    private boolean exportTypeCsv;

    public CommandLine(String[] arguments)
    {
        for (String string : arguments)
        {
            if (string.startsWith("--template"))
            {
                this.templatePath = string.substring(11);
            }
            else if (string.startsWith("--input"))
            {
                this.inputFolder = string.substring(8);
            }
            else if (string.startsWith("--output"))
            {
                this.outputFolder = string.substring(9);
            }
            else if (string.startsWith("--merge"))
            {
                this.merge = true;
            }
            else if (string.startsWith("--depth"))
            {
                try
                {
                    this.depth = Integer.parseInt(string.substring(8));
                }
                catch (Exception e)
                {
                    System.out.println("invalid depth value, it must be int value and greater than 0");
                }
            }
            else if (string.startsWith("--type"))
            {
                this.exportTypeCsv = string.substring(7).equals("csv");
            }
        }
        System.out.println(this);
        if (checkCommandParameters())
        {
            //todo process data
            //if template variable is not defined
            if (!templatePath.equals(""))
            {
                processTemplate();
            }
            processXML();
        }
    }

    @Override
    public String toString()
    {
        return "\nCommandLine\n" +
                "\ninputFolder='" + inputFolder + '\'' +
                "\noutputFolder='" + outputFolder + '\'' +
                "\ntemplatePath='" + templatePath + '\'' +
                "\ndepth=" + depth +
                "\nmerge=" + merge + '\n';
    }

    boolean checkCommandParameters()
    {
        if (this.templatePath.equals(""))
        {
            System.out.println("missing parameter template");
            System.out.println("proceeding to extract all data");
        }
        if (this.inputFolder.equals(""))
        {
            System.out.println("missing parameter input");
            return false;
        }
        if (this.outputFolder.equals(""))
        {
            System.out.println("missing parameter output");
            return false;
        }
        return true;
    }

    void processTemplate()
    {
        Logic.setupTempFolder();
        parameterListInput.clear();
        File template = new File(templatePath);
        Logic.getParameterListFromTemplate(template, parameterListInput);
    }

    void processXML()
    {
        try
        {
            FileUtils.cleanDirectory(new File(Constants.TEMPORARY_FOLDER + "/extraction/files/"));
            FileUtils.cleanDirectory(new File(Constants.TEMPORARY_FOLDER + "/extraction/merged/"));
        }
        catch (IOException e)
        {
            try
            {
                FileUtils.forceMkdir(new File(Constants.TEMPORARY_FOLDER + "/extraction/merged/"));
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        File[] xmlFiles = Logic.getFilesWithExtensionInDirectory(inputFolder, "xml");
        List<NodeValue> temp = new ArrayList<>();
        for (File file : xmlFiles)
        {
            System.out.println("processing file " + file.getName());
            //key and value pair for associating file name with file
            Extractor extractor;
            if (depth > 0)
            {
                extractor = new Extractor(file, parameterListInput, depth);
            }
            else
            {
                extractor = new Extractor(file, parameterListInput);
            }
            if (merge)
            {
                temp.addAll(extractor.getArrayCSV());
                temp.addAll(extractor.getObjectCSV());
            }
            else
            {
                Logic logic = new Logic();
                new CSVHandler(extractor.getObjectCSV(), extractor.getArrayCSV(), logic.getCsvDirectoryFromFileName(file), new File(outputFolder), false, exportTypeCsv);
            }
//            new Thread(() -> {
//
//            }).start();
        }
        if (merge)
        {
            new CSVHandler(new ArrayList<>(), temp, Constants.TEMPORARY_FOLDER + "/extraction/merged/", new File(outputFolder), true, exportTypeCsv);
        }
    }
}
