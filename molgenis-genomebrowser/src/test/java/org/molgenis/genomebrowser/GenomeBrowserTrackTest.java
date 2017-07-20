package org.molgenis.genomebrowser;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributes;
import org.molgenis.genomebrowser.meta.GenomeBrowserSettings;
import org.molgenis.genomebrowser.service.GenomeBrowserServiceTest;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * Created by Bart on 7/20/2017.
 */
public class GenomeBrowserTrackTest
{
	@Test
	public void testToTrack() throws Exception
	{
		GenomeBrowserTrack reference = GenomeBrowserTrack.create("ref_id", "ref_label", null, null, null, null, null,
				null, null, null, null);
		GenomeBrowserAttributes genomeBrowserAttributes = GenomeBrowserServiceTest.getGenomeBrowserAttributes("postion",
				"chrom", "normal", "mutant");

		EntityType molgenisEntity = mock(EntityType.class);
		GenomeBrowserTrack track = GenomeBrowserTrack.create("id", "entityLabel", molgenisEntity,
				GenomeBrowserSettings.TrackType.VARIANT, Collections.singletonList(reference),
				GenomeBrowserSettings.MolgenisReferenceMode.NONE, genomeBrowserAttributes, "alert(\"test\")",
				"attr 1:attr1,reference attribute:REF,position on genome:POS", null, null);

		String expected = "{\"genome_attrs\":{\"ref\":\"normal\",\"pos\":\"postion\",\"alt\":\"mutant\",\"chr\":\"chrom\"},\"label_attr\":\"entityLabel\",\"tier_type\":\"molgenis\",\"uri\":\"http://localhost:8080/api/v2/null?id\",\"actions\":\"alert(\\\"test\\\")\",\"track_type\":\"VARIANT\",\"attrs\":[\"attr 1:attr1\",\"reference attribute:REF\",\"position on genome:POS\"]}";

		assertEquals(track.toTrackJson().toString(), expected);
	}
}