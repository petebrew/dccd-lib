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
import nl.knaw.dans.common.lang.user.User;
import nl.knaw.dans.dccd.application.services.DccdUserService;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  To check if a registration is conform the specification
 *
 * @author paulboon
 *
 */
public final class UserRegistrationSpecification
{

    private static Logger logger = LoggerFactory.getLogger(UserRegistrationSpecification.class);

    private UserRegistrationSpecification()
    {
        // empty
    }

    /**
     * Changes the state of the registration when something is not conforn.
     *
     * @param registration
     * @return
     */
    public static boolean isSatisfiedBy(UserRegistration registration)
    {
        boolean satisfied = hasSufficientData(registration) && hasUniqueID(registration);
        return satisfied;
    }

    private static boolean hasSufficientData(UserRegistration registration)
    {
        // TODO more sophisticated tests
        boolean sufficientData = true;
        User user = registration.getUser();
        if (StringUtils.isBlank(user.getId()))
        {
            sufficientData = false;
            registration.setState(UserRegistration.State.UserIdCannotBeBlank);
        }
        if (StringUtils.isBlank(user.getInitials()))
        {
            sufficientData = false;
            registration.setState(UserRegistration.State.InitialsCannotBeBlank);
        }
        //if (StringUtils.isBlank(user.getFirstname()))
        //{
        //    sufficientData = false;
        //    registration.setState(State.FirstnameCannotBeBlank);
        //}
        if (StringUtils.isBlank(user.getSurname()))
        {
            sufficientData = false;
            registration.setState(UserRegistration.State.SurnameCannotBeBlank);
        }
        if (StringUtils.isBlank(user.getPassword()))
        {
            sufficientData = false;
            registration.setState(UserRegistration.State.PasswordCannotBeBlank);
        }
        if (StringUtils.isBlank(user.getEmail()))
        {
            sufficientData = false;
            registration.setState(UserRegistration.State.EmailCannotBeBlank);
        }
        // added Organisation and Department for DCCD
        if (StringUtils.isBlank(user.getOrganization()))
        {
            sufficientData = false;
            registration.setState(UserRegistration.State.OrganisationCannotBeBlank);
        }

        return sufficientData;
    }

    private static boolean hasUniqueID(UserRegistration registration)
    {
        boolean hasUniqueId = false;
        String userId = registration.getUser().getId();
        try
        {
            if (!DccdUserService.getService().getUserRepo().exists(userId))
            {
                hasUniqueId = true;
            }
            else
            {
                registration.setState(UserRegistration.State.UserIdNotUnique);
            }
        }
        catch (RepositoryException e)
        {
            logger.error("Could not verify if userId is unique: ", e);
            registration.setState(UserRegistration.State.SystemError, e);
        }
        return hasUniqueId;
    }

}
