package org.integratedmodelling.geoprocessing.hydrology;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.geotools.coverage.grid.GridCoverage2D;
import org.hortonmachine.gears.io.rasterwriter.OmsRasterWriter;
import org.integratedmodelling.klab.Configuration;
import org.integratedmodelling.klab.api.observations.IState;
import org.integratedmodelling.klab.api.runtime.IContextualizationScope;
import org.integratedmodelling.klab.components.geospace.utils.GeotoolsUtils;

public class SwyDebugUtils {

	public static final boolean DUMP_MAPS = true;

	private static SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd_HHmmss");

	private static File getDumpFolder() {
		File klabFolder = Configuration.INSTANCE.getDataPath();

		File swyFolder = new File(klabFolder, "swy_tmp_dump_folder");
		if (!swyFolder.exists()) {
			swyFolder.mkdirs();
		}
		return swyFolder;
	}

	public static void dumpToRaster(long timestamp, IContextualizationScope scope, String producingModel,
			IState... states) {
		if (DUMP_MAPS) {
			
			String cname = "";
			if (!scope.getRootSubject().equals(scope.getContextSubject())) {
				cname = scope.getContextSubject().getName() + "_";
			}
			
			for (IState state : states) {
				if (state != null) {
					GridCoverage2D coverage = GeotoolsUtils.INSTANCE.stateToCoverage(state, scope.getScale(), false);
					String name = cname + state.getObservable().getName();

					String dateStr = f.format(new Date(timestamp));
					String fileName = name + "_" + producingModel + ".tif";

					File outFolder = new File(getDumpFolder(), dateStr);
					if (!outFolder.exists()) {
						outFolder.mkdir();
					}
					File outfile = new File(outFolder, fileName);

					scope.getMonitor().debug("Dumping state of ts " + dateStr + " to file " + fileName);

					try {
						OmsRasterWriter.writeRaster(outfile.getAbsolutePath(), coverage);
					} catch (Exception e) {
						e.printStackTrace();
						scope.getMonitor().error(e.getMessage());
					}
				}
			}
		}
	}

}
