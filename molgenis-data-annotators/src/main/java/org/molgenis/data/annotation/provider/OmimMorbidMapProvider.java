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
public class OmimMorbidMapProvider
{
	private static final String KEY_OMIM_MORBIDMAP = "plugin.annotators.omim.morbidmap.url";
	private static final String DEFAULT_OMIM_MORBIDMAP_VALUE = "https://molgenis26.target.rug.nl/downloads/5gpm/morbidmap";

	private final MolgenisSettings molgenisSettings;

	@Autowired
	public OmimMorbidMapProvider(MolgenisSettings molgenisSettings)
	{
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
	}

	public Reader getOmimMorbidMap() throws MalformedURLException, IOException
	{
		String url = molgenisSettings.getProperty(KEY_OMIM_MORBIDMAP, DEFAULT_OMIM_MORBIDMAP_VALUE);
		return new InputStreamReader(new URL(url).openStream(), Charset.forName("UTF-8"));
	}
}
