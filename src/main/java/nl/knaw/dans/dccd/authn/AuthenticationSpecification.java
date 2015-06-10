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

import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.common.lang.repo.exception.ObjectNotInStoreException;
import nl.knaw.dans.common.lang.user.User;
import nl.knaw.dans.dccd.application.services.DccdUserService;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationSpecification
{
    private static Logger logger = LoggerFactory.getLogger(AuthenticationSpecification.class);

    private AuthenticationSpecification()
    {
    	// only static members, disallow construction by others
    }

    /**
     * Check against authentication criteria. <br/>
     * !SIDE EFFECT: the user maybe loaded onto authentication if everything is well.
     *
     * @param authentication
     *        check this authentication
     * @return the authentication in a certain state
     */
    public static boolean isSatisfiedBy(UsernamePasswordAuthentication authentication)
    {
        boolean satisfied = hasSufficientData(authentication) && isAuthenticated(authentication)
                && userIsInQualifiedState(authentication);
        return satisfied;
    }

    private static boolean hasSufficientData(UsernamePasswordAuthentication authentication)
    {
        boolean sufficientData = true;
        if (StringUtils.isBlank(authentication.getUserId()))
        {
            sufficientData = false;
            authentication.setState(Authentication.State.UserIdConnotBeBlank);
            logger.debug("userId cannot be blank " + authentication.toString());
        }
        if (StringUtils.isBlank(authentication.getCredentials()))
        {
            sufficientData = false;
            authentication.setState(Authentication.State.CredentialsCannotBeBlank);
            logger.debug("credentials cannot be blank " + authentication.toString());
        }
        return sufficientData;
    }

    private static boolean isAuthenticated(UsernamePasswordAuthentication authentication)
    {
        boolean authenticated = false;
        String userId = authentication.getUserId();
        String password = authentication.getCredentials();
        try
        {
            authenticated = DccdUserService.getService().getUserRepo().authenticate(userId, password);

            if (!authenticated)
            {
                authentication.setState(Authentication.State.InvalidUsernameOrCredentials);
                logger.debug("Invalid userId or credentials for user " + userId);
            }
        }
        catch ( RepositoryException e )
        {
            logger.error("Could not authenticate user with userId '" + userId, e);
            authentication.setState(Authentication.State.SystemError, e);
        }
        return authenticated;
    }

    // SIDE EFFECT: user may be loaded onto authentication
    public static boolean userIsInQualifiedState(Authentication authentication)
    {
        boolean isInQualifiedState = false;
        User user = loadUser(authentication.getUserId());
        if (user == null)
        {
            authentication.setState(Authentication.State.NotFound);
            return isInQualifiedState;
        }

        if (checkUserState(authentication, user))
        {
            isInQualifiedState = true;
            authentication.setUser(user);
        }
        else
        {
            authentication.setState(Authentication.State.NotQualified);
            logger.warn("Attempt to authenticate while in unqualified state: " + authentication.toString());
        }
        return isInQualifiedState;
    }


    private static boolean checkUserStateForUsernamePassword(final User user)
    {
        return User.State.ACTIVE.equals(user.getState()) || User.State.CONFIRMED_REGISTRATION.equals(user.getState());
    }

//    private static boolean checkUserStateForRegistration(final User user)
//    {
//        return User.State.REGISTERED.equals(user.getState());
//    }

    public static boolean checkUserStateForForgottenPassword(final User user)
    {
        return User.State.ACTIVE.equals(user.getState()) || User.State.CONFIRMED_REGISTRATION.equals(user.getState());
    }

    private static boolean checkUserState(final Authentication authentication, final User user)
    {
        if (authentication instanceof UsernamePasswordAuthentication)
        {
            return checkUserStateForUsernamePassword(user);
        }
//        else if (authentication instanceof RegistrationMailAuthentication)
//        {
//            return checkUserStateForRegistration(user);
//        }
        else if (authentication instanceof ForgottenPasswordMailAuthentication)
        {
            return checkUserStateForForgottenPassword(user);
        }
        else
        {
            final String msg = "Unknown type of authentication: " + authentication.getClass().getName()
                    + "\n\tBetter make sure there is a method for this type of authentication!";
            logger.error(msg);
            authentication.setState(Authentication.State.SystemError);
            throw new IllegalStateException(msg);
        }
    }

    private static User loadUser(String userId)
    {
        User user = null;
        try
        {
            user = DccdUserService.getService().getUserRepo().findById(userId);
        }
        catch (ObjectNotInStoreException e)
        {
            logger.error("User with userId'" + userId + "' not found after authentication: ", e);
        }
        catch (RepositoryException e)
        {
            logger.error("Loading user with userId '" + userId + "' failed: ", e);
        }
        return user;
    }
}
