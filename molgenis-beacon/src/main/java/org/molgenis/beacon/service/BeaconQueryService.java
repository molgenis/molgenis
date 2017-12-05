package org.molgenis.beacon.service;

import org.molgenis.beacon.controller.model.BeaconAlleleRequest;
import org.molgenis.beacon.controller.model.BeaconAlleleResponse;
import org.molgenis.data.meta.model.EntityType;

public interface BeaconQueryService
{
	/**
	 * Query a beacon with a {@link BeaconAlleleRequest}
	 * A beacon searches through the {@link EntityType}s that it references
	 *
	 * @return A {@link BeaconAlleleResponse}
	 */
	BeaconAlleleResponse query(String beaconId, BeaconAlleleRequest request);

	/**
	 * Query a BeaconResponse with chromosome, start position, reference allele and alternative allele.
	 * A beacon searches through the {@link EntityType}s that it references
	 *
	 * @return A {@link BeaconAlleleResponse}
	 */
	BeaconAlleleResponse query(String referenceName, Long start, String referenceBases, String alternateBases,
			String beaconId);
}
