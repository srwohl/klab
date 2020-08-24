package org.integratedmodelling.klab.components.geospace.utils;

import org.integratedmodelling.klab.exceptions.KlabInternalErrorException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

import com.vividsolutions.jts.io.WKBWriter;

public class JTSUtils {

	static WKBWriter wkbWriter = new WKBWriter();
	
	@SuppressWarnings("unchecked")
	public static <T extends Geometry, E extends com.vividsolutions.jts.geom.Geometry> T convert(E geometry) {
		String s = WKBWriter.toHex(wkbWriter.write(geometry));
		try {
			return (T)new WKBReader().read(WKBReader.hexToBytes(s));
		} catch (ParseException e) {
			throw new KlabInternalErrorException(e);
		}
	}
}
