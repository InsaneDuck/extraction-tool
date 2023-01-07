package dev.insaneduck.extraction_tool.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

//class to deal with zip files

public class Zip
{
    public static void zipFiles(List<String> srcFiles, String output)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(output);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            for (String srcFile : srcFiles)
            {
                File fileToZip = new File(srcFile);
                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0)
                {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }
            zipOut.close();
            fos.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException
    {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator))
        {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

    public static void unZipFiles(File source, File destination)
    {
        try
        {
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(source));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null)
            {
                File newFile = newFile(destination, zipEntry);
                if (zipEntry.isDirectory())
                {
                    if (!newFile.isDirectory() && !newFile.mkdirs())
                    {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                }
                else
                {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs())
                    {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0)
                    {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
