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
//package nl.knaw.dans.easy.util;
package nl.knaw.dans.dccd.authn;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Utility methods for security purposes.
 *
 * @author Herman Suijs
 */
public final class SecurityUtil
{
    /**
     * Hashing algorithm used.
     */
    private static final String HASHING_ALGORITHM              = "SHA1";

    /**
     * Length of the generated random string.
     */
    public static final int     GENERATED_RANDOM_STRING_LENGTH = 20;

    /**
     * Default constructor.
     */
    private SecurityUtil()
    {
        // Do not instantiate.
    }

    /**
     * Create a new object and return its representation as a random string.
     *
     * @return random string.
     */
    public static String getRandomString()
    {
        String randomString = RandomStringUtils.random(GENERATED_RANDOM_STRING_LENGTH);
        return randomString;
    }

    /**
     * Create hashCode.
     *
     * @param strings memberStrings for the hashCode.
     * @return integer hash
     */
    public static int generateHashCode(final String... strings)
    {
        HashCodeBuilder builder = new HashCodeBuilder(12345, 54321);
        for (String memberString : strings)
        {
            builder.append(memberString);
        }
        return builder.toHashCode();
    }

    /**
     * Create hashCode string.
     *
     * @param strings memberStrings for the hashCode
     * @return String hash
     */
    public static String generateHashCodeString(final String... strings)
    {
        MessageDigest messageDigest;
        String returnValue = null;
        try
        {
            messageDigest = MessageDigest.getInstance(HASHING_ALGORITHM);
            StringBuilder completeHashingString = new StringBuilder();
            for (String hashString : strings)
            {
                completeHashingString.append(hashString);
            }
            messageDigest.update(completeHashingString.toString().getBytes());
            returnValue = new String(messageDigest.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            // use HashCodeBuilder to generate a fallback hashcode.
            returnValue = Integer.valueOf(generateHashCode(strings)).toString();
        }

        return returnValue;
    }

}
