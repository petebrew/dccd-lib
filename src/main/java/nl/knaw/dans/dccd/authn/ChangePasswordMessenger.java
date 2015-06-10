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

import nl.knaw.dans.common.lang.user.User;

public class ChangePasswordMessenger extends Messenger<ChangePasswordMessenger.State>
{
    public enum State
    {
        NotChanged,
        InsufficientData,
        NotAuthenticated,
        SystemError,
        PasswordChanged
    }

    private static final long serialVersionUID = -4626315934144818803L;
    private final User user;
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
    private String token;
    private final boolean mailContext;

    public ChangePasswordMessenger(User user, boolean mailContext)
    {
        super(ChangePasswordMessenger.State.class);
        this.user = user;
        this.mailContext = mailContext;
    }

    public User getUser()
    {
        return user;
    }

    public String getUserId()
    {
        return user.getId();
    }

    public String getOldPassword()
    {
        return oldPassword;
    }

    public String getNewPassword()
    {
        return newPassword;
    }

    public void setOldPassword(final String oldPassword)
    {
        this.oldPassword = oldPassword;
    }

    public void setNewPassword(final String newPassword)
    {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword()
    {
        return confirmPassword;
    }

    public void setConfirmPassword(final String confirmPassword)
    {
        this.confirmPassword = confirmPassword;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public boolean isMailContext()
    {
        return mailContext;
    }

    protected void setState(final State state)
    {
        super.setState(state);
    }

    protected void setState(final State state, final Throwable e)
    {
        super.setState(state, e);
    }

    public String getDisplayName()
    {
        return user.getDisplayName();
    }

    @Override
    public String toString()
    {
        return  this.getClass().getSimpleName() + " [mailContext=" + mailContext + " state=" + getState() + " user="
            + (user == null ? "null" : user.toString()) + "] " + getExceptionsAsString();
    }
}
