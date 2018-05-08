package org.molgenis.core.ui.data.config;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withCreatedEntity;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { HttpClientConfig.class })
public class HttpClientConfigTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	RestTemplate restTemplate;
	MockRestServiceServer server;
	TestNegotiatorQuery testNegotiatorQuery = TestNegotiatorQuery.createQuery("url", "ntoken");

	@BeforeClass
	public void beforeClass()
	{
		server = MockRestServiceServer.bindTo(restTemplate).build();
	}

	@Test
	public void testGsonSerialization()
	{
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Basic ABCDE");

		HttpEntity<TestNegotiatorQuery> entity = new HttpEntity<>(testNegotiatorQuery, headers);
		server.expect(once(), requestTo("http://directory.url/request"))
			  .andExpect(method(HttpMethod.POST))
			  .andExpect(content().string("{\"URL\":\"url\",\"nToken\":\"ntoken\"}"))
			  .andExpect(MockRestRequestMatchers.header("Authorization", "Basic ABCDE"))
			  .andRespond(withCreatedEntity(URI.create("http://directory.url/request/DEF")));

		String redirectURL = restTemplate.postForLocation("http://directory.url/request", entity).toASCIIString();
		assertEquals(redirectURL, "http://directory.url/request/DEF");

		// Verify all expectations met
		server.verify();
	}

	@AutoValue
	@AutoGson(autoValueClass = AutoValue_HttpClientConfigTest_TestNegotiatorQuery.class)
	public abstract static class TestNegotiatorQuery
	{
		public abstract String getURL();

		public abstract String getnToken();

		public static TestNegotiatorQuery createQuery(String url, String nToken)
		{
			return new AutoValue_HttpClientConfigTest_TestNegotiatorQuery(url, nToken);
		}
	}
}
