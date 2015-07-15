package org.molgenis.data.annotation.resources.impl;

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
	private Map<String, String> overrideChromosomeFiles;

	public static final String DEFAULT_CHROMOSOMES = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,Y,X";
	public static final String DEFAULT_PATTERN = "chr%.vcf";
	public static final String DEFAULT_FOLDER = "/data/resources/";

	/**
	 * overrideChromosomeFiles contains the chromosome that do not comply with the pattern.
	 * those chromosomes and corresponding filenames should be stated comma separated per chromosome,
	 * and the chromosome and filename itself should be colon separated.
	 * e.g. 1:file1.vcf,2:file2.txt,X:XYZ.vcf
	 * */
	public MultiResourceConfigImpl(String chromosomesProperty, String filePatternProperty, String folderProperty,
			MolgenisSettings molgenisSettings, String overrideChromosomeFiles)
	{
		this.chromosomesProperty = chromosomesProperty;
		this.filePatternProperty = filePatternProperty;
		this.folderProperty = folderProperty;
		this.molgenisSettings = molgenisSettings;
		this.overrideChromosomeFiles = new HashMap<>();
		for(String keyValue : overrideChromosomeFiles.split(",")) {
			String[] pairs = keyValue.split(":", 2);
			this.overrideChromosomeFiles.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
		}
	}

	public MultiResourceConfigImpl(String chromosomesProperty, String filePatternProperty, String folderProperty,
			MolgenisSettings molgenisSettings)
	{
		this(chromosomesProperty, filePatternProperty, folderProperty, molgenisSettings, "");
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
					String filename;
					if(overrideChromosomeFiles.containsKey(chrom)) {
						filename = overrideChromosomeFiles.get(chrom);
					}
					else {
						filename = String.format(pattern, chrom);
					}
					return new File(folder + "/" + filename);
				}
			});
		}
		return result;
	}
}
