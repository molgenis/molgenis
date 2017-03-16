package org.molgenis.test;

import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;

public class AbstractMockitoTest
{
	@BeforeMethod
	public void initMocks()
	{
		MockitoAnnotations.initMocks(this);
	}
}
