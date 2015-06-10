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

import java.io.*;
import java.util.Properties;

import nl.knaw.dans.common.lang.mail.CommonMailerConfiguration;
import nl.knaw.dans.common.lang.mail.Mailer;
import nl.knaw.dans.common.lang.mail.MailerConfiguration;
import nl.knaw.dans.dccd.application.services.DccdConfigurationService;

/**
 * TODO get (default) settings from DCCD configuration service!
 * 
 * Configuration of a {@link Mailer} instance.
 *
 * @author Joke Pol
 *
 */
public final class DccdMailerConfiguration extends CommonMailerConfiguration implements
        MailerConfiguration
{
	static Properties settings = DccdConfigurationService.getService().getSettings();
	
    /** The default value for the SMTP host. */
    public static final String SMTP_HOST_DEFAULT = settings.getProperty("mail.smtp.host");//"localhost";//"mailrelay.knaw.nl";

    /** Default value for the senders e-mail address. */
    public static final String FROM_ADDRESS_DEFAULT = settings.getProperty("mail.fromAddress");//"info@dans.knaw.nl";

    /** Default value for the senders name. */
    public static final String FROM_NAME_DEFAULT = "DCCD Team";

    /** Lazy initialization to catch exceptions */
    private static MailerConfiguration defaultInstance = null;

    /**
     * Creates a customized instance.
     *
     * @param input The customized configuration values.
     * @throws IOException IOException If an error occurred when reading from the input stream.
     * @throws IllegalArgumentException If the input stream contains a malformed UniCode escape
     *             sequence.
     */
    public DccdMailerConfiguration(final InputStream inputStream) throws IOException
    {
        super(inputStream);
        if (getSmtpHost() == null)
            setSmtpHost(SMTP_HOST_DEFAULT);
        if (getSenderName() == null)
            setSenderName(FROM_NAME_DEFAULT);
        if (getFromAddress() == null)
            setFromAddress(FROM_ADDRESS_DEFAULT);
    }

    /**
     * Creates a customized instance. Calls {@link #MailerProperties(InputStream)} with a wrapped
     * string.
     *
     * @param input The customized configuration values. If {@link #SMTP_HOST_KEY} is not specified,
     *            no host will be set and no mails will be sent.
     * @return A customized instance.
     * @throws MailerConfiguration.Exception An unexpected {@link IOException} of
     *             {@link #MailerProperties(InputStream)} is turned into a runtime exception.
     */
    public static MailerConfiguration createCustomized(final String input) throws DccdMailerConfigurationException
    {
        try
        {
            final InputStream inputStream =
                    input == null ? (InputStream) null : new ByteArrayInputStream(input.getBytes());
            return new DccdMailerConfiguration(inputStream);
        }
        catch (final IOException exception)
        {
            throw new DccdMailerConfigurationException("Unexpected exception", exception);
        }
    }

    /**
     * Gets a default instance. Calls {@link #MailerProperties(InputStream)} with a null argument.
     *
     * @return A default instance.
     * @throws MailerConfiguration.Exception An unexpected {@link IOException} of
     *             {@link #MailerProperties(InputStream)} is turned into a runtime exception.
     */
    public static MailerConfiguration getDefaultInstance() throws DccdMailerConfigurationException
    {
        if (defaultInstance == null)
        {
            try
            {
                defaultInstance = new DccdMailerConfiguration((InputStream) null);
            }
            catch (final IOException e)
            {
                throw new DccdMailerConfigurationException("Unexpected exception", e);
            }
        }
        return defaultInstance;
    }
}
