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
package nl.knaw.dans.dccd.mail;

import java.io.IOException;

import nl.knaw.dans.common.lang.mail.CommonMailer;
import nl.knaw.dans.common.lang.mail.Mailer;
import nl.knaw.dans.common.lang.mail.MailerConfiguration;

public class DccdMailer extends CommonMailer
{
    /**
     * Creates a {@link Mailer} instance with a customized configuration.
     *
     * @param configuration
     * @throws Mailer.MailerException in case of problems creating an address from
     *             {@link MailerConfiguration#getSmtpUserName()} and
     *             {@link MailerConfiguration#getSmtpPassword()}
     */
    public DccdMailer(MailerConfiguration configuration) throws MailerException
    {
        super(configuration);
    }

    /** Lazy initialization to catch exceptions */
    private static Mailer defaultInstance = null;

    /**
     * Gets a {@link Mailer} instance with a default configuration.
     *
     * @throws CommonMailer.MailerException in case of problems creating an address from
     *             {@link DccdMailerConfiguration#getSmtpUserName()} and
     *             {@link DccdMailerConfiguration#getSmtpPassword()}
     * @throws MailerConfiguration.Exception An unexpected {@link IOException} of
     *             {@link #MailerProperties(InputStream)} is turned into a runtime exception.
     */
    public final static Mailer getDefaultInstance() throws MailerException, DccdMailerConfigurationException
    {
        if (defaultInstance == null)
            defaultInstance = new CommonMailer(DccdMailerConfiguration.getDefaultInstance());
        return defaultInstance;
    }

}
