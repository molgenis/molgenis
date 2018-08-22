# Groups and Roles
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
  
  Plugins are specific modules within MOLGENIS that offer certain functionality. For example the [Data Explorer](../../finding-data/guide-explore.md).

## Overview
You can start performing these steps to configure groups in MOLGENIS.

- **Step 1: Add a group**
  
  You can add groups to MOLGENIS to structure user groups and data you want to share or manage. Every group gets a folder to put resources in.
  
- **Step 2: Add members**
  
  When you have defined your group, you can add members to the group. Admins can also add new users, for more information see the [user management guide](guide-user-management.md).

- **Step 3: Add members to groups** 

  You can now determine who does what in your group. There are standard roles that you can use to enable members to edit, view or manage your group content.
  The available roles are:
  - **Manager**
    
    Can manage members from a group and all resources in a group (tables, scripts and files)
    
  - **Editor**
  
    Can edit all resources in a group (folders, tables)
    
  - **Viewer**
  
    Can view all resources in a group (folders, tables)

When you have setup this basic configuration you can start using MOLGENIS. 
      
## Adding groups
*note: you need to be superuser to add groups*

You can add groups in MOLGENIS by navigating to **Admin --> Security Manager**. 

![Security Manager menu](../../../images/security/main_menu_security.png?raw=true, "Security Manager menu")

Click on the **Add group**-button. 

![Groups screen](../../../images/security/group_overview_no_groups.png?raw=true, "Groups")

Fill out the necessary field and click on **Create**.

![Create a group](../../../images/security/group_creation.png?raw=true, "Create a group")

When you've added the group a folder is created and the default roles are created for that group. The user who creates the group becomes manager of the group.

## Adding members to a group
*note: you need to be superuser or group manager to add members to a group*

You can add members to groups via the security manager. Navigate to **Admin --> Security Manager**. Click on a group

![Group overview](../../../images/security/group_overview.png?raw=true, "Group overview")

Click on **Add member**.

![Membership overview](../../../images/security/membership_overview_only_admin.png?raw=true, "Membership overview")

Select a member from the **User-pulldown** and specify the **role**, for example: Editor. Click on **Add member**.

![Add membership](../../../images/security/add_membership.png?raw=true, "Add membership")

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
  * Plugins
    * Search all
    * Job overview
    
**Editor**:
* You can edit data in the group
* You get access to the following plugins:
  * Import Data
    * Advanced importer
  * Navigator
  * Data Explorer
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

![Membership overview](../../../images/security/membership_overview.png?raw=true, "Membership overview")

You can switch between roles whenever you want by updating the role of the member. Click on a member ("test" in this example).

![Update membership](../../../images/security/update_membership.png?raw=true, "Update membershio")

Click on "Edit" to update the role and click on "Update role".

![Update role membership](../../../images/security/update_membershiprole.png?raw=true, "Update rolemembershio")

You can now view the members of the group and see that the "test" member has a Manager-role.

![Membership overview with role change](../../../images/security/membership_overview_with_rolechange.png?raw=true, "Membership overview with role change")

You can also remove a member from a group. Click on a member ("test" in this example) and click on "Remove from group".

![Remove member from group](../../../images/security/remove_member_from_group.png?raw=true, "Remove member from group")

## Roles and inclusion
MOLGENIS comes with three default roles out of the box: Manager, Editor and Viewer. When you add a Group, the three group roles
that are added actually include (or inherit) these default roles. That's how you get all those permissions on plugins
listed above automatically. 

The default roles include each other as well. The Editor can see and do everything a Viewer can, and some more. The Manager 
can see and do everything the Editor can, and some more. System admins can change the permissions of these roles, so that 
certain plugins and functionality can be turned on or off across the system for all groups. Some examples:
1. As the admin, you don't want any user to use the Search All plugin. You achieve this by removing the Search All plugin permission
from the `VIEWER` role. 
2. As the admin, you only want Managers and Editors to use the Navigator. You achieve this by removing the Navigator plugin permission
from the `VIEWER` role, and giving the `EDITOR` role `VIEW` permission to the Navigator plugin.

*For more information on how to give permissions to roles, read up on the [Permission Manager](guide-permission-manager.md)*

The group roles include each other as well, in the same manner as the default roles. Your group's Manager can see and do 
everything your group's Editor can, etc. See the following schematic for an overview of how all the roles connect for a group 
that has been aptly named 'test':

![Role inclusions](../../../images/security/role_model.png?raw=true, "Overview of the roles and how they work together")

The group roles do not contain any permissions when you first create them; they only include the default roles. However, 
just like with any other role, you can still add permissions to them. This setup gives you fine grained control, especially
when your MOLGENIS hosts multiple groups. Some examples:

1. You want all the users in your group to see a special plugin that's not relevant for other groups. You achieve this by
giving the `TEST_VIEWER` role `VIEW` permission on that plugin.
2. You don't want all Editors in the system to see the Job Overview plugin. You achieve this by removing that plugin permission 
from the default Editor role and adding it to your group's Editor role. Now both the Editors and Managers in your group can use it, 
but from now on any new group that's added won't have these permission by default.

Even though you'll find that the default settings are sufficient most of the time, you have complete control over all the roles and
permissions in the system if you ever wish to change something.
