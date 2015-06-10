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

import java.util.List;

import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.common.lang.repo.exception.ObjectNotInStoreException;
import nl.knaw.dans.common.ldap.ds.LdapClient;
import nl.knaw.dans.common.ldap.repo.AbstractGenericRepo;
import nl.knaw.dans.common.ldap.repo.LdapMapper;
import nl.knaw.dans.common.ldap.repo.LdapMappingException;
import nl.knaw.dans.dccd.model.DccdOrganisation;
import nl.knaw.dans.dccd.model.DccdOrganisationImpl;

public class DccdLdapOrganisationRepo extends AbstractGenericRepo<DccdOrganisation> implements DccdOrganisationRepo
{
	public static final String  RDN          = "ou"; // was "cn";

	public DccdLdapOrganisationRepo(LdapClient client, String context) {
		super(client, context, RDN, new LdapMapper<DccdOrganisation>(DccdOrganisationImpl.class));
	}

	@Override
	protected DccdOrganisation unmarshal(Attributes attrs)
			throws LdapMappingException
	{
		return this.getLdapMapper().unmarshal(new DccdOrganisationImpl(), attrs);
	}

	public void addUserToOrganisation(String userId, String organisationId)
			throws ObjectNotInStoreException, RepositoryException
	{
		DccdOrganisation organisation = findById(organisationId);
		// Should we check if that userId is already there,
		// and if, so throw an exception?
		// Also note that we don't check if the user with the given Id exists
		organisation.addUserId(userId);
		update(organisation);
	}

	public DccdOrganisation findOrganisationWithUser(String userId) throws RepositoryException
	{
		DccdOrganisation organisationFound = null;

		List<DccdOrganisation> organisations = findAll();
		for (DccdOrganisation organisation : organisations) {
			if (organisation.hasUserId(userId))
			{
				organisationFound = organisation;
				break;
			}
		}

		return organisationFound;
	}

	public List<DccdOrganisation> find(DccdOrganisation example)
			throws RepositoryException 
	{
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Similar to the UserRepo's findByCommonNameStub, 
	 * except that we return the objects, and not a map with strings
	 * 
	 * @param stub
	 * @param maxCount
	 * @return
	 * @throws RepositoryException
	 */
	public List<DccdOrganisation> findOrganisationsByStub(String stub, long maxCount)
	throws RepositoryException
	{		
        String text = censorHumanoidSearchPhrase(stub);
        // Note the 'ou=' instead of 'cn='
        String filter = "(&(objectClass=" + getObjectClassName() + ")(ou=" + text + "*))";
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        ctls.setCountLimit(maxCount);

        return search(filter, ctls);
	}
}
