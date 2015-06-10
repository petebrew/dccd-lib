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
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.DccdUser.Role;

/*
 * Note by pboon: copied from eof project package nl.knaw.dans.easy.business.authn;
 * Made it specific for a DccdUser, which has a role (and User from the dans common has not)
 * This could be made generic with <T extends User>, but then no role specifics.
 */

/**
 * Messenger object for user registration.
 *
 * @author ecco Feb 12, 2009
 */
public class UserRegistration extends Messenger<UserRegistration.State>
{

    /**
     *
     */
    private static final long serialVersionUID = -4037234100764375061L;

    /**
     * Indicates the state of the registration.
     * @author ecco Feb 17, 2009
     *
     */
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
        UserIdCannotBeBlank,
        InitialsCannotBeBlank,
        //FirstnameCannotBeBlank,
        SurnameCannotBeBlank,
        PasswordCannotBeBlank,
        EmailCannotBeBlank,
        // added Organisation and department for DCCD
        OrganisationCannotBeBlank,
        DepartmentCannotBeBlank,

        /**
         * Rejected because the userId already exists: not registered.
         */
        UserIdNotUnique,
        /**
         * So far so good, but the registration validation mail could not be send: not registered.
         */
        MailNotSend,
        /**
         * Oeps, but it happens: not registered.
         */
        SystemError,
        /**
         * Registration accepted and all necessary steps undertaken: registered.
         */
        Registered
    }

    private final DccdUser user;
    // need this, because when registering we might have a new organisation,
    //that also needs to be registered
    private DccdOrganisation organisation;

	private final String mailToken;
//    private String validationUrl;
    /**
     * URL which leads to the page where the administrator can 'activate' a registered user
     */
    private String activationUrl;

	public UserRegistration(final DccdUser user)
    {
        super(UserRegistration.State.class);
        this.user = user;
        this.user.addRole(Role.USER);
        mailToken = super.createMailToken(user.getId());
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

    public DccdUser getUser()
    {
        return user;
    }

    public String getUserId()
    {
        return user.getId();
    }

    public String getMailToken()
    {
        return mailToken;
    }

    public DccdOrganisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(DccdOrganisation organisation) {
		this.organisation = organisation;
	}


//    public String getValidationUrl()
//    {
//        return validationUrl;
//    }
//
//    public void setValidationUrl(String validationUrl)
//    {
//        this.validationUrl = validationUrl;
//    }

    public String getActivationUrl() {
		return activationUrl;
	}

	public void setActivationUrl(String activationUrl) {
		this.activationUrl = activationUrl;
	}

    @Override
    public String toString()
    {
        return  super.toString() + " [state=" + getState() + " user="
                + (user == null ? "null" : user.toString()) + "] " + getExceptionsAsString();
    }

}
