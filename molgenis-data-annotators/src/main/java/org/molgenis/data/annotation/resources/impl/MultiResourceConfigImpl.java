package org.molgenis.data.annotation.resources.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.molgenis.data.annotation.resources.MultiResourceConfig;
import org.molgenis.data.annotation.resources.ResourceConfig;
import org.molgenis.framework.server.MolgenisSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by charbonb on 15/06/15.
 */
public class MultiResourceConfigImpl implements MultiResourceConfig
{
	private static final Logger LOG = LoggerFactory.getLogger(MultiResourceConfigImpl.class);
	private MolgenisSettings molgenisSettings;
	private String chromosomesProperty;
	private String filePatternProperty;
	private String rootDirectoryProperty;
	private String overrideChromosomeFilesProperty;
	public static final String DEFAULT_CHROMOSOMES = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,Y,X";
	public static final String DEFAULT_PATTERN = "chr%s.vcf";
	public static final String DEFAULT_ROOT_DIRECTORY = "/data/resources";
	public Map<String, ResourceConfig> configs = null;

	/**
	 * overrideChromosomeFiles contains the chromosome that do not comply with the pattern. those chromosomes and
	 * corresponding filenames should be stated comma separated per chromosome, and the chromosome and filename itself
	 * should be colon separated. e.g. 1:file1.vcf,2:file2.txt,X:XYZ.vcf
	 * */
	public MultiResourceConfigImpl(String chromosomesProperty, String filePatternProperty,
			String rootDirectoryProperty, String overrideChromosomeFilesProperty, MolgenisSettings molgenisSettings)
	{
		this.chromosomesProperty = chromosomesProperty;
		this.filePatternProperty = filePatternProperty;
		this.rootDirectoryProperty = rootDirectoryProperty;
		this.overrideChromosomeFilesProperty = overrideChromosomeFilesProperty;
		this.molgenisSettings = molgenisSettings;
	}

	public MultiResourceConfigImpl(String chromosomesProperty, String filePatternProperty, String folderProperty,
			MolgenisSettings molgenisSettings)
	{
		this(chromosomesProperty, filePatternProperty, folderProperty, null, molgenisSettings);
	}

	@Override
	public Map<String, ResourceConfig> getConfigs()
	{
		if (null == configs)
		{
			this.refreshConfigs();
		}

		return this.configs;
	}

	public void refreshConfigs()
	{
		String[] chromosomes = molgenisSettings.getProperty(this.chromosomesProperty, DEFAULT_CHROMOSOMES).split(",");
		String pattern = molgenisSettings.getProperty(this.filePatternProperty, DEFAULT_PATTERN);
		String folder = molgenisSettings.getProperty(this.rootDirectoryProperty, DEFAULT_ROOT_DIRECTORY);
		Map<String, String> overrideChromosomeFilesMap = this.getOverrideChromosomeFiles();

		Map<String, ResourceConfig> configs = new HashMap<>();
		for (String chrom : chromosomes)
		{
			configs.put(chrom, new ResourceConfig()
			{
				private File file = null;

				@Override
				public File getFile()
				{
					final String filename;
					if (overrideChromosomeFilesMap.containsKey(chrom))
					{
						filename = overrideChromosomeFilesMap.get(chrom);
					}
					else
					{
						filename = String.format(pattern, chrom);
					}
					final String pathname = folder + "/" + filename;

					if (null == file || pathname.equals(file.getPath()))
					{
						this.file = new File(folder + "/" + filename);
					}

					return this.file;
				}
								
			});
		}
		this.configs = configs;

		LOG.debug("refreshConfigs() --- refresh config");
	}

	private Map<String, String> getOverrideChromosomeFiles()
	{
		Map<String, String> overrideChromosomeFilesMap = new HashMap<String, String>();
		if (null != this.overrideChromosomeFilesProperty
				&& molgenisSettings.propertyExists(this.overrideChromosomeFilesProperty))
		{
			String overrideChromosomeFiles = molgenisSettings.getProperty(this.overrideChromosomeFilesProperty).trim();
			for (String keyValue : overrideChromosomeFiles.split(","))
			{
				String[] pairs = keyValue.split(":", 2);
				overrideChromosomeFilesMap.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
			}
		}
		return overrideChromosomeFilesMap;
	}
}
