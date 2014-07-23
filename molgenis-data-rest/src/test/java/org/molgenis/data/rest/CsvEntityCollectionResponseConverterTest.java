package org.molgenis.data.rest;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.data.Entity;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class CsvEntityCollectionResponseConverterTest {

	private static final String HEADER = "\"href\",\"name\"\n";
	private EntityPager pager = new EntityPager(0, 2, new Long(2),
			(Iterable<Entity>) null);
	private EntityCollectionResponse response;
	private Map<String, Object> jan, piet, naamloos;
	private CsvEntityCollectionResponseConverter converter;
	private MockHttpOutputMessage outputMessage;

	@BeforeTest
	public void beforeTest() {
		converter = new CsvEntityCollectionResponseConverter();
	}

	@BeforeMethod
	public void beforeMethod() {
		jan = createPersonMap("Jan", 12, true);
		piet = createPersonMap("Konijn, Piet", 8, false);
		naamloos = createPersonMap(null, 10, true);
		outputMessage = new MockHttpOutputMessage();
	}

	private static Map<String, Object> createPersonMap(String name, int age,
			boolean happyChappy) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		result.put("name", name);
		result.put("age", age);
		result.put("happyChappy", happyChappy);
		return result;
	}

	@Test
	public void testSerializeSingleRowCSV()
			throws HttpMessageNotWritableException, IOException {
		response = new EntityCollectionResponse(pager,
				Collections.singletonList(jan), "href");
		converter.write(response, new MediaType("text", "csv"), outputMessage);
		assertEquals(outputMessage.getBodyAsString(), HEADER
				+ "\"Jan\",\"12\",\"true\"\n");
	}

	@Test
	public void testSerializeTwoRowsCSV()
			throws HttpMessageNotWritableException, IOException {
		response = new EntityCollectionResponse(pager,
				Arrays.asList(jan, piet), "href");
		converter.write(response, new MediaType("text", "csv"), outputMessage);
		assertEquals(outputMessage.getBodyAsString(), HEADER
				+ "\"Jan\",\"12\",\"true\"\n\"Konijn, Piet\",\"8\",\"false\"\n");
	}

	@Test
	public void testSerializeSingleRowWithNullValueCSV()
			throws HttpMessageNotWritableException, IOException {
		response = new EntityCollectionResponse(pager,
				Collections.singletonList(naamloos), "href");
		converter.write(response, new MediaType("text", "csv"), outputMessage);
		assertEquals(outputMessage.getBodyAsString(), HEADER
				+ ",\"10\",\"true\"\n");
	}

	@Test
	public void testSerializeNestedCSV()
			throws HttpMessageNotWritableException, IOException {
		jan.put("parent", piet);
		jan.put("last", "lastjan");
		response = new EntityCollectionResponse(pager,
				Collections.singletonList(jan), "href");
		converter.write(response, new MediaType("text", "csv"), outputMessage);
		assertEquals(outputMessage.getBodyAsString(), HEADER
				+ "\"Jan\",\"12\",\"true\",\"Konijn, Piet\",\"8\",\"false\",\"lastjan\"\n");
	}

}
