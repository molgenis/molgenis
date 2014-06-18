package org.molgenis.data.annotation.provider;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.annotation.impl.datastructures.CgdData;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CgdDataProvider
{
	private final MolgenisSettings molgenisSettings;
	public static final String CGD_FILE_LOCATION_PROPERTY = "cgd_location";

	HashMap<String, CgdData> result = new HashMap<String, CgdData>();

	@Autowired
	public CgdDataProvider(MolgenisSettings molgenisSettings)
	{
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
	}

	public Map<String, CgdData> getCgdData() throws IOException
	{
		// Lazy loading for the cgd data
		if (result.isEmpty())
		{
			List<String> cgdDataLines = IOUtils.readLines(getCgdDataReader());

			for (String line : cgdDataLines)
			{
				if (!line.startsWith("#"))
				{
					String[] split = line.split("\t");
					CgdData cgdData = new CgdData(split[1], split[2], split[3], split[4], split[5], split[6], split[7],
							split[8], split[9], split[10], split[11]);

					cgdData.getHgnc_id();
					cgdData.getEntrez_gene_id();
					cgdData.getCondition();
					cgdData.getInheritance();
					cgdData.getAge_group();
					cgdData.getAllelic_conditions();
					cgdData.getManifestation_categories();
					cgdData.getIntervention_categories();
					cgdData.getComments();
					cgdData.getIntervention_rationale();
					cgdData.getReferences();

					result.put(split[0], cgdData);
				}
			}

			return result;
		}

		return result;
	}

	private Reader getCgdDataReader() throws IOException
	{
		String fileLocation = molgenisSettings.getProperty(CGD_FILE_LOCATION_PROPERTY);
		return new InputStreamReader(new FileInputStream(fileLocation), Charset.forName("UTF-8"));
	}
}
