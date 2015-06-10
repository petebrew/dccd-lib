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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangePasswordSpecification
{

    private static Logger logger = LoggerFactory.getLogger(ChangePasswordSpecification.class);

    private ChangePasswordSpecification()
    {

    }

    public static boolean isSatisFiedBy(ChangePasswordMessenger messenger)
    {
        boolean satisfied = true;
        satisfied &= hasSufficientData(messenger);
        if (satisfied && !messenger.isMailContext())
        {
            logger.debug("No mail context: checking for authentication.");
            satisfied &= checkAuthentication(messenger);
        }
        else if (satisfied && messenger.isMailContext())
        {
            logger.debug("Mail context: checking for qualifiedState.");
            satisfied &= AuthenticationSpecification.checkUserStateForForgottenPassword(messenger.getUser());
        }
        return satisfied;
    }

    private static boolean checkAuthentication(ChangePasswordMessenger messenger)
    {
        boolean authenticated = false;
        UsernamePasswordAuthentication authentication =
            new UsernamePasswordAuthentication(messenger.getUserId(), messenger.getOldPassword());
        if (AuthenticationSpecification.isSatisfiedBy(authentication))
        {
            authenticated = true;
        }
        else
        {
            logger.debug("ChangePassword did not get past Authentication" + messenger.toString());
            messenger.setState(ChangePasswordMessenger.State.NotAuthenticated);
            messenger.getExceptions().addAll(authentication.getExceptions());
        }
        return authenticated;
    }

    private static boolean hasSufficientData(ChangePasswordMessenger messenger)
    {
        boolean hasSufficientData = true;
        if (StringUtils.isBlank(messenger.getNewPassword()))
        {
            hasSufficientData = false;
            messenger.setState(ChangePasswordMessenger.State.InsufficientData);
        }
        return hasSufficientData;
    }

}
