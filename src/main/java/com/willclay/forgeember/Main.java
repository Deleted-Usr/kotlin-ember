package com.willclay.forgeember;

import com.willclay.forgeember.ui.Window;

import javax.swing.*;

public class Main
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            Window w = new Window();
            w.setVisible(true);
        });
    }
}
