package org.molgenis.r;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.molgenis.script.core.ScriptException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

/**
 * Executes an R script using OpenCPU
 */
@Service
public class RScriptExecutor
{
	private final CloseableHttpClient httpClient;
	private final OpenCpuSettings openCpuSettings;

	public RScriptExecutor(CloseableHttpClient httpClient, OpenCpuSettings openCpuSettings)
	{
		this.httpClient = requireNonNull(httpClient);
		this.openCpuSettings = requireNonNull(openCpuSettings);
	}

	/**
	 * Execute R script and parse response:
	 * - write the response to outputPathname if outputPathname is not null
	 * - else return the response
	 *
	 * @param script         R script to execute
	 * @param outputPathname optional output pathname for output file
	 * @return response value or null in outputPathname is not null
	 */
	String executeScript(String script, String outputPathname)
	{
		// Workaround: script contains the absolute output pathname in case outputPathname is not null
		// Replace the absolute output pathname with a relative filename such that OpenCPU can handle the script.
		String scriptOutputFilename;
		if (outputPathname != null)
		{
			scriptOutputFilename = generateRandomString();
			script = script.replace(outputPathname, scriptOutputFilename);
		}
		else
		{
			scriptOutputFilename = null;
		}

		try
		{
			// execute script and use session key to retrieve script response
			String openCpuSessionKey = executeScriptExecuteRequest(script);
			return executeScriptGetResponseRequest(openCpuSessionKey, scriptOutputFilename, outputPathname);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Execute R script using OpenCPU
	 *
	 * @param rScript R script
	 * @return OpenCPU session key
	 * @throws IOException if error occured during script execution request
	 */
	private String executeScriptExecuteRequest(String rScript) throws IOException
	{
		URI uri = getScriptExecutionUri();
		HttpPost httpPost = new HttpPost(uri);
		NameValuePair nameValuePair = new BasicNameValuePair("x", rScript);
		httpPost.setEntity(new UrlEncodedFormEntity(singletonList(nameValuePair)));

		String openCpuSessionKey;
		try (CloseableHttpResponse response = httpClient.execute(httpPost))
		{
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode >= 200 && statusCode < 300)
			{
				Header openCpuSessionKeyHeader = response.getFirstHeader("X-ocpu-session");
				if (openCpuSessionKeyHeader == null)
				{
					throw new IOException("Missing 'X-ocpu-session' header");
				}
				openCpuSessionKey = openCpuSessionKeyHeader.getValue();
				EntityUtils.consume(response.getEntity());
			}
			else if (statusCode == 400)
			{
				HttpEntity entity = response.getEntity();
				String rErrorMessage = EntityUtils.toString(entity);
				EntityUtils.consume(entity);
				throw new ScriptException(rErrorMessage);
			}
			else
			{
				throw new ClientProtocolException(format("Unexpected response status: %d", statusCode));
			}
		}
		return openCpuSessionKey;
	}

	/**
	 * Retrieve R script response using OpenCPU
	 *
	 * @param openCpuSessionKey    OpenCPU session key
	 * @param scriptOutputFilename R script output filename (can be null)
	 * @param outputPathname       output pathname (can be null)
	 * @return response value or null if scriptOutputFilename is not null
	 * @throws IOException if error occured during script response retrieval
	 */
	private String executeScriptGetResponseRequest(String openCpuSessionKey, String scriptOutputFilename,
			String outputPathname) throws IOException
	{
		String responseValue;
		if (scriptOutputFilename != null)
		{
			executeScriptGetFileRequest(openCpuSessionKey, scriptOutputFilename, outputPathname);
			responseValue = null;
		}
		else
		{
			responseValue = executeScriptGetValueRequest(openCpuSessionKey);
		}
		return responseValue;
	}

	/**
	 * Retrieve R script file response using OpenCPU and write to file
	 *
	 * @param openCpuSessionKey    OpenCPU session key
	 * @param scriptOutputFilename R script output filename
	 * @param outputPathname       Output pathname
	 * @throws IOException if error occured during script response retrieval
	 */
	private void executeScriptGetFileRequest(String openCpuSessionKey, String scriptOutputFilename,
			String outputPathname) throws IOException
	{
		URI scriptGetValueResponseUri = getScriptGetFileResponseUri(openCpuSessionKey, scriptOutputFilename);
		HttpGet httpGet = new HttpGet(scriptGetValueResponseUri);
		try (CloseableHttpResponse response = httpClient.execute(httpGet))
		{
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode >= 200 && statusCode < 300)
			{
				HttpEntity entity = response.getEntity();
				Files.copy(entity.getContent(), Paths.get(outputPathname));
				EntityUtils.consume(entity);
			}
			else
			{
				throw new ClientProtocolException(format("Unexpected response status: %d", statusCode));
			}
		}
	}

	/**
	 * Retrieve and return R script STDOUT response using OpenCPU
	 *
	 * @param openCpuSessionKey OpenCPU session key
	 * @return R script STDOUT
	 * @throws IOException if error occured during script response retrieval
	 */
	private String executeScriptGetValueRequest(String openCpuSessionKey) throws IOException
	{
		URI scriptGetValueResponseUri = getScriptGetValueResponseUri(openCpuSessionKey);
		HttpGet httpGet = new HttpGet(scriptGetValueResponseUri);
		String responseValue;
		try (CloseableHttpResponse response = httpClient.execute(httpGet))
		{
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode >= 200 && statusCode < 300)
			{
				HttpEntity entity = response.getEntity();
				responseValue = EntityUtils.toString(entity);
				EntityUtils.consume(entity);
			}
			else
			{
				throw new ClientProtocolException(format("Unexpected response status: %d", statusCode));
			}
		}
		return responseValue;
	}

	private String generateRandomString()
	{
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	private URI getScriptExecutionUri()
	{
		try
		{
			return new URI(getOpenCpuUri() + "library/base/R/identity");
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

	private URI getScriptGetValueResponseUri(String openCpuSessionKey)
	{
		try
		{
			return new URI(getOpenCpuUri() + "tmp/" + openCpuSessionKey + "/stdout");
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

	private URI getScriptGetFileResponseUri(String openCpuSessionKey, String fileId)
	{
		try
		{
			return new URI(getOpenCpuUri() + "tmp/" + openCpuSessionKey + "/files/" + fileId);
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

	private URI getOpenCpuUri()
	{
		try
		{
			return new URIBuilder().setScheme(openCpuSettings.getScheme())
								   .setHost(openCpuSettings.getHost())
								   .setPort(openCpuSettings.getPort())
								   .setPath(openCpuSettings.getRootPath())
								   .build();
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}
}
