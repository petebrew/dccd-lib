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
package nl.knaw.dans.dccd.application.services;

public class UserServiceException extends Exception {
	private static final long serialVersionUID = 4918945248321781086L;

	public UserServiceException()
    {
    	super();
    }

    public UserServiceException(String message)
    {
        super(message);
    }

    public UserServiceException(Throwable cause)
    {
        super(cause);
    }

    public UserServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
