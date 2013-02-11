package org.molgenis.omx.auth.service.tokenmanager;

import java.util.List;
import java.util.Map;

import org.molgenis.framework.server.Token;

public class TokenManagerModel
{

	Map<String, Token> tokens;

	public Map<String, Token> getTokens()
	{
		return tokens;
	}

	public void setTokens(Map<String, Token> tokens)
	{
		this.tokens = tokens;
	}

}
