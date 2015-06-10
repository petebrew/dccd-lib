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
package nl.knaw.dans.dccd.repository.ldap;

import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.common.lang.repo.exception.ObjectNotInStoreException;
import nl.knaw.dans.dccd.model.DccdOrganisation;
import nl.knaw.dans.common.lang.ldap.GenericRepo;

public interface DccdOrganisationRepo extends GenericRepo<DccdOrganisation>
{
	// TODO: Put the Organisation specific stuff here
	// like finding members of the organisation?

	void addUserToOrganisation(String userId, String organisationId)
			throws ObjectNotInStoreException, RepositoryException;

	DccdOrganisation findOrganisationWithUser(String userId)
		throws RepositoryException;
}
