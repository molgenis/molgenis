<#assign form = model.registrationForm>
<h2>Register as a new user</h2>
<h3>Login details</h3>
<table>
<tr><td>Username</td><td>${form.username}</td><td>*</td></tr>
<tr><td>Password</td><td>${form.password}</td><td>*</td></tr>
<tr><td>Repeat password</td><td>${form.password2}</td><td>*</td></tr>
<tr><td>Email address</td><td>${form.email}</td><td>*</td></tr>
<tr><td>Phone</td><td>${form.phone}</td></tr>
<tr><td>Fax</td><td>${form.fax}</td></tr>
<tr><td>tollFreePhone</td><td>${form.tollFreePhone}</td></tr>
<tr><td>Address</td><td>${form.address}</td></tr>
</table>
<h3>Personal and professional details</h3>
<table>
<tr><td>Title</td><td>${form.title}</td></tr>
<tr><td>Last name</td><td>${form.lastname}</td><td>*</td></tr>
<tr><td>First name</td><td>${form.firstname}</td><td>*</td></tr>
<tr><td>Position</td><td>${form.position}</td></tr>
<tr><td>Institute</td><td>${form.institute}</td></tr>
<tr><td>Department</td><td>${form.department}</td></tr>
<tr><td>City</td><td>${form.city}</td></tr>
<tr><td>Country</td><td>${form.country}</td></tr>
</table>
<h3>Code verification</h3>
<table>
<tr><td></td><td><img src="captchaImg"/></td></tr>
<tr><td>Enter code</td><td>${form.code}</td><td>*</td></tr>
</table>
<br/>
${form.Cancel} ${form.AddUser}