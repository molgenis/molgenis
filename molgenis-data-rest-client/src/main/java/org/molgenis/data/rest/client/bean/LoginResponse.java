package org.molgenis.data.rest.client.bean;

import javax.annotation.Nullable;

import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

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