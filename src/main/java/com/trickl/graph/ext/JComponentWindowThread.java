/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trickl.graph.ext;

import java.awt.event.WindowListener;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 *
 * @author tgee
 */
public class JComponentWindowThread<V, E> implements Runnable {
   private JComponent component;
   private WindowListener windowListener;
   private String title;
   private int x;
   private int y;
   private int width;
   private int height;

   public JComponentWindowThread(JComponent component, String title, int x, int y, int width, int height, WindowListener windowListener) {
      this.component = component;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.windowListener = windowListener;
   }

   @Override
   public void run() {
      JFrame mainFrame = new JFrame(title);
      mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      mainFrame.setLocation(x, y);
      mainFrame.setSize(width, height);
      mainFrame.add(component);
      mainFrame.setVisible(true);
      mainFrame.addWindowListener(windowListener);
   }   
}
