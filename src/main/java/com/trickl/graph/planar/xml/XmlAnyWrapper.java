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
package com.trickl.graph.planar.xml;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import java.util.LinkedHashMap;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;

public class XmlAnyWrapper<V> {

    private V value;
    
    private Class valueClass;
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    public XmlAnyWrapper() {
        AnnotationIntrospector annotationIntrospector
            = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        mapper.setAnnotationIntrospector(annotationIntrospector);
    }

    // Jackson does not support XmlAnyElement in 2.8     
    @XmlAnyElement(lax = true)
    public V getValue() {
        if (valueClass != null && 
            valueClass != value.getClass()) {
            // Conversion required
            value = (V) mapper.convertValue(value, valueClass);
        }
        
        return value;
    }
    
    @XmlAttribute(name="class")
    public Class getValueClass() {
        return valueClass;
    }

    public void setValue(V value) {
        this.value = value;
    }
    
    public void setValueClass(Class valueClass) {
        this.valueClass = valueClass;
    }
}
