package org.integratedmodelling.klab.raster.files;

import java.awt.image.DataBuffer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileWriter;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.RasterSymbolizer;
import org.integratedmodelling.kim.api.IParameters;
import org.integratedmodelling.klab.Logging;
import org.integratedmodelling.klab.Observations;
import org.integratedmodelling.klab.api.data.IGeometry.Dimension.Type;
import org.integratedmodelling.klab.api.data.ILocator;
import org.integratedmodelling.klab.api.data.IResource;
import org.integratedmodelling.klab.api.data.IResource.Builder;
import org.integratedmodelling.klab.api.data.adapters.IResourceImporter;
import org.integratedmodelling.klab.api.data.classification.IDataKey;
import org.integratedmodelling.klab.api.observations.IObservation;
import org.integratedmodelling.klab.api.observations.IState;
import org.integratedmodelling.klab.api.runtime.monitoring.IMonitor;
import org.integratedmodelling.klab.components.geospace.utils.GeotoolsUtils;
import org.integratedmodelling.klab.components.geospace.visualization.Renderer;
import org.integratedmodelling.klab.data.adapters.AbstractFilesetImporter;
import org.integratedmodelling.klab.ogc.RasterAdapter;
import org.integratedmodelling.klab.rest.Colormap;
import org.integratedmodelling.klab.rest.StateSummary;
import org.integratedmodelling.klab.utils.FileUtils;
import org.integratedmodelling.klab.utils.MiscUtilities;
import org.integratedmodelling.klab.utils.Pair;
import org.integratedmodelling.klab.utils.Parameters;
import org.integratedmodelling.klab.utils.Triple;
import org.integratedmodelling.klab.utils.ZipUtils;

import it.geosolutions.imageio.plugins.tiff.BaselineTIFFTagSet;

public class RasterImporter extends AbstractFilesetImporter {

    RasterValidator validator = new RasterValidator();
    IParameters<String> options = Parameters.create();

    public RasterImporter() {
        super(RasterAdapter.fileExtensions.toArray(new String[RasterAdapter.fileExtensions.size()]));
    }

    @Override
    public IResourceImporter withOption(String option, Object value) {
        return this;
    }

    @Override
    protected Builder importFile(String urn, File file, IParameters<String> userData, IMonitor monitor) {
        try {

            Builder builder = validator.validate(urn, file.toURI().toURL(), userData, monitor);

            if (builder != null) {
                String layerId = MiscUtilities.getFileBaseName(file).toLowerCase();
                builder.withLocalName(layerId).setResourceId(layerId);
                for (File f : validator.getAllFilesForResource(file)) {
                    builder.addImportedFile(f);
                }
            }

            return builder;

        } catch (MalformedURLException e) {
            Logging.INSTANCE.error(e);
            return null;
        }
    }

    @Override
    public List<Triple<String, String, String>> getExportCapabilities(IObservation observation) {
        List<Triple<String, String, String>> ret = new ArrayList<>();

        if (observation instanceof IState) {
            if (observation.getScale().getSpace() != null && observation.getScale().getSpace().isRegular()
                    && observation.getScale().isSpatiallyDistributed()) {
//                IState state = (IState) observation;
//                IDataKey dataKey = state.getDataKey();
//                if (dataKey != null) {
                    ret.add(new Triple<>("tiff", "GeoTIFF raster archive", "zip"));
//                } else {
//                    ret.add(new Triple<>("tiff", "GeoTIFF raster", "tiff"));
//                }
                ret.add(new Triple<>("png", "PNG image", "png"));
            }
        }

        return ret;
    }

    @Override
    public File exportObservation(File file, IObservation observation, ILocator locator, String format, IMonitor monitor) {

        if (observation instanceof IState && observation.getGeometry().getDimension(Type.SPACE) != null) {

            if (observation.getScale().isSpatiallyDistributed() && observation.getScale().getSpace().isRegular()) {
                File dir = new File(MiscUtilities.changeExtension(file.toString(), "dir"));
                dir.mkdirs();
                File out = new File(dir + File.separator + MiscUtilities.getFileName(file));
                GridCoverage2D coverage;
                IState state = (IState) observation;
                IDataKey dataKey = state.getDataKey();
                File outQml = new File(MiscUtilities.changeExtension(out.toString(), "qml"));
                if (dataKey != null) {
                    File outAux = new File(MiscUtilities.changeExtension(out.toString(), "tiff.aux.xml"));
                    File outCpg = new File(MiscUtilities.changeExtension(out.toString(), "tiff.vat.cpg"));
                    File outDbf = new File(MiscUtilities.changeExtension(out.toString(), "tiff.vat.dbf"));
                    try {
                        // write categories aux xml
                        writeAuxXml(outAux, dataKey);

                        // write categories dbf
                        writeAuxDbf(outDbf, dataKey);
                        FileUtils.writeStringToFile(outCpg, "UTF-8");

                        // write QGIS style
                        writeQgisStyleCategories(outQml, state, locator);
                    } catch (Exception e1) {
                        // ignore, since the output still will be a valid tiff
                        // THIS SHOULD BE LOGGED THOUGH
                    }

                    int noValue = -2147483648;// Integer.MAX_VALUE;
                    coverage = GeotoolsUtils.INSTANCE.stateToIntCoverage((IState) observation, locator, noValue, null);
                } else {
                 // write QGIS style
                    try {
                        writeQgisStyleContinuous(outQml, state, locator);
                    } catch (Exception e) {
                        // ignore, since the output still will be a valid tiff
                        // THIS SHOULD BE LOGGED THOUGH
                    }
                    
                    coverage = GeotoolsUtils.INSTANCE.stateToCoverage((IState) observation, locator, DataBuffer.TYPE_FLOAT,
                            Float.NaN, true, null);
                }

                if (format.equalsIgnoreCase("tiff")) {
                    try {
                        
                        File raster = new File(MiscUtilities.changeExtension(out.toString(), "tiff"));
                        GeoTiffWriter writer = new GeoTiffWriter(raster);

                        writer.setMetadataValue(Integer.toString(BaselineTIFFTagSet.TAG_SOFTWARE),
                                "k.LAB (www.integratedmodelling.org)");

                        writer.write(coverage, null);

                        if (dir != null) {
                            if (!options.get(OPTION_DO_NOT_ZIP_MULTIPLE_FILES, Boolean.FALSE)) {
                                File zip = new File(MiscUtilities.changeExtension(file.toString(), "zip"));
                                ZipUtils.zip(zip, dir, false, false);
                                file = zip;
                                org.apache.commons.io.FileUtils.deleteQuietly(dir);
                            } else {
                                file = dir;
                            }
                        }
                        return file;
                    } catch (IOException e) {
                        return null;
                    }
                }
            }
        }

        return null;
    }

    private void writeAuxXml(File auxFile, IDataKey dataKey) throws Exception {

        RasterAuxXml rasterAuxXml = new RasterAuxXml();
        rasterAuxXml.rasterBand = new PAMRasterBand();
        rasterAuxXml.rasterBand.band = 1;
        rasterAuxXml.rasterBand.attributeTable = new GDALRasterAttributeTable();

        FieldDefn oidFieldDefn = new FieldDefn();
        oidFieldDefn.index = 0;
        oidFieldDefn.name = "OBJECTID";
        oidFieldDefn.type = 0;
        oidFieldDefn.usage = 0;
        rasterAuxXml.rasterBand.attributeTable.fieldDefnList.add(oidFieldDefn);
        FieldDefn valueFieldDefn = new FieldDefn();
        valueFieldDefn.index = 1;
        valueFieldDefn.name = "value";
        valueFieldDefn.type = 0;
        valueFieldDefn.usage = 0;
        rasterAuxXml.rasterBand.attributeTable.fieldDefnList.add(valueFieldDefn);
        FieldDefn labelFieldDefn = new FieldDefn();
        labelFieldDefn.index = 2;
        labelFieldDefn.name = "label";
        labelFieldDefn.type = 2;
        labelFieldDefn.usage = 0;
        rasterAuxXml.rasterBand.attributeTable.fieldDefnList.add(labelFieldDefn);

        List<Pair<Integer, String>> values = dataKey.getAllValues();
        int index = 0;
        for (Pair<Integer, String> pair : values) {
            Integer code = pair.getFirst();
            String classString = pair.getSecond();
            Row row = new Row();
            row.index = index;
            row.fList.add(String.valueOf(index));
            row.fList.add(code.toString());
            row.fList.add(classString);
            rasterAuxXml.rasterBand.attributeTable.rowList.add(row);
            index++;
        }

        JAXBContext context = JAXBContext.newInstance(RasterAuxXml.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        // StringWriter stringWriter = new StringWriter();
        marshaller.marshal(rasterAuxXml, auxFile);
        // System.out.println(stringWriter.toString());
    }

    private void writeQgisStyleCategories(File qmlFile, IState state, ILocator locator) throws Exception {
        IDataKey dataKey = state.getDataKey();

        Pair<RasterSymbolizer, String> rasterSymbolizerPair = Renderer.INSTANCE.getRasterSymbolizer(state, locator);
        RasterSymbolizer rasterSymbolizer = rasterSymbolizerPair.getFirst();
        ColorMap colorMap = rasterSymbolizer.getColorMap();
        ColorMapEntry[] colorMapEntries = colorMap.getColorMapEntries();
        HashMap<String, String> label2ColorMap = new HashMap<>();
        for (ColorMapEntry colorMapEntry : colorMapEntries) {
            String label = colorMapEntry.getLabel();
            String color = colorMapEntry.getColor().evaluate(null, String.class);
            label2ColorMap.put(label, color);
        }

        String ind = "\t";
        StringBuilder sb = new StringBuilder();
        sb.append("<qgis>\n");
        sb.append(ind).append("<pipe>\n");
        sb.append(ind).append(ind)
                .append("<rasterrenderer band=\"1\" type=\"paletted\" alphaBand=\"-1\" opacity=\"1\" nodataColor=\"\">\n");
        sb.append(ind).append(ind).append(ind).append("<colorPalette>\n");
        List<Pair<Integer, String>> values = dataKey.getAllValues();
        for (Pair<Integer, String> pair : values) {
            sb.append(ind).append(ind).append(ind).append(ind);

            Integer code = pair.getFirst();
            String classString = pair.getSecond();
            String color = label2ColorMap.get(classString);

            // <paletteEntry value="0" alpha="255" color="#7e7fef" label="cat0"/>
            sb.append("<paletteEntry value=\"" + code + "\" alpha=\"255\" color=\"" + color + "\" label=\"" + classString
                    + "\"/>\n");
        }
        sb.append(ind).append(ind).append(ind).append("</colorPalette>\n");
        sb.append(ind).append(ind).append("</rasterrenderer>\n");
        sb.append(ind).append("</pipe>\n");
        sb.append("</qgis>\n");

        FileUtils.writeStringToFile(qmlFile, sb.toString());
    }

    private void writeQgisStyleContinuous(File qmlFile, IState state, ILocator locator) throws Exception {
        StateSummary stateSummary = Observations.INSTANCE.getStateSummary(state, locator);
        
        Colormap colorMap = stateSummary.getColormap();
        List<Double> range = stateSummary.getRange();
        
        double min = range.get(0);
        double max = range.get(1);
        
        List<String> labels = colorMap.getLabels();
        List<String> colors = colorMap.getColors();

        
        String ind = "\t";
        StringBuilder sb = new StringBuilder();
        sb.append("<qgis>\n");
        sb.append(ind).append("<pipe>\n");
        sb.append(ind).append(ind)
        .append("<rasterrenderer band=\"1\" type=\"singlebandpseudocolor\"")
        .append(" classificationMax=\"").append(max).append("\"")
        .append(" classificationMin=\"").append(min).append("\"")
        .append(" alphaBand=\"-1\" opacity=\"1\" nodataColor=\"\">\n");
        
        
        sb.append(ind).append(ind).append(ind).append("<rastershader>\n");
        sb.append(ind).append(ind).append(ind).append(ind).append("<colorrampshader ")
            .append(" minimumValue=\"").append(min).append("\"")
            .append(" maximumValue=\"").append(max).append("\"")
            .append(" colorRampType=\"INTERPOLATED\"")
            .append(" classificationMode=\"1\"")
            .append(" clip=\"0\"")
            .append(">\n");
        for(int i = 0; i < labels.size(); i++) {
            sb.append(ind).append(ind).append(ind).append(ind).append(ind);

            String label = labels.get(i);
            String color = colors.get(i);
            
            // <item color="#d7191c" value="846.487670898438" label="846,4877" alpha="255"/>
            sb.append("<item color=\"" + color + "\" value=\"" + label + "\" label=\"" + label + "\" alpha=\"255\"/>\n");
        }
        sb.append(ind).append(ind).append(ind).append(ind).append("</colorrampshader>\n");
        sb.append(ind).append(ind).append(ind).append("</rastershader>\n");
        
        sb.append(ind).append(ind).append("</rasterrenderer>\n");
        sb.append(ind).append("</pipe>\n");
        sb.append("</qgis>\n");
        
        FileUtils.writeStringToFile(qmlFile, sb.toString());
    }

    private boolean writeAuxDbf(File auxDbfFile, IDataKey dataKey) throws Exception {

        DbaseFileHeader header = new DbaseFileHeader();
        header.addColumn("Value", 'N', 10, 0);
        int stringLimit = 100;
        header.addColumn("Label", 'C', stringLimit, 0);

        List<Pair<Integer, String>> values = dataKey.getAllValues();
        header.setNumRecords(values.size());

        try (FileOutputStream fout = new FileOutputStream(auxDbfFile)) {
            DbaseFileWriter dbf = new DbaseFileWriter(header, fout.getChannel(), Charset.forName("UTF-8"));
            for (Pair<Integer, String> pair : values) {
                Integer code = pair.getFirst();
                String classString = pair.getSecond();
                if (classString.length() > stringLimit) {
                    classString = classString.substring(0, stringLimit);
                }
                dbf.write(new Object[]{code, classString});
            }
            dbf.close();
        }
        return true;
    }

    @Override
    public Map<String, String> getExportCapabilities(IResource resource) {
        Map<String, String> ret = new HashMap<>();
        ret.put("zip", "GeoTiff");
        return ret;
    }

    @Override
    public boolean exportResource(File file, IResource resource, String format) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean importIntoResource(URL importLocation, IResource target, IMonitor monitor) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean resourceCanHandle(IResource resource, String importLocation) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean acceptsMultiple() {
        // TODO Auto-generated method stub
        return false;
    }

}
