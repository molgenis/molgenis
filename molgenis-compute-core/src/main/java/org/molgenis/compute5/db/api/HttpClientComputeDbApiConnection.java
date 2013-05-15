package org.molgenis.compute5.db.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

import com.google.gson.Gson;

/**
 * Apache commons http client implementaton of the ComputeDbApiConnection
 * 
 * @author erwin
 * 
 */
public class HttpClientComputeDbApiConnection implements ComputeDbApiConnection
{
	private static final Charset CHARSET_UTF_8 = Charset.forName("UTF-8");
	private final String apiBaseUri;
	private final DefaultHttpClient httpClient = new DefaultHttpClient();
	private final HttpHost targetHost;

	public HttpClientComputeDbApiConnection(String host, int port, String apiBaseUri, String username, String password)
	{
		this.apiBaseUri = apiBaseUri;
		targetHost = new HttpHost(host, port);

		httpClient.getCredentialsProvider().setCredentials(
				new AuthScope(targetHost.getHostName(), targetHost.getPort()),
				new UsernamePasswordCredentials(username, password));
	}

	@Override
	public <T extends ApiResponse> T doRequest(Object request, String uri, Class<T> returnType) throws ApiException
	{
		HttpPost httpPost = new HttpPost(apiBaseUri + uri);

		// Create AuthCache instance
		AuthCache authCache = new BasicAuthCache();

		// Generate BASIC scheme object and add it to the local
		// auth cache
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(targetHost, basicAuth);

		// Add AuthCache to the execution context
		BasicHttpContext localcontext = new BasicHttpContext();
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

		Reader reader = null;
		try
		{

			if (request != null)
			{
				String json = new Gson().toJson(request);
				httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
			}

			HttpResponse httpResponse = httpClient.execute(targetHost, httpPost, localcontext);

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK)
			{
				throw new ApiException("Api service returned statuscode " + statusCode);
			}

			reader = new InputStreamReader(httpResponse.getEntity().getContent(), CHARSET_UTF_8);
			return new Gson().fromJson(reader, returnType);
		}
		catch (ClientProtocolException e)
		{
			throw new ApiException("Exception calling api service", e);
		}
		catch (IOException e)
		{
			throw new ApiException("Exception calling api service", e);
		}
		finally
		{
			IOUtils.closeQuietly(reader);
			httpPost.reset();
		}

	}

	@Override
	public void close() throws IOException
	{
		httpClient.getConnectionManager().shutdown();
	}
}
