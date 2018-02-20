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
	private MutableAclService mutableAclService;

	public PackageRepositorySecurityDecorator(Repository<Package> delegateRepository,
			MutableAclService mutableAclService)
	{
		super(delegateRepository);
		this.mutableAclService = requireNonNull(mutableAclService);
	}

	@Override
	public void update(Package package_)
	{
		updateAcl(package_);
		delegate().update(package_);
	}

	@Override
	public void update(Stream<Package> packages)
	{
		super.update(packages.filter(package_ ->
		{
			updateAcl(package_);
			return true;
		}));
	}

	@Override
	public void delete(Package package_)
	{
		deleteAcl(package_);
		delegate().delete(package_);
	}

	@Override
	public void delete(Stream<Package> packages)
	{
		packages.forEach(package_ ->
		{
			deleteAcl(package_);
		});
		delegate().delete(packages);
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
		iterator().forEachRemaining(package_ ->
		{
			deleteAcl(package_);
		});
		super.deleteAll();
	}

	@Override
	public void add(Package package_)
	{
		createAcl(package_);
		delegate().add(package_);
	}

	@Override
	public Integer add(Stream<Package> packages)
	{
		LinkedList<Package> resolved = new LinkedList();
		resolveDependencies(packages.collect(Collectors.toList()), resolved);
		return super.add(resolved.stream().filter(package_ ->
		{
			createAcl(package_);
			return true;
		}));
	}

	private void resolveDependencies(List<Package> packages, LinkedList<Package> resolved)
	{
		if (packages.size() != resolved.size())
		{
			for (Package pack : packages)
			{
				if (!resolved.contains(pack))
				{
					if (!packages.contains(pack.getParent()) || resolved.contains(pack.getParent()))
					{
						resolved.add(pack);
					}
				}
			}
			resolveDependencies(packages, resolved);
		}
	}

	private MutableAcl createAcl(Package package_)
	{
		PackageIdentity packageIdentity = new PackageIdentity(package_);
		MutableAcl acl = mutableAclService.createAcl(packageIdentity);
		if (package_.getParent() != null)
		{
			ObjectIdentity parentIdentity = new PackageIdentity(package_.getParent());
			Acl parentAcl = mutableAclService.readAclById(parentIdentity);
			acl.setParent(parentAcl);
			mutableAclService.updateAcl(acl);
		}
		return acl;
	}

	private void deleteAcl(String id)
	{
		PackageIdentity packageIdentity = new PackageIdentity(id);
		mutableAclService.deleteAcl(packageIdentity, true);
	}

	private void deleteAcl(Package package_)
	{
		deleteAcl(package_.getId());
	}

	private MutableAcl updateAcl(Package package_)
	{
		PackageIdentity packageIdentity = new PackageIdentity(package_);
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(packageIdentity);
		if (package_.getParent() != null)
		{
			ObjectIdentity parentIdentity = new PackageIdentity(package_.getParent());
			Acl parentAcl = mutableAclService.readAclById(parentIdentity);
			if (!parentAcl.equals(acl.getParentAcl()))
			{
				acl.setParent(parentAcl);
				mutableAclService.updateAcl(acl);
			}
		}
		return acl;
	}
}