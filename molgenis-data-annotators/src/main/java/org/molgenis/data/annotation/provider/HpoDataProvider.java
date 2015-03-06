package org.molgenis.data.annotation.provider;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.annotation.impl.HpoServiceAnnotator;
import org.molgenis.data.annotation.impl.datastructures.HpoData;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HpoDataProvider
{
	private final MolgenisSettings molgenisSettings;

	HashMap<String, List<HpoData>> result = new HashMap<String, List<HpoData>>();

	@Autowired
	public HpoDataProvider(MolgenisSettings molgenisSettings)
	{
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
	}

	public Map<String, List<HpoData>> getHpoData() throws IOException
	{
		// Lazy loading for the HPO data
		if (result.isEmpty())
		{
			List<String> hpoDataLines = IOUtils.readLines(getHpoDataReader());

			for (String line : hpoDataLines)
			{
				if (!line.startsWith("#"))
				{
					String[] split = line.split("\t");
					String gene = split[1];
					HpoData hpoData = new HpoData(split[0], gene, split[2], split[3], split[4]);
					if (result.containsKey(gene))
					{
						result.get(gene).add(hpoData);
					}
					else
					{
						List<HpoData> newList = new ArrayList<HpoData>();
						newList.add(hpoData);
						result.put(gene, newList);
					}
				}
			}
			return result;
		}
		return result;
	}

	private Reader getHpoDataReader() throws IOException
	{
		String fileLocation = molgenisSettings.getProperty(HpoServiceAnnotator.HPO_FILE_LOCATION);
		return new InputStreamReader(new FileInputStream(fileLocation), Charset.forName("UTF-8"));
	}
}
