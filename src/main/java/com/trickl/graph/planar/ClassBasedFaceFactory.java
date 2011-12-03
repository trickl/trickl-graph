package com.trickl.graph.planar;

import java.io.Serializable;

public class ClassBasedFaceFactory<V, F>
        implements FaceFactory<V, F>,
		             Serializable {
 static final long serialVersionUID = 3618135658586388792L;

	private final Class<? extends F> faceClass;

	public ClassBasedFaceFactory(Class<? extends F> faceClass) {
		this.faceClass = faceClass;
	}

   @Override
	public F createFace(V source, V target, boolean isBoundary) {
		try {
			return faceClass.newInstance();
		} catch (Exception ex) {
			throw new RuntimeException("Face factory failed", ex);
		}
	}
}

