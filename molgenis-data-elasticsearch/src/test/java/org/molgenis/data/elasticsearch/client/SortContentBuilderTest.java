package org.molgenis.data.elasticsearch.client;

import org.elasticsearch.search.sort.SortBuilder;
import org.molgenis.data.elasticsearch.generator.model.Sort;
import org.molgenis.data.elasticsearch.generator.model.SortOrder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.elasticsearch.generator.model.SortDirection.ASC;
import static org.molgenis.data.elasticsearch.generator.model.SortDirection.DESC;
import static org.testng.Assert.assertEquals;

public class SortContentBuilderTest
{
	private SortContentBuilder sortContentBuilder;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		sortContentBuilder = new SortContentBuilder();
	}

	@Test
	public void createSortsAsc()
	{
		List<SortBuilder> sorts = sortContentBuilder.createSorts(
				Sort.create(singletonList(SortOrder.create("field", ASC))));
		assertSortsEqual(sorts, singletonList(JSON_SORT_ASC));
	}

	@Test
	public void createSortsDesc()
	{
		List<SortBuilder> sorts = sortContentBuilder.createSorts(
				Sort.create(singletonList(SortOrder.create("field", DESC))));
		assertSortsEqual(sorts, singletonList(JSON_SORT_DESC));
	}

	private void assertSortsEqual(List<SortBuilder> sorts, List<String> contentStrings)
	{
		assertEquals(sorts.stream().map(SortBuilder::toString).collect(toList()), contentStrings);
	}

	private static final String JSON_SORT_ASC =
			"{\n" + "  \"field\" : {\n" + "    \"order\" : \"asc\",\n" + "    \"mode\" : \"min\"\n" + "  }\n" + "}";
	private static final String JSON_SORT_DESC =
			"{\n" + "  \"field\" : {\n" + "    \"order\" : \"desc\",\n" + "    \"mode\" : \"min\"\n" + "  }\n" + "}";
}