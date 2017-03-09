package org.molgenis.data.meta.persist;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.Package;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.data.meta.model.PackageMetadata.NAME;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;

@Component
public class PackagePersister
{
	private static final int BATCH_SIZE = 1000;

	private final DataService dataService;

	@Autowired
	public PackagePersister(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Transactional
	public void upsertPackages(Stream<Package> packages)
	{
		Iterator<List<Package>> partitions = Iterators.partition(packages.iterator(), BATCH_SIZE);
		partitions.forEachRemaining(packageBatch -> upsertPackages(packageBatch));
	}

	private void upsertPackages(Collection<Package> packages)
	{
		List<Package> packagesToAdd = new ArrayList<>(packages.size());
		List<Package> packagesToUpdate = new ArrayList<>(packages.size());

		Map<String, Package> packageMap = getCandidateExistingPackageMap(packages);
		packages.forEach(pack ->
		{
			Package existingPackage = packageMap.get(pack.getFullyQualifiedName());
			if (existingPackage != null)
			{
				pack.setId(existingPackage.getId()); // inject existing package identifier into package
				if (!pack.equals(existingPackage))
				{
					packagesToUpdate.add(pack);
				}
			}
			else
			{
				packagesToAdd.add(pack);
			}
		});

		if (!packagesToAdd.isEmpty())
		{
			dataService.add(PACKAGE, packagesToAdd.stream());
		}
		if (!packagesToUpdate.isEmpty())
		{
			dataService.update(PACKAGE, packagesToUpdate.stream());
		}
	}

	private Map<String, Package> getCandidateExistingPackageMap(Collection<Package> packages)
	{
		Set<String> packageNames = Sets.newHashSetWithExpectedSize(packages.size());
		packages.forEach(pack -> packageNames.add(pack.getName()));

		Query<Package> query = dataService.query(PACKAGE, Package.class).in(NAME, packageNames);
		return query.findAll().collect(toMap(Package::getFullyQualifiedName, identity()));
	}
}
