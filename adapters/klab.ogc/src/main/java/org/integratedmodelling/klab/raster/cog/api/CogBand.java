package org.integratedmodelling.klab.raster.cog.api;

import java.util.HashSet;
import java.util.Set;

import org.integratedmodelling.klab.utils.Range;

public class CogBand {
	String name;
	Set<Double> nodata = new HashSet<>();
	Range boundaries;

}
