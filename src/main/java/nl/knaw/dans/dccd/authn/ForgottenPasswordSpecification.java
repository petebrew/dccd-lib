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

import java.util.ArrayList;
import java.util.List;

import nl.knaw.dans.dccd.application.services.DccdUserService;
import nl.knaw.dans.dccd.authn.ForgottenPasswordMessenger.State;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.common.lang.ldap.UserRepo;
import nl.knaw.dans.common.lang.repo.exception.ObjectNotInStoreException;
import nl.knaw.dans.common.lang.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForgottenPasswordSpecification
{
    private static Logger logger = LoggerFactory.getLogger(ForgottenPasswordSpecification.class);

    /**
     * Get the user repository
     * replaces Easy On Fedora call: Data.getUserRepo()
     *
     * @return The repository
     */
    private static UserRepo<DccdUser> getUserRepo() {
    	return DccdUserService.getService().getUserRepo();
    }

    public static boolean isSatisfiedBy(ForgottenPasswordMessenger messenger)
    {
        boolean satisfied = hasSufficientData(messenger) && usersCanBeFound(messenger) && anyQualifiedUsers(messenger);
        return satisfied;
    }

    private static boolean hasSufficientData(ForgottenPasswordMessenger messenger)
    {
        boolean hasSufficientData = true;
        if (StringUtils.isBlank(messenger.getUserId()) && StringUtils.isBlank(messenger.getEmail()))
        {
            hasSufficientData = false;
            logger.debug("Both userId and email are blank.");
            messenger.setState(State.InsufficientData);
        }
        return hasSufficientData;
    }

    // side effect: a list of users maybe loaded onto the messenger
    private static boolean usersCanBeFound(ForgottenPasswordMessenger messenger)
    {
        boolean userFound = false;
        if (StringUtils.isNotBlank(messenger.getUserId()))
        {
            userFound = findUserByUserId(messenger);
        }
        if (!userFound && StringUtils.isNotBlank(messenger.getEmail()))
        {
            userFound = findUserByEmail(messenger);
        }
        return userFound;
    }

    // side effect: a list of users maybe loaded onto the messenger
    private static boolean findUserByEmail(ForgottenPasswordMessenger messenger)
    {
        boolean userFound = false;
        try
        {
            List<DccdUser> users = getUserRepo().findByEmail(messenger.getEmail());
            if (!users.isEmpty())
            {
                userFound = true;
                messenger.getUsers().addAll(users);
            }
            else
            {
                logger.debug("User cannot be found by email: " + messenger.getEmail());
                messenger.setState(State.UserNotFound);
            }
        }
        catch (RepositoryException e)
        {
            logger.error("Could not find user by email: ", e);
            messenger.setState(State.SystemError, e);
        }
        return userFound;
    }

    // side effect: a list of users maybe loaded onto the messenger
    private static boolean findUserByUserId(ForgottenPasswordMessenger messenger)
    {
        boolean userFound = false;
        try
        {
            User user = getUserRepo().findById(messenger.getUserId());
            if (user != null)
            {
                userFound = true;
                messenger.addUser(user);
            }
        }
        catch (ObjectNotInStoreException e)
        {
            logger.debug("User cannot be found by userId: " + messenger.getUserId());
            messenger.setState(State.UserNotFound, e);
        }
        catch (RepositoryException e)
        {
            logger.error("Could not find user by id: ", e);
            messenger.setState(State.SystemError, e);
        }
        return userFound;
    }

    private static boolean anyQualifiedUsers(ForgottenPasswordMessenger messenger)
    {
        List<User> qualifiedUsers = new ArrayList<User>();
        for (User user : messenger.getUsers())
        {
            if (user.isQualified())
            {
                qualifiedUsers.add(user);
            }
        }
        messenger.getUsers().retainAll(qualifiedUsers);
        boolean anyUsersLeft = qualifiedUsers.size() > 0;
        if (!anyUsersLeft)
        {
            logger.debug("No qualified users. userId=" + messenger.getUserId() + " email=" + messenger.getEmail());
            messenger.setState(State.NoQualifiedUsers);
        }
        return anyUsersLeft;
    }
}
