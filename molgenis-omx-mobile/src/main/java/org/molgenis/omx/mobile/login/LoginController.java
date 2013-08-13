package org.molgenis.omx.mobile.login;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.molgenis.framework.db.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/mobile/login")
public class LoginController
{
	private final Database database;

	@Autowired
	public LoginController(Database database)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.database = database;
	}

	/**
	 * Login to molgenis.
	 * 
	 * If login failed, the LoginResponse contains an error message
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<LoginResponse> login(@RequestBody
	LoginRequest request) throws Exception
	{
		String errorMessage = null;

		boolean success = database.getLogin().login(database, request.getUsername(), request.getPassword());
		if (!success)
		{
			errorMessage = "Invalid password or username";
		}

		return new ResponseEntity<LoginResponse>(new LoginResponse(errorMessage), HttpStatus.OK);
	}
}
