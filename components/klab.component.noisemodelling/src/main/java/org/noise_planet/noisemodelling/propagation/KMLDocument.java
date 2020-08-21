/**
 * NoiseMap is a scientific computation plugin for OrbisGIS developed in order to
 * evaluate the noise impact on urban mobility plans. This model is
 * based on the French standard method NMPB2008. It includes traffic-to-noise
 * sources evaluation and sound propagation processing.
 *
 * This version is developed at French IRSTV Institute and at IFSTTAR
 * (http://www.ifsttar.fr/) as part of the Eval-PDU project, funded by the
 * French Agence Nationale de la Recherche (ANR) under contract ANR-08-VILL-0005-01.
 *
 * Noisemap is distributed under GPL 3 license. Its reference contact is Judicaël
 * Picaut <judicael.picaut@ifsttar.fr>. It is maintained by Nicolas Fortin
 * as part of the "Atelier SIG" team of the IRSTV Institute <http://www.irstv.fr/>.
 *
 * Copyright (C) 2011 IFSTTAR
 * Copyright (C) 2011-2012 IRSTV (FR CNRS 2488)
 *
 * Noisemap is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Noisemap is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Noisemap. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 *
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.noise_planet.noisemodelling.propagation;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.cts.CRSFactory;
import org.cts.IllegalCoordinateException;
import org.cts.crs.CRSException;
import org.cts.crs.CoordinateReferenceSystem;
import org.cts.crs.GeodeticCRS;
import org.cts.op.CoordinateOperation;
import org.cts.op.CoordinateOperationException;
import org.cts.op.CoordinateOperationFactory;
import org.cts.registry.EPSGRegistry;
import org.cts.registry.RegistryManager;
import org.locationtech.jts.io.kml.KMLWriter;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Export objects of NoiseModelling into KML format.
 * Modified version of org.h2gis.functions.io.kml.KMLWriterDriver
 * @author Erwan Bocher
 * @author Nicolas Fortin 2019
 */
public class KMLDocument {
//    private List<MeshBuilder.PolygonWithHeight> buildings = new ArrayList<>();
//    private List<TriMarkers> isoCountours = new ArrayList<>();
    private final XMLStreamWriter xmlOut;
    private Coordinate offset = new Coordinate(0, 0, 0);
    // 0.011 meters precision
    //https://gisjames.wordpress.com/2016/04/27/deciding-how-many-decimal-places-to-include-when-reporting-latitude-and-longitude/
    private int wgs84Precision = 7;
    private GeometryFactory geometryFactory = new GeometryFactory();
    private CoordinateOperation transform = null;

    public KMLDocument(OutputStream outputStream) throws XMLStreamException {
        final XMLOutputFactory streamWriterFactory = XMLOutputFactory.newFactory();
        streamWriterFactory.setProperty("escapeCharacters", false);
        xmlOut = streamWriterFactory.createXMLStreamWriter(
                new BufferedOutputStream(outputStream), "UTF-8");
    }

    /**
     * @return how-many-decimal-places-to-include-when-reporting-latitude-and-longitude
     */
    public int getWgs84Precision() {
        return wgs84Precision;
    }

    /**
     * @param wgs84Precision how-many-decimal-places-to-include-when-reporting-latitude-and-longitude
     */
    public void setWgs84Precision(int wgs84Precision) {
        this.wgs84Precision = wgs84Precision;
    }

    public void setInputCRS(String crs) throws CRSException, CoordinateOperationException {
        // Create a new CRSFactory, a necessary element to create a CRS without defining one by one all its components
        CRSFactory cRSFactory = new CRSFactory();

        // Add the appropriate registry to the CRSFactory's registry manager. Here the EPSG registry is used.
        RegistryManager registryManager = cRSFactory.getRegistryManager();
                registryManager.addRegistry(new EPSGRegistry());

        // CTS will read the EPSG registry seeking the 4326 code, when it finds it,
        // it will create a CoordinateReferenceSystem using the parameters found in the registry.
        CoordinateReferenceSystem crsKML = cRSFactory.getCRS("EPSG:4326");
        CoordinateReferenceSystem crsSource = cRSFactory.getCRS(crs);
        if(crsKML instanceof GeodeticCRS && crsSource instanceof GeodeticCRS) {
            transform = CoordinateOperationFactory.createCoordinateOperations((GeodeticCRS) crsSource, (GeodeticCRS) crsKML).iterator().next();
        }
    }

    public KMLDocument writeHeader() throws XMLStreamException {
        xmlOut.writeStartDocument("UTF-8", "1.0");
        xmlOut.writeStartElement("kml");
        xmlOut.writeDefaultNamespace("http://www.opengis.net/kml/2.2");
        xmlOut.writeNamespace("atom", "http://www.w3.org/2005/Atom");
        xmlOut.writeNamespace("kml", "http://www.opengis.net/kml/2.2");
        xmlOut.writeNamespace("gx", "http://www.google.com/kml/ext/2.2");
        xmlOut.writeNamespace("xal", "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0");
        xmlOut.writeStartElement("Document");
        return this;
    }

    public KMLDocument writeFooter() throws XMLStreamException {
        xmlOut.writeEndElement();//Doc
        xmlOut.writeEndDocument();//KML
        xmlOut.close();
        return this;
    }

    public KMLDocument setOffset(Coordinate offset) {
        this.offset = offset;
        return this;
    }

    private Coordinate copyCoord(Coordinate in) {
        return new Coordinate(in.x + offset.x, in.y + offset.y, Double.isNaN(in.z) ? offset.z : in.z + offset.z);
    }

    public KMLDocument writeTopographic(List<Triangle> triVertices, List<Coordinate> vertices) throws XMLStreamException {
        // Write style
        xmlOut.writeStartElement("Style");
        xmlOut.writeAttribute("id", "mnt");
        xmlOut.writeStartElement("LineStyle");
        xmlOut.writeStartElement("width");
        xmlOut.writeCharacters("0");
        xmlOut.writeEndElement(); // width
        xmlOut.writeEndElement();// LineStyle
        xmlOut.writeStartElement("PolyStyle");
        xmlOut.writeStartElement("color");
        xmlOut.writeCharacters("640078FF");
        xmlOut.writeEndElement(); // color
        xmlOut.writeEndElement();// LineStyle
        xmlOut.writeEndElement();// Style

        xmlOut.writeStartElement("Schema");
        xmlOut.writeAttribute("name", "mnt");
        xmlOut.writeAttribute("id", "mnt");
        xmlOut.writeEndElement();//Write schema
        xmlOut.writeStartElement("Folder");
        xmlOut.writeStartElement("name");
        xmlOut.writeCharacters("mnt");
        xmlOut.writeEndElement();//Name
        xmlOut.writeStartElement("Placemark");
        xmlOut.writeStartElement("styleUrl");
        xmlOut.writeCharacters("#mnt");
        xmlOut.writeEndElement(); // styleUrl
        xmlOut.writeStartElement("name");
        xmlOut.writeCharacters("tri");
        xmlOut.writeEndElement();//Name
        Polygon[] polygons = new Polygon[triVertices.size()];
        int idTri = 0;
        for(Triangle triangle : triVertices) {
            Polygon poly = geometryFactory.createPolygon(new Coordinate[]{copyCoord(vertices.get(triangle.getA())),
                    copyCoord(vertices.get(triangle.getB())), copyCoord(vertices.get(triangle.getC())),
                    copyCoord(vertices.get(triangle.getA()))});
            if(CGAlgorithms.isCCW(poly.getCoordinates())) {
                poly = (Polygon) poly.reverse();
            }
            // Apply CRS transform
            doTransform(poly);
            polygons[idTri++] = poly;
        }
        //Write geometry
        xmlOut.writeCharacters(KMLWriter.writeGeometry(geometryFactory.createMultiPolygon(polygons), Double.NaN,
                wgs84Precision, false, KMLWriter.ALTITUDE_MODE_ABSOLUTE));
        xmlOut.writeEndElement();//Write Placemark
        xmlOut.writeEndElement();//Folder
        return this;
    }

    public KMLDocument writeBuildings(FastObstructionTest manager) throws XMLStreamException {
        xmlOut.writeStartElement("Schema");
        xmlOut.writeAttribute("name", "buildings");
        xmlOut.writeAttribute("id", "buildings");
        xmlOut.writeEndElement();//Write schema
        xmlOut.writeStartElement("Folder");
        xmlOut.writeStartElement("name");
        xmlOut.writeCharacters("buildings");
        xmlOut.writeEndElement();//Name
        xmlOut.writeStartElement("Placemark");
        xmlOut.writeStartElement("name");
        xmlOut.writeCharacters("building");
        xmlOut.writeEndElement();//Name
        List<MeshBuilder.PolygonWithHeight> buildings = manager.getPolygonWithHeight();
        List<Polygon> polygons = new ArrayList<>(buildings.size());
        int idPoly = 0;

        for(MeshBuilder.PolygonWithHeight triangle : buildings) {
            Coordinate[] original = triangle.geo.getCoordinates();
            Coordinate[] coordinates = new Coordinate[original.length];
            double z = manager.getBuildingRoofZ(idPoly + 1);
            for(int i = 0; i < coordinates.length; i++) {
                coordinates[i] = copyCoord(new Coordinate(original[i].x, original[i].y, z));
            }
            if(coordinates.length > 3 && coordinates[0].equals2D(coordinates[coordinates.length - 1])) {
                Polygon poly = geometryFactory.createPolygon(coordinates);
                if(!CGAlgorithms.isCCW(poly.getCoordinates())) {
                    poly = (Polygon) poly.reverse();
                }
                // Apply CRS transform
                doTransform(poly);
                polygons.add(poly);
            }
            idPoly++;
        }
        //Write geometry
        xmlOut.writeCharacters(KMLWriter.writeGeometry(geometryFactory.createMultiPolygon(
                polygons.toArray(new Polygon[polygons.size()])), Double.NaN,
                wgs84Precision, true, KMLWriter.ALTITUDE_MODE_RELATIVETOGROUND));
        xmlOut.writeEndElement();//Write Placemark
        xmlOut.writeEndElement();//Folder
        return this;
    }

    public KMLDocument writeRays(Collection<PropagationPath> rays) throws XMLStreamException {
        xmlOut.writeStartElement("Schema");
        xmlOut.writeAttribute("name", "rays");
        xmlOut.writeAttribute("id", "rays");
        xmlOut.writeEndElement();//Write schema
        xmlOut.writeStartElement("Folder");
        xmlOut.writeStartElement("name");
        xmlOut.writeCharacters("rays");
        xmlOut.writeEndElement();//Name
        for(PropagationPath line : rays) {
            xmlOut.writeStartElement("Placemark");
            xmlOut.writeStartElement("name");
            xmlOut.writeCharacters(String.format("R:%d S:%d", line.idReceiver, line.idSource));
            xmlOut.writeEndElement();//Name
            Coordinate[] coordinates = new Coordinate[line.getPointList().size()];
            int i=0;
            for(PointPath pointPath : line.getPointList()) {
                coordinates[i++] = copyCoord(pointPath.coordinate);
            }
            LineString lineString = geometryFactory.createLineString(coordinates);
            // Apply CRS transform
            doTransform(lineString);
            //Write geometry
            xmlOut.writeCharacters(KMLWriter.writeGeometry(lineString, Double.NaN,
                    wgs84Precision, false, KMLWriter.ALTITUDE_MODE_ABSOLUTE));
            xmlOut.writeEndElement();//Write Placemark
        }
        xmlOut.writeEndElement();//Folder
        return this;
    }

    public void doTransform(Geometry geometry) {
        if(transform != null && geometry != null) {
            geometry.apply(new CRSTransformFilter(transform));
            // Recompute envelope
            geometry.setSRID(4326);
        }
    }

    public static final class CRSTransformFilter implements CoordinateFilter {
        private final CoordinateOperation coordinateOperation;

        public CRSTransformFilter(CoordinateOperation coordinateOperation) {
            this.coordinateOperation = coordinateOperation;
        }

        public void filter(Coordinate coord) {
            try {
                if (Double.isNaN(coord.z)) {
                    coord.z = 0.0D;
                }

                double[] xyz = this.coordinateOperation.transform(new double[]{coord.x, coord.y, coord.z});
                coord.x = xyz[0];
                coord.y = xyz[1];
                if (xyz.length > 2) {
                    coord.z = xyz[2];
                }
            } catch (IllegalCoordinateException | CoordinateOperationException var3) {
                Logger.getLogger(KMLDocument.class.getName()).log(Level.SEVERE, (String)null, var3);
            }

        }
    }

    public static void exportScene(String name, FastObstructionTest manager, ComputeRaysOut result) throws IOException {
        try {
            Coordinate proj = new Coordinate( 351714.794877, 6685824.856402, 0);
            FileOutputStream outData = new FileOutputStream(name);
            KMLDocument kmlDocument = new KMLDocument(outData);
            kmlDocument.setInputCRS("EPSG:2154");
            kmlDocument.setOffset(proj);
            kmlDocument.writeHeader();
            if(manager != null) {
                kmlDocument.writeTopographic(manager.getTriangles(), manager.getVertices());
            }
            if(result != null) {
                kmlDocument.writeRays(result.getPropagationPaths());
            }
            if(manager != null && manager.isHasBuildingWithHeight()) {
                kmlDocument.writeBuildings(manager);
            }
            kmlDocument.writeFooter();
        } catch (XMLStreamException | CoordinateOperationException | CRSException ex) {
            throw new IOException(ex);
        }
    }
}
