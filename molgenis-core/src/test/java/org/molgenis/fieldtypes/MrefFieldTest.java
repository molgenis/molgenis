package org.molgenis.fieldtypes;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MrefFieldTest
{
	private MrefField mrefField;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		mrefField = new MrefField();
	}

	@Test
	public void convertList()
	{
		List<String> strList = Arrays.asList("a", "b");
		assertEquals(mrefField.convert(strList), Arrays.asList("a", "b"));
	}

	@Test
	public void convertIterable()
	{
		Iterable<String> strIterable = new Iterable<String>()
		{

			@Override
			public Iterator<String> iterator()
			{
				return Arrays.asList("a", "b").iterator();
			}
		};
		assertEquals(mrefField.convert(strIterable), Arrays.asList("a", "b"));
	}

	@Test
	public void convertStream()
	{
		Stream<String> strStream = Stream.of("a", "b");
		assertEquals(mrefField.convert(strStream), Arrays.asList("a", "b"));
	}
}