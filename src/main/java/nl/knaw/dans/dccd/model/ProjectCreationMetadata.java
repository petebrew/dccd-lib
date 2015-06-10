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

import nl.knaw.dans.common.jibx.AbstractTimestampedJiBXObject;
import nl.knaw.dans.common.lang.repo.MetadataUnit;

public class ProjectCreationMetadata extends AbstractTimestampedJiBXObject<ProjectCreationMetadata> implements MetadataUnit, Serializable
{
	private static final long serialVersionUID = 7894625118325723957L;
	private DccdUser user;
	private DccdOrganisation organisation;

	public ProjectCreationMetadata()
	{
		// empty
	}

	public ProjectCreationMetadata(DccdUser user, DccdOrganisation organisation)
	{
		this.user = user;
		this.organisation = organisation;
	}

	public DccdUser getUser()
	{
		return user;
	}

	public void setUser(DccdUser user)
	{
		this.user = user;
	}

	public DccdOrganisation getOrganisation()
	{
		return organisation;
	}

	public void setOrganisation(DccdOrganisation organisation)
	{
		this.organisation = organisation;
	}

	/* MetadataUnit methods */
	public final static String UNIT_ID = "PCM";
    String UNIT_LABEL = "Project Creation Metadata";
    String UNIT_FORMAT = "http://dans.knaw.nl/dccd/projectcreationmetadata";;
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
