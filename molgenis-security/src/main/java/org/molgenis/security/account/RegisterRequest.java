package org.molgenis.security.account;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static org.molgenis.security.user.UserAccountService.MIN_PASSWORD_LENGTH;

class RegisterRequest
{
	@NotNull
	private String username;
	@NotNull
	@Size(min = MIN_PASSWORD_LENGTH)
	private String password;
	@NotNull
	@Size(min = MIN_PASSWORD_LENGTH)
	private String confirmPassword;
	@NotNull
	@Email
	private String email;
	private String phone;
	private String fax;
	private String tollFreePhone;
	private String title;
	@NotNull
	private String lastname;
	@NotNull
	private String firstname;
	private Integer institute;
	private String department;
	private Integer position;
	private String address;
	private String city;
	private String country;

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getConfirmPassword()
	{
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword)
	{
		this.confirmPassword = confirmPassword;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getPhone()
	{
		return phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getFax()
	{
		return fax;
	}

	public void setFax(String fax)
	{
		this.fax = fax;
	}

	public String getTollFreePhone()
	{
		return tollFreePhone;
	}

	public void setTollFreePhone(String tollFreePhone)
	{
		this.tollFreePhone = tollFreePhone;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getLastname()
	{
		return lastname;
	}

	public void setLastname(String lastname)
	{
		this.lastname = lastname;
	}

	public String getFirstname()
	{
		return firstname;
	}

	public void setFirstname(String firstname)
	{
		this.firstname = firstname;
	}

	public Integer getInstitute()
	{
		return institute;
	}

	public void setInstitute(Integer institute)
	{
		this.institute = institute;
	}

	public String getDepartment()
	{
		return department;
	}

	public void setDepartment(String department)
	{
		this.department = department;
	}

	public Integer getPosition()
	{
		return position;
	}

	public void setPosition(Integer position)
	{
		this.position = position;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public String getCity()
	{
		return city;
	}

	public void setCity(String city)
	{
		this.city = city;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

}