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

import java.util.ArrayList;
import java.util.List;

import nl.knaw.dans.common.lang.user.User;

public class ForgottenPasswordMessenger extends Messenger<ForgottenPasswordMessenger.State>
{
    public enum State
    {
        NothingSend,
        InsufficientData,
        UserNotFound,
        NoQualifiedUsers,
        MailError,
        SystemError,
        /**
         * End state if a new password was send successfully by mail.
         */
        NewPasswordSend,
        /**
         * End state if a link was successfully send by mail.
         */
        UpdateURLSend
    }

    private static final long serialVersionUID = 7949580373425981106L;

    private String userId;
    private final String mailToken;
    private String email;
    private String updateURL;
    private String userIdParamKey;

    // different users can have the same email address
    private List<User> users = new ArrayList<User>();

    public ForgottenPasswordMessenger()
    {
        super(ForgottenPasswordMessenger.State.class);
        mailToken = super.createMailToken(userId);
    }

    public ForgottenPasswordMessenger(String userId, String email)
    {
        super(ForgottenPasswordMessenger.State.class);
        this.userId = userId;
        this.email = email;
        mailToken = super.createMailToken(userId);
    }

    @Override
    public boolean isCompleted()
    {
        return State.NewPasswordSend.equals(getState()) || State.UpdateURLSend.equals(getState());
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getMailToken()
    {
        return mailToken;
    }

    public String getUpdateURL()
    {
        return updateURL;
    }

    public void setUpdateURL(String updateURL)
    {
        this.updateURL = updateURL;
    }

    public void setUserIdParamKey(String userIdParamKey)
    {
        this.userIdParamKey = userIdParamKey;
    }

    public String getUserIdParamKey()
    {
        return userIdParamKey;
    }

    public List<User> getUsers()
    {
        return users;
    }

    void addUser(User user)
    {
        users.add(user);
    }

    protected void setState(State state)
    {
        super.setState(state);
    }

    protected void setState(State state, Throwable e)
    {
        super.setState(state, e);
    }
}
