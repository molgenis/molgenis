# User authentication in MOLGENIS
In MOLGENIS you can sign in using two methods. 
 * Default authentication
 * Google Authentication

## Default-authentication
The default way to authenticate in MOLGENIS is to click 'Sign in'. You can register a new account by using the 'Sign-up'-link. If there is no 'Sign-up'-link, you'll have to contact the administrator to register an account.

### 2-factor-authentication
If you have an existing MOLGENIS-account you can secure it with two factor authentication, depending on the server's settings. 

*note: you can not use Google-Authentication in combination with two factor authentication in MOLGENIS*

When two factor authentication is enabled and you sign in for the first time, you will be promted to secure your account with an authenticator app. The authentication can be configured by scanning a QR-code.
Examples of authenticator-apps are:

 * **Android**
   * [Google Authenticator](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2)
   * [Authy](https://play.google.com/store/apps/details?id=com.authy.authy)
   * [DUO](https://play.google.com/store/apps/details?id=com.duosecurity.duomobile&hl=nl)
 * **iPhone**
   * [Google Authenticator](https://itunes.apple.com/app/google-authenticator/id388497605?mt=8)
   * [Authy](https://itunes.apple.com/app/authy/id494168017?mt=8)
   * [DUO](https://itunes.apple.com/app/duo-mobile/id422663827?mt=8)
 
When the QR-code is scanned, your authenticator-app will create an account for MOLGENIS and also generate a verification code for that account. 
You have to fill in the verification code in de box below the QR-code. If you have entered the verification code you will be redirected to 
the *Account-Security*-tab. This will show the recovery-codes. Make sure you have stored the recovery codes somewhere (not in MOLGENIS) so you 
can access them when you for example lost your phone (see TROUBLESHOOTING). Each time you sign in, you will have to enter the verification code.  

Depending on the server's settings, you can enable, disable and reset your two factor authentication in your account settings (under Security).

**TROUBLESHOOTING**  
When you have lost your phone or misplaced it, you have to use one of the recovery-codes to unlock your account. You can view your recovery codes 
in the *Account-Security*-tab. Make sure to store the recovery codes somewhere outside MOLGENIS. You can click on the 'Enter a recovery code'-link, 
in the screen where you have to enter the verification code. You can then enter the recovery code to unlock your account.

## Google-Authentication
When your administrator has enabled 'Google Sign in' you can use your Google-account to authenticate in MOLGENIS. In the login screen you can 
see an additional button. Click it to authenticate with your Google account.

### Link your Google-account
When you already have a MOLGENIS account you can link it to your Google account. This way you can use your Google account to authenticate in MOLGENIS. 
Before you 'Sign in' with your Google account, make sure your MOLGENIS account has the same email address as your Google account. This way the 
MOLGENIS account will automatically be linked to your Google account.

When you've already signed in with your Google account and did not use the same email address as your MOLGENIS account you have to contact your administrator. 
