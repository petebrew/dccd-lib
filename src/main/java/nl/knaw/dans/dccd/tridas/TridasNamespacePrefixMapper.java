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
package nl.knaw.dans.dccd.tridas;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Needed for getting readable namespaces like xlink and gml instead of ns1 and ns2 used by getXMLString
 * 
 * @author paulboon
 */
public class TridasNamespacePrefixMapper extends NamespacePrefixMapper
{
	@Override
	public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix)
	{
		if ("http://www.opengis.net/gml".equals(namespaceUri))
			return "gml";
		else if ("http://www.w3.org/1999/xlink".equals(namespaceUri))
			return "xlink";
		// Changed with version 1.2.1
		// else if ( "http://www.tridas.org/1.2".equals(namespaceUri) )
		//else if ("http://www.tridas.org/1.2.1".equals(namespaceUri))
		else if (namespaceUri.startsWith("http://www.tridas.org/"))
			return "tridas"; // now prefixes every element with tridas, except gml and xlink

		return suggestion;
	}
}
