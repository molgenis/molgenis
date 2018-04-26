package org.molgenis.core.ui.data.config;

import com.google.common.collect.Lists;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.molgenis.core.ui.util.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
@Import(GsonConfig.class)
public class HttpClientConfig
{
	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@Bean
	public CloseableHttpClient httpClient()
	{
		return HttpClients.createDefault();
	}

	@Bean
	public ClientHttpRequestFactory clientHttpRequestFactory()
	{
		return new HttpComponentsClientHttpRequestFactory();
	}

	@Bean
	public RestTemplate restTemplate()
	{
		final RestTemplate result = new RestTemplate(clientHttpRequestFactory());
		result.setMessageConverters(converters());
		return result;
	}

	private List<HttpMessageConverter<?>> converters()
	{
		List<HttpMessageConverter<?>> result = Lists.newArrayList();
		result.add(new ByteArrayHttpMessageConverter());
		result.add(new StringHttpMessageConverter());
		result.add(new ResourceHttpMessageConverter());
		result.add(new SourceHttpMessageConverter<>());
		result.add(new AllEncompassingFormHttpMessageConverter());
		result.add(new Jaxb2RootElementHttpMessageConverter());
		result.add(gsonHttpMessageConverter);
		return result;
	}
}
