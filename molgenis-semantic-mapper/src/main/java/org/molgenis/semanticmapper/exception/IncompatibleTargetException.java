package org.molgenis.semanticmapper.exception;

/**
 * Throw if during the comparison of mapping target EntityTypes the metadata is not the same.
 */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public abstract class IncompatibleTargetException extends MappingServiceException
{
	IncompatibleTargetException(String errorCode)
	{
		super(errorCode);
	}
}
