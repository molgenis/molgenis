package org.molgenis.variome;

import org.springframework.stereotype.Service;

/**
 * Service implementation for the variome service
 * 
 * @author Mark-de-Haan
 * 
 */
@Service
public class VariomeServiceImpl implements VariomeService
{
	@Override
	public String getInputData(String input) {
		return input;
	}
}