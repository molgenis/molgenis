package org.molgenis.data.annotation.provider;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.annotation.impl.datastructures.HGNCLocations;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HgncLocationsProvider
{
	public static final String KEY_HGNC_LOCATIONS_VALUE = "plugin.annotators.hgcn.locations.url";
	public static final String DEFAULT_HGNC_LOCATIONS_VALUE = "https://molgenis26.target.rug.nl/downloads/5gpm/GRCh37p13_HGNC_GeneLocations_noPatches.tsv";
	private final MolgenisSettings molgenisSettings;

	@Autowired
	public HgncLocationsProvider(MolgenisSettings molgenisSettings)
	{
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
	}

	/**
	 * 
	 * @return map of GeneSymbol to HGNCLoc(GeneSymbol, Start, End, Chrom)
	 * @throws IOException
	 */
	public Map<String, HGNCLocations> getHgncLocations() throws IOException
	{
		List<String> geneLocations = IOUtils.readLines(getHgncLocationsReader());

		HashMap<String, HGNCLocations> res = new HashMap<String, HGNCLocations>();
		for (String line : geneLocations)
		{
			String[] split = line.split("\t");
			HGNCLocations hgncLoc = new HGNCLocations(split[0], Long.parseLong(split[1]), Long.parseLong(split[2]),
					split[3]);
			if (hgncLoc.getChrom().matches("[0-9]+|X"))
			{
				res.put(hgncLoc.getHgnc(), hgncLoc);
			}
		}

		return res;
	}

	private Reader getHgncLocationsReader() throws IOException
	{
		String url = molgenisSettings.getProperty(KEY_HGNC_LOCATIONS_VALUE);
		return new InputStreamReader(new URL(url).openStream(), Charset.forName("UTF-8"));
	}
}
