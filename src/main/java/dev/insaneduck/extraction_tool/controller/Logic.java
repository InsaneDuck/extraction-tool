package dev.insaneduck.extraction_tool.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.insaneduck.extraction_tool.modal.Constants;
import dev.insaneduck.extraction_tool.modal.Parameter;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

//class that has different utility methods all static and public to be used anywhere
public class Logic extends Component
{
    //get List of files containing .xml extension in a folder
    public static File[] getFilesWithExtensionInDirectory(String directory, String extension)
    {
        File folder = new File(directory);
        return folder.listFiles((dir, name) -> name.endsWith("." + extension));
    }

    //convert xml to JSON
    public static String xmlToJson(String xmlString)
    {
        JSONObject xmlJson = XML.toJSONObject(xmlString);
        return xmlJson.toString(4);
    }

    //read text from any document containing plain text
    public static String readTextFromFile(File file)
    {
        try
        {
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    //writes text to a file in UTF-8
    public static void writeTextToFile(File file, String data)
    {
        try
        {
            FileUtils.writeStringToFile(file, data, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    //beautify a json string with proper indents
    public static String beautify(String json)
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            Object obj = mapper.readValue(json, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }

    //returns path to temporary folder which this program will create files when it's running
    public static void setupTempFolder()
    {
        try
        {
            File tempDirectory = new File(Constants.TEMPORARY_FOLDER);
            if (tempDirectory.exists())
            {
                FileUtils.cleanDirectory(tempDirectory);
            }
            //create temp directory
            FileUtils.forceMkdir(tempDirectory);
            //create folders in temp directory
            FileUtils.forceMkdir(new File(Constants.TEMPLATE_GENERATION));
            FileUtils.forceMkdir(new File(Constants.EXTRACTION));
            FileUtils.forceMkdir(new File(Constants.EXTRACTION_TEMPLATE));
            FileUtils.forceMkdir(new File(Constants.EXTRACTION_FILES));
            FileUtils.forceMkdir(new File(Constants.EXTRACTION_MERGED));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }

    public static void getParameterListFromTemplate(File template, List<Parameter> parameterListInput)
    {
        Zip.unZipFiles(template, new File(Constants.EXTRACTION_TEMPLATE));
        JSONObject jsonObject = new JSONObject(Logic.readTextFromFile(new File(Constants.EXTRACTION_TEMPLATE + "/template.json")));
        JSONArray jsonArray = jsonObject.getJSONArray("parameters");
        for (int i = 0; i < jsonArray.length(); i++)
        {
            Parameter parameter = new Parameter();
            parameter.setNodePath(jsonArray.getJSONObject(i).getString("nodePath"));
            parameter.setParameterClass(jsonArray.getJSONObject(i).getString("parameterClass"));
            parameter.setParameterName(jsonArray.getJSONObject(i).getString("parameterName"));
            parameter.setParameterType(jsonArray.getJSONObject(i).getString("parameterType"));
            parameter.setSelected(jsonArray.getJSONObject(i).getBoolean("selected"));
            parameterListInput.add(parameter);
        }
    }

    public static boolean validateSchema(File xml)
    {
        Logic logic = new Logic();
        String jsonSchema = Logic.readTextFromFile(new File(Constants.EXTRACTION_TEMPLATE + "/schema.json"));
        String json = Logic.xmlToJson(Logic.readTextFromFile(xml));
        //todo get schema here
        String tempSchema = "";
        return Objects.equals(tempSchema, jsonSchema);
    }

    public String getCsvDirectoryFromFileName(File xmlFile)
    {
        String filesPath = Constants.EXTRACTION_FILES + xmlFile.getName().replace(".xml", "") + "/";
        try
        {
            File file = new File(filesPath);
            if (file.exists())
            {
                FileUtils.deleteDirectory(file);
                FileUtils.forceMkdir(file);
            }
            else
            {
                FileUtils.forceMkdir(file);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return filesPath;
    }

}
