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
package com.trickl.graph.vertices;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="id-vertex")
@XmlRootElement(name="id-vertex")
public class IdVertex implements Serializable {

   private Integer id;

   private IdVertex() {
      this(null);
   }

   public IdVertex(Integer id) {
      this.id = id;
   }

   @XmlID
   @XmlAttribute(name="id")
   public String getIdString() {
      String prefix = getIdStringPrefix();
      return prefix + (id == null ? "" : Integer.toString(id));
   }

   protected void setIdString(String idString) {
      String prefix = getIdStringPrefix();
      if (idString.startsWith(prefix)) idString = idString.substring(prefix.length());
      this.id = Integer.parseInt(idString);
   }

   public String getIdStringPrefix() {
      // XML ids need to be unique across the entire document, so a unique prefix per class
      // is required
      XmlType classXmlType = this.getClass().getAnnotation(XmlType.class);
      return (classXmlType == null ? "" : classXmlType.name() + "-");
   }

   @XmlTransient
   public Integer getId() {
      return id;
   }

   @Override
   public String toString() {
      return Integer.toString(id);
   }

   @Override
   public int hashCode() {
      int hash = 5;
      hash = 83 * hash + (this.id != null ? this.id.hashCode() : 0);
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final IdVertex other = (IdVertex) obj;
      if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
         return false;
      }
      return true;
   }
}
