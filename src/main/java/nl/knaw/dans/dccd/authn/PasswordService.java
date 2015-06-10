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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.common.lang.ldap.UserRepo;
import nl.knaw.dans.common.lang.mail.CommonMailer;

import nl.knaw.dans.common.lang.mail.MailComposer;
import nl.knaw.dans.common.lang.mail.MailComposerException;

import nl.knaw.dans.common.lang.mail.Mailer.MailerException;
import nl.knaw.dans.common.lang.user.User;

import nl.knaw.dans.dccd.application.services.DccdUserService;
import nl.knaw.dans.dccd.mail.DccdMailer;
import nl.knaw.dans.dccd.mail.DccdMailerConfigurationException;
import nl.knaw.dans.dccd.model.DccdUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordService extends AbstractTokenList
{
    public static final String NEW_PASS_TEXT  = "NewPasswordMail.txt";
    public static final String UPDATE_PASS_TEXT = "UpdatePasswordMail.txt";
    public static final String UPDATE_PASSWORD_SUBJECT = "DCCD: password change procedure";

    private static Logger logger  = LoggerFactory.getLogger(PasswordService.class);

    /**
     * Store all tokens for update password requests.
     */
    private static final Map<String, String> TOKEN_MAP = new HashMap<String, String>();

    public PasswordService()
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
    private static UserRepo<DccdUser> getUserRepo() {
    	return DccdUserService.getService().getUserRepo();
    }

    public ForgottenPasswordMailAuthentication newAuthentication(final String userId, final String returnedTime, final String returnedToken)
    {
        ForgottenPasswordMailAuthentication fpmAuthn = new ForgottenPasswordMailAuthentication(userId, returnedTime, returnedToken);
        return fpmAuthn;
    }

    public void login(ForgottenPasswordMailAuthentication authentication)
    {
        final String userId = authentication.getUserId();
        final String requestTime = authentication.getReturnedTime();
        final String requestToken = authentication.getReturnedToken();
        boolean authenticated = checkToken(userId, requestTime, requestToken)
            // gets the user
            && AuthenticationSpecification.userIsInQualifiedState(authentication);

        if (authenticated)
        {
            authentication.setState(Authentication.State.Authenticated);
        }
        else
        {
            logger.warn("Invalid authentication: " + authentication.toString());
        }
        removeTokenFromList(userId);
    }

    /**
     * Change the password of a user.
     *
     * @param messenger
     *        messenger for this job
     */
    public void changePassword(ChangePasswordMessenger messenger)
    {
        if (ChangePasswordSpecification.isSatisFiedBy(messenger))
        {
            changePasswordOnDataLayer(messenger);
            resetRequestToken(messenger);
        }
        else
        {
            logger.debug("ChangePassword does not confirm to specification: " + messenger.toString());
        }
    }

    /**
     * Sends a mail with a link to a change-password-page.
     *
     * @param messenger
     *        messenger for this job
     */
    public void sendUpdatePasswordLink(ForgottenPasswordMessenger messenger)
    {
        if (ForgottenPasswordSpecification.isSatisfiedBy(messenger))
        {
            try
            {
                handleSendUpdatePasswordLink(messenger);
                messenger.setState(ForgottenPasswordMessenger.State.UpdateURLSend);
            }
            catch (MailComposerException e)
            {
                logger.error("Mail could not be composed: ", e);
                messenger.setState(ForgottenPasswordMessenger.State.MailError, e);
            }
            catch (MailerException e)
            {
                logger.error("Mail could not be send: ", e);
                messenger.setState(ForgottenPasswordMessenger.State.MailError, e);
            } catch (DccdMailerConfigurationException e) {
    			logger.error("Could not configure mailer to send mail: ", e);
    			messenger.setState(ForgottenPasswordMessenger.State.MailError, e);
			}
        }
        else
        {
            logger.debug("Forgotten password data do not confirm to specification: " + messenger.getState());
        }
    }

    private void handleSendUpdatePasswordLink(final ForgottenPasswordMessenger messenger)
    	throws MailComposerException, MailerException, DccdMailerConfigurationException
    {
        final String mailToken = messenger.getMailToken();
        final String requestTime = messenger.getRequestTimeAsString();
        for (User user : messenger.getUsers())
        {
            putTokenInTokenList(user.getId(), requestTime, mailToken);
            final MailVO mailVO = new MailVO(messenger.getUpdateURL(), messenger.getUserIdParamKey(), user.getId());
            final MailComposer composer = new MailComposer(user, mailVO);
            final InputStream inStream = this.getClass().getResourceAsStream(UPDATE_PASS_TEXT);
            final String message = composer.compose(inStream);
            final String mailsubject = UPDATE_PASSWORD_SUBJECT + " for " + user.getId();

            CommonMailer mailer = (CommonMailer) DccdMailer.getDefaultInstance();
            
            mailer.sendSimpleMail(mailsubject, message, user.getEmail());

            logger.debug("Update password link send to " + user.getEmail());
        }
    }

    private void changePasswordOnDataLayer(ChangePasswordMessenger messenger)
    {
        try
        {
            User user = messenger.getUser();
            user.setPassword(messenger.getNewPassword());
            getUserRepo().update((DccdUser) user);
            messenger.setState(ChangePasswordMessenger.State.PasswordChanged);
            logger.debug("Changed password for user " + user);
        }
        catch (RepositoryException e)
        {
            messenger.setState(ChangePasswordMessenger.State.SystemError, e);
            logger.error("Could not change password for user " + messenger.getUserId(), e);
        }
    }

    private void resetRequestToken(ChangePasswordMessenger messenger)
    {
        if (messenger.isMailContext())
        {
            removeTokenFromList(messenger.getUserId());
        }
    }

    /**
     * Value object for individual url's, derived from a part-url.
     *
     * @author ecco Mar 1, 2009
     */
    public static class MailVO
    {
        private final String url;

        public MailVO(String partUrl, String key, String value)
        {
            //url = partUrl + key + "/" + value + "/";
        	// the URL parameters can be specified differently
            url = partUrl + "&" + key + "=" + value;
        }

        public String getURL()
        {
            return url;
        }
    }
}
