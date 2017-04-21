package org.molgenis.data.matrix.impl;

import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class DoubleMatrixTest
{

	private DoubleMatrix doubleMatrix;

	@BeforeTest
	public void setup()
	{
		doubleMatrix = new DoubleMatrix(ResourceUtils.getFile(getClass(), "/testmatrix.txt"), '\t');
	}

	@Test
	public void matrixValueByIndex()
	{
		assertTrue(doubleMatrix.getValueByIndex(2, 1) == 2.123);
		assertTrue(doubleMatrix.getValueByIndex(1, 2) == 1.234);
		assertTrue(doubleMatrix.getValueByIndex(1, 3) == 1.345);
	}

	@Test
	public void matrixTest()
	{
		assertTrue(doubleMatrix.getValueByName("gene1", "hpo123") == 1.123);
		assertTrue(doubleMatrix.getValueByName("gene2", "hpo123") == 2.123);
		assertTrue(doubleMatrix.getValueByName("gene3", "hpo345") == 3.345);
	}
}
