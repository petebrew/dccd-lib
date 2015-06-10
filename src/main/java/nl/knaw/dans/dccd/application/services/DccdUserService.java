/*******************************************************************************
 * Copyright 2015 DANS - Data Archiving and Networked Services
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package nl.knaw.dans.dccd.application.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.common.lang.mail.CommonMailer;
import nl.knaw.dans.common.lang.mail.Mailer.MailerException;
import nl.knaw.dans.common.lang.repo.exception.ObjectExistsException;
import nl.knaw.dans.common.lang.repo.exception.ObjectNotInStoreException;
import nl.knaw.dans.common.lang.user.User;
import nl.knaw.dans.dccd.authn.Authentication;
import nl.knaw.dans.dccd.authn.ChangePasswordMessenger;
import nl.knaw.dans.dccd.authn.ForgottenPasswordMailAuthentication;
import nl.knaw.dans.dccd.authn.ForgottenPasswordMessenger;
import nl.knaw.dans.dccd.authn.LoginService;
import nl.knaw.dans.dccd.authn.OrganisationRegistration;
import nl.knaw.dans.dccd.authn.PasswordService;
import nl.knaw.dans.dccd.authn.RegistrationService;
import nl.knaw.dans.dccd.authn.UserRegistration;
import nl.knaw.dans.dccd.authn.UserService;
import nl.knaw.dans.dccd.authn.UsernamePasswordAuthentication;
import nl.knaw.dans.dccd.mail.DccdMailer;
import nl.knaw.dans.dccd.mail.DccdMailerConfigurationException;
import nl.knaw.dans.dccd.model.DccdOrganisation;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.DccdUserImpl;
import nl.knaw.dans.dccd.model.DccdUser.Role;
import nl.knaw.dans.dccd.repository.ldap.DccdLdapOrganisationRepo;
import nl.knaw.dans.dccd.repository.ldap.DccdLdapUserRepo;
import nl.knaw.dans.common.ldap.ds.LdapClient;
import nl.knaw.dans.common.ldap.ds.StandAloneDS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DccdUserService implements UserService
{
	private static Logger logger = LoggerFactory
			.getLogger(DccdUserService.class);

	static Properties settings = DccdConfigurationService.getService().getSettings();
	static final String LDAP_URL = settings.getProperty("ldap.url");//"ldap://localhost:10389"
	static final String LDAP_PRINCIPAL = settings.getProperty("ldap.securityPrincipal");
	static final String LDAP_CREDENTIALS = settings.getProperty("ldap.securityCredentials");

	private LoginService loginService = new LoginService();
	private RegistrationService registrationService = new RegistrationService();
	private PasswordService passwordService = new PasswordService();

	// Note: getUserRepo was member of the Data class
	private static DccdLdapUserRepo userRepo = null;
	private static DccdLdapOrganisationRepo organisationRepo = null;

	// Get an access point for the user repository
	// singleton pattern with lazy construction
	private static DccdUserService service = null;

	public static DccdUserService getService()
	{
		if (service == null)
		{
			service = new DccdUserService();
		}
		return service;
	}

	private DccdUserService()
	{
		// disallow construction
	}

	public DccdLdapUserRepo getUserRepo()
	{
		if (userRepo == null)
		{
			// TODO: the strings must be placed in an application settings using
			// DccdConfigurationService.getService().getSettings();
			StandAloneDS supplier = new StandAloneDS();
			supplier.setProviderURL(LDAP_URL);
			supplier.setSecurityPrincipal(LDAP_PRINCIPAL);
			supplier.setSecurityCredentials(LDAP_CREDENTIALS);
			LdapClient client = new LdapClient(supplier);
			userRepo = new DccdLdapUserRepo(client,
					"ou=users,ou=dccd,dc=dans,dc=knaw,dc=nl");
		}
		return userRepo;
	}

	public DccdLdapOrganisationRepo getOrganisationRepo()
	{
		if (organisationRepo == null)
		{
			// TODO: the strings must be placed in an application settings using
			// DccdConfigurationService.getService().getSettings();
			StandAloneDS supplier = new StandAloneDS();
			supplier.setProviderURL(LDAP_URL);
			supplier.setSecurityPrincipal(LDAP_PRINCIPAL);
			supplier.setSecurityCredentials(LDAP_CREDENTIALS);
			LdapClient client = new LdapClient(supplier);

			organisationRepo = new DccdLdapOrganisationRepo(client,
					"ou=organisations,ou=dccd,dc=dans,dc=knaw,dc=nl");
		}
		return organisationRepo;
	}

	public UsernamePasswordAuthentication newUsernamePasswordAuthentication()
	{
		return loginService.newAuthentication();
	}

	/**
	 * When we want to send a mail to the user, and we compose the message
	 * ourselves
	 *
	 * @param user
	 * @param subject
	 * @param message
	 */
	public void sendMailToUser(DccdUser user, String subject, String message)
			throws UserServiceException
	{
		try
		{
			CommonMailer mailer = (CommonMailer) DccdMailer
					.getDefaultInstance();
			mailer.sendSimpleMail(subject, message, user.getEmail());
		}
		catch (DccdMailerConfigurationException e)
		{
			throw new UserServiceException("Could not mail user with id '"
					+ user.getId() + "' :", e);
		}
		catch (MailerException e)
		{
			throw new UserServiceException("Could not mail user with id '"
					+ user.getId() + "' :", e);
		}

	}

	//
	// public RegistrationMailAuthentication newRegistrationMailAuthentication(
	// final String userId, final String returnedTime, final String
	// returnedToken)
	// {
	// return registrationService.newAuthentication(userId, returnedTime,
	// returnedToken);
	// }
	//
	public ForgottenPasswordMailAuthentication newForgottenPasswordMailAuthentication(
			final String userId, final String returnedTime,
			final String returnedToken)
	{
		return passwordService.newAuthentication(userId, returnedTime,
				returnedToken);
	}

	public void authenticate(Authentication authentication)
	{
		if (authentication instanceof UsernamePasswordAuthentication)
		{
			loginService.login((UsernamePasswordAuthentication) authentication);
			logAuthentication(authentication);
		}
		// else if (authentication instanceof RegistrationMailAuthentication)
		// {
		// registrationService.login((RegistrationMailAuthentication)
		// authentication);
		// logAuthentication(authentication);
		// }
		else if (authentication instanceof ForgottenPasswordMailAuthentication)
		{
			passwordService
					.login((ForgottenPasswordMailAuthentication) authentication);
			logAuthentication(authentication);
		}
		else
		{
			final String msg = "No method for athentication: " + authentication;
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}
	}

	private void logAuthentication(Authentication authentication)
	{
		if (authentication.isCompleted())
		{
			if (logger.isDebugEnabled())
				logger.debug("Authentication successful: "
						+ authentication.toString());
		}
		else
		{
			logger.warn("Authentication unsuccessful: "
					+ authentication.toString());
		}
	}

	public UserRegistration handleRegistrationRequest(
			final UserRegistration registration)
	{
		registrationService.handleRegistrationRequest(registration);

		logger.debug("Handled registration: " + registration.toString());
		return registration;
	}

	public DccdUser getUserById(final String uid) throws UserServiceException
	{
		DccdUser user = null;
		try
		{
			// user = Data.getUserRepo().findById(uid);
			user = getUserRepo().findById(uid);
			logger.debug("Found user: " + user.toString());
		}
		catch (final ObjectNotInStoreException e)
		{
			logger.debug("Object not found. userId='" + uid + "'");
			// just return null indicating that it wasn't there
			// throw new UserServiceException("Object not found. userId='" + uid
			// + "' :", e);
		}
		catch (final RepositoryException e)
		{
			logger.debug("Could not get user with id '" + uid + "' :", e);
			throw new UserServiceException("Could not get user with id '" + uid
					+ "' :", e);
		}
		return user;
	}

	public List<DccdUser> getAllUsers() throws UserServiceException
	{
		List<DccdUser> users = null;
		try
		{
			users = getUserRepo().findAll();
		}
		catch (final RepositoryException e)
		{
			logger.debug("Could not retrieve users: ", e);
			throw new UserServiceException("Could not retrieve users: ", e);
		}
		return users;
	}

	/** Activated non-Admin users, instead of just all users
	 * 
	 * @return
	 * @throws UserServiceException
	 */
	public List<DccdUser> getActiveNormalUsers() throws UserServiceException
	{
		List<DccdUser> users = null;
		try
		{
			users = getUserRepo().getActiveNormalUsers();
		}
		catch (final RepositoryException e)
		{
			logger.debug("Could not retrieve users: ", e);
			throw new UserServiceException("Could not retrieve users: ", e);
		}
		return users;
	}
	
	//
	// public List<User> retrieveUserByEmail(final String email) throws
	// ServiceException
	// {
	// List<User> users = null;
	// try
	// {
	// users = Data.getUserRepo().findByEmail(email);
	// }
	// catch (DataAccessException e)
	// {
	// logger.debug("Could not retrieve users by email: ", e);
	// throw new ServiceException("Could not retrieve users by email: ", e);
	// }
	// return users;
	// }
	//
	// public List<User> getUsersByRole(Role role) throws ServiceException
	// {
	// List<User> users = null;
	// try
	// {
	// users = Data.getUserRepo().findByRole(role);
	// }
	// catch (DataAccessException e)
	// {
	// logger.debug("Could not retrieve users by role: ", e);
	// throw new ServiceException("Could not retrieve users by role: ", e);
	// }
	// return users;
	// }

	// Given the first characters of the "Surname" it returns the matches
	public Map<String, String> findByCommonNameStub(String stub, long maxCount)
			throws UserServiceException
	{
		Map<String, String> idNameMap = null;
		try
		{
			idNameMap = getUserRepo().findByCommonNameStub(stub, maxCount);
		}
		catch (RepositoryException e)
		{
			logger.debug("Could not retrieve users by common name stub: ", e);
			throw new UserServiceException(
					"Could not retrieve users by common name stub: ", e);
		}
		return idNameMap;
	}

	public Map<String, String> findActiveNormal() throws UserServiceException
    {
		Map<String, String> idNameMap = null;
		try
		{
			idNameMap = getUserRepo().findActiveNormal();
		}
		catch (RepositoryException e)
		{
			logger.debug("Could not retrieve users: ", e);
			throw new UserServiceException(
					"Could not retrieve users: ", e);
		}
		return idNameMap;	
    }
	
	// Like findByCommonNameStub but restricts the result to ACTIVE users
	// without the ADMIN role
	public Map<String, String> findActiveNormalByCommonNameStub(String stub,
			long maxCount) throws UserServiceException
	{
		Map<String, String> idNameMap = null;
		try
		{
			idNameMap = getUserRepo().findActiveNormalByCommonNameStub(stub, maxCount);
		}
		catch (RepositoryException e)
		{
			logger.debug("Could not retrieve users by common name stub: ", e);
			throw new UserServiceException(
					"Could not retrieve users by common name stub: ", e);
		}
		return idNameMap;
	}

	
	public Map<String, String> findActiveInOrganisationByCommonNameStub(String stub, String organisationId, long maxCount)
	throws UserServiceException
	{
		Map<String, String> idNameMap = null;
		try
		{
			idNameMap = getUserRepo().findActiveInOrganisationByCommonNameStub(stub, organisationId, maxCount);
		}
		catch (RepositoryException e)
		{
			logger.debug("Could not retrieve users by common name stub: ", e);
			throw new UserServiceException(
					"Could not retrieve users by common name stub: ", e);
		}
		return idNameMap;
	}
	
	public OrganisationRegistration handleRegistrationRequest(
			final OrganisationRegistration registration)
	{
		registrationService.handleRegistrationRequest(registration);

		logger.debug("Handled registration: " + registration.toString());
		return registration;
	}

	public List<DccdOrganisation> findOrganisationsByStub(String stub, long maxCount)
		throws UserServiceException
	{
		List<DccdOrganisation> organisations = null;
		try
		{
			organisations = getOrganisationRepo().findOrganisationsByStub(stub, maxCount);
		}
		catch (RepositoryException e)
		{
			logger.debug("Could not retrieve organisation: ", e);
			throw new UserServiceException("Could not retrieve organisation: ",
					e);
		}
		return organisations;
	}	

	public List<DccdOrganisation> getAllOrganisations()
			throws UserServiceException
	{
		List<DccdOrganisation> organisations = null;
		try
		{
			organisations = getOrganisationRepo().findAll();
		}
		catch (RepositoryException e)
		{
			logger.debug("Could not retrieve organisation: ", e);
			throw new UserServiceException("Could not retrieve organisation: ",
					e);
		}
		return organisations;
	}

	public List<DccdOrganisation> getActiveOrganisations()
			throws UserServiceException
	{
		List<DccdOrganisation> organisations = new ArrayList<DccdOrganisation>();

		organisations = getAllOrganisations();
		organisations = removeNonActive(organisations);
		return organisations;
	}

	private List<DccdOrganisation> removeNonActive(
			List<DccdOrganisation> organisations)
	{
		// filter = remove non-active organisations
		for (Iterator<DccdOrganisation> it = organisations.iterator(); it
				.hasNext();)
		{
			DccdOrganisation organisation = it.next();
			if (organisation.getState() != DccdOrganisation.State.ACTIVE)
			{
				it.remove();
			}
		}

		return organisations;
	}

	public List<DccdOrganisation> getNonblockedOrganisations()
			throws UserServiceException
	{
		return removeBlocked(getAllOrganisations());
	}

	private List<DccdOrganisation> removeBlocked(
			List<DccdOrganisation> organisations)
	{
		// filter = remove blocked organisations
		for (Iterator<DccdOrganisation> it = organisations.iterator(); it
				.hasNext();)
		{
			DccdOrganisation organisation = it.next();
			if (organisation.getState() == DccdOrganisation.State.BLOCKED)
			{
				it.remove();
			}
		}

		return organisations;
	}

	public DccdOrganisation getOrganisationById(final String id)
			throws UserServiceException
	{
		DccdOrganisation organisation = null;
		try
		{
			// user = Data.getUserRepo().findById(uid);
			organisation = getOrganisationRepo().findById(id);
			logger.debug("Found organisation: " + organisation.toString());
		}
		catch (final ObjectNotInStoreException e)
		{
			logger.debug("Object not found. organisationId='" + id + "'");
			throw new UserServiceException("Object not found. organisationId='"
					+ id + "' :", e);
		}
		catch (final RepositoryException e)
		{
			logger
					.debug("Could not get organisation with id '" + id + "' :",
							e);
			throw new UserServiceException(
					"Could not get organisation with id '" + id + "' :", e);
		}
		return organisation;
	}

	public DccdUser update(final DccdUser updater, final DccdUser user)
			throws UserServiceException
	{
		// check if input is not null
		if (updater == null || user == null)
		{
			logger.error("updater and or user was null");
			throw new IllegalArgumentException("updater and or user was null");
		}

		// validate user

		// set state of user: active after first login and update personal info

		// Note: The following is not for DCCD, but only on Dans-Easy !
		// DCCD needs (non-automated) activation from a useradmin
		//
		// if (updater.equals(user) &&
		// DccdUser.State.CONFIRMED_REGISTRATION.equals(user.getState()))
		// {
		// user.setState(DccdUser.State.ACTIVE);
		// }

		changeUserOrganisationWhenNeeded(updater, user);

		// update the user information
		try
		{
			// Data.getUserRepo().update(user);
			getUserRepo().update(user);
		}
		catch (final RepositoryException e)
		{
			throw new UserServiceException(e);
		}
		return user;
	}

	private void changeUserOrganisationWhenNeeded(final DccdUser updater,
			final DccdUser user) throws UserServiceException
	{
		removeUserFromOrganisationWhenNeeded(updater, user);

		try
		{
			DccdOrganisation givenOrganisation = getOrganisationRepo()
					.findById(user.getOrganization());

			boolean organistionNeedsUpdate = false;

			// check if the user needs to be added to the organisation
			if (!givenOrganisation.hasUserId(user.getId()))
			{
				logger.debug("Adding user to organisation");
				givenOrganisation.addUserId(user.getId());
				organistionNeedsUpdate = true;
			}

			// check if organisation needs to be activated?
			// if the user is active the organisation must be active as well
			// after registration of a new user and organisation
			// a user can be activated, and then the organisation also needs to
			// be activated
			if (!givenOrganisation.getState().equals(
					DccdOrganisation.State.ACTIVE))
			{
				logger.debug("Organisation not Active");
				if (user.getState().equals(User.State.ACTIVE))
				{
					logger
							.debug("User is Active, therefore Activating organisation");
					givenOrganisation.setState(DccdOrganisation.State.ACTIVE);
					organistionNeedsUpdate = true;
				}
			}

			if (organistionNeedsUpdate)
			{
				logger.debug("Updating organisation");
				// update organisation
				update(updater, givenOrganisation);
			}
		}
		catch (ObjectNotInStoreException e)
		{
			throw new UserServiceException(e);
		}
		catch (RepositoryException e)
		{
			throw new UserServiceException(e);
		}
	}

	private void removeUserFromOrganisationWhenNeeded(final DccdUser updater,
			final DccdUser user) throws UserServiceException
	{
		// check if the user needs to be removed from an organisation
		try
		{
			DccdOrganisation foundOrganisation = getOrganisationRepo()
					.findOrganisationWithUser(user.getId());
			if (null != foundOrganisation
					&& !(foundOrganisation.getId().equals(user
							.getOrganization())))
			{
				// it is not the same organisation
				// remove from the found organisation
				foundOrganisation.removeUserId(user.getId());
				update(updater, foundOrganisation);
			}
		}
		catch (RepositoryException e)
		{
			e.printStackTrace();
		}
	}

	public void update(final DccdUser updater,
			final DccdOrganisation organisation) throws UserServiceException
	{
		// check if input is not null
		if (updater == null || organisation == null)
		{
			logger.error("updater and or organisation was null");
			throw new IllegalArgumentException(
					"updater and or organisation was null");
		}

		// check if user is allowed
		if (!updater.hasRole(DccdUser.Role.ADMIN))
		{
			throw new UserServiceException("Not allowed to update organisation");
		}

		// check if the organisation is (being) blocked,
		// and then check if we allow that
		if (DccdOrganisation.State.BLOCKED == organisation.getState())
		{
			// no mater what the previous state was, we cannot be blocked if
			// - there are members
			// - there are projects that the organisation is owner of

			// check for members
			// NOTE: maybe check for non-blocked users, and only allow when all are blocked? 
			// we need to make the organisation candidate again when one of those users becomes candidate again.
			if (organisation.hasUsers())
				throw new OrganisationBlockedWithUsersException(
						"Not allowed to delete organisation with users");

			// TODO check for projects
		}

		// update
		try
		{
			getOrganisationRepo().update(organisation);
		}
		catch (RepositoryException e)
		{
			throw new UserServiceException(e);
		}

	}

	public void changePassword(final ChangePasswordMessenger messenger)
	{
		// delegate to specialized service.
		passwordService.changePassword(messenger);
	}

	public void handleForgottenPasswordRequest(
			final ForgottenPasswordMessenger messenger)
	{
		// delegate to specialized service.
		// We can send a new password by mail:
		// passwordService.sendNewPassword(messenger);

		// Or do it in the fancy way by sending a link to a page where the user
		// can type in a new password:
		passwordService.sendUpdatePasswordLink(messenger);
	}

	// public OperationalAttributes getOperationalAttributes(User user)
	// {
	// try
	// {
	// return Data.getUserRepo().getOperationalAttributes(user.getId());
	// }
	// catch (DataAccessException e)
	// {
	// throw new ApplicationException(e);
	// }
	// }
	//
	// public OperationalAttributes getOperationalAttributes(Group group)
	// {
	// try
	// {
	// return Data.getGroupRepo().getOperationalAttributes(group.getId());
	// }
	// catch (DataAccessException e)
	// {
	// throw new ApplicationException(e);
	// }
	// }

	LoginService getLoginService()
	{
		return loginService;
	}

	public UserRegistration handleRegistrationRequest(
			UserRegistration userRegistration,
			OrganisationRegistration organisationRegistration)
	{

		registrationService.handleRegistrationRequest(userRegistration,
				organisationRegistration);

		logger.debug("Handled registration of user: "
				+ userRegistration.toString() + " and organisation: "
				+ organisationRegistration.toString());
		return userRegistration;
	}

	// RegistrationService getRegistrationService()
	// {
	// return registrationService;
	// }
	//
	// PasswordService getPasswordService()
	// {
	// return passwordService;
	// }

	
	// Initialize the LDAP repo  with at least one admin user
	public void createInitialAdminUser() throws UserServiceException
	{
		final String DCCDADMIN = "dccduseradmin";
		
		//DccdUserService dccdUserService = DccdUserService.getService();
		try
		{
			
			if (!getUserRepo().exists(DCCDADMIN))
			{
				// create 
				DccdUser adminUser = new DccdUserImpl();
				adminUser.setId(DCCDADMIN);   // uid
				adminUser.setSurname(DCCDADMIN);     // sn
				adminUser.setEmail(".");
				adminUser.setCity(".");
				adminUser.addRole(Role.USER);
				adminUser.addRole(Role.ADMIN);
				adminUser.setAcceptConditionsOfUse(true);
				adminUser.setState(DccdUser.State.ACTIVE);
				adminUser.setPassword(DCCDADMIN); // not save, need to change it!
				adminUser.setInitials(".");
				// No Organisation DANS lijkt correct, maar die moet dan ook aangemaakt worden!?
				
				getUserRepo().add(adminUser);
			}
			
			// Note: could also add some users that cannot be used by others (admin, administrator, root)
		}
		catch (ObjectExistsException e)
		{
			throw new UserServiceException(e);
		}
		catch (RepositoryException e)
		{
			throw new UserServiceException(e);
		}
		
	}
}
