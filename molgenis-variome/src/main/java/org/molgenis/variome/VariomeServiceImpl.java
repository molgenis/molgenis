package org.molgenis.variome;

import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for the variome service
 * 
 * @author Mark-de-Haan
 * 
 */

@Service
public class VariomeServiceImpl implements VariomeService
{
	@Autowired
	DataService dataService;
	
	@Override
	@Transactional
	public void dataSetAnnotater() {
		
	}
}