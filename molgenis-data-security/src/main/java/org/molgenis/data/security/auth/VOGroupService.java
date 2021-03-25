package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.security.auth.VOGroupMetadata.NAME;
import static org.molgenis.data.security.auth.VOGroupMetadata.VO_GROUP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.stereotype.Service;

@Service
public class VOGroupService {
  private final DataService dataService;
  private final VOGroupFactory voGroupFactory;

  public VOGroupService(DataService dataService, VOGroupFactory voGroupFactory) {
    this.dataService = requireNonNull(dataService);
    this.voGroupFactory = requireNonNull(voGroupFactory);
  }

  /**
   * Gets a VO group by name
   *
   * @param groupName group name
   * @return VOGroup with group name
   * @throws UnknownEntityException if no VO group with that name exists
   */
  @RunAsSystem
  public VOGroup getGroup(String groupName) {
    var result = dataService.query(VO_GROUP, VOGroup.class).eq(NAME, groupName).findOne();
    if (result == null) {
      throw new UnknownEntityException(
          voGroupFactory.getEntityType(),
          voGroupFactory.getEntityType().getAttribute(RoleMetadata.NAME),
          groupName);
    }
    return result;
  }

  /**
   * Gets a VO group by name
   *
   * @param id VO group id
   * @return VOGroup with group name
   * @throws UnknownEntityException if no VO group with that name exists
   */
  @RunAsSystem
  public VOGroup getGroupByID(String id) {
    var result = dataService.findOneById(VO_GROUP, id, VOGroup.class);
    if (result == null) {
      throw new UnknownEntityException(voGroupFactory.getEntityType(), id);
    }
    return result;
  }

  /**
   * Creates a VO group
   *
   * @param groupName group name
   * @return VOGroups with group name
   */
  @RunAsSystem
  public VOGroup createGroup(String groupName) {
    VOGroup result = voGroupFactory.withName(groupName);
    dataService.add(VO_GROUP, result);
    return result;
  }

  /** Gets all VO groups. */
  @RunAsSystem
  public Collection<VOGroup> getGroups() {
    return dataService.findAll(VO_GROUP, VOGroup.class).collect(Collectors.toList());
  }

  /**
   * Gets the VO group entities by their unique name, creating the ones not present.
   *
   * @param groupNames unique group names
   * @return VOGroups with given group names
   */
  @RunAsSystem
  public Collection<VOGroup> getGroups(Set<String> groupNames) {
    if (groupNames.isEmpty()) {
      return Collections.emptyList();
    }
    Collection<VOGroup> result =
        dataService
            .query(VO_GROUP, VOGroup.class)
            .in(NAME, groupNames)
            .findAll()
            .collect(toCollection(ArrayList::new));
    var foundNames = result.stream().map(VOGroup::getName).collect(toSet());
    var newlyCreated =
        groupNames.stream()
            .filter(name -> !foundNames.contains(name))
            .map(voGroupFactory::withName)
            .collect(toList());
    dataService.add(VO_GROUP, newlyCreated.stream());
    result.addAll(newlyCreated);
    return result;
  }
}
