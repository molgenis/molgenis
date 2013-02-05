package org.molgenis.omx;

import static org.molgenis.util.DetectOS.getLineSeparator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.omx.observ.ObservableFeature;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EMeasureFeatureWriterTest
{

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testEMeasure() throws IOException
	{
		new EMeasureFeatureWriter(null).close();
	}

	@Test
	public void testConvert() throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		EMeasureFeatureWriter featureWriter = new EMeasureFeatureWriter(bos);
		try
		{
			List<ObservableFeature> observableFeatures = new ArrayList<ObservableFeature>();
			ObservableFeature observableFeature1 = new ObservableFeature();
			observableFeature1.setName("feature1");
			observableFeature1.setDescription("this is feature1");
			observableFeature1.setDataType("boolean");
			ObservableFeature observableFeature2 = new ObservableFeature();
			observableFeature2.setName("feature2");
			observableFeature2.setDescription("this is feature2");
			observableFeature2.setDataType("string");
			observableFeatures.add(observableFeature1);
			observableFeatures.add(observableFeature2);

			featureWriter.writeFeatures(observableFeatures);
		}
		finally
		{
			featureWriter.close();
		}
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><QualityMeasureDocument xmlns=\"urn:hl7-org:v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" classCode=\"CONTAINER\" moodCode=\"DEF\" xsi:schemaLocation=\"urn:hl7-org:v3 multicacheschemas/REPC_MT000100UV01.xsd\" xsi:type=\"REPC_MT000100UV01.Organizer\">	<subjectOf>"
				+ getLineSeparator()
				+ "		<measureAttribute><code code=\"feature1\" codeSystem=\"TBD\" displayName=\"this is feature1\"/><value code=\"dunno\" codeSystem=\"TBD\" displayName=\"This should be the mappingsname\" xsi:type=\"boolean\"/>		</measureAttribute>"
				+ getLineSeparator()
				+ "	</subjectOf>	<subjectOf>"
				+ getLineSeparator()
				+ "		<measureAttribute><code code=\"feature2\" codeSystem=\"TBD\" displayName=\"this is feature2\"/><value code=\"dunno\" codeSystem=\"TBD\" displayName=\"This should be the mappingsname\" xsi:type=\"string\"/>		</measureAttribute>"
				+ getLineSeparator() + "	</subjectOf></QualityMeasureDocument>";

		Assert.assertEquals(bos.toString("UTF-8"), expected);
	}
}
