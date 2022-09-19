package dev.insaneduck.extraction_tool.modal;

//class containing constant
//convenient for parameter that are used in multiple places, so you can change here, and it will update everywhere
public class Constants
{
    //temp folder location
    public static String TEMPORARY_FOLDER = System.getProperty("user.home") + "/.cache/xml_tool_temp";
    public static String TEMPLATE_GENERATION = TEMPORARY_FOLDER + "/template";

    public static String EXTRACTION = TEMPORARY_FOLDER + "/extraction";

    public static String EXTRACTION_TEMPLATE = TEMPORARY_FOLDER + "/extraction/template";

    public static String EXTRACTION_FILES = TEMPORARY_FOLDER + "/extraction/files";

    public static String EXTRACTION_MERGED = TEMPORARY_FOLDER + "/extraction/merged";
}
