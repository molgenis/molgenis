# Setup security in MOLGENIS
To understand the security context of MOLGENIS you need to understand the following concepts.

* **Group**
  
  A number of people that work together.

* **User**
  
  A person that can login to MOLGENIS.

* **Roles**
  
  A role is a job function within the context of a group with certain permissions on resources. Examples of roles are:
  * Managers
  * Editors
  * Viewers
 
* **Resource**
  
  Resources can be tables or folders.

* **Plugin**
  
  Plugins are specific modules within MOLGENIS that offer certain functionality. For example the [Data Explorer](../finding-data/guide-explore.md).
      
## Add groups
*note: you need to be superuser to add groups*

You can add groups in MOLGENIS by navigating to **Admin --> Security Manager**. 

![Security Manager menu](../../images/security/main_menu_security.png?raw=true, "Security Manager menu")

Click on the **Add group**-button. 

![Groups screen](../../images/security/group_overview_no_groups.png?raw=true, "Groups")

Fill out the necessary field and click on **Create**.

![Create a group](../../images/security/group_creation.png?raw=true, "Create a group")

When you've added the group a folder is created and the default roles are created for that group. The user who creates the group becomes manager of the group.

## Add members to group
*note: you need to be superuser or group manager to add members to a group*

You can add members to groups via the security manager. Navigate to **Admin --> Security Manager**. Click on a group

![Group overview](../../images/security/group_overview.png?raw=true, "Group overview")

Click on **Add member**.

![Membership overview](../../images/security/membership_overview_only_admin.png?raw=true, "Membership overview")

Select a member from the **User-pulldown** and specify the **role**, for example: Editor. Click on **Add member**.

![Add membership](../../images/security/add_membership.png?raw=true, "Add membership")

When a member is added to a group, permissions are set according to the role he/she has been given.
The default permissions for these roles are as follows:

**Manager**:
* You can manage the memberships in the group
* You get access to the following plugins:
  * Import Data
    * Advanced importer
    * One click importer
  * Navigator
  * Data Explorer
  * Data Integration
    * Mapping service
    * SORTA
  * Plugins
    * Search all
    * Job overview
    * Questionnaires
    
**Editor**:
* You can edit data in the group
* You get access to the following plugins:
  * Import Data
    * Advanced importer
    * One click importer
  * Navigator
  * Data Explorer
  * Data Integration
    * Mapping service
    * SORTA
  * Plugins
    * Search all
    * Job overview
    * Questionnaires
    
**Viewer**:
* You can view data in the group
* You get access to the following plugins:
  * Navigator
  * Data Explorer
  * Plugins
    * Search all
    * Questionnaires

![Membership overview](../../images/security/membership_overview.png?raw=true, "Membershio overview")

You can switch between roles whenever you want by updating the role of the member. Click on a member ("test" in this example).

![Update membership](../../images/security/update_membership.png?raw=true, "Update membershio")

Click on "Edit" to update the role and click on "Update role".

![Update role membership](../../images/security/update_membershiprole.png?raw=true, "Update rolemembershio")

You can no view the members of the group and see that the "test"-member has a Manager-role.

![Membership overview with role change](../../images/security/membership_overview_with_rolechange.png?raw=true, "Membership overview with role change")

You can also remove a member from a group. Click on a member ("test" in this example) and click on "Remove from group".

![Remove member from group](../../images/security/remove_member_from_group.png?raw=true, "Remove member from group")