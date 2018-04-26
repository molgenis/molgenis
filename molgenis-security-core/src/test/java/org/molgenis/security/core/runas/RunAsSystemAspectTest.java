package org.molgenis.security.core.runas;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.testng.Assert.assertEquals;

@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
@ContextConfiguration(classes = RunAsSystemAspectTest.Config.class)
public class RunAsSystemAspectTest extends AbstractTestNGSpringContextTests
{
	private static Authentication AUTHENTICATION_PREVIOUS;

	@BeforeClass
	public void setUpBeforeClass()
	{
		AUTHENTICATION_PREVIOUS = SecurityContextHolder.getContext().getAuthentication();
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
		SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_PREVIOUS);
	}

	@Test
	@WithMockUser
	public void invoke()
	{
		assertEquals(getCurrentUsername(), "user");
		assertEquals(runAsSystem(this::getCurrentUsername), "SYSTEM");
		assertEquals(getCurrentUsername(), "user");
	}

	private String getCurrentUsername()
	{
		return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
	}

	@Configuration
	public static class Config
	{

	}
}
