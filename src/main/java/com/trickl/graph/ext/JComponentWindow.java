package com.trickl.graph.ext;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.VertexNameProvider;

public class JComponentWindow<V, E> extends WindowAdapter {
   private JComponent component;
   private String title;
   private int x;
   private int y;
   private int width;
   private int height;
   
   public JComponentWindow(JComponent component) {
      this(component, "Graph Window", 0, 0, 600, 600, false, null, null, null, null);
   }

   public JComponentWindow(JComponent component, String title, int x, int y, int width, int height,  
           boolean isEditable,
           VertexNameProvider<V> vertexLabelProvider,
           EdgeNameProvider<E> edgeLabelProvider,
           ComponentAttributeProvider<V> vertexAttributeProvider,
           ComponentAttributeProvider<E> edgeAttributeProvider) {
      this.component = component;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }
   
   public void show() throws InterruptedException, InvocationTargetException {            
      Runnable runnable = new JComponentWindowThread(component, title, x, y, width, height, this);
      SwingUtilities.invokeLater(runnable);
   }

   public void showAndWait() throws InterruptedException, InvocationTargetException {
      show();
      synchronized (this) {
         wait();
      }
   }

   @Override
   public void windowClosed(WindowEvent e) {
      synchronized (this) {
         this.notifyAll();
      }
   }
}
