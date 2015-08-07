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
public class HpoMappingProvider
{
	private final AnnotationSettings annotationSettings;

	@Autowired
	public HpoMappingProvider(AnnotationSettings annotationSettings)
	{
		this.annotationSettings = checkNotNull(annotationSettings);
	}

	public Reader getHpoMapping() throws IOException
	{
		String url = annotationSettings.getHpoLocation();
		return new InputStreamReader(new URL(url).openStream(), Charset.forName("UTF-8"));
	}
}