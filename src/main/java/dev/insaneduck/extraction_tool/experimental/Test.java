package dev.insaneduck.extraction_tool.experimental;

import dev.insaneduck.extraction_tool.view.Settings;

import javax.swing.*;

public class Test extends JFrame
{
    public Test()
    {
      add(new Settings().getSettingsPanel());
      pack();
      setVisible(true);
    }

    public static void main(String[] args)
    {
        new Test();
    }
}
