# Setup security in MOLGENIS
To understand the security context of MOLGENIS you need to understand the follwoing concepts.

**Groups**
A number of people that work together.

**Users**
Is a person that can login to MOLGENIS.

**Roles**
A role is a job function within the context of an group with certain permissions on resources. Examples of roles are:
* Managers
* Editors
* Viewers
 
**Resource**
A resource can be tables or folders.

**Plugins**
You can give rights to a plugins so a user can use the plugin.
      
## Add groups
*note: you need to be superuser*

You can add groups in MOLGENIS by navigating to **Admin --> Security Manager**. 

![Security Manager menu](../../images/security/main_menu_security.png?raw=true, "Security Manager menu")

Click on the **Add group**-button. 

![Groups screen](../../images/security/groups.png?raw=true, "Groups")

Fill out the necessary field and click on **Create**.

![Create a group](../../images/security/group_creation.png?raw=true, "Create a group")

When you added a group a folder is created and the default roles are created for that group. The user which creates the group becomes manager of the group.

## Add members to group
*note: you need to be superuser or group manager*

You can add members to groups via the security-manager. Navigate to **Admin --> Security Manager**. Click on a group

![Group overview](../../images/security/group_overview.png?raw=true, "Group overview")

Click on **Add member**.

![Membership overview](../../images/security/membership_overview.png?raw=true, "Membership overview")

Select a member from the **User-pulldown** and specify the **role**, for example: Editor. Click on **Add member**.

![Add membership](../../images/security/add_membership.png?raw=true, "Add membership")

When a member is added to a group permissions are set according to the role he/her had been given.
The default settings for membership to a group is:

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
   