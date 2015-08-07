package org.molgenis.data.annotation.provider;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KeggDataProvider
{
	private static final String KEY_KEGG_PATHWAY = "plugin.annotators.kegg.pathway";
	private static final String KEY_KEGG_HSA = "plugin.annotators.kegg.hsa";
	private static final String KEY_KEGG_PATHWAY_HSA_LINK = "plugin.annotators.kegg.pathway.hsa.link";
	private static final String DEFAULT_KEGG_PATHWAY_VALUE = "http://rest.kegg.jp/list/pathway/hsa";
	private static final String DEFAULT_KEGG_HSA_VALUE = "http://rest.kegg.jp/list/hsa";
	private static final String DEFAULT_KEGG_PATHWAY_HSA_LINK_VALUE = "http://rest.kegg.jp/link/hsa/pathway";

	private final MolgenisSettings molgenisSettings;

	@Autowired
	public KeggDataProvider(MolgenisSettings molgenisSettings)
	{
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
	}

	public Reader getKeggPathwayReader() throws IOException
	{
		String url = molgenisSettings.getProperty(KEY_KEGG_PATHWAY, DEFAULT_KEGG_PATHWAY_VALUE);
		return new InputStreamReader(new URL(url).openStream(), Charset.forName("UTF-8"));
	}

	public Reader getKeggHsaReader() throws IOException
	{
		String url = molgenisSettings.getProperty(KEY_KEGG_HSA, DEFAULT_KEGG_HSA_VALUE);
		return new InputStreamReader(new URL(url).openStream(), Charset.forName("UTF-8"));
	}

	public Reader getKeggPathwayHsaReader() throws IOException
	{
		String url = molgenisSettings.getProperty(KEY_KEGG_PATHWAY_HSA_LINK, DEFAULT_KEGG_PATHWAY_HSA_LINK_VALUE);
		return new InputStreamReader(new URL(url).openStream(), Charset.forName("UTF-8"));
	}
}
