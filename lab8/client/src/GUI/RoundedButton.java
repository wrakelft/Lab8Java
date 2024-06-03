package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedButton extends JButton {
    public RoundedButton(String text) {
        super(text);
        setOpaque(false); // Чтобы фоновый цвет кнопки не закрашивал её фон
        setFocusPainted(false); // Чтобы убрать рамку фокуса
        setContentAreaFilled(false); // Чтобы фон не заполнялся цветом
        setBorderPainted(false); // Чтобы убрать границу кнопки
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape round = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);


        // Задаем цвет фона и рисуем скругленный прямоугольник
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));

        // Рисуем текст кнопки

        g2.setColor(getForeground());
        g2.draw(round);
//        g2.drawString(getText(), (r.width - fm.stringWidth(getText())) / 2, (r.height + fm.getAscent()) / 2 - 2);

        g2.dispose();
        super.paintComponent(g);
    }
}
