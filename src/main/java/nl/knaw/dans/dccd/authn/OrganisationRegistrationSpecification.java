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
import nl.knaw.dans.dccd.application.services.DccdUserService;
import nl.knaw.dans.dccd.model.DccdOrganisation;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Refactoring note: commonalities with UserRegistrationSpecification
 *
 * @author paulboon
 *
 */
public final class OrganisationRegistrationSpecification {
	private static Logger logger = LoggerFactory.getLogger(OrganisationRegistrationSpecification.class);

	public static boolean isSatisfiedBy(OrganisationRegistration registration)
    {
        boolean satisfied = hasSufficientData(registration) && hasUniqueID(registration);
        return satisfied;
    }

    private static boolean hasSufficientData(OrganisationRegistration registration)
    {
        // TODO more sophisticated tests
        boolean sufficientData = true;
        DccdOrganisation organisation = registration.getOrganisation();
        if (StringUtils.isBlank(organisation.getId()))
        {
            sufficientData = false;
            registration.setState(OrganisationRegistration.State.OrganisationIdCannotBeBlank);
        }

        return sufficientData;
    }

    private static boolean hasUniqueID(OrganisationRegistration registration)
    {
        boolean hasUniqueId = false;
        String organisationId = registration.getOrganisation().getId();
        try
        {
            if (!DccdUserService.getService().getOrganisationRepo().exists(organisationId))
            {
                hasUniqueId = true;
            }
            else
            {
                registration.setState(OrganisationRegistration.State.OrganisationIdNotUnique);
            }
        }
        catch (RepositoryException e)
        {
            logger.error("Could not verify if Id is unique: ", e);
            registration.setState(OrganisationRegistration.State.SystemError, e);
        }
        return hasUniqueId;
    }

}
