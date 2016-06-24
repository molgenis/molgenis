package org.molgenis.data.rest.client.bean;

import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoGson(autoValueClass = AutoValue_LoginRequest.class)
@AutoValue
public abstract class LoginRequest
{
	public static LoginRequest create(String username, String password)
	{
		return new AutoValue_LoginRequest(username, password);
	}

	public abstract String getUsername();

	public abstract String getPassword();
}