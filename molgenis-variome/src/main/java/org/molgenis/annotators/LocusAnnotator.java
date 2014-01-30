package org.molgenis.annotators;

import org.molgenis.data.RepositoryAnnotator;

/**
 * 
 * This type repository interface expects a genomic location to be present in the
 * data set. This consists of atleast a chromosome position, genome build 
 * (and possibly organism code??) and other (example: http://genome.ucsc.edu/cgi-bin/hgVai)
 * 
 * */
public interface LocusAnnotator extends RepositoryAnnotator{
	
}
