package org.integratedmodelling.klab.components.geospace.utils;

import org.integratedmodelling.klab.exceptions.KlabInternalErrorException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

import com.vividsolutions.jts.io.WKBWriter;

public class JTSUtils {

	static WKBWriter wkbWriter = new WKBWriter();
	static org.locationtech.jts.io.WKBWriter wkbLegacyWriter = new org.locationtech.jts.io.WKBWriter();
	
	@SuppressWarnings("unchecked")
	public static <T extends Geometry, E extends com.vividsolutions.jts.geom.Geometry> T convert(E geometry) {
		String s = WKBWriter.toHex(wkbWriter.write(geometry));
		try {
			return (T)new WKBReader().read(WKBReader.hexToBytes(s));
		} catch (ParseException e) {
			throw new KlabInternalErrorException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends com.vividsolutions.jts.geom.Geometry, E extends Geometry> T convert(E geometry) {
		String s = org.locationtech.jts.io.WKBWriter.toHex(wkbLegacyWriter.write(geometry));
		try {
			return (T)new com.vividsolutions.jts.io.WKBReader().read(org.locationtech.jts.io.WKBReader.hexToBytes(s));
		} catch (com.vividsolutions.jts.io.ParseException e) {
			throw new KlabInternalErrorException(e);
		}
	}
}
