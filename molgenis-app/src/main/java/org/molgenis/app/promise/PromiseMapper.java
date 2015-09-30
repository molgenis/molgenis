package org.molgenis.app.promise;

import java.io.IOException;

public interface PromiseMapper
{
	public static final String BBMRI_NL_SAMPLE_COLLECTIONS_ENTITY = "bbmri_nl_sample_collections";

	public String getId();

	public void map(String biobankId) throws IOException;
}
