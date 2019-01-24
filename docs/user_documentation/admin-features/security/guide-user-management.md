# User management
Prerequisites: [configure mailsettings](../guide-mailsettings.md)

User management is crucial for keeping an overview of people visiting your online database, 
but it is also important for security reasons. MOLGENIS has an extensive user management system, 
allowing people to register themselves, or be registered by an administrator.  
You can find the User manager module under the Admin menu:

![Menu manager screen](../../../images/user_manager.png?raw=true, "user manager")

The user management menu allows you to create new users. 
But also lets you edit existing users. 
The users *admin* and *anonymous* are created for a new installation. 
The admin user, as the name suggests, is the administrator user. 
The anonymous user is used for people navigating to your website. 
This means that giving rights to the anonymous user will give permissions for everyone, 
also those that are not registered in your system. 
Note that it is not possible to delete users.
You can only set them to inactive, which will prevent them from logging in.

## Authentication

To configure how to authenticate in MOLGENIS you can read [configure authentication](guide-authentication.md).

## Permissions
 
You can also define more fine grained permissions to each user by setting the permission on users instead of 

## Try it out
To let you get a feel of how the user manager works, we will create a new user called molgenis_user.
First, click the ![New button](../../../images/new.png?raw=true, "new button") button. 
This will open up a form for creating a new User. 
Most of the fields are pretty self-explanatory, but there are a few that we will elaborate on. 

First, there is a distinction between required and non-required fields. Required fields are marked by an asterisk. 
You can decide to only show the required fields by pressing the eye icon at the top right. 
This will save you the trouble of scrolling past all the non-required fields.

For now, click the eye so it only shows the required fields. It should show:

*  Username: The user's login name
*  Password: The user's password
*  Active: This sets if the user account is active, meaning that the user can login
*  Superuser: A super user is a form of administrator, he or she has access to everything
*  Email: The users email
*  Change password: This sets whether the user has to change his or her password on the first login

Fill these fields with the following values:

*  Username: molgenis_user
*  Password: password
*  Active: Yes
*  Superuser: No
*  Email: molgenis_user@example.org
*  Change password: No

Then click create. 
You will now return to the start screen and you can see that the molgenis_user user has been added to the bottom of the table. 

**Edit a user**  
If at any time you want to edit an existing user, you can click the icon in the edit column. This will show you a form where you can edit all the information pertaining to that specific user.

Managing your users is quick and easy. And opens up way to our next big security item, setting permissions for your data.
