package org.molgenis.data.rest.service;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/*
 * Wrapper around static spring framework builder to make it injectable
 */
@Component
public class ServletUriComponentsBuilderFactory
{
	public ServletUriComponentsBuilder fromCurrentRequest()
	{
		return ServletUriComponentsBuilder.fromCurrentRequest();
	}
}
