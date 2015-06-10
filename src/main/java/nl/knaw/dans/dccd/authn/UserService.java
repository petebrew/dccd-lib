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
/**
 *
 */
package nl.knaw.dans.dccd.authn;

import nl.knaw.dans.common.lang.user.User;
import nl.knaw.dans.common.lang.ldap.UserRepo;

/**
 * Services related to the user.
 *
 * @author ecco
 */
public interface UserService
{
	// Added by pboon to get an access point for the user repository
	// only AuthenticationSpecification is using it right now...
	UserRepo<? extends User> getUserRepo();

    UsernamePasswordAuthentication newUsernamePasswordAuthentication();

//    RegistrationMailAuthentication newRegistrationMailAuthentication(final String userId, final String returnedTime,
//            final String returnedToken);
//
//    ForgottenPasswordMailAuthentication newForgottenPasswordMailAuthentication(final String userId,
//            final String returnedTime, final String returnedToken);

    void authenticate(Authentication authentication);


//    void logout(User user);
//
//    User getUserById(String uid) throws ObjectNotInDataLayerException, ServiceException;
//
//    List<User> retrieveUserByEmail(String email) throws ServiceException;
//
//    List<User> getUsersByRole(Role role) throws ServiceException;
//
//    Map<String, String> findByCommonNameStub(String stub, long maxCount) throws ServiceException;
//
//    User update(User updater, User user) throws ServiceException;
//
//    /**
//     * Handle registration of a new user.
//     *
//     * @param registration
//     *        registration messenger with valid user and confirmation url
//     * @see RegistrationSpecification
//     */
//    Registration handleRegistrationRequest(Registration registration);
//
//    void changePassword(ChangePasswordMessenger messenger);
//
//    void handleForgottenPasswordRequest(ForgottenPasswordMessenger messenger);
//
//    List<User> getAllUsers() throws ServiceException;
//
//    List<Group> getAllGroups() throws ServiceException;
//
//    List<String> getAllGroupIds() throws ServiceException;
//
//    OperationalAttributes getOperationalAttributes(User user);
//
//    OperationalAttributes getOperationalAttributes(Group group);

}
