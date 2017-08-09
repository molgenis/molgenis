package org.molgenis.genomebrowser;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributes;
import org.molgenis.genomebrowser.meta.GenomeBrowserSettings;
import org.molgenis.genomebrowser.service.GenomeBrowserServiceTest;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.molgenis.genomebrowser.meta.GenomeBrowserSettings.TrackType.VARIANT;
import static org.testng.Assert.*;

public class GenomeBrowserTrackTest
{
	@Test
	public void testToTrack() throws Exception
	{
		EntityType entity = mock(EntityType.class);
		GenomeBrowserAttributes genomeBrowserAttributes = GenomeBrowserServiceTest.getGenomeBrowserAttributes("postion",
				"chrom", "normal", "mutant");
		GenomeBrowserTrack reference = GenomeBrowserTrack.create("ref_id", "ref_label", entity, VARIANT, null,
				GenomeBrowserSettings.MolgenisReferenceMode.NONE, genomeBrowserAttributes, null, null, null, null);

		EntityType molgenisEntity = mock(EntityType.class);
		GenomeBrowserTrack track = GenomeBrowserTrack.create("id", "entityLabel", molgenisEntity, VARIANT,
				Collections.singletonList(reference),
				GenomeBrowserSettings.MolgenisReferenceMode.NONE, genomeBrowserAttributes, "alert(\"test\")",
				"attr 1:attr1,reference attribute:REF,position on genome:POS", null, null);

		String expected = "{\"genome_attrs\":{\"ref\":\"normal\",\"pos\":\"postion\",\"alt\":\"mutant\",\"chr\":\"chrom\"},\"label_attr\":\"entityLabel\",\"tier_type\":\"molgenis\",\"uri\":\"http://localhost:8080/api/v2/null?id\",\"actions\":\"alert(\\\"test\\\")\",\"track_type\":\"VARIANT\",\"attrs\":[\"attr 1:attr1\",\"reference attribute:REF\",\"position on genome:POS\"]}";

		assertEquals(track.toTrackJson().toString(), expected);
	}
}