package org.molgenis.data.matrix.impl;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class MatrixMapperImplTest
{
	private MatrixMapperImpl matrixMapper;

	@BeforeTest
	public void setup()
	{
		matrixMapper = new MatrixMapperImpl(ResourceUtils.getFile(getClass(), "/mapping.txt"));
	}

	@Test
	public void testMapping()
	{
		assertEquals(matrixMapper.map("mapping1"), "matrix1");
		assertEquals(matrixMapper.map("mapping4"), "matrix4");
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "the specified value \\[foo\\] was not found in the mappingfile")
	public void testMappingInvalid()
	{
		matrixMapper.map("foo");
	}
}
