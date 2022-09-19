package dev.insaneduck.extraction_tool.modal;

import java.io.File;

//object for setting data to jList
public class FileNameAndFile
{
    private String FileName;
    private File File;

    public FileNameAndFile()
    {
    }

    public FileNameAndFile(String fileName, java.io.File file)
    {
        FileName = fileName;
        File = file;
    }

    public String getFileName()
    {
        return FileName;
    }

    public void setFileName(String fileName)
    {
        FileName = fileName;
    }

    public java.io.File getFile()
    {
        return File;
    }

    public void setFile(java.io.File file)
    {
        File = file;
    }

    @Override
    public String toString()
    {
        return "FileNameAndFile{" +
                "FileName='" + FileName + '\'' +
                ", File=" + File +
                '}';
    }
}
