package com.trickl.graph.planar.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class XmlAnyWrapperAdapter<V> extends XmlAdapter<XmlAnyWrapper<V>, V> {

   @Override
   public V unmarshal(XmlAnyWrapper<V> xmlAnyWrapper) throws Exception {
      return xmlAnyWrapper.getValue();
   }

   @Override
   public XmlAnyWrapper<V> marshal(V value) throws Exception {
      XmlAnyWrapper<V> wrapper = new XmlAnyWrapper<V>();
      wrapper.setValue(value);
      return wrapper;
   }
 }
