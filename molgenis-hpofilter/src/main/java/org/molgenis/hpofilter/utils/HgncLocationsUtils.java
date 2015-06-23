package org.molgenis.hpofilter.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.molgenis.hpofilter.data.HGNCLocations;
import org.molgenis.hpofilter.data.Locus;

public class HgncLocationsUtils
{
	public static List<String> locationToHgcn(Map<String, HGNCLocations> hgncLocations, Locus locus) throws IOException
	{
		List<String> hgncSymbols = new ArrayList<String>();

		boolean variantMapped = false;
		for (HGNCLocations hgncLocation : hgncLocations.values())
		{
			if (hgncLocation.getChrom().equals(locus.getChrom()) && locus.getPos() >= (hgncLocation.getStart() - 5)
					&& locus.getPos() <= (hgncLocation.getEnd() + 5))
			{
				hgncSymbols.add(hgncLocation.getHgnc());
				variantMapped = true;
				break;
			}
		}

		if (!variantMapped)
		{
			hgncSymbols.add(null);
		}

		return hgncSymbols;
	}
}
