package com.willclay.forgeember.ui

import java.awt.Color
import java.awt.Graphics
import javax.swing.JPanel

class GamePreview : JPanel() {
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        g.color = Color.RED;
        g.fillRect(400, 350, 150, 150)
    }
}
