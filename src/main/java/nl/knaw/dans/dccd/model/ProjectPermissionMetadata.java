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

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import nl.knaw.dans.common.jibx.AbstractTimestampedJiBXObject;
import nl.knaw.dans.common.lang.repo.MetadataUnit;

import org.apache.log4j.Logger;

public class ProjectPermissionMetadata extends AbstractTimestampedJiBXObject<ProjectCreationMetadata> implements MetadataUnit, Serializable
{
	private static Logger logger = Logger.getLogger(ProjectPermissionMetadata.class);
	private static final long serialVersionUID = -5685157329061379407L;
	private Map<String, ProjectPermissionLevel> userpermissions = new HashMap<String, ProjectPermissionLevel>();
	// use the lowest level as default
	public static final ProjectPermissionLevel DEFAULT_DEFAULT_LEVEL = ProjectPermissionLevel.MINIMAL;
	private ProjectPermissionLevel defaultLevel = DEFAULT_DEFAULT_LEVEL;

	public void setUserPermission(final String userId, final ProjectPermissionLevel level)
	{
		ProjectPermissionLevel oldLevel = userpermissions.put(userId, level);

		if (oldLevel == null)
		{
			// added new level
			logger.debug("Added new level for user with id=" + userId + " to " + level);
		}
		else
		{
			// changed existing level
			logger.debug("Changed level for user with id=" + userId + " from " + oldLevel + " to " + level);
		}
	}

	public ProjectPermissionLevel getUserPermission(final String userId)
	{
		if (userpermissions.containsKey(userId))
		{
			return userpermissions.get(userId);
		}
		else
		{
			return defaultLevel;
		}
	}

	public void removeUserPermission(final String userId)
	{
		userpermissions.remove(userId);
	}

	public ProjectPermissionLevel getDefaultLevel()
	{
		return defaultLevel;
	}

	public void setDefaultLevel(ProjectPermissionLevel defaultLevel)
	{
		this.defaultLevel = defaultLevel;
	}

	// for JiBX marshalling
	public ArrayList<UserPermission> getUserPermissionsArrayList()
	{
		ArrayList<UserPermission> permissions = new ArrayList<UserPermission>();

		// add all to the list
		Set<Entry<String, ProjectPermissionLevel>> entries = userpermissions.entrySet();
		Iterator<Entry<String, ProjectPermissionLevel>> it = entries.iterator();
		while (it.hasNext())
		{
		    Map.Entry<String, ProjectPermissionLevel> entry = it.next();
		    //System.out.println(entry.getKey() + "-->" + entry.getValue());
		    UserPermission userPermission = new UserPermission(entry.getKey(), entry.getValue());
		    permissions.add(userPermission);
		}

		return permissions;
	}

	// for JiBX unmarshalling
	public void setUserPermissionsArrayList(ArrayList<UserPermission> permissions)
	{
		// clear the map
		userpermissions.clear();

		// put all into the map
		for (UserPermission permission : permissions)
		{
			setUserPermission(permission.getUserId(), permission.getLevel());
		}
	}

	/* MetadataUnit methods */
	public final static String UNIT_ID = "PPM";
    String UNIT_LABEL = "Project Permission Metadata";
    String UNIT_FORMAT = "http://dans.knaw.nl/dccd/projectpermissionmetadata";;
    URI UNIT_FORMAT_URI = URI.create(UNIT_FORMAT);

	public String getUnitFormat()
	{
		return UNIT_FORMAT;
	}

	public URI getUnitFormatURI()
	{
		return UNIT_FORMAT_URI;
	}

	public String getUnitId()
	{
		return UNIT_ID;
	}

	public String getUnitLabel()
	{
		return UNIT_LABEL;
	}
	public boolean isVersionable()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void setVersionable(boolean versionable)
	{
		// TODO Auto-generated method stub

	}



}
