package dev.insaneduck.extraction_tool.modal;

import javax.swing.*;
import java.awt.*;

//depth box in ui
public class IncrementBox extends JPanel
{
    private JButton increaseCount;
    private JTextField count;
    private JButton decreaseCount;

    public IncrementBox()
    {
        setLayout(new GridLayout());
        increaseCount.setText("+");
        decreaseCount.setText("-");
        count.setText("1");
        increaseCount.addActionListener(actionEvent -> {
            int i = Integer.parseInt(count.getText());
            i++;
            count.setText(String.valueOf(i));
        });
        decreaseCount.addActionListener(actionEvent -> {
            int i = Integer.parseInt(count.getText());
            if (i >= 1)
            {
                i--;
            }
            count.setText(String.valueOf(i));
        });
        add(increaseCount);
        add(count);
        add(decreaseCount);
    }

    public void createUIComponents()
    {
        increaseCount = new JButton();
        count = new JTextField();
        decreaseCount = new JButton();
    }

    public int getCount()
    {
        return Integer.parseInt(count.getText());
    }
}
