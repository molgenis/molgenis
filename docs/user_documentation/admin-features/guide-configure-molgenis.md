**
When the system administrator has setup the global definition of groups and users you can now manage your own *group*. Assuming you are a group-manager in MOLGENIS.
**

# Configure MOLGENIS
When you are using MOLGENIS you have to understand some basic concepts.

**Structure:**
* **Groups**
  * **Members / Users**
    * **Roles**
      * Managers
      * Editors
      * Viewers
  
**Resources:**
* Folders
* Tables

**Plugins**
Plugins are specific modules within MOLGENIS that offer certain functionality. For example the [Data Explorer](../finding-data/guide-explore.md). 

## Basic setup
You can start performing these steps to configure MOLGENIS.

- **Step 1: Add a group**
  
  You can add groups to MOLGENIS to structure user groups and data you want to share or manage. Every group gets a folder to put resources in.
  
- **Step 2: Add members**
  
  When you have defined your group, you can add members to the group. Admins can also add new users, for more information see the [user management guide](guide-ref-user-management.md).

- **Step 3: Add members to groups** 

  You can now determine who does what in your group. There are standard roles that you can use to enable members to edit, view or manage your group content.
  The available roles are:
  - **Manager**
    
    Can manage members from a group and all resources in a group (tables, scripts and files)
    
  - **Editor**
  
    Can edit all resources in a group (folders, tables)
    
  - **Viewer**
  
    Can view all resources in a group (folders, tables)

When you have setup this basic configuration you can start using MOLGENIS. A detailed description on how to setup groups can be found [here](guide-ref-security.md).

