package dev.insaneduck.extraction_tool.experimental;

import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import dev.insaneduck.extraction_tool.view.Extraction;
import dev.insaneduck.extraction_tool.view.Settings;

import javax.swing.*;

public class Test extends JFrame
{
    public Test()
    {

        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args)
    {
        FlatOneDarkIJTheme.setup();
        new Test();
    }
}
