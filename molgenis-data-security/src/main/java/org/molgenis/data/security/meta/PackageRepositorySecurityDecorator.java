package org.molgenis.data.security.meta;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.exception.NullParentPackageNotSuException;
import org.molgenis.data.security.exception.PackagePermissionDeniedException;
import org.molgenis.data.security.owned.AbstractRowLevelSecurityRepositoryDecorator.Action;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.PackagePermission.ADD_PACKAGE;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSuOrSystem;

public class PackageRepositorySecurityDecorator extends AbstractRepositoryDecorator<Package>
{
	private final MutableAclService mutableAclService;
	private final UserPermissionEvaluator userPermissionEvaluator;
	private final DataService dataService;

	public PackageRepositorySecurityDecorator(Repository<Package> delegateRepository,
			MutableAclService mutableAclService, UserPermissionEvaluator userPermissionEvaluator,
			DataService dataService)
	{
		super(delegateRepository);
		this.mutableAclService = requireNonNull(mutableAclService);
		this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public void update(Package pack)
	{
		checkParentPermission(pack, Action.UPDATE);
		updateAcl(pack);
		delegate().update(pack);
	}

	@Override
	public void update(Stream<Package> packages)
	{
		super.update(packages.filter(pack ->
		{
			checkParentPermission(pack, Action.UPDATE);
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
		checkParentPermission(pack, Action.CREATE);
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
			checkParentPermission(pack, Action.CREATE);
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

	private void checkParentPermission(Package newPackage, Action action)
	{
		Package parent = newPackage.getParent();
		if (parent != null)
		{
			boolean checkPackage = isParentUpdated(action, newPackage);
			if (checkPackage && !userPermissionEvaluator.hasPermission(new PackageIdentity(parent.getId()),
					ADD_PACKAGE))
			{
				throw new PackagePermissionDeniedException(ADD_PACKAGE, parent);
			}
		}
		else
		{
			if (!currentUserIsSuOrSystem() && isParentUpdated(action, newPackage))
			{
				throw new NullParentPackageNotSuException();
			}
		}
	}

	private boolean isParentUpdated(Action action, Package pack)
	{
		boolean updated;
		if (action == Action.CREATE)
		{
			updated = true;
		}
		else
		{
			Package currentpackage = dataService.findOneById(PackageMetadata.PACKAGE, pack.getId(), Package.class);
			if (currentpackage.getParent() == null)
			{
				updated = pack.getParent() != null;
			}
			else
			{
				updated = !currentpackage.getParent().equals(pack.getParent());
			}
		}
		return updated;
	}
}