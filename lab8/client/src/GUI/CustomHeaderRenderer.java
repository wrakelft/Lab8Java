package GUI;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class CustomHeaderRenderer implements TableCellRenderer {
    private final TableCellRenderer originalRenderer;

    public CustomHeaderRenderer(TableCellRenderer originalRenderer) {
        this.originalRenderer = originalRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component header = originalRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(145, 187, 188));
        header.setForeground(Color.WHITE);
        return header;
    }
}
