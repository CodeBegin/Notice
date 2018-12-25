/*
 * Copyright (c) [2018] [Seung Hwan, Jin]

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */

package com.jdev.apns.main.util;

import java.io.*;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.*;

import org.apache.log4j.Logger;

public class CertificateReader {
	private final static Logger LOGGER = Logger.getLogger(CertificateReader.class); 

	/**
	 * Method that returns private key entry if found in the key store.
	 *  
	 * @param fileLocation is a key store location
	 * @param password
	 * 
	 * @throws KeyStoreException 
	 * @throws IOException
	 */
	public static PrivateKeyEntry getPrivateKey(InputStream p12, final String password) 
			throws KeyStoreException, IOException {
		Objects.requireNonNull(password, "Password must not be null");

		final KeyStore keyStore = KeyStore.getInstance("PKCS12");
		char[] pwdChars = password.toCharArray();

		try {
			keyStore.load(p12, pwdChars);
		} catch (NoSuchAlgorithmException | CertificateException e) {
			LOGGER.fatal("Failed to load certificate", e);
			throw new KeyStoreException(e);
		}

		Enumeration<String> aliases = keyStore.aliases();
		while (aliases.hasMoreElements()) {
			KeyStore.Entry entry;

			try {
				entry = keyStore.getEntry(aliases.nextElement(), 
						new KeyStore.PasswordProtection(pwdChars));
				if (entry instanceof KeyStore.PrivateKeyEntry) {
					return (PrivateKeyEntry) entry;
				}
			} catch (NoSuchAlgorithmException | UnrecoverableEntryException e) {
				LOGGER.fatal("Failed to get entry from the keyStore", e);
				throw new KeyStoreException(e);
			}
		}// iterate through aliases found in keyStore

		LOGGER.fatal("No private key entries were found");
		throw new KeyStoreException("No private key entries were found");	
	}

}
