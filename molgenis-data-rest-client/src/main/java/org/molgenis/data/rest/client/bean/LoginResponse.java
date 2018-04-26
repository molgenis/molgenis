package org.molgenis.data.rest.client.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;

import javax.annotation.Nullable;

@AutoGson(autoValueClass = AutoValue_LoginResponse.class)
@AutoValue
public abstract class LoginResponse
{
	public static LoginResponse create(String token, String username, String firstname, String lastname)
	{
		return new AutoValue_LoginResponse(token, username, firstname, lastname);
	}

	public abstract String getToken();

	public abstract String getUsername();

	@Nullable
	public abstract String getFirstname();

	@Nullable
	public abstract String getLastname();
}