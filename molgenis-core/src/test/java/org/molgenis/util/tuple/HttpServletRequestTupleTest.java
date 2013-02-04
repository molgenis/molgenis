package org.molgenis.util.tuple;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

// TODO add test for getFile
public class HttpServletRequestTupleTest
{
	private MockHttpServletRequest req;
	private HttpServletRequestTuple tuple;

	@BeforeMethod
	public void setUp() throws IOException
	{
		req = new MockHttpServletRequest();
		req.addParameter("key_str", "value");
		req.addParameter("key_int", "1");
		tuple = new HttpServletRequestTuple(req);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void HttpServletRequestTuple() throws IOException
	{
		new HttpServletRequestTuple(null);
	}

	@Test
	public void getString()
	{
		assertEquals(tuple.get("key_str"), "value");
	}

	@Test
	public void getint()
	{
		assertEquals(tuple.getInt("key_int"), Integer.valueOf(1));
	}

	@Test
	public void getColNames()
	{
		List<String> colNames = new ArrayList<String>();
		for (String colName : tuple.getColNames())
			colNames.add(colName);
		assertEquals(colNames.size(), 2);
		assertTrue(colNames.contains("key_str"));
		assertTrue(colNames.contains("key_int"));
	}

	@Test
	public void getNrCols()
	{
		assertEquals(tuple.getNrCols(), 2);
	}

	@Test
	public void getRequest()
	{
		assertEquals(tuple.getRequest(), req);
	}

	@Test
	public void getResponse() throws IOException
	{
		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse resp = new MockHttpServletResponse();
		HttpServletRequestTuple tuple = new HttpServletRequestTuple(req, resp);
		assertEquals(tuple.getResponse(), resp);
	}

	@Test
	public void getResponse_noResponse()
	{
		assertEquals(tuple.getResponse(), null);
	}
}
