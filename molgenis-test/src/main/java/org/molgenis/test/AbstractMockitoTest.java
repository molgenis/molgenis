package org.molgenis.test;

import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.mockitoSession;

public class AbstractMockitoTest
{
	private final Strictness strictness;

	private MockitoSession mockitoSession;

	public AbstractMockitoTest()
	{
		this(Strictness.STRICT_STUBS);
	}

	@Deprecated
	public AbstractMockitoTest(Strictness strictness)
	{
		this.strictness = requireNonNull(strictness);
	}

	@BeforeMethod
	public void initMocks()
	{
		mockitoSession = mockitoSession().initMocks(this).strictness(strictness).startMocking();
	}

	@AfterMethod
	public void tearDownAfterMethod()
	{
		mockitoSession.finishMocking();
	}
}
