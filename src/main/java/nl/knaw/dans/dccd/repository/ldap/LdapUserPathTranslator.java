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

import nl.knaw.dans.common.lang.annotations.ldap.LdapAttributeValueTranslator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this translator to map a 'plain' user id string in a POJO on
 * a complete ldap path in the dccd user ldap entry
 *
 * For example:
 * if the user id is;
 * "someone"
 *  then the entry in ldap for
 *  the value of the attribute uniqueMember of
 *  the dccdOrganisation objectClass must be;
 * "uid=someone,ou=users,ou=dccd,dc=dans,dc=knaw,dc=nl"
 *
 * @author paulboon
 *
 * TODO add type parameter String
 */
public class LdapUserPathTranslator implements LdapAttributeValueTranslator<Object>
{
    private static final Logger logger = LoggerFactory.getLogger(LdapUserPathTranslator.class);

	// It would be nice if the following constants could be extracted
	// from other ldap classes
	static final String PREFIX = "uid=";
	static final String POSTFIX = ",ou=users,ou=dccd,dc=dans,dc=knaw,dc=nl";

	public Object fromLdap(Object value)
	{
		if (value instanceof String)
        {
			return removePreAndPostFix((String)value);
        }
		else
		{
			logger.debug("not a String object, but " + value.getClass().getSimpleName());
			return value; // passthrough, maybe throw an exception?
		}
	}

	public Object toLdap(Object value)
	{
		if (value instanceof String)
        {
			return addPreAndPostFix((String)value);
        }
		else
		{
			logger.debug("not a String object, but " + value.getClass().getSimpleName());
			return value; // passthrough, maybe throw an exception?
		}
	}

	private String addPreAndPostFix(String value)
	{
		String translatedValue = PREFIX + value + POSTFIX;
		logger.debug("translated value from \"" + value + "\" to \"" + translatedValue + "\"");

		return translatedValue;
	}

	private String removePreAndPostFix(String value)
	{
		// Could use regexp's here but lets try simple approach:
		// take the part from the first "=" to the first ","
		// Assume userId's don't have "," in it,
		// otherwise we need to take care of escaped comma's "\,"
		//
		// We could also check if the given string really
		// starts with the prefix and ends with the postfix
		// but we DON'T
		int beginIndex = value.indexOf('=') + 1;
		int endIndex = value.indexOf(',');
		String translatedValue = value.substring(beginIndex, endIndex);
		logger.debug("translated value from \"" + value + "\" to \"" + translatedValue + "\"");

		return translatedValue;
	}
}
