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
package nl.knaw.dans.dccd.application.services;


/**
 * A not recoverable exception from the service layer.
 *
 * @author Herman Suijs
 */
public class ServiceException extends Exception
{
    /**
     * Serial version uid.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor with message.
     *
     * @param message Message with the exception
     */
    public ServiceException(final String message)
    {
        super(message);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message Message with the exception
     * @param cause Cause of the exception
     */
    public ServiceException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public ServiceException(final Throwable cause)
    {
        super(cause);
    }
}
