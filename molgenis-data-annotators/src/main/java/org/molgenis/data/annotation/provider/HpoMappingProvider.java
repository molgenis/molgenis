package org.molgenis.data.annotation.provider;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HpoMappingProvider
{
	private static final String KEY_HPO_MAPPING = "plugin.annotators.hpo.mapping.url";

	// TODO: more fancy symptom information by using
	// http://compbio.charite.de/hudson/job/hpo.annotations/lastStableBuild/artifact/misc/phenotype_annotation.tab
	public final String default_hpo_mapping_value = "http://compbio.charite.de/hudson/job/hpo.annotations.monthly/lastStableBuild/artifact/annotation/ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt";

	private final MolgenisSettings molgenisSettings;

	@Autowired
	public HpoMappingProvider(MolgenisSettings molgenisSettings)
	{
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
	}

	public Reader getHpoMapping() throws MalformedURLException, IOException
	{
		String url = molgenisSettings.getProperty(KEY_HPO_MAPPING, default_hpo_mapping_value);
		return new InputStreamReader(new URL(url).openStream(), Charset.forName("UTF-8"));
	}
}