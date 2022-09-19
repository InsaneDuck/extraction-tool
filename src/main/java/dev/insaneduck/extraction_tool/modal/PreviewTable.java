package dev.insaneduck.extraction_tool.modal;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;

//to show preview table in ui
public class PreviewTable extends JPanel
{
    public PreviewTable(TableModel model)
    {
        setLayout(new BorderLayout());
        JTable table = new JTable(model);
        add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setDefaultEditor(Object.class, null);
        resizeColumnWidth(table);
    }

    public void resizeColumnWidth(JTable table)
    {
        TableColumnModel tableColumnModel = table.getColumnModel();
        for (int col = 0; col < table.getColumnCount(); col++)
        {
            int maxWidth = 0;
            //iterate over all rows to get max value to fit that
            for (int row = 0; row < table.getRowCount(); row++)
            {
                TableCellRenderer rend = table.getCellRenderer(row, col);
                Object value = table.getValueAt(row, col);
                Component component = rend.getTableCellRendererComponent(table, value, false, false, row, col);
                maxWidth = Math.max(component.getPreferredSize().width, maxWidth);
            }
            //check all column header and update maxWidth if needed based on header width
            TableColumn tableColumn = tableColumnModel.getColumn(col);
            TableCellRenderer tableCellRenderer = tableColumn.getHeaderRenderer();
            if (tableCellRenderer == null)
            {
                tableCellRenderer = table.getTableHeader().getDefaultRenderer();
            }
            Object headerValue = tableColumn.getHeaderValue();
            //row = 0 for header
            Component headerComp = tableCellRenderer.getTableCellRendererComponent(table, headerValue, false, false, 0, col);
            maxWidth = Math.max(maxWidth, headerComp.getPreferredSize().width);
            tableColumn.setPreferredWidth(maxWidth + 6);
        }
    }
}
