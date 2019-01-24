# Permission manager
For the scientific community, the need for data security is very large. 
We tried to meet this demand by implementing an extensive permission system. 
The system allows for the setting of count, read and write permission sets on the different datasets 
and modules present in MOLGENIS. These permissions can be set either for specific Users, or for Roles.

All users have the 'User' role, which gives them some basic permissions like permissions to view the home page and 
edit own account information.

You can navigate to the permission module under the Admin menu, and then navigating to the Permission Manager.

![Permission manager screen](../../../images/permission_manager.png?raw=true, "permission manager")

Here you can manage permissions for groups and users. Permissions can be set on datasets, packages and plugins. 
Permissions on a package also grant permissions on datasets within this package.
For datasets and packages 5 permission sets are available
-	None: the user has no permissions on the dataset at all.
-	Count: This means a user can see the counts and aggregates of the dataset, but not the values itself.
-	Read: This means the user can also view the values of the dataset but not edit anything.
-	Write: This means the user can also edit the values in the dataset.
-	WriteMeta: this means the user also has permission to edit the metadata of a dataset.

For plugins a user either has permission to view a plugin or they don’t.

## Try it out  
Remember that molgenis_user that we created in the user management section? 
If you go to the users tab and look for molgenis_user, you will find it does not have any permissions yet, 
except for those inherited from the User role.
Let's change it so that the User role grants the permission to open the Data explorer, 
and the molgenis_user will be able to see the example_data_table data set, which we created in the [upload-guide](../../import-data/guide-upload.md).

### Setting Role permissions  
As you open the permission manager, the Roles tab is already selected. 
For the Role *User* we want to set the permissions in such a way to the members of that Role can use the 
Data Explorer to look at data sets. 
To do this, select the User Role from the drop down. 
Next you will want to lookup data explorer in the Plugin column, and set the permission to *View*. 
Press the Save button which is below the table to save your change. 

To make it work perfectly, we will also have to give permissions to the User Role to read the data explorer settings table.
To do this, select the *Entity Class Permissions* menu, select the User Role from the drop down, and find the 
*settings_dataexplorer* in the Entity Class column and set it to *View*. 

### Setting User permissions  
Now that we have the Data Explorer module working for the test_group, we want to give our molgenis_user the permission 
to see the example_data_table. To do this, select Entity Class Permissions menu, switch to the Users tab, and select 
molgenis_user from the dropdown. Look up example_data_table in the Entity Class column and set the permission to *View*.

Congratulations! The molgenis_user account should now be able to use the data explorer, and see the example_data_table data set. 
You can verify this by logging out as admin, and logging in again as the molgenis_user.

Note that if you are creating more complex data sets that have references to other data sets, that you should also 
consider giving permissions to those reference tables.