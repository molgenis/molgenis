package org.molgenis.data.annotation.impl;

import java.io.IOException;

import org.mockito.Mockito;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.provider.ClinvarDataProvider;
import org.molgenis.framework.server.MolgenisSettings;
import org.testng.annotations.BeforeTest;

public class ClinVarVCFServiceAnnotatorTest
{
	private ClinVarVCFServiceAnnotator annotator;
	private MolgenisSettings molgenisSettings;
	private AnnotationService annotatorService;
	private ClinvarDataProvider clinvarDataProvider;

	@BeforeTest
	public void beforeTest() throws IOException
	{
		molgenisSettings = Mockito.mock(MolgenisSettings.class);
		annotatorService = Mockito.mock(AnnotationService.class);
		clinvarDataProvider = Mockito.mock(ClinvarDataProvider.class);
		annotator = new ClinVarVCFServiceAnnotator(molgenisSettings, annotatorService, clinvarDataProvider);
	}
}
