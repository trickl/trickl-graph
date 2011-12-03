/*
 * This file is part of the Trickl Open Source Libraries.
 *
 * Trickl Open Source Libraries - http://open.trickl.com/
 *
 * Copyright (C) 2011 Tim Gee.
 *
 * Trickl Open Source Libraries are free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Trickl Open Source Libraries are distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this project.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.trickl.graph.xml.bind;

public enum WellKnownNamespace {

   TRICKL("trickl", "http://trickl.com"),
   XML_SCHEMA("xs", "http://www.w3.org/2001/XMLSchema"),
   XML_SCHEMA_INSTANCE("xsi", "http://www.w3.org/2001/XMLSchema-instance"),
   XML_SCHEMA_DATATYPES("xsd", "http://www.w3.org/2001/XMLSchema-datatypes"),
   XML_NAMESPACE_URI("ns", "http://www.w3.org/XML/1998/namespace"),
   XML_MIME_URI("mime", "http://www.w3.org/2005/05/xmlmime"),
   XHTML("xhtml", "http://www.w3.org/1999/xhtml"),
   JAXB("jaxb", "http://java.sun.com/xml/ns/jaxb"),
   UNDEFINED("undefined", "");

   private String prefix;
   private String uri;

   private WellKnownNamespace(String prefix, String uri) {
      this.prefix = prefix;
      this.uri = uri;
   }
   
   public String getPrefix() {
      return prefix;
   }
   
   public String getURI() {
      return uri;
   }

   @Override
   public String toString() {
      return uri;
   }
}
