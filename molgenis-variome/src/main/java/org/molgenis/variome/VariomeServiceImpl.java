package org.molgenis.variome;

import java.io.File;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

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
	public File vcfFile(File vcfFile, Model model) {
		// TODO Parse vcf file into list for html table
		
		model.addAttribute("parsed", vcfFile);
		
		return null;
	}
}