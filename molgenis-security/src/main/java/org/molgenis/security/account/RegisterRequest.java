package org.molgenis.security.account;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

import static org.molgenis.security.core.service.UserAccountService.MIN_PASSWORD_LENGTH;

class RegisterRequest
{
	@NotBlank
	private String username;
	@NotNull
	@Size(min = MIN_PASSWORD_LENGTH)
	private String password;
	@NotNull
	@Size(min = MIN_PASSWORD_LENGTH)
	private String confirmPassword;
	@NotBlank
	@Email
	private String email;
	private String phone;
	private String fax;
	private String tollFreePhone;
	private String title;
	@NotBlank
	private String lastname;
	@NotBlank
	private String firstname;
	private String middleNames;
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

	public String getMiddleNames()
	{
		return middleNames;
	}

	public void setMiddleNames(String middleNames)
	{
		this.middleNames = middleNames;
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

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RegisterRequest that = (RegisterRequest) o;
		return Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(
				confirmPassword, that.confirmPassword) && Objects.equals(email, that.email) && Objects.equals(phone,
				that.phone) && Objects.equals(fax, that.fax) && Objects.equals(tollFreePhone, that.tollFreePhone)
				&& Objects.equals(title, that.title) && Objects.equals(lastname, that.lastname) && Objects.equals(
				firstname, that.firstname) && Objects.equals(middleNames, that.middleNames) && Objects.equals(institute,
				that.institute) && Objects.equals(department, that.department) && Objects.equals(position,
				that.position) && Objects.equals(address, that.address) && Objects.equals(city, that.city)
				&& Objects.equals(country, that.country);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(username, password, confirmPassword, email, phone, fax, tollFreePhone, title, lastname,
				firstname, middleNames, institute, department, position, address, city, country);
	}
}