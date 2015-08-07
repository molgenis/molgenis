package org.molgenis.data.annotation.provider;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import org.molgenis.data.annotation.settings.AnnotationSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OmimMorbidMapProvider
{
	private final AnnotationSettings annotationSettings;

	@Autowired
	public OmimMorbidMapProvider(AnnotationSettings annotationSettings)
	{
		this.annotationSettings = checkNotNull(annotationSettings);
	}

	public Reader getOmimMorbidMap() throws MalformedURLException, IOException
	{
		String url = annotationSettings.getOmimMorbidMapLocation();
		return new InputStreamReader(new URL(url).openStream(), Charset.forName("UTF-8"));
	}
}
