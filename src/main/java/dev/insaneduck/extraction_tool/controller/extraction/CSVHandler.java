package dev.insaneduck.extraction_tool.controller.extraction;

import dev.insaneduck.extraction_tool.controller.Logic;
import dev.insaneduck.extraction_tool.modal.Constants;
import dev.insaneduck.extraction_tool.modal.NodeValue;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

//anything that deals with cvs or xls is in this class
public class CSVHandler
{

    public CSVHandler(List<NodeValue> objectCSV, List<NodeValue> arrayCSV, String filesPath, File outputFolder, boolean merge, boolean exportTypeCsv)
    {
        if (!objectCSV.isEmpty())
        {
            nodeValuesToCsvFile(objectCSV, filesPath);
        }
        if (!arrayCSV.isEmpty())
        {
//            arrayCSV.forEach(nodeValue -> {
//                List<NodeValue> temp = arrayCSV.stream().filter(n -> n.getNodeName().toString().equals(nodeValue.getNodeName().toString())).toList();
//                if (temp.size() > 65000)
//                {
//                    fileTooBigFlag = true;
//                }
//            });
            nodeValuesToCsvFile(arrayCSV, filesPath);
        }
        try
        {
            //generating xls from csv files
            if (outputFolder != null)
            {
                File xlsFile = new File(outputFolder + "/" + StringUtils.chop(filesPath.substring((StringUtils.chop(filesPath)).lastIndexOf("/"))) + ".xls");
                if (merge)
                {
                    if (exportTypeCsv)
                    {
                        FileUtils.copyToDirectory(new File(filesPath), outputFolder);
                    }
                    else
                    {
                        convertCsvToXls(new File(filesPath), xlsFile);
                    }
                    System.out.println("\ngenerated output files to " + outputFolder);
                }
                else
                {
                    if (exportTypeCsv)
                    {
                        File[] files = new File(Constants.TEMPORARY_FOLDER + "/extraction/files/").listFiles();
                        if (files != null)
                        {
                            for (File f : files)
                            {
                                FileUtils.copyToDirectory(f, outputFolder);
                            }
                        }
                    }
                    else
                    {
                        //todo handle this
                        convertCsvToXls(new File(filesPath), xlsFile);
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void nodeValuesToCsvFile(List<NodeValue> nodeValues, String filesPath)
    {
        for (NodeValue nodeValue : nodeValues)
        {
            File file = new File(filesPath + nodeValue.getNodeName().toString() + ".csv");
            if (file.exists())
            {
                try
                {
                    FileUtils.writeStringToFile(file, "\n" + hashMapToCsv(nodeValue.getNodeKeysAndValues(), false), true);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
            else
            {
                Logic.writeTextToFile(file, hashMapToCsv(nodeValue.getNodeKeysAndValues(), true));
            }
        }
    }

    public String hashMapToCsv(LinkedHashMap<String, String> hashMap, boolean isNewFile)
    {
        String csv = "";
        String columnNames = "";
        for (String string : hashMap.values())
        {
            csv = csv + "," + string;
        }
        if (isNewFile)
        {
            for (String string : hashMap.keySet())
            {
                columnNames = columnNames + "," + string;
            }
            columnNames = columnNames.substring(1);
            columnNames = columnNames + "\n";
            return columnNames + csv.substring(1);
        }
        return csv.substring(1);
    }

    public void objectToCsv(List<NodeValue> nodeValues, String filesPath)
    {
        String csv = "";
        for (NodeValue nodeValue : nodeValues)
        {
            String content = "";
            csv = csv + nodeValue.getNodeName().toString() + "\n";
            for (String s : nodeValue.getNodeKeysAndValues().keySet())
            {
                content = content + s + "," + nodeValue.getNodeKeysAndValues().get(s) + "\n";
            }
            csv = csv + content + "\n";
        }
        Logic.writeTextToFile(new File(filesPath + "root.csv"), csv);
    }

    //takes csv directory as input and outputs xls in same folder
    public void convertCsvToXls(File csvDirectory, File xlsOutput)
    {
        String thisLine;
        int rowCounter;
        List<String> rowList;
        try
        {
            HSSFWorkbook workbook = new HSSFWorkbook();
            for (File file : Objects.requireNonNull(csvDirectory.listFiles()))
            {
                if (file.isFile())
                {
                    rowCounter = 0;
                    rowList = new ArrayList<>();
                    HSSFSheet sheet = workbook.createSheet(file.getName().substring(0, file.getName().lastIndexOf(".")));
                    FileInputStream fileInputStream = new FileInputStream(file);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

                    while ((thisLine = bufferedReader.readLine()) != null)
                    {
                        rowList.add(thisLine);
                    }
                    for (String rowLine : rowList)
                    {
                        HSSFRow row = sheet.createRow(rowCounter);
                        rowCounter++;
                        String[] rowContentArr = rowLine.split(",");
                        for (int p = 0; p < rowContentArr.length; p++)
                        {
                            HSSFCell cell = row.createCell(p);
                            cell.setCellValue(rowContentArr[p]);
                        }
                    }
                    fileInputStream.close();
                    workbook.write(xlsOutput);
                    bufferedReader.close();
                }
            }
            System.out.println("generated xls file " + xlsOutput.getName());
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null, "error occurred while exporting to xls file reached row limit for XLS");
            System.out.println("error occurred while exporting to xls file");
            try
            {
                FileUtils.copyToDirectory(csvDirectory, xlsOutput.getParentFile());
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            ex.printStackTrace();
        }
    }
}
