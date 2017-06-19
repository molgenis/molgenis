package org.molgenis.data.elasticsearch.generator;

import org.testng.annotations.Test;

public class DocumentContentBuilderTest
{
	@Test(expectedExceptions = NullPointerException.class)
	public void DocumentContentBuilder()
	{
		new DocumentContentBuilder(null);
	}
}
