package org.molgenis.r;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class RScriptExecutorTest extends AbstractMockitoTest
{
	@Mock
	private CloseableHttpClient httpClient;
	@Mock
	private OpenCpuSettings openCpuSettings;

	private RScriptExecutor rScriptExecutor;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		when(openCpuSettings.getScheme()).thenReturn("http");
		when(openCpuSettings.getHost()).thenReturn("ocpu.molgenis.org");
		when(openCpuSettings.getPort()).thenReturn(80);
		when(openCpuSettings.getRootPath()).thenReturn("/ocpu/");
		rScriptExecutor = new RScriptExecutor(httpClient, openCpuSettings);
	}

	@Test
	public void testExecuteScriptValueOutput() throws IOException, URISyntaxException
	{
		CloseableHttpResponse executeScriptResponse = getExecuteScriptHttpResponse();
		CloseableHttpResponse getScriptResultResponse = getScriptResultHttpResponse();
		ArgumentCaptor<HttpUriRequest> requestsCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
		when(httpClient.execute(requestsCaptor.capture())).thenReturn(executeScriptResponse, getScriptResultResponse);

		String script = "script";
		String resultValue = rScriptExecutor.executeScript(script, null);
		assertEquals(resultValue, "value");

		List<HttpUriRequest> requests = requestsCaptor.getAllValues();
		assertEquals(requests.get(0).getURI(), new URI("http://ocpu.molgenis.org:80/ocpu/library/base/R/identity"));
		assertEquals(requests.get(1).getURI(), new URI("http://ocpu.molgenis.org:80/ocpu/tmp/sessionId/stdout"));
	}

	@Test
	public void testExecuteScriptFileOutput() throws IOException, URISyntaxException
	{
		CloseableHttpResponse executeScriptResponse = getExecuteScriptHttpResponse();
		CloseableHttpResponse getScriptResultResponse = getScriptResultHttpResponse();
		ArgumentCaptor<HttpUriRequest> requestsCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
		when(httpClient.execute(requestsCaptor.capture())).thenReturn(executeScriptResponse, getScriptResultResponse);

		String outputPath = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID()
																						.toString()
																						.replaceAll("-", "");
		boolean deleted;
		try
		{
			String script = "script";
			String resultValue = rScriptExecutor.executeScript(script, outputPath);
			assertNull(resultValue);
			assertEquals(Files.readAllBytes(new File(outputPath).toPath()), "value".getBytes(UTF_8));

			List<HttpUriRequest> requests = requestsCaptor.getAllValues();
			assertEquals(requests.get(0).getURI(), new URI("http://ocpu.molgenis.org:80/ocpu/library/base/R/identity"));
			assertTrue(requests.get(1)
							   .getURI()
							   .toString()
							   .matches("http://ocpu.molgenis.org:80/ocpu/tmp/sessionId/files/[a-z0-9]+"));
		}
		finally
		{
			deleted = new File(outputPath).delete();
		}
		if (!deleted)
		{
			throw new IOException(String.format("Cannot delete '%s'", outputPath));
		}
	}

	private CloseableHttpResponse getExecuteScriptHttpResponse()
	{
		CloseableHttpResponse executeScriptResponse = mock(CloseableHttpResponse.class);
		StatusLine statusLine = when(mock(StatusLine.class).getStatusCode()).thenReturn(201).getMock();
		when(executeScriptResponse.getStatusLine()).thenReturn(statusLine);
		Header header = when(mock(Header.class).getValue()).thenReturn("sessionId").getMock();
		when(executeScriptResponse.getFirstHeader("X-ocpu-session")).thenReturn(header);
		HttpEntity executeScriptEntity = mock(HttpEntity.class);
		when(executeScriptResponse.getEntity()).thenReturn(executeScriptEntity);
		return executeScriptResponse;
	}

	private CloseableHttpResponse getScriptResultHttpResponse() throws IOException
	{
		CloseableHttpResponse getScriptResultResponse = mock(CloseableHttpResponse.class);
		StatusLine statusLine = when(mock(StatusLine.class).getStatusCode()).thenReturn(200).getMock();
		when(getScriptResultResponse.getStatusLine()).thenReturn(statusLine);
		HttpEntity getScriptResultEntity = mock(HttpEntity.class);
		when(getScriptResultEntity.getContent()).thenReturn(new ByteArrayInputStream("value".getBytes(UTF_8)));
		when(getScriptResultResponse.getEntity()).thenReturn(getScriptResultEntity);
		return getScriptResultResponse;
	}
}