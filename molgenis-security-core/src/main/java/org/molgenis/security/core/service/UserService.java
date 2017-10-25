package org.molgenis.security.core.service;

import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.exception.EmailAlreadyExistsException;
import org.molgenis.security.core.service.exception.UsernameAlreadyExistsException;

import java.util.List;
import java.util.Optional;

/**
 * Manages the User administration.
 */
public interface UserService
{
	/**
	 * Returns e-mail addresses of super users.
	 */
	List<String> getSuEmailAddresses();

	/**
	 * Retrieves the {@link User} with a given username.
	 *
	 * @param username the username to look up
	 * @return Optional User
	 */
	Optional<User> findByUsernameIfPresent(String username);

	/**
	 * Find a user by their email address.
	 *
	 * @param email the email address to look up
	 * @return Optional User
	 */
	Optional<User> findByEmailIfPresent(String email);

	/**
	 * Finds a user by their google account ID
	 *
	 * @param googleAccountId the ID in google
	 * @return Optional User
	 */
	Optional<User> findByGoogleAccountIdIfPresent(String googleAccountId);

	/**
	 * Retrieves the {@link User} with a given username.
	 *
	 * @param username the username to look up
	 * @return the User
	 * @throws IllegalArgumentException if no {@link User} with that username exists
	 */
	default User findByUsername(String username)
	{
		return findByUsernameIfPresent(username).orElseThrow(
				() -> new IllegalArgumentException(String.format("User with username [%s] not found.", username)));
	}

	/**
	 * Find a user by their email address.
	 *
	 * @param email the email address to look up
	 * @return the User
	 * @throws IllegalArgumentException if no {@link User} with that email address exists
	 */
	default User findByEmail(String email)
	{
		return findByEmailIfPresent(email).orElseThrow(
				() -> new IllegalArgumentException(String.format("User with email [%s] not found.", email)));
	}

	/**
	 * Updates user
	 */
	User update(User user);

	/**
	 * Activates a User using activation code.
	 *
	 * @param activationCode the activation code to use
	 * @return the activated User if activation succeeded, otherwise empty
	 */
	Optional<User> activateUserUsingCode(String activationCode);

	/**
	 * Activates user
	 *
	 * @param id the ID of the user to activate
	 */
	void activateUser(String id);

	/**
	 * Deactivates user
	 *
	 * @param id the ID of the user to activate
	 */
	void deactivateUser(String id);

	/**
	 * Connects an existing user to a googleAccountId.
	 *
	 * @param username        the username of the user to connect
	 * @param googleAccountId the googleAccountId
	 * @return Optional User if the user with that name was found and could be connected, otherwise empty
	 */
	Optional<User> connectExistingUser(String username, String googleAccountId);

	/**
	 * Adds new User.
	 *
	 * @param user the User to add
	 */
	User add(User user) throws UsernameAlreadyExistsException, EmailAlreadyExistsException;

	/**
	 * Retrieves all users.
	 *
	 * @return List with all users
	 */
	List<User> getAllUsers();
}
