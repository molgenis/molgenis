package org.molgenis.data.annotation.provider;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.annotation.impl.datastructures.ClinvarData;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClinvarDataProvider
{
	private final MolgenisSettings molgenisSettings;
	public static final String CLINVAR_FILE_LOCATION_PROPERTY = "clinvar_location";

	HashMap<List<String>, ClinvarData> result = new HashMap<List<String>, ClinvarData>();

	@Autowired
	public ClinvarDataProvider(MolgenisSettings molgenisSettings)
	{
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
	}

	public Map<List<String>, ClinvarData> getClinvarData() throws IOException
	{
		// Lazy loading for the clinvar data
		if (result.isEmpty())
		{
			List<String> clinvarDataLines = IOUtils.readLines(getClinvarDataReader());
			for (String line : clinvarDataLines)
			{
				if (!line.startsWith("#"))
				{
					String[] split = line.split("\t");
					ClinvarData clinvarData = new ClinvarData(split[0], split[1], split[2], split[3], split[4],
							split[5], split[6], split[7], split[8], split[9], split[10], split[11], split[12],
							split[13], split[14], split[15], split[16], split[17], split[18], split[19], split[20],
							split[21], split[22], split[23], split[24]);

					List<String> clinvarKeys = getChromosomePositionReferenceAlternativeInformationFromClinvarLine(split);
					result.put(clinvarKeys, clinvarData);
				}
			}
			return result;
		}
		return result;
	}

	public List<String> getChromosomePositionReferenceAlternativeInformationFromClinvarLine(String[] clinvarLine)
	{
		String type = clinvarLine[1];
		String chromosome = clinvarLine[13];
		String position = clinvarLine[14];
		String reference = ".";
		String alternative = ".";

		String hgvs = clinvarLine[2];
		if (hgvs.startsWith("NM") && hgvs.contains(">"))
		{
			if (type.equals("indel"))
			{
				// NM_001110792.1(MECP2):c.-28_-27delGAinsTT
				reference = hgvs.split("del")[1].split("ins")[0];
				alternative = hgvs.split("del")[1].split("ins")[1];
			}
			else if (type.equals("deletion"))
			{
				// NM_001110792.1(MECP2):c.-27_-26delAG
				reference = hgvs.split("del")[1];
			}
			else if (type.equals("insertion"))
			{
				// NM_003159.2(CDKL5):c.1886_1887insTT
				alternative = hgvs.split("ins")[1];
			}
			else if (type.equals("single nucleotide variant"))
			{
				// NM_003159.2(CDKL5):c.1721C>T				
				int gtIndex = hgvs.lastIndexOf('>');
				reference = hgvs.substring(gtIndex - 1, gtIndex);
				alternative = hgvs.substring(gtIndex + 1, gtIndex + 2);	
			}
		} else {
			// Some lines do not have a standard notation of variants, we cant handle these at the moment
		}

		List<String> clinvarKeys = Arrays.asList(chromosome, position, reference, alternative);
		return clinvarKeys;
	}

	private Reader getClinvarDataReader() throws IOException
	{
		String fileLocation = molgenisSettings.getProperty(CLINVAR_FILE_LOCATION_PROPERTY);
		return new InputStreamReader(new FileInputStream(fileLocation), Charset.forName("UTF-8"));
	}
}
