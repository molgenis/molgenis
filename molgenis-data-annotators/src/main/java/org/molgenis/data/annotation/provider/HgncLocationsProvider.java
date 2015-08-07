package org.molgenis.data.annotation.provider;

import static com.google.common.base.Preconditions.checkNotNull;

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
import org.molgenis.data.annotation.settings.AnnotationSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HgncLocationsProvider
{
	private final AnnotationSettings annotationSettings;

	@Autowired
	public HgncLocationsProvider(AnnotationSettings annotationSettings)
	{
		this.annotationSettings = checkNotNull(annotationSettings);
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
		String url = annotationSettings.getHgncLocation();
		return new InputStreamReader(new URL(url).openStream(), Charset.forName("UTF-8"));
	}
}
