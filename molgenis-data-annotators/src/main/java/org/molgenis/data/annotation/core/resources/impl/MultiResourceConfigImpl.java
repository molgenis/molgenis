package org.molgenis.data.annotation.core.resources.impl;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.resources.MultiResourceConfig;
import org.molgenis.data.annotation.core.resources.ResourceConfig;
import org.molgenis.security.core.runas.RunAsSystemProxy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by charbonb on 15/06/15.
 */
public class MultiResourceConfigImpl implements MultiResourceConfig
{
	private final Entity molgenisSettings;
	private final String chromosomesProperty;
	private final String filePatternProperty;
	private final String rootDirectoryProperty;
	private final String overrideChromosomeFilesProperty;
	public static final String DEFAULT_CHROMOSOMES = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,Y,X";
	public static final String DEFAULT_PATTERN = "chr%s.vcf";
	public static final String DEFAULT_ROOT_DIRECTORY = File.separatorChar + "data" + File.separatorChar + "resources";

	/**
	 * overrideChromosomeFiles contains the chromosome that do not comply with the pattern. those chromosomes and
	 * corresponding filenames should be stated comma separated per chromosome, and the chromosome and filename itself
	 * should be colon separated. e.g. 1:file1.vcf,2:file2.txt,X:XYZ.vcf
	 */
	public MultiResourceConfigImpl(String chromosomesProperty, String filePatternProperty, String rootDirectoryProperty,
			String overrideChromosomeFilesProperty, Entity molgenisSettings)
	{
		this.chromosomesProperty = chromosomesProperty;
		this.filePatternProperty = filePatternProperty;
		this.rootDirectoryProperty = rootDirectoryProperty;
		this.overrideChromosomeFilesProperty = overrideChromosomeFilesProperty;
		this.molgenisSettings = molgenisSettings;
	}

	public MultiResourceConfigImpl(String chromosomesProperty, String filePatternProperty, String folderProperty,
			Entity molgenisSettings)
	{
		this(chromosomesProperty, filePatternProperty, folderProperty, null, molgenisSettings);
	}

	@Override
	public Map<String, ResourceConfig> getConfigs()
	{
		String[] chromosomes = getSetting(this.chromosomesProperty, DEFAULT_CHROMOSOMES).split(",");
		String pattern = getSetting(this.filePatternProperty, DEFAULT_PATTERN);
		String folder = getSetting(this.rootDirectoryProperty, DEFAULT_ROOT_DIRECTORY);
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
					final String pathname = folder + File.separatorChar + filename;

					if (null == file || pathname.equals(file.getPath()))
					{
						this.file = new File(folder + File.separatorChar + filename);
					}

					return this.file;
				}

			});
		}

		return configs;
	}

	private String getSetting(String name, String defaultValue)
	{
		String value = RunAsSystemProxy.runAsSystem(() -> molgenisSettings.getString(name));
		return value != null ? value : defaultValue;
	}

	private Map<String, String> getOverrideChromosomeFiles()
	{
		Map<String, String> overrideChromosomeFilesMap = new HashMap<>();
		if (null != this.overrideChromosomeFilesProperty
				&& molgenisSettings.getString(this.overrideChromosomeFilesProperty) != null)
		{
			String overrideChromosomeFiles = getSetting(this.overrideChromosomeFilesProperty, "").trim();
			for (String keyValue : overrideChromosomeFiles.split(","))
			{
				String[] pairs = keyValue.split(":", 2);
				overrideChromosomeFilesMap.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
			}
		}
		return overrideChromosomeFilesMap;
	}
}
