package org.molgenis.data.security.meta;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.PackageIdentity;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class PackageRepositorySecurityDecorator extends AbstractRepositoryDecorator<Package>
{
	private final MutableAclService mutableAclService;

	public PackageRepositorySecurityDecorator(Repository<Package> delegateRepository,
			MutableAclService mutableAclService)
	{
		super(delegateRepository);
		this.mutableAclService = requireNonNull(mutableAclService);
	}

	@Override
	public void update(Package pack)
	{
		updateAcl(pack);
		delegate().update(pack);
	}

	@Override
	public void update(Stream<Package> packages)
	{
		super.update(packages.filter(pack ->
		{
			updateAcl(pack);
			return true;
		}));
	}

	@Override
	public void delete(Package pack)
	{
		deleteAcl(pack);
		delegate().delete(pack);
	}

	@Override
	public void delete(Stream<Package> packages)
	{
		delegate().delete(packages.filter(pack ->
		{
			deleteAcl(pack);
			return true;
		}));
	}

	@Override
	public void deleteById(Object id)
	{
		deleteAcl(id.toString());
		delegate().deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		super.deleteAll(ids.filter(id ->
		{
			deleteAcl(id.toString());
			return true;
		}));
	}

	@Override
	public void deleteAll()
	{
		iterator().forEachRemaining(this::deleteAcl);
		super.deleteAll();
	}

	@Override
	public void add(Package pack)
	{
		createAcl(pack);
		delegate().add(pack);
	}

	@Override
	public Integer add(Stream<Package> packages)
	{
		LinkedList<Package> resolved = new LinkedList<>();
		resolveDependencies(packages.collect(Collectors.toList()), resolved);
		return super.add(resolved.stream().filter(pack ->
		{
			createAcl(pack);
			return true;
		}));
	}

	private void resolveDependencies(List<Package> packages, LinkedList<Package> resolved)
	{
		if (packages.size() != resolved.size())
		{
			for (Package pack : packages)
			{
				if (!resolved.contains(pack) && (!packages.contains(pack.getParent()) || resolved.contains(
						pack.getParent())))
					{
						resolved.add(pack);
					}
			}
			resolveDependencies(packages, resolved);
		}
	}

	private void createAcl(Package pack)
	{
		PackageIdentity packageIdentity = new PackageIdentity(pack);
		MutableAcl acl = mutableAclService.createAcl(packageIdentity);
		if (pack.getParent() != null)
		{
			ObjectIdentity parentIdentity = new PackageIdentity(pack.getParent());
			Acl parentAcl = mutableAclService.readAclById(parentIdentity);
			acl.setParent(parentAcl);
			mutableAclService.updateAcl(acl);
		}
	}

	private void deleteAcl(String id)
	{
		PackageIdentity packageIdentity = new PackageIdentity(id);
		mutableAclService.deleteAcl(packageIdentity, true);
	}

	private void deleteAcl(Package pack)
	{
		deleteAcl(pack.getId());
	}

	private void updateAcl(Package pack)
	{
		PackageIdentity packageIdentity = new PackageIdentity(pack);
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(packageIdentity);
		if (pack.getParent() != null)
		{
			ObjectIdentity parentIdentity = new PackageIdentity(pack.getParent());
			Acl parentAcl = mutableAclService.readAclById(parentIdentity);
			if (!parentAcl.equals(acl.getParentAcl()))
			{
				acl.setParent(parentAcl);
				mutableAclService.updateAcl(acl);
			}
		}
	}
}