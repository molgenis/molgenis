**
Once you have a server running and data loaded, you are probably eager to share your data with the world. However, you might want to only show your data to a select few. In the following paragraphs we explain how to define groups and permissions.
**

# Mail settings
You should configure an email server to interact with your users for things like lost password recovery.
You can find the mail settings in the Admin menu, under Settings.
At the top of the page, type "Mail settings" into the selection box.

For backwards compatability, the default settings are filled with the values provided in `molgenis-server.properties` 
under the keys `mail.username` and `mail.password`, but the values provided here will override those settings.

For basic configuration, you only need to provide the username and password fields with a valid Gmail username and password.
But you may also specify a different (non-Gmail) SMTP server.

If you've filled in a username and password, the settings will be validated when you save them, by making a connection
with the mail server. If you do not want the settings to be tested at all, you can set `testConnection` to false.

### JavaMail properties
By default, the following low-level JavaMail properties, needed to interact with the Gmail SMTP server, are set:
```
mail.smtp.starttls.enable=true
mail.smtp.quitwait=false
mail.smtp.auth=true
```
You may override these properties or add additional properties and override these defaults by adding entities to the 
```JavaMailProperty``` repository in the Data Explorer. Each key may be provided at most once.
For a list of valid keys, check https://javamail.java.net/nonav/docs/api/

> E.g. Add an entity with key ```mail.debug``` and value ```true``` if you'd like to debug the mail dialog with the server.

# User management
User management is crucial for keeping an overview of people visiting your online database, but it is also important for security reasons. MOLGENIS has an extensive user management system, allowing people to register themselves, or be registered by an administrator. MOLGENIS uses groups and users to efficiently control permissions. Groups and users can both have individual permissions on certain data sets for example. But users can also be part of a group, automatically inheriting the permissions set for that group. You can find the User manager module under the Admin menu:

![Menu manager screen](../images//user_manager.png?raw=true, "user manager")

The user management menu allows you to create new users and groups. But also lets you edit existing users, or add users to certain groups. The users *admin* and *anonymous* always exist. The admin user, as the name suggests, is the administrator user. The anonymous user is used for people navigating to your website. This means that giving rights to the anonymous user will give permissions for everyone, also those that are not registered in your system. Note that it is not possible to delete users! You can only set them to inactive, which will prevent him or her from logging in.

## Try it out
To let you get a feel of how the user manager works, we will create a new user called molgenis_user. First, click the ![New button](../images/new.png?raw=true, "new button") button. This will open up a form for creating a new MolgenisUser. Most of the fields are pretty self-explanatory, but there are a few that we will elaborate on. 

First, there is a distinction between required and non-required fields. Required fields are marked by an asterisk. You can decide to only show the required fields by pressing the eye icon at the top right. This will save you the trouble of scrolling past all the non-required fields.

For now, click the eye so it only shows the required fields. It should show:

*  Username: The users login name
*  Password: The users password
*  Active: This sets if the user account is active, meaning that the user can login
*  Superuser: A super user is a form of administrator, he or she has access to everything
*  Email: The users email
*  Change password: This sets whether the user has to change his or her password on the first login

Fill these fields with the following:

*  Username: molgenis_user
*  Password: password
*  Active: Yes
*  Superuser: No
*  Email: molgenis_user@email.org
*  Change password: No

Then click create. You will now return to the start screen and you can see that the molgenis_user user has been added to the bottom of the table. It has been automatically added to the 'All users' group, which will give the newly created user some basic permissions like the home page and its own account.

But we want more than just a user, we want to create a group as well. In the long run, setting permissions for groups is a lot less time consuming then treating every user individually. 

**Creating groups**  
At the top of the screen, select the *Groups* tab. Here, you will there is only one group, the All users group. Click the ![New button](../images//new.png?raw=true, "new button") button to create a new group.

In the popup, set the Name to test_group, and set the group to Active. Click create and you will see the newly created group being added to the bottom of the table.

Go back to the users tab, and you can see there is now a column behind every user, that allows you to place that user in the test_group group. Why don't we add the molgenis_user to this test_group.

**Edit a user**  
If at any time you want to edit an existing user, you can click the icon in the edit column. This will show you a form where you can edit all the information pertaining to that specific user.

Managing your users is quick and easy. And opens up way to our next big security item, setting permissions for your data.

# Authentication
When you are a superuse in MOLGENIS, you can configure three authentication methods: 
 * Username/password sigin *(default)*
 * Google-sigin
 * Token-authentication
 
**Username/password sigin (default)**  
In the App Settings:
 * Set user-moderation to user can sign up
 * Set two factor authentication: 
   * Disabled: users CAN NOT use two-factor-authentication
   * Enforced: users MUST use two-factor-authentication
   * Enabled: users CAN use two-factor-authentication
   
**prompted:** The default setting for tow-factor-authentication for a fresh installation *"Disabled"*. When two-factor-authentication is *"Enabled"* it is default disabled for the users.  

**Google-sigin**  
The Google-sigin setting is used to enable the possibility for users to login with their Google-account. 
 
**Token-authentication**
No specific superuser settings are necessary for the implementation of token-authentication.
 
 
# Permissions
For the scientific community, the need for data security is very large. We tried to meet this demand by implementing an extensive permission system. The system allows for the setting of count, read and write permissions on the different datasets 
and modules present in MOLGENIS. These permissions can be set either for specific users, or entire user groups.

You can navigate to the permission module under the Admin menu, and then navigating to the Permission Manager.

![Menu manager screen](../images//permission_manager.png?raw=true, "permission manager")

Here you can manage permissions for groups and users. Permissions can be set on datasets, packages and plugins. Permission on a package are permissions on datasets within this package, not permissions on the package itself.
For datasets and packages 5 categories of permissions are available
-	None: the user has no permissions on the dataset at all.
-	Count: This means a user can see the counts and aggregates of the dataset, but not the values itself.
-	Read: This means the user can view the values of the dataset but not edit anything. The user also inherits count permissions.
-	Write: This means the user can edit the values in the dataset. The user also inherits count and read permissions.
-	WriteMeta: this means the user has permission to edit the metadata of a dataset. The user also inherits count, read and write permissions.

For plugins a user either has permission to view a plugin or they don’t.

**Try it out**  
Remember that molgenis_user that we created in the user management section? If you go to the users tab and look for molgenis_user, you will find it does not have any permissions yet, except for those inherited from the All users group. Let's change it so that our test_group has the permission to open the Data explorer, and the molgenis_user will be able to see the example_data_table data set, which we created in the [Upload](guide-upload) guide.

**Setting group permissions**  
As you open the permission manager, the groups tab is already selected. For the group *test_group* we want to set the permissions in such a way to the members of that group can use the data explorer to look at data sets. To do this, select the test_group from the drop down. Next you will want to lookup data explorer in the Plugin column, and set the permission to *View*. Press the Save button which is below the table to save your change. 

To make it work perfectly, we will also have to give rights to the group to read the data explorer settings table. To do this, select the *Entity Class Permissions* menu, select the test_group from the drop down, and find the *settings_dataexplorer* in the Entity Class column and set it to *View*. 

**Setting user permissions**  
Now that we have the Data Explorer module working for the test_group, we want to give our molgenis_user the permission to see the example_data_table. To do this, select Entity Class Permissions menu, switch to the Users tab, and select molgenis_user from the dropdown. Look up example_data_table in the Entity Class column and set the permission to *View*.

Congratulations! The molgenis_user account should now be able to use the data explorer, and see the example_data_table data set. You can verify this by logging out as admin, and logging in again as the molgenis_user.

Note that if you are creating more complex data sets that have references to other data sets, that you should also consider giving permissions to those reference tables.

And if you are wondering about the Entity permissions menu, that is reserved for when we implement row level security.

