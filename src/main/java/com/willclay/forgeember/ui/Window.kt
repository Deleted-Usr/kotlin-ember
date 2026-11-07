package com.willclay.forgeember.ui

import java.awt.Dimension
import javax.swing.JFrame

class Window : JFrame() {
    init {
        setDefaultCloseOperation(EXIT_ON_CLOSE)
        size = Dimension(1280, 720)

        add(GamePreview());
    }
}
