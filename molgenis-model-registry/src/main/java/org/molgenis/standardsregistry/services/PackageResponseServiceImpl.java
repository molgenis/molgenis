package org.molgenis.standardsregistry.services;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataSearchService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.standardsregistry.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 *
 * <p></p>
 *
 * @author sido
 */
@Component
public class PackageResponseServiceImpl implements PackageResponseService {

    private final MetaDataService metaDataService;
    private final TagService<LabeledResource, LabeledResource> tagService;
    private final MetaDataSearchService metaDataSearchService;
    private final MolgenisPermissionService molgenisPermissionService;
    private final DataService dataService;

    @Autowired
    public PackageResponseServiceImpl(MetaDataService metaDataService, TagService<LabeledResource, LabeledResource> tagService, MetaDataSearchService metaDataSearchService, MolgenisPermissionService molgenisPermissionService, DataService dataService)
    {
        this.metaDataService = Objects.requireNonNull(metaDataService);
        this.tagService = Objects.requireNonNull(tagService);
        this.metaDataSearchService = Objects.requireNonNull(metaDataSearchService);
        this.molgenisPermissionService = Objects.requireNonNull(molgenisPermissionService);
        this.dataService = Objects.requireNonNull(dataService);
    }

    @Override
    public PackageSearchResponse search(PackageSearchRequest packageSearchRequest)
    {
        String searchQuery = packageSearchRequest.getQuery();
        List<PackageResponse> packageResponses = Lists.newArrayList();

        List<PackageSearchResultItem> searchResults = metaDataSearchService.findRootPackages(searchQuery);
        for (PackageSearchResultItem searchResult : searchResults)
        {
            Package p = searchResult.getPackageFound();
            List<StandardRegistryEntity> entitiesInPackageUnfiltered = getEntitiesInPackage(p.getId());
            List<StandardRegistryEntity> entitiesInPackageFiltered = Lists.newArrayList(Iterables.filter(entitiesInPackageUnfiltered, new Predicate<StandardRegistryEntity>()
            {
                @Override
                public boolean apply(StandardRegistryEntity entity)
                {
                    if (entity.isAbtract()) return false;

                    String entityTypeId = entity.getName();

                    // Check read permission
                    if (!molgenisPermissionService.hasPermissionOnEntity(entityTypeId, Permission.READ))
                        return false;

                    // Check has data
                    if (!dataService.hasRepository(entityTypeId)
                            || dataService.count(entityTypeId, new QueryImpl<>()) == 0) return false;

                    return true;
                }
            }));

            PackageResponse pr = new PackageResponse(p.getId(), p.getLabel(), p.getDescription(),
                    searchResult.getMatchDescription(), entitiesInPackageFiltered, getTagsForPackage(p));
            packageResponses.add(pr);
        }

        int total = packageResponses.size();
        if (total > 0)
        {
            if (packageSearchRequest.getOffset() != null)
            {
                packageResponses = packageResponses.subList(packageSearchRequest.getOffset(), packageResponses.size());
            }

            if (packageSearchRequest.getNum() != null && packageResponses.size() > packageSearchRequest.getNum())
            {
                packageResponses = packageResponses.subList(0, packageSearchRequest.getNum());
            }
        }

        int offset = packageSearchRequest.getOffset() != null ? packageSearchRequest.getOffset() : 0;
        int num = packageSearchRequest.getNum() != null ? packageSearchRequest.getNum() : packageResponses.size();

        PackageSearchResponse packageSearchResponse = new PackageSearchResponse(searchQuery, offset, num, total,
                packageResponses);

        return packageSearchResponse;
    }

    @Override
    public List<StandardRegistryTag> getTagsForPackage(Package p)
    {
        List<StandardRegistryTag> tags = Lists.newArrayList();

        for (SemanticTag<Package, LabeledResource, LabeledResource> tag : tagService.getTagsForPackage(p))
        {
            tags.add(new StandardRegistryTag(tag.getObject().getLabel(), tag.getObject().getIri(),
                    tag.getRelation().toString()));
        }

        return tags;
    }

    @Override
    public List<StandardRegistryEntity> getEntitiesInPackage(String packageName)
    {
        List<StandardRegistryEntity> entiesForThisPackage = new ArrayList<StandardRegistryEntity>();
        Package aPackage = metaDataService.getPackage(packageName);
        getEntitiesInPackageRec(aPackage, entiesForThisPackage);
        return entiesForThisPackage;
    }

    private void getEntitiesInPackageRec(Package aPackage, List<StandardRegistryEntity> entiesForThisPackage)
    {
        for (EntityType emd : aPackage.getEntityTypes())
        {
            entiesForThisPackage.add(new StandardRegistryEntity(emd.getId(), emd.getLabel(), emd.isAbstract()));
        }
        Iterable<Package> subPackages = aPackage.getChildren();
        if (subPackages != null)
        {
            for (Package subPackage : subPackages)
            {
                getEntitiesInPackageRec(subPackage, entiesForThisPackage);
            }
        }
    }

    @Override
    public PackageTreeNode createPackageTreeNode(Package package_)
    {
        String title = package_.getLabel() != null ? package_.getLabel() : package_.getId();
        String key = package_.getId();
        String tooltip = package_.getDescription();
        List<PackageTreeNode> result = new ArrayList<PackageTreeNode>();
        boolean folder = true;
        boolean expanded = true;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("type", "package");

        for (Package subPackage : package_.getChildren())
        {
            result.add(createPackageTreeNode(subPackage));
        }

        for (EntityType emd : package_.getEntityTypes())
        {
            result.add(createPackageTreeNode(emd));
        }

        return new PackageTreeNode("package", title, key, tooltip, folder, expanded, data, result);
    }

    private PackageTreeNode createPackageTreeNode(EntityType emd)
    {
        String title = emd.getLabel();
        String key = emd.getId();
        String tooltip = emd.getDescription();
        List<PackageTreeNode> result = new ArrayList<PackageTreeNode>();
        boolean folder = true;
        boolean expanded = false;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("type", "entity");
        data.put("href", "/api/v1/" + emd.getId() + "/meta");

        for (Attribute amd : emd.getAttributes())
        {
            result.add(createPackageTreeNode(amd, emd));
        }

        return new PackageTreeNode("entity", title, key, tooltip, folder, expanded, data, result);
    }

    private PackageTreeNode createPackageTreeNode(Attribute amd, EntityType emd)
    {
        String title = amd.getLabel();
        String key = amd.getName();
        String tooltip = amd.getDescription();
        List<PackageTreeNode> result = new ArrayList<PackageTreeNode>();
        boolean folder;
        boolean expanded = false;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("type", "attribute");
        data.put("href", "/api/v1/" + emd.getId() + "/meta/" + amd.getName());
        data.put("tags", tagService.getTagsForAttribute(emd, amd));

        if (amd.getDataType() == AttributeType.COMPOUND)
        {
            for (Attribute subAmd : amd.getChildren())
            {
                result.add(createPackageTreeNode(subAmd, emd));
            }
            folder = true;
        }
        else
        {
            folder = false;
        }

        return new PackageTreeNode("attribute", title, key, tooltip, folder, expanded, data, result);
    }


}
