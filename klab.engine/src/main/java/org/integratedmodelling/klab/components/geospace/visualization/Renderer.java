package org.integratedmodelling.klab.components.geospace.visualization;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.ColorBrewer;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.ShadedRelief;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.integratedmodelling.klab.Concepts;
import org.integratedmodelling.klab.Observations;
import org.integratedmodelling.klab.api.data.ILocator;
import org.integratedmodelling.klab.api.knowledge.IConcept;
import org.integratedmodelling.klab.api.model.IAnnotation;
import org.integratedmodelling.klab.api.observations.IState;
import org.integratedmodelling.klab.api.observations.scale.space.IEnvelope;
import org.integratedmodelling.klab.api.observations.scale.space.IProjection;
import org.integratedmodelling.klab.api.observations.scale.space.ISpace;
import org.integratedmodelling.klab.api.provenance.IArtifact.Type;
import org.integratedmodelling.klab.api.runtime.monitoring.IMonitor;
import org.integratedmodelling.klab.components.geospace.api.IGrid;
import org.integratedmodelling.klab.components.geospace.extents.Envelope;
import org.integratedmodelling.klab.components.geospace.extents.Projection;
import org.integratedmodelling.klab.components.geospace.extents.Space;
import org.integratedmodelling.klab.components.geospace.utils.GeotoolsUtils;
import org.integratedmodelling.klab.components.runtime.observations.Observation;
import org.integratedmodelling.klab.exceptions.KlabInternalErrorException;
import org.integratedmodelling.klab.rest.Colormap;
import org.integratedmodelling.klab.rest.StateSummary;
import org.integratedmodelling.klab.utils.ColorUtils;
import org.integratedmodelling.klab.utils.JsonUtils;
import org.integratedmodelling.klab.utils.Pair;
import org.integratedmodelling.klab.utils.Range;
import org.integratedmodelling.klab.utils.Triple;
import org.opengis.style.ContrastMethod;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

/**
 * Rendering functions for raster coverages and possibly more. Uses Geotools'
 * SLD support and manages SLD definitions created from classpath, files, URLs
 * and annotations.
 * 
 * @author ferdinando.villa
 *
 */
public enum Renderer {

    INSTANCE;

    private static final String      NO_DATA_MESSAGE = "NO DATA";

    private Map<String, ColorScheme> colorSchemata   = new HashMap<>();
    // private Map<String, Style> styles = new HashMap<>();
    private StyleBuilder             styleBuilder    = new StyleBuilder();
    private ColorBrewer              colorBrewer     = new ColorBrewer();
    private StyleFactory             styleFactory    = CommonFactoryFinder.getStyleFactory();

    Renderer() {
        /*
         * find color schemata in classpath
         * 
         * TODO do the same from confDir/colors and components
         */
        for (String schema : new Reflections("colors", new ResourcesScanner())
                .getResources(Pattern.compile(".*\\.json"))) {
            ColorScheme scheme = JsonUtils
                    .load(getClass().getClassLoader().getResource(schema), ColorScheme.class);
            colorSchemata.put(scheme.getName(), scheme);
        }
    }

    /**
     * Render a given state to an image within a specified viewport using settings
     * from annotations or defaults.
     * 
     * @param state
     * @param locator
     *            must specify a full spatial slice
     * @param imageFile
     * @param viewport
     */
    public BufferedImage render(IState state, ILocator locator, int[] viewport) {

        if (state.getSpace() == null
                || !(state.getSpace() instanceof Space && ((Space) state.getSpace()).getGrid() != null)) {
            throw new IllegalArgumentException("cannot render a state as a map unless its space is gridded");
        }

        ISpace space = state.getSpace();
        IGrid grid = ((Space) state.getSpace()).getGrid();

        // https://github.com/geotools/geotools/blob/master/modules/library/render/src/test/java/org/geotools/renderer/lite/GridCoverageRendererTest.java
        try {

            Viewport vport = new Viewport(viewport[0], viewport.length == 1 ? viewport[0] : viewport[1]);
            GridCoverage2D coverage = GeotoolsUtils.INSTANCE.stateToCoverage(state, locator, Float.NaN);
            IEnvelope envelope = space.getEnvelope();
            IProjection projection = space.getProjection();
            int[] imagesize = vport.getSize(grid.getXCells(), grid.getYCells());
            Rectangle screenSize = new Rectangle(imagesize[0], imagesize[1]);
            AffineTransform w2s = RendererUtilities
                    .worldToScreenTransform(((Envelope) envelope).getJTSEnvelope(), screenSize);
            GridCoverageRenderer renderer = new GridCoverageRenderer(((Projection) projection)
                    .getCoordinateReferenceSystem(), ((Envelope) envelope).getJTSEnvelope(), screenSize, w2s);
            Pair<RasterSymbolizer, String> rasterSymbolizer = getRasterSymbolizer(state, locator);
            Rectangle imageBounds = new Rectangle(imagesize[0], imagesize[1]);
            BufferedImage image = new BufferedImage(imagesize[0], imagesize[1], BufferedImage.TYPE_INT_ARGB);
            Graphics2D gr = image.createGraphics();
            gr.setPaint(new Color(0f, 0f, 0f, 0f));
            gr.fill(imageBounds);
            if (rasterSymbolizer.getFirst() != null) {
                renderer.paint(gr, coverage, rasterSymbolizer.getFirst());
            } else {
                String s = rasterSymbolizer.getSecond();
                Font f = new Font("SansSerif", Font.BOLD, 172);
                gr.setColor(s.equals(NO_DATA_MESSAGE) ? Color.red : Color.white);
                gr.setFont(f);
                FontMetrics fm = gr.getFontMetrics();
                int x = (imagesize[0] - fm.stringWidth(s)) / 2;
                int y = (fm.getAscent() + (imagesize[1] - (fm.getAscent() + fm.getDescent())) / 2);
                gr.drawString(s, x, y);
            }
            return image;

        } catch (Exception e) {
            throw new KlabInternalErrorException(e);
        }
    }

    public Pair<RasterSymbolizer, String> getRasterSymbolizer(IState state, ILocator locator) {

        StateSummary summary = Observations.INSTANCE.getStateSummary(state, locator);
        IMonitor monitor = ((Observation) state).getRuntimeContext().getMonitor();

        if (summary.isDegenerate()) {
            return new Pair<>(null, NO_DATA_MESSAGE);
        }

        if (summary.getRange().get(0).equals(summary.getRange().get(1))) {
            // TODO this won't show anything when we have one value AND no-data - which is
            // wrong.
            String label = "";
            double value = summary.getRange().get(0);
            if (state.getDataKey() != null) {
                label = state.getDataKey().getLabels().get((int) value);
            } else if (state.getType() == Type.BOOLEAN) {
                label = value == 0 ? "Not present" : "Present";
            } else {
                label = NumberFormat.getNumberInstance().format(value);
            }
            return new Pair<>(null, label);
        }

        float opacity = 1f;
        Color[] colors = null;
        double[] values = null;
        double midpoint = Double.NaN;
        String[] labels = null;
        int colormapType = state.getDataKey() == null ? ColorMap.TYPE_RAMP
                : (state.getDataKey().isOrdered() ? ColorMap.TYPE_INTERVALS : ColorMap.TYPE_VALUES);

        double shadedRelief = 0.0;
        boolean shadedReliefBrightnessOnly = false;
        String contrastEnhancement = null;
        double gamma = 1.0;

        // TODO parameters for shaded relief and contrast enhancement

        for (IAnnotation annotation : state.getAnnotations()) {
            if (annotation.getName().equals("colormap")) {

                // check if we have just a name
                String name = annotation.get("value", String.class);
                if (name == null) {
                    name = annotation.get("name", String.class);
                }

                gamma = annotation.get("gamma", 1.0);
                shadedRelief = annotation.get("shading", 0.0);
                contrastEnhancement = annotation.get("contrast", (String) null);
                shadedReliefBrightnessOnly = annotation.get("brighten", false);

                if (name != null) {
                    if (colorSchemata.containsKey(name)) {

                        ColorScheme scheme = colorSchemata.get(name);
                        Triple<double[], Color[], String[]> result = scheme.computeScheme(state, locator);

                        values = result.getFirst();
                        colors = result.getSecond();
                        labels = result.getThird();

                    } else {

                        colors = getColormap(name, opacity);
                        if (colors == null && colorBrewer.hasPalette(name)) {
                            BrewerPalette palette = colorBrewer.getPalette(name);
                            colors = palette
                                    .getColors(state.getDataKey() == null ? 256 : state.getDataKey().size());
                            if (palette.getType() == ColorBrewer.DIVERGING) {
                                midpoint = 0.0;
                            } else if (palette.getType() == ColorBrewer.QUALITATIVE) {
                                if (colormapType != ColorMap.TYPE_VALUES) {
                                    throw new IllegalArgumentException("qualitative colormap chosen for quantitative data");
                                }
                            }
                        }

                    }
                } else if (annotation.containsKey("values")) {

                    Map<?, ?> vals = annotation.get("values", Map.class);
                    List<Pair<Object, Color>> svals = new ArrayList<>();
                    Class<?> type = null;
                    for (Object o : vals.keySet()) {
                        if (type == null) {
                            type = o.getClass();
                        } else if (!type.equals(o.getClass())) {
                            throw new IllegalArgumentException("color keys must be of the same type in a colormap specification");
                        }
                        svals.add(new Pair<>(o, parseColor(vals.get(o))));
                    }

                    if (IConcept.class.isAssignableFrom(type)) {
                        colormapType = ColorMap.TYPE_VALUES;
                    } else if (Range.class.isAssignableFrom(type)) {
                        colormapType = ColorMap.TYPE_INTERVALS;
                    } else if (Number.class.isAssignableFrom(type)) {
                        colormapType = annotation.get("continuous", Boolean.TRUE) ? ColorMap.TYPE_RAMP
                                : ColorMap.TYPE_INTERVALS;
                    } else if (Boolean.class.isAssignableFrom(type)) {
                        colormapType = ColorMap.TYPE_VALUES;
                        for (Pair<Object, Color> pair : svals) {
                            if ((Boolean) pair.getFirst()) {
                                pair.setFirst(1.0);
                            } else {
                                pair.setFirst(0.0);
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("invalid color keys: must be number, boolean, concept or range");
                    }

                    labels = new String[svals.size()];
                    values = new double[svals.size()];
                    colors = new Color[svals.size()];

                    int i = 0;
                    for (Pair<Object, Color> pair : svals) {

                        // we assume the user has used proper sorting.
                        if (pair.getFirst() instanceof IConcept) {
                            labels[i] = Concepts.INSTANCE.getDisplayName((IConcept) pair.getFirst());
                            values[i] = state.getDataKey().reverseLookup((IConcept) pair.getFirst());
                        } else if (pair.getFirst() instanceof Number) {
                            values[i] = ((Number) pair.getFirst()).doubleValue();
                            labels[i] = "" + values[i];
                        } else if (pair.getFirst() instanceof Boolean) {
                            values[i] = ((Boolean) pair.getFirst()) ? 1 : 0;
                            labels[i] = state.getObservable().getLocalName() + " "
                                    + ((Boolean) pair.getFirst() ? "present" : "absent");
                        }

                        colors[i] = pair.getSecond();

                        i++;
                    }

                } else if (annotation.containsKey("colors")) {

                    List<Color> clrs = new ArrayList<>();
                    for (Object o : annotation.get("colors", List.class)) {
                        clrs.add(parseColor(o));
                    }
                    colors = clrs.toArray(new Color[clrs.size()]);
                }

                if (annotation.containsKey("opacity")) {
                    opacity = annotation.get("opacity", Number.class).floatValue();
                }

                if (annotation.containsKey("midpoint")) {
                    midpoint = annotation.get("midpoint", Number.class).floatValue();
                    if (colors == null || (colors.length == 2 && colors.length == 3)) {
                        throw new IllegalArgumentException("invalid colormap annotation: a midpoint must be specified together with two or three colors");
                    }
                }

            } else if (annotation.getName().equals("color")) {

                // must be boolean

            }
        }

        if (colors == null) {
            if (state.getDataKey() == null) {
                colors = jet(1.0f);
            } else {

                // establish if we have an ordering; if so, partition the Jet colormap into
                // enough classes to
                // cover the full range and use the concept values/data.
                if (state.getDataKey().isOrdered()) {

                    colormapType = ColorMap.TYPE_VALUES;

                    List<Pair<Integer, String>> valabs = state.getDataKey().getAllValues();
                    colors = new Color[state.getDataKey().size()];
                    values = new double[state.getDataKey().size()];
                    labels = new String[state.getDataKey().size()];

                    Color[] jetcolors = jet(1.0f);

                    for (int i = 0; i < state.getDataKey().size(); i++) {
                        int index = (int) (((double) (i + 1) / (double) state.getDataKey().size())
                                * (double) (jetcolors.length - 1));
                        colors[i] = jetcolors[index];
                        values[i] = valabs.get(i).getFirst();
                        labels[i] = valabs.get(i).getSecond();
                    }

                } else {
                    if (state.getDataKey().size() <= random20.length) {
                        colors = Arrays.copyOf(random20, state.getDataKey().size());
                    } else if (state.getDataKey().size() <= excel.length) {
                        colors = Arrays.copyOf(excel, state.getDataKey().size());
                    } else if (state.getDataKey().size() <= random80.length) {
                        colors = Arrays.copyOf(random80, state.getDataKey().size());
                    } else {
                        monitor.warn("cannot build a contrasting colormap for " + state.getObservable().getLocalName()
                                + ": too many classes. Using a continuous color, results will look wrong.");
                        colors = jet(1.0f);
                        colormapType = ColorMap.TYPE_RAMP;
                    }
                }
            }
        }

        if (values == null
                && (colormapType == ColorMap.TYPE_RAMP || colormapType == ColorMap.TYPE_INTERVALS)) {
            values = new double[colors.length];
            labels = new String[colors.length];
            if (Double.isNaN(midpoint) || Range.create(summary.getRange()).contains(midpoint)) {
                for (int i = 0; i < colors.length; i++) {
                    // TODO handle midpoint
                    values[i] = summary.getRange().get(0)
                            + ((summary.getRange().get(1) - summary.getRange().get(0))
                                    * ((double) (i + 1) / (double) colors.length));
                    labels[i] = "" + values[i];
                }
            } else {

                /*
                 * midpoint: we have two or three colors. Stretch the values to equalize the
                 * interval; if we have a middle color, use that for the midpoint, otherwise
                 * leave it to the interpolator.
                 */
                Range range = Range.create(summary.getRange()).stretchForMidpoint(midpoint);
                for (int i = 0; i < colors.length; i++) {
                    values[i] = colors.length == 3 && i == 1 ? midpoint
                            : (i == 0 ? range.getLowerBound() : range.getUpperBound());
                    labels[i] = "" + values[i];
                }

            }
        } else if (state.getDataKey() != null && colors != null) {

            values = new double[state.getDataKey().size()];
            labels = new String[state.getDataKey().size()];
            int i = 0;
            for (Pair<Integer, String> pair : state.getDataKey().getAllValues()) {
                values[i] = ((Number) pair.getFirst()).doubleValue();
                labels[i] = pair.getSecond();
                i++;
            }
        }

        ColorMap colorMap = styleBuilder.createColorMap(labels, values, colors, colormapType);
        RasterSymbolizer ret = styleBuilder.createRasterSymbolizer(colorMap, opacity);

        if (contrastEnhancement != null || gamma != 1.0) {

            ContrastEnhancement cen = styleFactory.createContrastEnhancement();
            cen.setGammaValue(styleBuilder.literalExpression(gamma));
            if (cen != null) {
                switch (contrastEnhancement) {
                case "none":
                    cen.setMethod(ContrastMethod.NONE);
                    break;
                case "logarithmic":
                    cen.setMethod(ContrastMethod.LOGARITHMIC);
                    break;
                case "exponential":
                    cen.setMethod(ContrastMethod.EXPONENTIAL);
                    break;
                case "histogram":
                    cen.setMethod(ContrastMethod.HISTOGRAM);
                    break;
                case "normalize":
                    cen.setMethod(ContrastMethod.NORMALIZE);
                    break;
                }
            }

            ret.setContrastEnhancement(cen);
        }

        if (shadedRelief != 0.0) {
            ShadedRelief srl = styleFactory.createShadedRelief(styleBuilder.literalExpression(shadedRelief));
            srl.setBrightnessOnly(shadedReliefBrightnessOnly);
            ret.setShadedRelief(srl);
        }

        /*
         * notify key and colors to state summary for further inquiries
         */
        Colormap colormap = new Colormap();
        colormap.setColors(new ArrayList<>());
        ;
        for (Color color : colors) {
            colormap.getColors().add(ColorUtils.encodeRGB(color));
        }

        colormap.setLabels(Arrays.asList(labels));

        switch (colormapType) {
        case ColorMap.TYPE_INTERVALS:
            colormap.setType(Colormap.Type.INTERVALS);
            break;
        case ColorMap.TYPE_VALUES:
            colormap.setType(Colormap.Type.VALUES);
            break;
        case ColorMap.TYPE_RAMP:
            colormap.setType(Colormap.Type.RAMP);
            break;
        default:
            break;
        }

        summary.setColormap(colormap);

        return new Pair<>(ret, null);
    }

    private Color parseColor(Object o) {

        Color ret = null;

        if (o instanceof String) {
            if (o.equals("none")) {
                return new Color(0f, 0f, 0f, 0f);
            }
            int[] color = ColorUtils.getRGB(o.toString());
            if (color == null) {
                throw new IllegalArgumentException("identifier " + o + " does not specify a known CSS color");
            }
            ret = new Color(color[0], color[1], color[2]);
        } else if (o instanceof List) {
            // TODO more error handling
            ret = new Color(((Number) ((List<?>) o).get(0)).intValue(), ((Number) ((List<?>) o).get(1))
                    .intValue(), ((Number) ((List<?>) o).get(2)).intValue());
        }

        if (ret == null) {
            throw new IllegalArgumentException("bad color specification: need color name or RGB list");
        }

        return ret;
    }

    public Color[] getColormap(String name, float alpha) {

        switch (name) {
        case "jet":
            return jet(alpha);
        case "heat":
            return heat();
        case "rainbow":
            return rainbow();
        case "redgreen":
            return redgreen();
        case "redblackgreen":
            return redblackgreen();
        case "wave":
            return wave();
        case "excel":
            return excel;
        case "random":
            return random20;
        }
        return null;
    }

    /*
     * -----------------------------------------------------------------------------
     * ------ Well-known colormaps
     * -----------------------------------------------------------------------------
     * ------
     */
    public Color[] jet(float alpha) {

        Color[] ret = new Color[256];

        float r = 0;
        float g = 0;
        float b = 0;

        int n = 256 / 4;
        for (int i = 0; i < 256; ++i) {
            if (i < n / 2.) {
                r = 0;
                g = 0;
                b = 0.5f + (float) i / n;
            } else if (i >= n / 2. && i < 3. * n / 2.) {
                r = 0;
                g = (float) i / n - 0.5f;
                b = 1.f;
            } else if (i >= 3. * n / 2. && i < 5. * n / 2.) {
                r = (float) i / n - 1.5f;
                g = 1.f;
                b = 1.f - (float) i / n + 1.5f;
            } else if (i >= 5. * n / 2. && i < 7. * n / 2.) {
                r = 1.f;
                g = 1.f - (float) i / n + 2.5f;
                b = 0;
            } else if (i >= 7. * n / 2.) {
                r = 1.f - (float) i / n + 3.5f;
                g = 0;
                b = 0;
            }

            ret[i] = new Color(r, g, b, alpha);
        }
        return ret;
    }

    public Color[] heat() {

        Color[] ret = new Color[256];

        int n = (int) (3. / 8. * 256);
        for (int i = 0; i < 256; ++i) {
            double r = (1. / n) * (i + 1);
            double g = 0.;
            double b = 0.;

            if (i >= n) {
                r = 1.;
                g = (1. / n) * (i + 1 - n);
                b = 0.;
            }
            if (i >= 2 * n) {
                r = 1.;
                g = 1.;
                b = 1. / (256 - 2 * n) * (i + 1 - 2 * n);
            }

            ret[i] = new Color((int) (r * 255), (int) (g * 255), (int) (b * 255));
        }

        return ret;
    }

    public Color[] grayscale() {
        Color[] colors = new Color[256];
        for (int i = 0; i < colors.length; ++i)
            colors[i] = new Color(i, i, i);
        return colors;
    }

    public Color[] redgreen() {
        Color[] colors = new Color[256];

        double half = colors.length / 2.;
        for (int i = 0; i <= half; ++i)
            colors[i] = new Color(255, (int) ((i / half) * 255), (int) ((i / half) * 255));
        for (int i = (int) half + 1; i < colors.length; ++i)
            colors[i] = new Color(255 - (int) (((i - half) / half) * 255), 255, 255
                    - (int) (((i - half) / half) * 255));
        return colors;
    }

    public Color[] redblackgreen() {

        Color[] colors = new Color[256];

        double half = colors.length / 2.;
        for (int i = 0; i <= half; ++i)
            colors[i] = new Color(255 - (int) (((i) / half) * 255), 0, 0);
        for (int i = (int) half + 1; i < colors.length; ++i)
            colors[i] = new Color(0, (int) (((i - half) / half) * 255), 0);
        return colors;
    }

    public Color[] rainbow() {

        Color[] colors = new Color[256];

        for (int i = 0; i < colors.length; ++i) {
            if (i <= 29)
                colors[i] = new Color((int) (129.36 - i * 4.36), 0, 255);
            else if (i <= 86)
                colors[i] = new Color(0, (int) (-133.54 + i * 4.52), 255);
            else if (i <= 141)
                colors[i] = new Color(0, 255, (int) (665.83 - i * 4.72));
            else if (i <= 199)
                colors[i] = new Color((int) (-635.26 + i * 4.47), 255, 0);
            else
                colors[i] = new Color(255, (int) (1166.81 - i * 4.57), 0);
        }
        return colors;
    }

    public Color[] wave() {

        Color[] colors = new Color[256];
        for (int i = 0; i < colors.length; ++i) {
            colors[i] = new Color((int) ((Math.sin(((double) i / 40 - 3.2)) + 1)
                    * 128), (int) ((1 - Math.sin((i / 2.55 - 3.1))) * 70
                            + 30), (int) ((1 - Math.sin(((double) i / 40 - 3.1))) * 128));
        }
        return colors;
    }

    public BufferedImage createImage(Color[] colors, int width, int height) {

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        double step = height / (double) colors.length;
        for (int c = 0; c < height; ++c) {
            g.setColor(colors[(int) Math.floor(c / step)]);
            g.drawLine(0, height - (c + 1), width, height - (c + 1));
        }

        return img;
    }

    /**
     * Default different/random selected by Sasha Trubetskoy (see
     * https://sashat.me/2017/01/11/list-of-20-simple-distinct-colors)
     */
    static Color[] random20 = new Color[] {
            new Color(230, 25, 75),
            new Color(60, 180, 75),
            new Color(255, 225, 25),
            new Color(0, 130, 200),
            new Color(245, 130, 48),
            new Color(145, 30, 180),
            new Color(70, 240, 240),
            new Color(240, 50, 230),
            new Color(210, 245, 60),
            new Color(250, 190, 190),
            new Color(0, 128, 128),
            new Color(230, 190, 255),
            new Color(170, 110, 40),
            new Color(255, 250, 200),
            new Color(128, 0, 0),
            new Color(170, 255, 195),
            new Color(128, 128, 0),
            new Color(255, 215, 180),
            new Color(0, 0, 128),
            new Color(128, 128, 128),
            new Color(255, 255, 255),
            new Color(0, 0, 0) };

    static Color[] random80 = new Color[] {
            new Color(153,42,31),
            new Color(1,208,129),
            new Color(130,44,179),
            new Color(135,219,71),
            new Color(98,94,231),
            new Color(166,204,32),
            new Color(2,114,248),
            new Color(194,188,0),
            new Color(155,118,255),
            new Color(43,176,46),
            new Color(239,81,205),
            new Color(0,172,76),
            new Color(242,54,170),
            new Color(30,126,0),
            new Color(193,120,255),
            new Color(72,143,0),
            new Color(154,0,138),
            new Color(103,221,133),
            new Color(245,26,121),
            new Color(0,140,48),
            new Color(231,137,255),
            new Color(93,134,0),
            new Color(72,67,177),
            new Color(187,208,78),
            new Color(129,43,149),
            new Color(79,221,171),
            new Color(255,49,98),
            new Color(0,151,85),
            new Color(255,122,225),
            new Color(0,108,32),
            new Color(255,77,156),
            new Color(46,219,220),
            new Color(220,0,70),
            new Color(1,165,148),
            new Color(206,0,82),
            new Color(132,215,177),
            new Color(165,0,117),
            new Color(154,133,0),
            new Color(2,97,198),
            new Color(227,131,0),
            new Color(8,167,255),
            new Color(242,87,43),
            new Color(1,113,195),
            new Color(255,169,79),
            new Color(118,148,255),
            new Color(176,115,0),
            new Color(82,71,152),
            new Color(242,190,91),
            new Color(122,57,130),
            new Color(74,90,0),
            new Color(234,159,255),
            new Color(188,82,0),
            new Color(144,188,255),
            new Color(162,28,12),
            new Color(189,163,255),
            new Color(158,85,0),
            new Color(136,154,208),
            new Color(142,56,4),
            new Color(200,188,244),
            new Color(108,78,1),
            new Color(255,119,178),
            new Color(108,169,133),
            new Color(184,0,101),
            new Color(215,198,138),
            new Color(157,24,91),
            new Color(78,87,31),
            new Color(255,103,106),
            new Color(229,170,211),
            new Color(119,71,36),
            new Color(255,160,199),
            new Color(154,39,54),
            new Color(255,176,120),
            new Color(140,49,90),
            new Color(255,136,101),
            new Color(128,60,92),
            new Color(217,152,131),
            new Color(172,111,144),
            new Color(130,69,60),
            new Color(255,149,166),
            new Color(213,139,145)
    };
    
    // http://dmcritchie.mvps.org/excel/colors.htm
    static Color[] excel    = new Color[] {
            new Color(255, 0, 0),
            new Color(0, 255, 0),
            new Color(0, 0, 255),
            new Color(255, 255, 0),
            new Color(255, 0, 255),
            new Color(0, 255, 255),
            new Color(128, 0, 0),
            new Color(0, 128, 0),
            new Color(0, 0, 128),
            new Color(128, 128, 0),
            new Color(128, 0, 128),
            new Color(0, 128, 128),
            new Color(192, 192, 192),
            new Color(128, 128, 128),
            new Color(153, 153, 255),
            new Color(153, 51, 102),
            new Color(255, 255, 204),
            new Color(204, 255, 255),
            new Color(102, 0, 102),
            new Color(255, 128, 128),
            new Color(0, 102, 204),
            new Color(204, 204, 255),
            new Color(0, 0, 128),
            new Color(255, 0, 255),
            new Color(255, 255, 0),
            new Color(0, 255, 255),
            new Color(128, 0, 128),
            new Color(128, 0, 0),
            new Color(0, 128, 128),
            new Color(0, 0, 255),
            new Color(0, 204, 255),
            new Color(204, 255, 255),
            new Color(204, 255, 204),
            new Color(255, 255, 153),
            new Color(153, 204, 255),
            new Color(255, 153, 204),
            new Color(204, 153, 255),
            new Color(255, 204, 153),
            new Color(51, 102, 255),
            new Color(51, 204, 204),
            new Color(153, 204, 0),
            new Color(255, 204, 0),
            new Color(255, 153, 0),
            new Color(255, 102, 0),
            new Color(102, 102, 153),
            new Color(150, 150, 150),
            new Color(0, 51, 102),
            new Color(51, 153, 102),
            new Color(0, 51, 0),
            new Color(51, 51, 0),
            new Color(153, 51, 0),
            new Color(153, 51, 102),
            new Color(51, 51, 153),
            new Color(51, 51, 51) };

    public static void main(String[] args) throws IOException {
        ImageIO.write(INSTANCE.createImage(INSTANCE.wave(), 20, 256), "png", new java.io.File("legend.png"));
    }

}
