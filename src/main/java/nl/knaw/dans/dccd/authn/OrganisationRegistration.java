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
package nl.knaw.dans.dccd.authn;

import nl.knaw.dans.dccd.model.DccdOrganisation;

/**
 * Wraps the Organsation object and
 * allows to track the state of the registration process for this organisation
 *
 * @author paulboon
 *
 */
public class OrganisationRegistration extends Messenger<OrganisationRegistration.State> {
	private static final long serialVersionUID = -4993453204492584493L;

	public enum State
    {
        /**
         * Registration is not effectuated.
         */
        NotRegistered,
        /**
         * Rejected because data is invalid, inappropriate, insufficient or indecent: not registered.
         */
        InvalidData,
        OrganisationIdCannotBeBlank,

        /**
         * Rejected because the Id already exists: not registered.
         */
        OrganisationIdNotUnique,
        /**
         * Oeps, but it happens: not registered.
         */
        SystemError,
        /**
         * Registration accepted and all necessary steps undertaken: registered.
         */
        Registered
    }

	private final DccdOrganisation organisation;

	public OrganisationRegistration(final DccdOrganisation organisation) {
    	super(OrganisationRegistration.State.class);

		this.organisation = organisation;
	}

    protected void setState(State state)
    {
        super.setState(state);
    }

    @Override
    protected void setState(State state, Throwable e)
    {
        super.setState(state, e);
    }

    @Override
    public String toString()
    {
        return  super.toString() +
        	" [state=" + getState() + " organisation=" +
        	(organisation == null ? "null" : organisation.toString()) + "] " +
            getExceptionsAsString();
    }

    public DccdOrganisation getOrganisation() {
		return organisation;
	}

    public String getOrganisationId() {
		return organisation.getId();
	}

}
