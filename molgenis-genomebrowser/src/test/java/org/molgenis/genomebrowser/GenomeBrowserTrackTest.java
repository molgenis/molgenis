package org.molgenis.genomebrowser;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributes;
import org.molgenis.genomebrowser.meta.GenomeBrowserSettings;
import org.molgenis.genomebrowser.service.GenomeBrowserServiceTest;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.genomebrowser.meta.GenomeBrowserSettings.TrackType.VARIANT;
import static org.testng.Assert.assertEquals;

public class GenomeBrowserTrackTest
{
	@Test
	public void testToTrack() throws Exception
	{
		EntityType entity = mock(EntityType.class);
		GenomeBrowserAttributes genomeBrowserAttributes = GenomeBrowserServiceTest.getGenomeBrowserAttributes("postion",
				"chrom", "normal", "mutant");
		GenomeBrowserTrack reference = GenomeBrowserTrack.create("ref_id", "label", "ref_label", entity, VARIANT, null,
				GenomeBrowserSettings.MolgenisReferenceMode.NONE, genomeBrowserAttributes, null, null, null, null);

		EntityType molgenisEntityType = when(mock(EntityType.class).getId()).thenReturn("molgenisEntityType").getMock();
		GenomeBrowserTrack track = GenomeBrowserTrack.create("id", "label", "entityLabel", molgenisEntityType, VARIANT,
				Collections.singletonList(reference), GenomeBrowserSettings.MolgenisReferenceMode.NONE,
				genomeBrowserAttributes, "alert(\"test\")",
				"attr 1:attr1,reference attribute:REF,position on genome:POS", null, null);

		String expected = "{\"genome_attrs\":{\"ref\":\"normal\",\"pos\":\"postion\",\"alt\":\"mutant\",\"chr\":\"chrom\"},\"name\":\"label\",\"label_attr\":\"entityLabel\",\"tier_type\":\"molgenis\",\"uri\":\"/api/v2/molgenisEntityType\",\"actions\":\"alert(\\\"test\\\")\",\"track_type\":\"VARIANT\",\"entity\":\"molgenisEntityType\",\"attrs\":[\"attr 1:attr1\",\"reference attribute:REF\",\"position on genome:POS\"]}";

		assertEquals(track.toTrackJson().toString(), expected);
	}
}