package org.molgenis.beacon.controller.model.request;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import java.util.List;

/**
 * Query for information about a specific allele.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_BeaconAlleleRequest.class)
public abstract class BeaconAlleleRequest
{
	/**
	 * Reference name (chromosome).
	 * <p>
	 * Accepted values: 1-22, X, Y.
	 */
	public abstract String getReferenceName();

	/**
	 * Position, allele locus (0-based).
	 * <p>
	 * Accepted values: non-negative integers smaller than reference length.
	 */
	public abstract Long getStart();

	/**
	 * Reference bases for this variant (starting from `start`).
	 * <p>
	 * Accepted values: see the REF field in VCF 4.2 specification
	 * (https://samtools.github.io/hts-specs/VCFv4.2.pdf).
	 */
	public abstract String getReferenceBases();

	/**
	 * The bases that appear instead of the reference bases.
	 * <p>
	 * Accepted values: see the ALT field in VCF 4.2 specification
	 * (https://samtools.github.io/hts-specs/VCFv4.2.pdf).
	 */
	public abstract String getAlternateBases();

	/**
	 * Assembly identifier (GRC notation, e.g. `GRCh37`).
	 */
	public abstract String getAssemblyId();

	/**
	 * Identifiers of datasets, as defined in `BeaconDataset`.
	 * <p>
	 * If this field is null/not specified, all datasets should be queried.
	 */
	public abstract List<String> getDatasetIds();

	/**
	 * Indicator of whether responses for individual datasets
	 * (`datasetAlleleResponses`) should be included (not null) in the response
	 * (`BeaconAlleleResponse`) to this request.
	 * <p>
	 * If null (not specified), the default value of false is assumed.
	 */
	public abstract boolean getIncludeDatasetResponses();

	public static BeaconAlleleRequest create(String referenceName, Long start, String referenceBases,
			String alternateBases, String assemblyId, List<String> datasetIds, boolean includeDatasetResponses)
	{
		return new AutoValue_BeaconAlleleRequest(referenceName, start, referenceBases, alternateBases, assemblyId,
				datasetIds, includeDatasetResponses);
	}
}
