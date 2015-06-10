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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.common.ldap.ds.LdapClient;
import nl.knaw.dans.common.ldap.repo.AbstractLdapUserRepo;
import nl.knaw.dans.common.ldap.repo.LdapMapper;
import nl.knaw.dans.common.ldap.repo.LdapMappingException;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.DccdUserImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DccdLdapUserRepo extends AbstractLdapUserRepo<DccdUser>
{
	private static final Logger logger              = LoggerFactory.getLogger(DccdLdapUserRepo.class);
	
	public DccdLdapUserRepo(LdapClient client, String context)
	{
		super(client, context, new LdapMapper<DccdUser>(DccdUserImpl.class));
	}

    @Override
    protected DccdUser unmarshal(Attributes attrs) throws LdapMappingException
    {
        return getLdapMapper().unmarshal(new DccdUserImpl(), attrs);
    }

    /** Activated non-Admin users
     * Added to get a list of users and not only the id's
     * 
     * @return
     * @throws RepositoryException
     */
    public List<DccdUser> getActiveNormalUsers() throws RepositoryException
    {
    	String filterRestrictions = "(dansState=ACTIVE)(dccdRoles=USER)(!(dccdRoles=ADMIN))";// LDAP specific
        String filter = "(&(objectClass=" + getObjectClassName() + ")(cn=*)" + filterRestrictions + ")";
        
        List<DccdUser> entries = search(filter);
        if (logger.isDebugEnabled())
        {
            logger.debug("Find all found " + entries.size() + " entries in context " + getContext() + ".");
        }
        return entries;
    }    
    
    // find all active normal users
    public Map<String, String> findActiveNormal() throws RepositoryException
    {
    	String filterRestrictions = "(dansState=ACTIVE)(dccdRoles=USER)(!(dccdRoles=ADMIN))";// LDAP specific

        Map<String, String> idNameMap = new LinkedHashMap<String, String>();
        String filter = "(&(objectClass=" + getObjectClassName() + ")(cn=*)" + filterRestrictions + ")";
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        ctls.setReturningAttributes(new String[] {"cn", "uid"});

        try
        {
            NamingEnumeration<SearchResult> resultEnum = getClient().search(getContext(), filter, ctls);
            while (resultEnum.hasMoreElements())
            {
                SearchResult result = resultEnum.next();
                Attributes attrs = result.getAttributes();
                idNameMap.put((String) attrs.get("uid").get(), (String) attrs.get("cn").get());
            }
        }
        catch (NamingException e)
        {
            throw new RepositoryException(e);
        }
        return idNameMap;
    }
    
    // only find users that are active and do not have the admin role
    // Note : almost identical to the findByCommonNameStub, but added restrictions to the filter
    public Map<String, String> findActiveNormalByCommonNameStub(String stub, long maxCount) throws RepositoryException
    {
    	String filterRestrictions = "(dansState=ACTIVE)(dccdRoles=USER)(!(dccdRoles=ADMIN))";// LDAP specific

        Map<String, String> idNameMap = new LinkedHashMap<String, String>();
        String text = censorHumanoidSearchPhrase(stub);
        String filter = "(&(objectClass=" + getObjectClassName() + ")(cn=" + text + "*)" + filterRestrictions + ")";
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        ctls.setCountLimit(maxCount);
        ctls.setReturningAttributes(new String[] {"cn", "uid"});

        try
        {
            NamingEnumeration<SearchResult> resultEnum = getClient().search(getContext(), filter, ctls);
            while (resultEnum.hasMoreElements())
            {
                SearchResult result = resultEnum.next();
                Attributes attrs = result.getAttributes();
                idNameMap.put((String) attrs.get("uid").get(), (String) attrs.get("cn").get());
            }
        }
        catch (NamingException e)
        {
            throw new RepositoryException(e);
        }
        return idNameMap;
    }
    
    //
    public Map<String, String> findActiveInOrganisationByCommonNameStub(String stub, String organisationId, long maxCount) 
    	throws RepositoryException
    {
    	// LDAP specific query
    	String filterRestrictions = "(o="+organisationId+")(dansState=ACTIVE)(dccdRoles=USER)";

        Map<String, String> idNameMap = new LinkedHashMap<String, String>();
        String text = censorHumanoidSearchPhrase(stub);
        String filter = "(&(objectClass=" + getObjectClassName() + ")(cn=" + text + "*)" + filterRestrictions + ")";
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        ctls.setCountLimit(maxCount);
        ctls.setReturningAttributes(new String[] {"cn", "uid"});

        try
        {
            NamingEnumeration<SearchResult> resultEnum = getClient().search(getContext(), filter, ctls);
            while (resultEnum.hasMoreElements())
            {
                SearchResult result = resultEnum.next();
                Attributes attrs = result.getAttributes();
                idNameMap.put((String) attrs.get("uid").get(), (String) attrs.get("cn").get());
            }
        }
        catch (NamingException e)
        {
            throw new RepositoryException(e);
        }
        return idNameMap;
    }
}
