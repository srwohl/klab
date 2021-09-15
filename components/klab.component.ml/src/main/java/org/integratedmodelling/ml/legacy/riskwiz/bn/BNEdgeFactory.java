package org.integratedmodelling.ml.legacy.riskwiz.bn;

import org.integratedmodelling.ml.legacy.riskwiz.EdgeFactory;

public class BNEdgeFactory implements EdgeFactory {

    @Override
    public Object get() {
        return new BNEdge();
    }

    @Override
    public Object createEdge(Object sourceVertex, Object targetVertex) {
        return new BNEdge();
    }

}
