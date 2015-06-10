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

public class MailAuthentication extends Authentication
{
    private static final long serialVersionUID = -6084491600104733541L;

    private final String returnedTime;
    private final String returnedToken;

    protected MailAuthentication(String userId, String returnedTime, String returnedToken)
    {
        super(userId, returnedTime + returnedToken);
        this.returnedTime = returnedTime;
        this.returnedToken = returnedToken;
    }

    public String getReturnedTime()
    {
        return returnedTime;
    }

    public String getReturnedToken()
    {
        return returnedToken;
    }

}
