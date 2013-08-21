package org.molgenis.omx.mobile.login;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.security.Login;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LoginControllerTest
{
	private LoginController loginController;
	private Database database;
	private Login login;

	@BeforeMethod
	public void setUp()
	{
		login = mock(Login.class);
		database = mock(Database.class);
		when(database.getLogin()).thenReturn(login);

		loginController = new LoginController(database);
	}

	@Test
	public void loginSuccess() throws Exception
	{
		LoginRequest request = new LoginRequest();
		request.setPassword("password");
		request.setUsername("username");

		when(login.login(database, "username", "password")).thenReturn(true);

		ResponseEntity<LoginResponse> response = loginController.login(request);
		assertNotNull(response);
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertNotNull(response.getBody());
		assertTrue(response.getBody().isSuccess());
		assertNull(response.getBody().getErrorMessage());
	}

	@Test
	public void loginFailed() throws Exception
	{
		LoginRequest request = new LoginRequest();
		request.setPassword("password");
		request.setUsername("username");

		when(login.login(database, "username", "password")).thenReturn(false);

		ResponseEntity<LoginResponse> response = loginController.login(request);
		assertNotNull(response);
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertNotNull(response.getBody());
		assertFalse(response.getBody().isSuccess());
		assertNotNull(response.getBody().getErrorMessage());
	}

	@Test
	public void logout() throws Exception
	{
		loginController.logout();
		verify(login).logout(database);
		verify(login).reload(database);
	}
}
