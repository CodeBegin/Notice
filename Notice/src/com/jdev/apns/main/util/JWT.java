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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import org.apache.log4j.Logger;

import com.jdev.apns.main.Constants;

/**
 * Method that creates JWT token
 * 
 * @author seunghwanjin
 *
 */
public class JWT {
	/**
	 * Logger for class Demo
	 */
	private final static Logger LOGGER = Logger.getLogger(JWT.class);
	
	/**
	 * Creates a JWT token that is used for token authentication
	 * Follow {@link https://jwt.io} for more information on this
	 * 
	 * @param keyId the key identifier obtained from developer account (under key)
	 * @param teamId the team identifier obtained from developer account (under membership)
	 * @param secret the private key 
	 * @return token created with the key
	 * @throws SignatureException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws IOException 
	 */
	public static String generateToken(
			final String keyId, final String teamId, final String fileName) 
			throws InvalidKeyException, NoSuchAlgorithmException, 
			InvalidKeySpecException, SignatureException, IOException {
		long issuedAt = System.currentTimeMillis() / 1000l;
		final String header = "{\"alg\":\"ES256\",\"kid\":\"" + keyId + "\"}";
		final String payload = "{\"iss\":\"" + teamId + "\",\"iat\":\"" + issuedAt + "\"}";
		final String encodedPart = base64UrlEncoder(header) + "." + base64UrlEncoder(payload);
		
		return encodedPart + "." + getSignature(getPrivateKey(fileName), encodedPart);
	}
	
	/**
	 * This method gets the private key from the file of given directory
	 * @param fileName file directory
	 * @return private key
	 * @throws IOException
	 */
	private static String getPrivateKey(final String fileName) throws IOException {
		 try {
			 final StringBuilder keyBuilder = new StringBuilder();
			 
			 @SuppressWarnings("resource")
			final BufferedReader reader = 
					 new BufferedReader(new FileReader(new File(fileName)));
			 
			 String begin = "BEGIN PRIVATE KEY", end = "END PRIVATE KEY";
			 String line;
			 while ((line = reader.readLine()) != null) {
				 if (!(line.contains(begin) || line.contains(end))) {
					 keyBuilder.append(line);
				 }
			 }
			 
			 return Base64.getEncoder().encodeToString(keyBuilder.toString().getBytes());
		} catch (IOException e) {
			LOGGER.fatal("Could not open the file.", e);
			throw new IOException();
		}
	}
	
	/**
	 * This method returns ES256 from the given input
	 * 
	 * @param secret the private key
	 * @param data Encoded data that has header + payload information
	 * @return ES256
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 * @throws InvalidKeyException 
	 * @throws SignatureException 
	 */
	private static String getSignature(final String secret, final String data) 
			throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
		final byte[] keyBytes = Base64.getDecoder().decode(secret.getBytes());
		final PKCS8EncodedKeySpec keySpecForPtivateKey = 
				new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyBytes));
		
		KeyFactory keyFactory = KeyFactory.getInstance("EC");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpecForPtivateKey);
		
		if (privateKey == null) {
			LOGGER.fatal("The given PKCS8 key is null.");
			throw new IllegalStateException("The given Private Key is null.");
		}
		
		final Signature signature = Signature.getInstance("SHA256WITHECDSA");
		signature.initSign(privateKey);
		signature.update(data.getBytes(Constants.UTF8));
		
		final byte[] signatureBytes = signature.sign();
		
		if (signatureBytes == null) {
			LOGGER.fatal("Failed to generate signature.");
			throw new IllegalStateException("Failed to generate signature.");
		}
		
		return Base64.getUrlEncoder().encodeToString(signatureBytes);
	}
	
	/**
	 * Generates base64UrlEncoded String
	 * @param raw String
	 * @return encoded String
	 */
	private static String base64UrlEncoder(String raw) {
		return Base64.getUrlEncoder().encodeToString(raw.getBytes(Constants.UTF8));
	}
	
}
