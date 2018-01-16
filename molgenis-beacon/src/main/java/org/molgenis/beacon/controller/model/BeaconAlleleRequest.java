package org.molgenis.beacon.controller.model;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;

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

	public static BeaconAlleleRequest create(String referenceName, Long start, String referenceBases,
			String alternateBases)
	{
		return new AutoValue_BeaconAlleleRequest(referenceName, start, referenceBases, alternateBases);
	}
}
