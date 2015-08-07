package org.molgenis.data.annotation.provider;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.molgenis.data.annotation.settings.AnnotationSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KeggDataProvider
{
	private final AnnotationSettings annotationSettings;

	@Autowired
	public KeggDataProvider(AnnotationSettings annotationSettings)
	{
		this.annotationSettings = checkNotNull(annotationSettings);
	}

	public Reader getKeggPathwayReader() throws IOException
	{
		String url = annotationSettings.getKeggPathway();
		return new InputStreamReader(new URL(url).openStream(), Charset.forName("UTF-8"));
	}

	public Reader getKeggHsaReader() throws IOException
	{
		String url = annotationSettings.getKeggHsa();
		return new InputStreamReader(new URL(url).openStream(), Charset.forName("UTF-8"));
	}

	public Reader getKeggPathwayHsaReader() throws IOException
	{
		String url = annotationSettings.getKeggPathwayHsaLink();
		return new InputStreamReader(new URL(url).openStream(), Charset.forName("UTF-8"));
	}
}
