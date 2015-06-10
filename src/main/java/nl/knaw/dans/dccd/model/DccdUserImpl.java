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
package nl.knaw.dans.dccd.model;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.Source;

import nl.knaw.dans.common.jibx.JiBXObjectFactory;
import nl.knaw.dans.common.jibx.JiBXUtil;
import nl.knaw.dans.common.lang.annotations.ldap.LdapAttribute;
import nl.knaw.dans.common.lang.annotations.ldap.LdapObject;
import nl.knaw.dans.common.lang.user.User;
import nl.knaw.dans.common.lang.user.UserImpl;
import nl.knaw.dans.common.lang.xml.MinimalXMLBean;
import nl.knaw.dans.common.lang.xml.XMLSerializationException;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.jibx.runtime.JiBXException;

@LdapObject(objectClasses = {"dccdUser", "dansUser", "inetOrgPerson", "organizationalPerson", "person"})
public class DccdUserImpl extends UserImpl implements DccdUser, MinimalXMLBean
{
	private static final long serialVersionUID = 5412668624945549083L;
	private static JiBXUtil<DccdUserImpl> jibxUtil;

	/* Roles (as in Easy) */
    private Set<Role> roles = new HashSet<Role>();
    //private CreatorRole creatorRole;

    private String digitalAuthorIdentifier;

    public DccdUserImpl()
    {
    	// empty
    }

	public DccdUserImpl(final Role...roles )
    {
        for (Role role : roles)
        {
            addRole(role);
        }
    }

    public DccdUserImpl(String userid)
	{
    	super(userid);
	}

	@LdapAttribute(id = "dccdDAI")
    public String getDigitalAuthorIdentifier() {
		return digitalAuthorIdentifier;
	}

    @LdapAttribute(id = "dccdDAI")
	public void setDigitalAuthorIdentifier(String digitalAuthorIdentifier) {
		this.digitalAuthorIdentifier = digitalAuthorIdentifier;
	}

    @LdapAttribute(id = "dccdRoles")
    public Set<Role> getRoles()
    {
        return roles;
    }

	// for JiBX marshalling
	public ArrayList<Role> getRoleArrayList()
	{
		return new ArrayList<Role>(roles);
	}

	// for JiBX unmarshalling
	public void setRoleArrayList(ArrayList<Role> roles)
	{
		this.roles = new HashSet<Role>(roles);
	}

	// for JiBX marshalling
	public ArrayList<String> getRoleStringArrayList()
	{
		ArrayList<Role> rolesList = getRoleArrayList();
		ArrayList<String> rolesStringList = new ArrayList<String>();
		for(Role role: rolesList)
		{
			rolesStringList.add(role.toString());
		}

		return rolesStringList;
	}

	// for JiBX unmarshalling
	public void setRoleStringArrayList(ArrayList<String> roles)
	{
		ArrayList<Role> rolesList = new ArrayList<Role>();
		for(String roleString: roles)
		{
			rolesList.add(Role.valueOf(roleString));
		}

		this.roles = new HashSet<Role>(rolesList);
	}


    /**
     * Used by wicket
     * @param roles
     */
    public void setRoles(Set<Role> roles)
    {
        this.roles = roles;
    }

    @Override
	public void synchronizeOn(User otherUser) {
		super.synchronizeOn(otherUser);
		// copy more if we have a DccdUser
		// feels like we could use reflection here
		if (otherUser instanceof DccdUser) {
			setRoles(((DccdUser)otherUser).getRoles());
			setDigitalAuthorIdentifier(((DccdUser)otherUser).getDigitalAuthorIdentifier());
		}
	}

	public String getRolesString()
    {
        StringBuilder sb = new StringBuilder();
        for (Role role : getRoles())
        {
            sb.append(role);
            sb.append(" ");
        }
        if (sb.length() == 0)
        {
            sb.append("(none) ");
        }
        return sb.toString();
    }

    @LdapAttribute(id = "dccdRoles")
    public void addRole(Role role)
    {
		roles.add(role);
    }

    public boolean removeRole(Role role)
    {
        return roles.remove(role);
    }

    public boolean hasRole(Role... roles)
    {
        boolean hasRole = false;
        if (roles != null)
        {
            for (int i = 0; i < roles.length && !hasRole; i++)
            {
                hasRole = this.roles.contains(roles[i]);
            }
        }
        return hasRole;
    }

//    public CreatorRole getCreatorRole()
//    {
//        if (creatorRole == null)
//        {
//            if (hasRole(Role.ARCHIVIST, Role.ADMIN))
//            {
//                creatorRole = CreatorRole.ARCHIVIST;
//            }
//            else
//            {
//                creatorRole = CreatorRole.DEPOSITOR;
//            }
//        }
//        return creatorRole;
//    }

    public String getDisplayRoles()
    {
        //return StringUtil.commaSeparatedList(roles);
        return ""+roles;
    }

    /** TODO: override the equals and use this as well
     *
     * @param otherUser
     * @return
     */
    public boolean rolesAreEqual(DccdUserImpl otherUser)
    {
        boolean equal = roles.size() == otherUser.roles.size();
        if (equal)
        {
            for (Role role : roles)
            {
                equal &= otherUser.hasRole(role);
            }
        }
        return equal;
    }

    // We have no firstname, but use initials instead
    @Override
    public String getDisplayName()
    {
        StringBuilder sb = new StringBuilder();
        // initals are requered, but check anyway, otherwise we have a strange displayname!
        if (StringUtils.isNotBlank(getInitials()))
        {
        	sb.append(getInitials());
        	sb.append(" ");
        }
        if (StringUtils.isNotBlank(getPrefixes()))
        {
            sb.append(this.getPrefixes());
            sb.append(" ");
        }
        // surname is also required
        sb.append(getSurname());
        return sb.toString().trim();
    }

	public Document asDocument() throws XMLSerializationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Element asElement() throws XMLSerializationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	// copied from AbstractJiBXObject, since multiple inheritance was not available
	public byte[] asObjectXML() throws XMLSerializationException
	{
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try
        {
        	getJiBXUtil().marshalDocument((DccdUserImpl) this, outStream);
        }
        catch (final JiBXException e)
        {
            throw new XMLSerializationException(e);
        }
        return outStream.toByteArray();
    }

	public Source asSource() throws XMLSerializationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream asXMLInputStream() throws XMLSerializationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream asXMLInputStream(int indent)
			throws XMLSerializationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String asXMLString() throws XMLSerializationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String asXMLString(int indent) throws XMLSerializationException
	{
        try
        {
            return getJiBXUtil().marshalDocument(this, indent);
        }
        catch (final JiBXException e)
        {
            throw new XMLSerializationException(e);
        }
	}

	public String getVersion()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void serializeTo(OutputStream outStream)
			throws XMLSerializationException
	{
		// TODO Auto-generated method stub

	}

	public void serializeTo(OutputStream outStream, int indent)
			throws XMLSerializationException
	{
		// TODO Auto-generated method stub

	}

	public void serializeTo(File file) throws XMLSerializationException
	{
		// TODO Auto-generated method stub

	}

	public void serializeTo(File file, int indent)
			throws XMLSerializationException
	{
		// TODO Auto-generated method stub

	}

	public void serializeTo(String encoding, Writer out)
			throws XMLSerializationException
	{
		// TODO Auto-generated method stub

	}

	public void serializeTo(String encoding, Writer out, int indent)
			throws XMLSerializationException
	{
		// TODO Auto-generated method stub

	}

   private JiBXUtil<DccdUserImpl> getJiBXUtil()
   {
	   if (null == jibxUtil)
	   {
		   jibxUtil = JiBXObjectFactory.getJiBXUtil(this.getClass());
	   }

       return jibxUtil;
   }

	/* Groups */

//    private Set<String> groupIds = new HashSet<String>();
//
//    public Set<Group> getGroups()
//    {
//        return new LinkedHashSet<Group>(RepoAccess.getDelegator().getGroups(this));
//    }
//
//    public void joinGroup(Group group)
//    {
//        groupIds.add(group.getId());
//    }
//
//    public boolean leaveGroup(Group group)
//    {
//        return removeGroupId(group.getId());
//    }
//
//    public boolean isMemberOf(Group... groups)
//    {
//        boolean isMemberOf = false;
//        if (groups != null)
//        {
//            for (int i = 0; i < groups.length && !isMemberOf; i++)
//            {
//                isMemberOf = groupIds.contains(groups[i].getId());
//            }
//        }
//        return isMemberOf;
//    }
//
//    @LdapAttribute(id = "easyGroups")
//    public Set<String> getGroupIds()
//    {
//        return groupIds;
//    }
//
//    /**
//     * Used by wicket.
//     * @param groupIds
//     */
//    public void setGroupIds(Set<String> groupIds)
//    {
//        this.groupIds = groupIds;
//    }
//
//    @LdapAttribute(id = "easyGroups")
//    public void addGroupId(String groupId)
//    {
//        groupIds.add(groupId);
//    }
//
//    public boolean removeGroupId(String groupId)
//    {
//        return groupIds.remove(groupId);
//    }
//
//    public String getDisplayGroups()
//    {
//        //return StringUtil.commaSeparatedList(groupIds);
//        return ""+groupIds;
//    }
//
//    public boolean groupsAreEqual(UserImpl otherUser)
//    {
//        boolean equal = groupIds.size() == otherUser.groupIds.size();
//        if (equal)
//        {
//            for (String groupId : groupIds)
//            {
//                equal &= otherUser.groupIds.contains(groupId);
//            }
//        }
//        return equal;
//    }


}
