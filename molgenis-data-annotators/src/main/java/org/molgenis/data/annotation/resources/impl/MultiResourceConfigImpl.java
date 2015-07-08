package org.molgenis.data.annotation.resources.impl;

import static java.lang.String.format;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.molgenis.data.annotation.resources.MultiResourceConfig;
import org.molgenis.data.annotation.resources.ResourceConfig;
import org.molgenis.framework.server.MolgenisSettings;

/**
 * Created by charbonb on 15/06/15.
 */
public class MultiResourceConfigImpl implements MultiResourceConfig
{
	private MolgenisSettings molgenisSettings;

	private String chromosomesProperty;
	private String filePatternProperty;
	private String folderProperty;

	public static final String DEFAULT_CHROMOSOMES = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,Y,X";
	public static final String DEFAULT_PATTERN = "chr%s.vcf";
	public static final String DEFAULT_FOLDER = "/data/resources";

	public MultiResourceConfigImpl(String chromosomesProperty, String filePatternProperty, String folderProperty,
			MolgenisSettings molgenisSettings)
	{
		this.chromosomesProperty = chromosomesProperty;
		this.filePatternProperty = filePatternProperty;
		this.folderProperty = folderProperty;
		this.molgenisSettings = molgenisSettings;
	}

	@Override
	public Map<String, ResourceConfig> getConfigs()
	{
		String chromosomesPropertyValue = molgenisSettings.getProperty(chromosomesProperty, DEFAULT_CHROMOSOMES);
		String[] chromosomes = chromosomesPropertyValue.split(",");
		String pattern = molgenisSettings.getProperty(filePatternProperty, DEFAULT_PATTERN);
		String folder = molgenisSettings.getProperty(folderProperty, DEFAULT_FOLDER);

		Map<String, ResourceConfig> result = new HashMap<>();
		for (String chrom : chromosomes)
		{
			result.put(chrom, new ResourceConfig()
			{
				@Override
				public File getFile()
				{
					String filename = format(pattern, chrom);
					return new File(folder + "/" + filename);
				}
			});
		}
		return result;
	}
}
