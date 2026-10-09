package com.willclay.forgeember

import com.willclay.forgeember.ui.Window
import javax.swing.SwingUtilities

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater(Runnable {
            val w = Window().apply {
                title = "Forge Ember"
                isVisible = true
            }
        })
    }
}
