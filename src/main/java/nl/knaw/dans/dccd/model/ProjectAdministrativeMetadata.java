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
import nl.knaw.dans.common.lang.dataset.DatasetState;
import nl.knaw.dans.common.lang.repo.MetadataUnit;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

public class ProjectAdministrativeMetadata extends AbstractTimestampedJiBXObject<ProjectAdministrativeMetadata> implements MetadataUnit, Serializable
{
	private static Logger logger = Logger.getLogger(ProjectAdministrativeMetadata.class);
	private static final long serialVersionUID = 4451112745996608612L;
    private String managerId; // manager(user) of the projects repository data
    private String legalOwnerOrganisationId;

    private DatasetState administrativeState = DatasetState.DRAFT; // default initial state
    private DatasetState previousState;
    // Can't think of a reason to have them transient
    //private transient DateTime lastStateChange = new DateTime();
    //private transient DateTime previousStateChange;
    private DateTime lastStateChange = new DateTime();
    private DateTime previousStateChange;

	public ProjectAdministrativeMetadata()
	{
		// empty
	}

	public DatasetState getAdministrativeState()
	{
		return administrativeState;
	}

	public void setAdministrativeState(DatasetState administrativeState)
	{
		if (this.administrativeState == administrativeState)
			return; // no change

		// Store previous state and date of change

		// Undo is still a state change and should be timestamped
		// so we don't use the old timestamp and comment out the next lines
		//if (administrativeState.equals(previousState)) // undo
        //{
        //    lastStateChange = previousStateChange;
        //	previousState = this.administrativeState;
        //}
        //else
        //{
        	previousState = this.administrativeState;
            previousStateChange = lastStateChange;
            lastStateChange = new DateTime();
        //}

    	this.administrativeState = administrativeState;
	}

    public DatasetState getPreviousAdministrativeState()
    {
        return previousState;
    }

    public DateTime getLastStateChange()
    {
    	if (lastStateChange == null)
    	{
    		logger.warn("lastStateChange is not set, using general timestamp");
    		lastStateChange = getTimestamp();
    	}
        return lastStateChange;
    }

	public String getManagerId()
	{
		return managerId;
	}

	public void setManagerId(String managerId)
	{
		this.managerId = managerId;
	}

	public String getLegalOwnerOrganisationId()
	{
		return legalOwnerOrganisationId;
	}

	public void setLegalOwnerOrganisationId(String legalOwnerOrganisationId)
	{
		this.legalOwnerOrganisationId = legalOwnerOrganisationId;
	}

	/* MetadataUnit methods */
	public final static String UNIT_ID = "PAM";
    String UNIT_LABEL = "Project Administrative Metadata";
    String UNIT_FORMAT = "http://dans.knaw.nl/dccd/projectadministrativemetadata";;
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
