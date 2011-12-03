package com.trickl.graph.xml.bind;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import java.util.Arrays;

public class DefaultNamespace extends NamespacePrefixMapper {

   public final static String PROPERTY_NAME = "com.sun.xml.bind.namespacePrefixMapper";
   private String defaultNamespaceUri = "";
   private String[] predeclaredNamespaceUris = new String[0];

   public DefaultNamespace() {
   }

   public DefaultNamespace(String defaultNamespaceUri) {
      this.defaultNamespaceUri = defaultNamespaceUri;
   }

   public DefaultNamespace(String... uris) {
      if (uris.length > 0) {
         this.defaultNamespaceUri = uris[0];
         if (uris.length > 1) {
            this.predeclaredNamespaceUris = Arrays.copyOfRange(uris, 1, uris.length);
         }
      }
   }

   public void setDefaultNamespaceUri(String defaultNamespaceUri) {
      this.defaultNamespaceUri = defaultNamespaceUri;
   }

   public void setPreDeclaredNamespaceUris(String[] predeclaredNamespaceUris) {
      this.predeclaredNamespaceUris = predeclaredNamespaceUris;
   }

   @Override
   public String[] getPreDeclaredNamespaceUris() {
      return predeclaredNamespaceUris;
   }

   @Override
   public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
      if (namespaceUri.equals(defaultNamespaceUri)) {
         return "";
      }

      for (WellKnownNamespace namespace : WellKnownNamespace.values()) {
         if (namespaceUri.equals(namespace.getURI())) {
            return namespace.getPrefix();
         }
      }

      return suggestion;
   }
}