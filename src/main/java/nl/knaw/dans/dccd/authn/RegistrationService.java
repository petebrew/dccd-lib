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
package nl.knaw.dans.dccd.authn;

/*
 * note: was part of eof project package nl.knaw.dans.easy.business.authn
 *
 * Refactoring note: commonalities between User and Organisation Registration
 * could be factored out by generalization
 *
 */

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.common.lang.ldap.UserRepo;
import nl.knaw.dans.common.lang.mail.CommonMailer;
import nl.knaw.dans.common.lang.mail.MailComposer;
import nl.knaw.dans.common.lang.mail.MailComposerException;
import nl.knaw.dans.common.lang.mail.Mailer.MailerException;
import nl.knaw.dans.common.lang.repo.exception.ObjectNotInStoreException;
import nl.knaw.dans.common.lang.user.User;
import nl.knaw.dans.dccd.application.services.DccdUserService;
import nl.knaw.dans.dccd.mail.DccdMailer;
import nl.knaw.dans.dccd.mail.DccdMailerConfigurationException;
import nl.knaw.dans.dccd.model.DccdOrganisation;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.DccdUserImpl;
import nl.knaw.dans.dccd.repository.ldap.DccdOrganisationRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationService extends AbstractTokenList
{

    public static final String REGISTRATION_SUBJECT = "DCCD: account details";
    public static final String REGISTRATION_ADMIN_SUBJECT = "DCCD administrator: new candidate member";

    private static final String REGISTRATION_TEXT = "RegistrationMail.txt";
    private static final String REGISTRATION_ADMIN_TEXT = "RegistrationAdminMail.txt";

    private static Logger logger = LoggerFactory.getLogger(RegistrationService.class);

    /**
     * Store all tokens for registration requests.
     */
    private static final Map<String, String> TOKEN_MAP = new HashMap<String, String>();

    public RegistrationService()
    {
    }

    @Override
    public Map<String, String> getTokenMap()
    {
        return TOKEN_MAP;
    }

    /**
     * Get the user repository
     * replaces Easy On Fedora call: Data.getUserRepo()
     *
     * @return The repository
     */
    private UserRepo<DccdUser> getUserRepo() {
    	return DccdUserService.getService().getUserRepo();
    }

    private DccdOrganisationRepo getOrganisationRepo() {
    	return DccdUserService.getService().getOrganisationRepo();
    }

    /** The dccd user administrator gets an email notification,
     * so we need to know the address
     *
     * TODO: We try to retrieve that from the LDAP repository, the user name: dccduseradmin
     *
     * @return
     */
    public String getAdminEmail() {
		DccdUser admin;
		String email="";

		try {
			admin = getUserRepo().findById("dccduseradmin");
			email = admin.getEmail();
		} catch (ObjectNotInStoreException e) {
			logger.error("No admin user: " + "dccduseradmin" + ", Please make sure the userrepository has such user!");
		} catch (RepositoryException e) {
			logger.error("Could not get admin user from repository",  e);
		}

    	return email; //"paul.boon@dans.knaw.nl";
    }

//    public RegistrationMailAuthentication newAuthentication(final String userId, final String returnedTime, final String returnedToken)
//    {
//        RegistrationMailAuthentication authentication = new RegistrationMailAuthentication(userId, returnedTime, returnedToken);
//        // do more security things ...
//        return authentication;
//    }
//
//
//    public void login(final RegistrationMailAuthentication authentication)
//    {
//        final String userId = authentication.getUserId();
//        final String requestTime = authentication.getReturnedTime();
//        final String requestToken = authentication.getReturnedToken();
//        boolean authenticated = checkToken(userId, requestTime, requestToken)
//            // gets the user
//            && AuthenticationSpecification.userIsInQualifiedState(authentication);
//
//        if (authenticated)
//        {
//            handleConfirmation(authentication);
//        }
//        else
//        {
//            logger.warn("Invalid authentication: " + authentication.toString());
//        }
//        removeTokenFromList(userId);
//    }
//
//    // part of login(RegistrationMailAuthentication) procedure
//    private void handleConfirmation(final RegistrationMailAuthentication authentication)
//    {
//        User user = authentication.getUser();
//        user.setState(User.State.CONFIRMED_REGISTRATION);
//        try
//        {
//            Data.getUserRepo().update(user);
//            authentication.setState(Authentication.State.Authenticated);
//        }
//        catch (DataAccessException e)
//        {
//            authentication.setState(Authentication.State.SystemError, e);
//            authentication.setUser(null);
//            user.setState(User.State.REGISTERED);
//            logger.error("Could not update user after confirm registration: " + user.toString(), e);
//        }
//    }

    public UserRegistration handleRegistrationRequest(UserRegistration registration)
    {
    	logger.debug("Handling registration request...");

        if (!UserRegistrationSpecification.isSatisfiedBy(registration))
        {
            logger.debug("Registration does not conform to specification: " + registration.toString());
            return registration;
        }

        // do data persistent things
        handleRegistration(registration);
        if (registration.isCompleted())
        {
            logger.debug("Registered: " + registration.toString());
        }
        else
        {
            logger.error("Registration process unsuccessful: " + registration.toString());
            rollBackRegistration(registration);
        }
        return registration;
    }

    private void rollBackRegistration(UserRegistration registration)
    {
        logger.debug("Trying a rollback.");
        try
        {
            getUserRepo().delete(registration.getUser());
            logger.debug("Rollback of user registration successful.");
        }
        catch (RepositoryException e)
        {
            logger.error("Rollback of user registration unsuccessful: " + e);
        }
        removeTokenFromList(registration.getUser().getId());
    }

    private UserRegistration mailToUser(UserRegistration registration) {
        // Note: must use the DccdUserImpl, because the composer cannot deal with interfaces
        DccdUserImpl user = (DccdUserImpl)registration.getUser();

        // get the organisation
        DccdOrganisation organisation = null;
        organisation = registration.getOrganisation();

        MailComposer composer = new MailComposer(user, organisation);//, registration);
        InputStream inStream = this.getClass().getResourceAsStream(REGISTRATION_TEXT);

        try
        {
            String messagePlaintext = composer.compose(inStream);

            CommonMailer mailer = (CommonMailer) DccdMailer.getDefaultInstance();
            // Note: when html mail is needed, create an html file for that and use the next line
            //mailer.sendMail(REGISTRATION_SUBJECT, messagePlaintext, messageHtml, null, user.getEmail());
            mailer.sendSimpleMail(REGISTRATION_SUBJECT, messagePlaintext, user.getEmail());
/* TODO attach pdf
			final String ATTACH_FILE ="Licence_en.pdf";
			try
			{
	            final byte[] bytes = StreamUtil.getBytes(this.getClass().getClassLoader()
						.getResourceAsStream(ATTACH_FILE));
	            Attachement[] attachments = {new Attachement(ATTACH_FILE, "application/pdf", bytes)};
	            mailer.sendSimpleMail(REGISTRATION_SUBJECT, messagePlaintext, attachments, user.getEmail());

			}
			catch (IOException e)
			{
				logger.error("Could not get attachment from " + ATTACH_FILE + ": ", e);
				// Hint: check thefile
				registration.setState(UserRegistration.State.MailNotSend, e);
				return registration;
			}
*/
        }
		catch (MailComposerException e)
		{
			logger.error("Could not compose a mail message to " + user.getEmail() + ": ", e);
			// Hint: check the message template file
			registration.setState(UserRegistration.State.MailNotSend, e);
			return registration;
		}
		catch (DccdMailerConfigurationException e)
		{
			logger.error("Could not configure mailer to send a registration mail to " + user.getEmail() + ": ", e);
			registration.setState(UserRegistration.State.MailNotSend, e);
			return registration;
		}
		catch (MailerException e)
		{
			logger.error("Could not send a registration mail to " + user.getEmail() + ": ", e);
			registration.setState(UserRegistration.State.MailNotSend, e);
			return registration;
		}

		return registration;
    }

    private UserRegistration mailToAdmin(UserRegistration registration) {
        // Note: must use the DccdUserImpl, because the composer cannot deal with interfaces
        DccdUserImpl user = (DccdUserImpl)registration.getUser();

        DccdOrganisation organisation = registration.getOrganisation();

        MailComposer composer = new MailComposer(user, registration, organisation);
        InputStream inStream = this.getClass().getResourceAsStream(REGISTRATION_ADMIN_TEXT);

        String adminEmail = getAdminEmail();
        // not exceptions!
        if (adminEmail.length() == 0) {
        	logger.error("No admin email send");
        	return registration; // Bail out!
        }

        try
        {
            String messagePlaintext = composer.compose(inStream);

            CommonMailer mailer = (CommonMailer) DccdMailer.getDefaultInstance();

            String subject = REGISTRATION_ADMIN_SUBJECT + " " + user.getDisplayName();

            // Note: when html mail is needed, create an html file for that and use the next line
            //mailer.sendMail(REGISTRATION_SUBJECT, messagePlaintext, messageHtml, null, user.getEmail());

            mailer.sendSimpleMail(subject, messagePlaintext, adminEmail);
        }
		catch (MailComposerException e)
		{
			logger.error("Could not compose an admin mail message to " + adminEmail +
					" for user "+ user.getEmail() + ": ", e);
			// Hint: check the message template file
			//registration.setState(Registration.State.MailNotSend, e);
			return registration;
		}
		catch (DccdMailerConfigurationException e)
		{
			logger.error("Could not configure mailer to send an admin registration mail to " + adminEmail +
					" for user "+ user.getEmail() +": ", e);
			//registration.setState(Registration.State.MailNotSend, e);
			return registration;
		}
		catch (MailerException e)
		{
			logger.error("Could not send an admin registration mail to " + adminEmail +
					" for user "+ user.getEmail() +": ", e);
			//registration.setState(Registration.State.MailNotSend, e);
			return registration;
		}

		return registration;
    }

    /** Register user and notify by mail
     *
     * @param registration
     * @return
     */
    private UserRegistration handleRegistration(UserRegistration registration)
    {
        // Put new user in UserRepo
        try
        {
            getUserRepo().add(registration.getUser());
        }
        catch (RepositoryException e)
        {
            logger.error("Could not store a user for registration: ", e);
            registration.setState(UserRegistration.State.SystemError, e);
            return registration;
        }

        // Add new user to organisation in repo
        try
        {
            String userId = registration.getUser().getId();
            // use the organisation field of the user as id
            String organisationId = registration.getUser().getOrganization();
            getOrganisationRepo().addUserToOrganisation(userId, organisationId);
        }
        catch (RepositoryException e)
        {
            logger.error("Could not add a user to an organisation: ", e);
            registration.setState(UserRegistration.State.SystemError, e);
            return registration;
        }

        registration = mailToUser(registration);
        if (registration.getState() == UserRegistration.State.MailNotSend)
        	return registration; // don't continue!

		// How bad is it, when the admin mail fails?
		// are there no other means by which the admin can notice a new user ready for activation
        mailToAdmin(registration);

        // put token in list
        final String userId = registration.getUser().getId();
        final String dateTime = registration.getRequestTimeAsString();
        final String token = registration.getMailToken();
        putTokenInTokenList(userId, dateTime, token);

        // set state of registration and persist state of user
        registration.setState(UserRegistration.State.Registered);
        registration.getUser().setState(User.State.REGISTERED);
        try
        {
            getUserRepo().update(registration.getUser());
        }
        catch (RepositoryException e)
        {
            logger.error("Could not update a user as REGISTERED: ", e);
            registration.setState(UserRegistration.State.SystemError, e);
            registration.getUser().setState(null);
            return registration;
        }
        return registration;
    }

	// register user and organisation
    public UserRegistration handleRegistrationRequest(UserRegistration userRegistration,
    		OrganisationRegistration organisationRegistration)
    {
    	// register organisation
    	organisationRegistration = handleRegistrationRequest(organisationRegistration);
    	if (!organisationRegistration.isCompleted())
    	{
    		// don't register user
    		return userRegistration;
    	}

    	// set the user organisation
    	userRegistration.getUser().setOrganization(organisationRegistration.getOrganisationId());

    	// register user
    	userRegistration = handleRegistrationRequest(userRegistration);
        if (!userRegistration.isCompleted())
    	{
    		// rollback organisation
        	rollBackRegistration(organisationRegistration);
    	}

    	return userRegistration;
	}

    public OrganisationRegistration handleRegistrationRequest(OrganisationRegistration registration)
    {
    	logger.debug("Handling registration request...");

        if (!OrganisationRegistrationSpecification.isSatisfiedBy(registration))
        {
            logger.debug("Registration does not conform to specification: " + registration.toString());
            return registration;
        }

        // do data persistent things
        handleRegistration(registration);
        if (registration.isCompleted())
        {
            logger.debug("Registered: " + registration.toString());
        }
        else
        {
            logger.error("Registration process unsuccessful: " + registration.toString());
            rollBackRegistration(registration);
        }
        return registration;
    }

    private OrganisationRegistration handleRegistration(OrganisationRegistration registration)
    {
        // Put new organisation in Repository
        try
        {
            getOrganisationRepo().add(registration.getOrganisation());
        }
        catch (RepositoryException e)
        {
            logger.error("Could not store a organisation for registration: ", e);
            registration.setState(OrganisationRegistration.State.SystemError, e);
            return registration;
        }

        // Could sent mails here!

        // set state of registration
        registration.setState(OrganisationRegistration.State.Registered);

        // and persist state of the organisation,
        // could be done with the repository add !
        registration.getOrganisation().setState(DccdOrganisation.State.REGISTERED);
        try
        {
            getOrganisationRepo().update(registration.getOrganisation());
        }
        catch (RepositoryException e)
        {
            logger.error("Could not update a user as REGISTERED: ", e);
            registration.setState(OrganisationRegistration.State.SystemError, e);
            registration.getOrganisation().setState(null);
            return registration;
        }

        return registration;
    }

    private void rollBackRegistration(OrganisationRegistration registration)
    {
        logger.debug("Trying a rollback.");
        try
        {
            getOrganisationRepo().delete(registration.getOrganisation());
            logger.debug("Rollback of organisation registration successful.");
        }
        catch (RepositoryException e)
        {
            logger.error("Rollback of organisation registration unsuccessful: " + e);
        }
        //removeTokenFromList(registration.getOrganisation().getId());
    }

//    /*
//     * (non-Javadoc)
//     * @see nl.knaw.dans.easy.business.services.RegistrationService#validateRegistration(java.lang.String,
//     * java.lang.String, java.lang.String)
//     */
//    public boolean validateRegistration(final String userId, final String dateTime, final String token)
//    {
//        boolean valid = false;
//        logger.debug("userid: " + userId);
//        logger.debug("dateTime: " + dateTime);
//        logger.debug("token: " + token);
//
//        valid = checkToken(userId, dateTime, token);
//        if (valid)
//        {
//            try
//            {
//                User user = getUserRepo().findById(userId);
//                if (User.State.ACTIVE.equals(user.getState()) || user.getState() == null)
//                {
//                    logger.warn("Activating a user that is not in state " + User.State.REGISTERED + ". Actual state="
//                            + user.getState());
//                }
//                if (User.State.BLOCKED.equals(user.getState()))
//                {
//                    logger.warn("Not activating a user that is in state " + User.State.BLOCKED);
//                    return false;
//                }
//                user.setState(User.State.CONFIRMED_REGISTRATION);
//                getUserRepo().update(user);
//                removeTokenFromList(userId);
//            }
//            catch (ObjectNotInStoreException e)
//            {
//                logger.error("The user with userId '" + userId + "' could not be found: ", e);
//                removeTokenFromList(userId);
//                return false;
//            }
//            catch (RepositoryException e)
//            {
//                logger.error("Error while handling checkRegistrationRequest: ", e);
//                return false;
//            }
//        }
//        logger.debug("Validation of registration of user " + userId + " completed. Valid registration=" + valid);
//        return valid;
//    }

}
