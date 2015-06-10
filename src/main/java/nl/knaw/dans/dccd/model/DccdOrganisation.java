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

import java.util.Set;

import nl.knaw.dans.common.lang.user.RepoEntry;

/**
 * The organisation can be a research institute or laboratory
 * for which the dccdUser works
 * Because we can have more than one user for an organisation
 * the organisation information is kept in this separate entity
 * instead of duplicating it for every user.
 * The users of the dccd application must provide an organisation.
 * The DccdOrganisation object won't have DccdUser objects, but the Id's instead
 *
 */
public interface DccdOrganisation  extends RepoEntry {
    public enum State
    {
        /**
         * The user has successfully registered, but has not validated the registration; the account cannot be used
         * (yet).
         */
        REGISTERED,
        /**
         * The user has confirmed the registration and the confirmation was valid; the user has not logged in for the
         * first time.
         */
        CONFIRMED_REGISTRATION,
        /**
         * The user has a valid registration; the account can be used.
         */
        ACTIVE,
        /**
         * The user is blocked; the account cannot be used.
         */
        BLOCKED
    }


	String getId();
	void setId(String organisationId);

	String getAddress();
	void setAddress(String address);

	String getPostalCode();
	void setPostalCode(String postalCode);

	String getCity();
	void setCity(String city);

	String getCountry();
	void setCountry(String country);

	// User.State is used here, instead of an Organisation specific state or a more general State
	State getState();
	void setState(State state);

	String getDescription();
	void setDescription(String description);

    Set<String> getUserIds();
    String getUserIdsString();
    void addUserId(String userId);
    boolean removeUserId(String userId);
    boolean hasUserId(String... userIds);
    boolean hasUsers();
}
