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

/**
 * The level of permission for a user
 *
 * @author paulboon
 *
 */
public class UserPermission implements Serializable
{
	private static final long serialVersionUID = 3479245102787298901L;
	private String userId = "";
	private ProjectPermissionLevel level = ProjectPermissionLevel.PROJECT; // default

	public UserPermission()
	{
		// empty
	}

	public UserPermission(String userId, ProjectPermissionLevel level)
	{
		this.userId = userId;
		this.level = level;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public ProjectPermissionLevel getLevel()
	{
		return level;
	}

	public void setLevel(ProjectPermissionLevel level)
	{
		this.level = level;
	}

}
