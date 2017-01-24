package org.molgenis.dataexplorer.directory;

import com.google.gson.Gson;
import org.molgenis.util.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

@ContextConfiguration(classes = GsonConfig.class)
public class NegotiatorQueryTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private Gson gson;

	private NegotiatorQuery negotiatorQuery = NegotiatorQuery
			.createQuery("url", Collections.emptyList(), "humanReadable", null);

	private String json = "{\"URL\":\"url\",\"collections\":[],\"humanReadable\":\"humanReadable\"}";

	@Test
	public void testSerialization()
	{
		Assert.assertEquals(gson.toJson(negotiatorQuery), json);
	}

	@Test
	public void testDeserialization()
	{
		Assert.assertEquals(gson.fromJson(json, NegotiatorQuery.class), negotiatorQuery);
	}
}
