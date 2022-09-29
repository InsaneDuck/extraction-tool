package dev.insaneduck.extraction_tool.main;

import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import dev.insaneduck.extraction_tool.controller.CommandLine;
import dev.insaneduck.extraction_tool.controller.Logic;
import dev.insaneduck.extraction_tool.view.MainGUI;

import javax.swing.*;
import java.util.Objects;

//init class
public class Main
{
    public static void main(String[] args)
    {
        try
        {
            if (args.length > 1)
            {
                new CommandLine(args);
            }
            else if (Objects.equals(args[0], "--gui"))
            {
                FlatOneDarkIJTheme.setup();
                //UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                Logic.setupTempFolder();
                new MainGUI();
            }
            else if (Objects.equals(args[0], "--help"))
            {
                printCommands();
            }
            else if (!Objects.equals(args[0], "--gui") && !Objects.equals(args[0], "--help"))
            {
                System.out.println("Invalid Arguments");
                System.out.println("try --help");
            }
        }
        catch (Exception e)
        {
            System.out.println("No Arguments provided");
            System.out.println("try --help");
        }

    }

    static void printCommands()
    {
        String instructions = """
                XML Tool Usage
                                        
                "--gui"      - to start application in GUI mode
                "--template" - to define template, when not defined it will automatically extract all data
                "--input"    - to define folder containing xml files
                "--output"   - to defined folder to export output
                "--depth"    - to define depth (optional)
                "--merge"    - to merge all output files into one (optional)
                                        
                Usage examples:
                                        
                for using as command line application
                java jar XML_Tool.jar --template="/path/to/template" --input="/path/to/xml/folder" --output="/path/to/output/folder" --depth=1 --merge
                                        
                for using the application with GUI
                java jar XML_Tool.jar --gui
                (for gui do not add any more argument other than --gui)        
                """;
        System.out.println(instructions);
    }
}
