package org.molgenis.elasticsearch.response;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.AggregateResult;
import org.molgenis.search.SearchResult;
import org.testng.annotations.Test;

public class ResponseParserTest
{

	@Test
	public void parseSearchResponse_aggregation1D()
	{
		String col1 = "col1", col2 = "col2", colTotal = "Total";
		String row1 = "Count";
		long val1 = 1l, val2 = 2l;
		final Terms terms = mock(Terms.class);

		Bucket bucket1 = mock(Bucket.class);
		Aggregations bucket1Aggregations = mock(Aggregations.class);
		when(bucket1Aggregations.iterator()).thenReturn(Collections.<Aggregation> emptyList().iterator()); // 1D
		when(bucket1.getAggregations()).thenReturn(bucket1Aggregations);
		when(bucket1.getKey()).thenReturn(col1);
		when(bucket1.getDocCount()).thenReturn(val1);

		Bucket bucket2 = mock(Bucket.class);
		Aggregations bucket2Aggregations = mock(Aggregations.class);
		when(bucket2Aggregations.iterator()).thenReturn(Collections.<Aggregation> emptyList().iterator());
		when(terms.getBuckets()).thenReturn(Arrays.asList(bucket1, bucket2));
		when(bucket2.getKey()).thenReturn(col2);
		when(bucket2.getDocCount()).thenReturn(val2);

		Aggregations aggregations = mock(Aggregations.class);
		when(aggregations.iterator()).thenAnswer(new Answer<Iterator<Aggregation>>()
		{
			// called multiple times, so use thenAnswer instead of thenReturn
			@Override
			public Iterator<Aggregation> answer(InvocationOnMock invocation) throws Throwable
			{
				return Collections.<Aggregation> singleton(terms).iterator();
			}
		});

		SearchResponse response = when(mock(SearchResponse.class).getAggregations()).thenReturn(aggregations).getMock();
		SearchHits searchHits = mock(SearchHits.class);
		when(searchHits.getTotalHits()).thenReturn(Long.valueOf(0l));
		when(searchHits.hits()).thenReturn(new SearchHit[0]);
		when(response.getHits()).thenReturn(searchHits);

		SearchResult searchResult = new ResponseParser().parseSearchResponse(response);
		AggregateResult aggregateResult = searchResult.getAggregate();
		assertEquals(aggregateResult.getxLabels(), Arrays.asList(col1, col2, colTotal));
		assertEquals(aggregateResult.getyLabels(), Arrays.asList(row1));
		List<List<Long>> matrix = aggregateResult.getMatrix();
		assertEquals(matrix, Arrays.asList(Arrays.asList(val1), Arrays.asList(val2), Arrays.asList(val1 + val2)));
	}

	@Test
	public void parseSearchResponse_aggregation1D_colSort()
	{
		String col1 = "col1", col2 = "col2", colTotal = "Total";
		String row1 = "Count";
		long val1 = 1l, val2 = 2l;
		final Terms terms = mock(Terms.class);

		Bucket bucket1 = mock(Bucket.class);
		Aggregations bucket1Aggregations = mock(Aggregations.class);
		when(bucket1Aggregations.iterator()).thenReturn(Collections.<Aggregation> emptyList().iterator()); // 1D
		when(bucket1.getAggregations()).thenReturn(bucket1Aggregations);
		when(bucket1.getKey()).thenReturn(col2); // test sorting
		when(bucket1.getDocCount()).thenReturn(val2);

		Bucket bucket2 = mock(Bucket.class);
		Aggregations bucket2Aggregations = mock(Aggregations.class);
		when(bucket2Aggregations.iterator()).thenReturn(Collections.<Aggregation> emptyList().iterator());
		when(terms.getBuckets()).thenReturn(Arrays.asList(bucket1, bucket2));
		when(bucket2.getKey()).thenReturn(col1); // test sorting
		when(bucket2.getDocCount()).thenReturn(val1);

		Aggregations aggregations = mock(Aggregations.class);
		when(aggregations.iterator()).thenAnswer(new Answer<Iterator<Aggregation>>()
		{
			// called multiple times, so use thenAnswer instead of thenReturn
			@Override
			public Iterator<Aggregation> answer(InvocationOnMock invocation) throws Throwable
			{
				return Collections.<Aggregation> singleton(terms).iterator();
			}
		});

		SearchResponse response = when(mock(SearchResponse.class).getAggregations()).thenReturn(aggregations).getMock();
		SearchHits searchHits = mock(SearchHits.class);
		when(searchHits.getTotalHits()).thenReturn(Long.valueOf(0l));
		when(searchHits.hits()).thenReturn(new SearchHit[0]);
		when(response.getHits()).thenReturn(searchHits);

		SearchResult searchResult = new ResponseParser().parseSearchResponse(response);
		AggregateResult aggregateResult = searchResult.getAggregate();
		assertEquals(aggregateResult.getxLabels(), Arrays.asList(col1, col2, colTotal));
		assertEquals(aggregateResult.getyLabels(), Arrays.asList(row1));
		List<List<Long>> matrix = aggregateResult.getMatrix();
		assertEquals(matrix, Arrays.asList(Arrays.asList(val1), Arrays.asList(val2), Arrays.asList(val1 + val2)));
	}

	@Test
	public void parseSearchResponse_aggregation2D()
	{
		String col1 = "col1", col2 = "col2", colTotal = "Total";
		String row1 = "row1", row2 = "row2", rowTotal = "Total";
		long valRow1Col1 = 1l, valRow1Col2 = 2l, valRow1Total = valRow1Col1 + valRow1Col2;
		long valRow2Col1 = 1l, valRow2Col2 = 2l, valRow2Total = valRow2Col1 + valRow2Col2;
		long valCol1Total = valRow1Col1 + valRow2Col1, valCol2Total = valRow1Col2 + valRow2Col2;
		final Terms terms = mock(Terms.class);

		Bucket bucketCol1 = mock(Bucket.class);
		Aggregations bucket1Aggregations = mock(Aggregations.class);
		final Terms bucket1Terms = mock(Terms.class);
		Bucket bucketCol1Row1 = mock(Bucket.class);
		when(bucketCol1Row1.getKey()).thenReturn(row1);
		when(bucketCol1Row1.getDocCount()).thenReturn(valRow1Col1);
		Bucket bucketCol1Row2 = mock(Bucket.class);
		when(bucketCol1Row2.getKey()).thenReturn(row2);
		when(bucketCol1Row2.getDocCount()).thenReturn(valRow2Col1);
		when(bucket1Terms.getBuckets()).thenReturn(Arrays.asList(bucketCol1Row1, bucketCol1Row2));
		when(bucket1Aggregations.iterator()).thenAnswer(new Answer<Iterator<Aggregation>>()
		{
			// called multiple times, so use thenAnswer instead of thenReturn
			@Override
			public Iterator<Aggregation> answer(InvocationOnMock invocation) throws Throwable
			{
				return Collections.<Aggregation> singleton(bucket1Terms).iterator();
			}
		});
		when(bucketCol1.getAggregations()).thenReturn(bucket1Aggregations);
		when(bucketCol1.getKey()).thenReturn(col1);

		Bucket bucketCol2 = mock(Bucket.class);
		Aggregations bucket2Aggregations = mock(Aggregations.class);
		final Terms bucket2Terms = mock(Terms.class);
		Bucket bucketCol2Row1 = mock(Bucket.class);
		when(bucketCol2Row1.getKey()).thenReturn(row1);
		when(bucketCol2Row1.getDocCount()).thenReturn(valRow1Col2);
		Bucket bucketCol2Row2 = mock(Bucket.class);
		when(bucketCol2Row2.getKey()).thenReturn(row2);
		when(bucketCol2Row2.getDocCount()).thenReturn(valRow2Col2);
		when(bucket2Terms.getBuckets()).thenReturn(Arrays.asList(bucketCol2Row1, bucketCol2Row2));
		when(bucket2Aggregations.iterator()).thenAnswer(new Answer<Iterator<Aggregation>>()
		{
			// called multiple times, so use thenAnswer instead of thenReturn
			@Override
			public Iterator<Aggregation> answer(InvocationOnMock invocation) throws Throwable
			{
				return Collections.<Aggregation> singleton(bucket2Terms).iterator();
			}
		});
		when(terms.getBuckets()).thenReturn(Arrays.asList(bucketCol1, bucketCol2));
		when(bucketCol2.getAggregations()).thenReturn(bucket2Aggregations);
		when(bucketCol2.getKey()).thenReturn(col2);
		when(bucketCol2.getDocCount()).thenReturn(valRow1Col1);

		Aggregations aggregations = mock(Aggregations.class);
		when(aggregations.iterator()).thenAnswer(new Answer<Iterator<Aggregation>>()
		{
			// called multiple times, so use thenAnswer instead of thenReturn
			@Override
			public Iterator<Aggregation> answer(InvocationOnMock invocation) throws Throwable
			{
				return Collections.<Aggregation> singleton(terms).iterator();
			}
		});

		SearchResponse response = when(mock(SearchResponse.class).getAggregations()).thenReturn(aggregations).getMock();
		SearchHits searchHits = mock(SearchHits.class);
		when(searchHits.getTotalHits()).thenReturn(Long.valueOf(0l));
		when(searchHits.hits()).thenReturn(new SearchHit[0]);
		when(response.getHits()).thenReturn(searchHits);

		SearchResult searchResult = new ResponseParser().parseSearchResponse(response);
		AggregateResult aggregateResult = searchResult.getAggregate();
		assertEquals(aggregateResult.getxLabels(), Arrays.asList(col1, col2, colTotal));
		assertEquals(aggregateResult.getyLabels(), Arrays.asList(row1, row2, rowTotal));
		List<List<Long>> matrix = aggregateResult.getMatrix();
		assertEquals(
				matrix,
				Arrays.asList(Arrays.asList(valRow1Col1, valRow2Col1, valCol1Total),
						Arrays.asList(valRow1Col2, valRow2Col2, valCol2Total),
						Arrays.asList(valRow1Total, valRow2Total, valCol1Total + valCol2Total)));
	}

	@Test
	public void parseSearchResponse_aggregation2D_colSort()
	{
		String col1 = "Z_col1", col2 = "A_col2", colTotal = "Total";
		String row1 = "Z_row1", row2 = "A_row2", rowTotal = "Total";
		long valRow1Col1 = 1l, valRow1Col2 = 2l, valRow1Total = valRow1Col1 + valRow1Col2;
		long valRow2Col1 = 1l, valRow2Col2 = 2l, valRow2Total = valRow2Col1 + valRow2Col2;
		long valCol1Total = valRow1Col1 + valRow2Col1, valCol2Total = valRow1Col2 + valRow2Col2;
		final Terms terms = mock(Terms.class);

		Bucket bucketCol1 = mock(Bucket.class);
		Aggregations bucket1Aggregations = mock(Aggregations.class);
		final Terms bucket1Terms = mock(Terms.class);
		Bucket bucketCol1Row1 = mock(Bucket.class);
		when(bucketCol1Row1.getKey()).thenReturn(row1);
		when(bucketCol1Row1.getDocCount()).thenReturn(valRow1Col1);
		Bucket bucketCol1Row2 = mock(Bucket.class);
		when(bucketCol1Row2.getKey()).thenReturn(row2);
		when(bucketCol1Row2.getDocCount()).thenReturn(valRow2Col1);
		when(bucket1Terms.getBuckets()).thenReturn(Arrays.asList(bucketCol1Row1, bucketCol1Row2));
		when(bucket1Aggregations.iterator()).thenAnswer(new Answer<Iterator<Aggregation>>()
		{
			// called multiple times, so use thenAnswer instead of thenReturn
			@Override
			public Iterator<Aggregation> answer(InvocationOnMock invocation) throws Throwable
			{
				return Collections.<Aggregation> singleton(bucket1Terms).iterator();
			}
		});
		when(bucketCol1.getAggregations()).thenReturn(bucket1Aggregations);
		when(bucketCol1.getKey()).thenReturn(col1);

		Bucket bucketCol2 = mock(Bucket.class);
		Aggregations bucket2Aggregations = mock(Aggregations.class);
		final Terms bucket2Terms = mock(Terms.class);
		Bucket bucketCol2Row1 = mock(Bucket.class);
		when(bucketCol2Row1.getKey()).thenReturn(row1);
		when(bucketCol2Row1.getDocCount()).thenReturn(valRow1Col2);
		Bucket bucketCol2Row2 = mock(Bucket.class);
		when(bucketCol2Row2.getKey()).thenReturn(row2);
		when(bucketCol2Row2.getDocCount()).thenReturn(valRow2Col2);
		when(bucket2Terms.getBuckets()).thenReturn(Arrays.asList(bucketCol2Row1, bucketCol2Row2));
		when(bucket2Aggregations.iterator()).thenAnswer(new Answer<Iterator<Aggregation>>()
		{
			// called multiple times, so use thenAnswer instead of thenReturn
			@Override
			public Iterator<Aggregation> answer(InvocationOnMock invocation) throws Throwable
			{
				return Collections.<Aggregation> singleton(bucket2Terms).iterator();
			}
		});
		when(terms.getBuckets()).thenReturn(Arrays.asList(bucketCol1, bucketCol2));
		when(bucketCol2.getAggregations()).thenReturn(bucket2Aggregations);
		when(bucketCol2.getKey()).thenReturn(col2);
		when(bucketCol2.getDocCount()).thenReturn(valRow1Col1);

		Aggregations aggregations = mock(Aggregations.class);
		when(aggregations.iterator()).thenAnswer(new Answer<Iterator<Aggregation>>()
		{
			// called multiple times, so use thenAnswer instead of thenReturn
			@Override
			public Iterator<Aggregation> answer(InvocationOnMock invocation) throws Throwable
			{
				return Collections.<Aggregation> singleton(terms).iterator();
			}
		});

		SearchResponse response = when(mock(SearchResponse.class).getAggregations()).thenReturn(aggregations).getMock();
		SearchHits searchHits = mock(SearchHits.class);
		when(searchHits.getTotalHits()).thenReturn(Long.valueOf(0l));
		when(searchHits.hits()).thenReturn(new SearchHit[0]);
		when(response.getHits()).thenReturn(searchHits);

		SearchResult searchResult = new ResponseParser().parseSearchResponse(response);
		AggregateResult aggregateResult = searchResult.getAggregate();
		assertEquals(aggregateResult.getxLabels(), Arrays.asList(col2, col1, colTotal));
		assertEquals(aggregateResult.getyLabels(), Arrays.asList(row2, row1, rowTotal));
		List<List<Long>> matrix = aggregateResult.getMatrix();
		assertEquals(
				matrix,
				Arrays.asList(Arrays.asList(valRow2Col2, valRow1Col2, valCol2Total),
						Arrays.asList(valRow2Col1, valRow1Col1, valCol1Total),
						Arrays.asList(valRow2Total, valRow1Total, valCol1Total + valCol2Total)));
	}
}
