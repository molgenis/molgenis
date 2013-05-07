package org.molgenis.compute5.db.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

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
	private final String url;
	private final HttpClient httpClient = new DefaultHttpClient();

	public HttpClientComputeDbApiConnection(String url)
	{
		this.url = url;
	}

	@Override
	public <T extends ApiResponse> T doRequest(Object request, String uri, Class<T> returnType) throws ApiException
	{
		HttpPost httpPost = new HttpPost(url + uri);
		Reader reader = null;
		try
		{
			if (request != null)
			{
				String json = new Gson().toJson(request);
				httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
			}

			HttpResponse httpResponse = httpClient.execute(httpPost);

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
}
