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
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.Source;

import nl.knaw.dans.common.jibx.JiBXObjectFactory;
import nl.knaw.dans.common.jibx.JiBXUtil;
import nl.knaw.dans.common.lang.annotations.ldap.LdapAttribute;
import nl.knaw.dans.common.lang.annotations.ldap.LdapObject;
import nl.knaw.dans.common.lang.xml.MinimalXMLBean;
import nl.knaw.dans.common.lang.xml.XMLSerializationException;
import nl.knaw.dans.dccd.repository.ldap.LdapUserPathTranslator;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.dom4j.Document;
import org.dom4j.Element;
import org.jibx.runtime.JiBXException;

@LdapObject(objectClasses = {"dccdUserOrganisation", "organizationalUnit"})
public class DccdOrganisationImpl implements DccdOrganisation, MinimalXMLBean
{
	private static final long serialVersionUID = -79162650700094401L;
	private static JiBXUtil<DccdOrganisationImpl> jibxUtil;

	// The ldap attribute "uniqueMember" is being used
    private Set<String> userIds = new HashSet<String>();

    @LdapAttribute(id = "description")
    private String description;

	@LdapAttribute(id = "postalAddress")
    private String address;

    @LdapAttribute(id = "postalCode")
    private String postalCode;

    @LdapAttribute(id = "l")
    private String city;

    @LdapAttribute(id = "st")
    private String country;

    @LdapAttribute(id = "dansState")
    private State state;

    @LdapAttribute(id = "ou", required = true) // was "cn"
    private String organisationId;
    // This will probably be the name of the organisation

	public DccdOrganisationImpl()
	{
		// Empty
	}

	public DccdOrganisationImpl(String organisationId)
    {
        this.organisationId = organisationId;
	}

    // we need to place a different string into ldap tha what we have here,
    // and we don't want repository details here!
    // possible solutions atre extending the annotations for the ldapmapper...
    //@LdapAttribute(id = "uniqueMember", valuePrefix="uid=", valuePostifx=",ou=users...")
    //@LdapAttribute(id = "uniqueMember", valueTranslator=userPathTranslator.class)

    @LdapAttribute(id = "uniqueMember", valueTranslator=LdapUserPathTranslator.class)
    public Set<String> getUserIds()
    {
        return userIds;
    }

    //@LdapAttribute(id = "uniqueMember", valueTranslator=LdapUserPathTranslator.class)
    public void setUserIds(Set<String> userIds)
    {
        this.userIds = userIds;
    }

    public String getUserIdsString()
    {
        StringBuilder sb = new StringBuilder();
        for (String userId : getUserIds())
        {
            sb.append(userId);
            sb.append(" ");
        }
        if (sb.length() == 0)
        {
            sb.append("(none) ");
        }
        return sb.toString();
    }

    //@LdapAttribute(id = "uniqueMember")
    @LdapAttribute(id = "uniqueMember", valueTranslator=LdapUserPathTranslator.class)
    public void addUserId(String userId)
    {
    	userIds.add(userId);
    }

    public boolean removeUserId(String userId)
    {
        return userIds.remove(userId);
    }

    public boolean hasUserId(String... userIds)
    {
        boolean hasUserId = false;
        if (userIds != null)
        {
            for (int i = 0; i < userIds.length && !hasUserId; i++)
            {
                hasUserId = this.userIds.contains(userIds[i]);
            }
        }
        return hasUserId;
    }

    public boolean hasUsers()
    {
    	return (0 == getUserIds().size())?false:true;
    }

	public String getId() {
		return organisationId;
	}

	public void setId(String organisationId) {
		this.organisationId = organisationId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    /**
     * String representation.
     *
     * @return string representation
     */
    @Override
    public String toString()
    {
        return super.toString() + " [" + organisationId + "] " + " state=" + state;
    }

    /**
     * Test if object is equal.
     *
     * @param obj
     *        object to test
     * @return true if object is equal.
     */
    @Override
    public boolean equals(final Object obj)
    {
        boolean equals = false;
        if (obj != null)
        {
            if (obj == this)
            {
                equals = true;
            }
            else
            {
                if (obj.getClass() == this.getClass())
                {
                    final DccdOrganisationImpl otherOrganisation = (DccdOrganisationImpl) obj;
                    equals = new EqualsBuilder()
                        .append(this.organisationId, otherOrganisation.organisationId)
                        .append(this.state, otherOrganisation.state)
                        .isEquals();

                }
            }
        }

        return equals;
    }

    /**
     * Return hashCode.
     *
     * @return hashcode
     */
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(1, 3)
            .append(this.organisationId)
            .append(this.state)
            .toHashCode();
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

	public byte[] asObjectXML() throws XMLSerializationException
	{
       final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try
        {
        	getJiBXUtil().marshalDocument((DccdOrganisationImpl) this, outStream);
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

   private JiBXUtil<DccdOrganisationImpl> getJiBXUtil()
   {
	   if (null == jibxUtil)
	   {
		   jibxUtil = JiBXObjectFactory.getJiBXUtil(this.getClass());
	   }

       return jibxUtil;
   }
}
