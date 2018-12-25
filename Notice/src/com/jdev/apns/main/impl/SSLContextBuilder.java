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

package com.jdev.apns.main.impl;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import javax.net.ssl.*;

import org.apache.log4j.Logger;

import com.jdev.apns.main.Constants;

public class SSLContextBuilder {
	/**
	 * Logger for class ApnsUtil
	 */
	private final static Logger LOGGER = Logger.getLogger(SSLContextBuilder.class);

	private String algorithm;
	private KeyManagerFactory keyManagerFactory;
	private TrustManagerFactory trustManagerFactory;

	public SSLContextBuilder setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
		return this;
	}

	/**
	 * Method called by setCredentials that checks if the KeyStore is valid
	 * If there are more than one key, it checks if there is a key alias that
	 * matches to the keyStoer created by using credentials provided by user
	 * 
	 * @param keyStore InputStream
	 * @param password char[]
	 * @return KeyStore if a KeyStore with given input exists
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	private KeyStore getKeyStore(InputStream keyStore, char[] password) 
			throws NoSuchAlgorithmException, IOException {
		try {
			final KeyStore clientStore = KeyStore.getInstance("PKCS12");
			clientStore.load(keyStore, password);

			final Enumeration<String> aliases = clientStore.aliases();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();

				if (clientStore.isKeyEntry(alias)) {
					Key key = clientStore.getKey(alias, password);
					Certificate[] chain = clientStore.getCertificateChain(alias);
					clientStore.setKeyEntry(alias, key, password, chain);
					return clientStore;
				}
			}
		} catch (KeyStoreException | CertificateException e) {
			LOGGER.error("Failed to load keystore with " + keyStore, e);
		} catch (UnrecoverableKeyException e) {
			LOGGER.error("Failed to retrieve key from the entry found ", e);
		}
		LOGGER.fatal("Failed to retrieve a keystore");
		throw new RuntimeException("Failed to retrieve a keystore.");
	}//getKeyStore()

	/**
	 * Method called by setCredentials that gets default key manager factory
	 * 
	 * @param keystore
	 * @param password
	 */
	private void setDefaultKeyManagerFactory (KeyStore keystore, char[] password) {
		try {
			keyManagerFactory = 
					KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keystore, password);
		} catch (GeneralSecurityException e) {
			LOGGER.fatal("Failed to initiated KeyManagerFactory", e);
			throw new RuntimeException(e);
		}
	}

	@Deprecated
	public SSLContextBuilder setKeyManagerFactory (KeyStore keystore, String password) { 
		try {
			keyManagerFactory = 
					KeyManagerFactory.getInstance(algorithm);
			keyManagerFactory.init(keystore, password.toCharArray());
			return this;
		} catch (GeneralSecurityException e) {
			LOGGER.fatal("Failed to initiated KeyManagerFactory", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method called by setCredentials that gets default trust manager factory
	 */
	private void setDefaultTrustManagerFactory () { 
		try {
			trustManagerFactory = 
					TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init((KeyStore)null);
		} catch (GeneralSecurityException e) {
			LOGGER.fatal("Failed to initiated KeyManagerFactory", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method that gets trustManagerFactory using a specific KeyStore
	 * @param keyStore
	 * @return
	 */
	public SSLContextBuilder setTrustManagerFactory (KeyStore keyStore) { 
		try {
			trustManagerFactory = 
					TrustManagerFactory.getInstance(algorithm);
			trustManagerFactory.init(keyStore);
			return this;
		} catch (GeneralSecurityException e) {
			LOGGER.fatal("Failed to initiated KeyManagerFactory", e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method called to get KeyStore Object as InputStream
	 * It checks which type of KeyStore Object user provided
	 * and returns KeyStore object as InputStream for further use
	 * 
	 * @param keyStore
	 * @return InputStream if a corresponding instance was found
	 */
	private InputStream getKeyStoreInputStream(Object keyStore) {
		InputStream clientStore = null;
		if (keyStore instanceof String) {
			try {
				clientStore = new FileInputStream(new File((String) keyStore));
			} catch (FileNotFoundException e) {
				LOGGER.fatal("file not found: ", e);
			}
		} else if (keyStore instanceof File) {
			try {
				clientStore = new FileInputStream((File) keyStore);
			} catch (FileNotFoundException e) {
				LOGGER.fatal("file not found: ", e);
			}
		} else if (keyStore instanceof InputStream) {
			clientStore = (InputStream) keyStore;
		}
		
		if (clientStore == null) {
			LOGGER.fatal("Failed to get the instance of KeyStore");
			throw new IllegalArgumentException(keyStore.getClass().getName());
		}
		return clientStore;
	}

	/**
	 * Method that sets default key manager factory and trust manager factory
	 * with the credentials provided by the user
	 * 
	 * @param keyStore Object
	 * @param password String
	 * @return class itself
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public SSLContextBuilder setCredentials(Object keyStore, String password) 
			throws NoSuchAlgorithmException, IOException {
		
		KeyStore clientStore = getKeyStore(
				getKeyStoreInputStream(keyStore), password.toCharArray());
		setDefaultKeyManagerFactory(clientStore, password.toCharArray());
		setDefaultTrustManagerFactory();
		return this;
	}

	/**
	 * Method that essentially checks if KeyManagerFactory and TrustManagerFactory
	 * were created successfully with the credentials provided by a user 
	 * 
	 * @return SSLContext if the SSLContext initiated successfully.
	 * @throws NoSuchAlgorithmException
	 */
	public SSLContext build() throws NoSuchAlgorithmException {
		if (keyManagerFactory == null) {
			throw new RuntimeException("Failed to retrieve KeyManagerFactory");
		}

		if (trustManagerFactory == null) {
			throw new RuntimeException("Failed to retrieve TrustManagerFactory");
		}

		try {
			final SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(keyManagerFactory.getKeyManagers(), 
					trustManagerFactory.getTrustManagers(), null);
			return sslContext;
		} catch (KeyManagementException e) {
			LOGGER.error("Failed to create sslContext", e);
			throw new RuntimeException(e);
		}
	}

	@Deprecated
	public boolean createSocketFactory(InputStream keyStore, String password) {
		try {
			setCredentials(keyStore, password);
			SSLContext sslContext = build();
			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(
//					"gateway.sandbox.push.apple.com",2195);
					Constants.APNS_HOST_DEVELOPMENT,2195);
//					Constants.APNS_HOST_PRODUCTION, Constants.ALTERNATE_APNS_PORT);
			String[] cipherSuites = sslSocket.getSupportedCipherSuites();
			sslSocket.setEnabledCipherSuites(cipherSuites);
			
			sslSocket.startHandshake();
			
			InputStream is = sslSocket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();
			
			if (sb != null) {
				System.out.println(sb.toString());
			}
			
			is.close();
			sslSocket.close();
			
			return true;
		} catch (Exception e) {
			LOGGER.error("Failed to create sslContext", e);
		}
		throw new RuntimeException("Failed to create socketFactory.");
	}
	
}
