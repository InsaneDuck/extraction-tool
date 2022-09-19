package dev.insaneduck.extraction_tool.controller;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Objects;

//class for utility methods dealing with swing ui
public class UiLogic extends Component
{
    public File[] getFilesFromFolderPicker(String dialogTitle, String description, String extension, JLabel directory)
    {
        FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extension);
        File[] files = null;
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(filter);
        chooser.setDialogTitle(dialogTitle);
        chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            directory.setText(chooser.getSelectedFile().toString());
            files = Logic.getFilesWithExtensionInDirectory(chooser.getSelectedFile().toString(), extension);
        }
        return files;
    }

    public File folderPicker(String dialogTitle)
    {
        File file = null;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(dialogTitle);
        chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            file = new File(chooser.getSelectedFile().toString() + "/");
        }
        return file;
    }

    //pops up when selecting xml for template generation,  when selecting template to extract data, and when saving template to file system
    public File filePicker(String dialogTitle, String description, String extension)
    {
        FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extension);
        File file = null;
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(filter);
        chooser.setDialogTitle(dialogTitle);
        chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (Objects.equals(description, "Template"))
        {
            chooser.setSelectedFile(new File("new.template"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                file = new File(chooser.getSelectedFile().toString());
            }
        }
        else
        {
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                file = new File(chooser.getSelectedFile().toString());
            }
        }
        return file;
    }
}
