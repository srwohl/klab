package org.integratedmodelling.ml.legacy.riskwiz;

import java.util.function.Supplier;

/**
 * A vertex factory used by graph algorithms for creating new vertices.
 * Normally, vertices are constructed by user code and added to a graph
 * explicitly, but algorithms which generate new vertices require a factory.
 *
 * @author John V. Sichi
 * @since Sep 16, 2003
 */
public interface VertexFactory<V> extends Supplier<V>
{
    

    /**
     * Creates a new vertex.
     *
     * @return the new vertex
     */
    public V createVertex();
}