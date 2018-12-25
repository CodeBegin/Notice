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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.log4j.Logger;

import com.jdev.apns.main.ApnsService;
import com.jdev.apns.main.Constants;
import com.jdev.apns.main.Notification;
import com.jdev.apns.main.model.Headers;
import com.jdev.apns.main.model.NotificationResponse;
import com.jdev.apns.main.util.JWT;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.*;

/**
 * ApnsServiceBuilder class implements ApnsService interface.
 * This class has methods that send notifications to APNs
 * 
 * @author seunghwanjin
 */
public class ApnsServiceBuilder implements ApnsService {
	/**
	 * Logger for class ApnsServiceBuilder
	 */
	private final static Logger LOGGER = Logger.getLogger(ApnsServiceBuilder.class);
	/**
	 * Default version for the request is HTTP/2 
	 */
//	private final static Version VERSION = Version.HTTP_2;

	private ExecutorService executorService;
	private SSLContext context;

	/**
	 * File location of .p12 certificate
	 */
	private InputStream p12;

	/**
	 * Connection pool size
	 * Default value = 3
	 */
	private int poolSize = 3;
	
	/**
	 * TimeUnit for request timeout
	 * Default value is Seconds
	 */
	private TimeUnit timeUnit = TimeUnit.SECONDS;

	/**
	 * Time for request timeout
	 * Default value is 1
	 */
	private byte waitTime = 5;
	
	/**
	 * Default value is true
	 */
	private boolean isDevelopment = true;
	
	/**
	 * Default value is false
	 */
	private boolean isSynchronous = false;
	
	/**
	 * Duration for request timeout
	 * Default value is 15 seconds
	 */
	private Duration timeOut = Duration.ofSeconds(15);
	
	private String keyId;
	private String teamId;
	private String apnsAuthKey;
	
	private String token;
	private long tokenTimeStamp;
	/**
	 * Default token life time is 30 minutes
	 */
	private long tokenLifetime = 30 * 60 * 1000;
	
	/**
	 * This method is used for assigning necessary variables for token authentication
	 * @param keyId 
	 * @param teamId
	 * @param apnsAuthKey file directory
	 * @return this
	 */
	public ApnsServiceBuilder authenticateWithToken(
			String keyId, String teamId, String apnsAuthKey) {
		this.keyId = keyId;
		this.teamId = teamId;
		this.apnsAuthKey = apnsAuthKey;
		return this;
	}
	
	/**
	 * Sets the token life time
	 * @param tokenLifeTime
	 * @return this
	 */
	public ApnsServiceBuilder setTokenLifeTime(long tokenLifeTime) {
		this.tokenLifetime = tokenLifeTime;
		return this;
	}
	
	/**
	 * Set the time unit
	 * @param timeUnit
	 * @return this
	 */
	public ApnsServiceBuilder setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
		return this;
	}

	/**
	 * Set the pool size for Executors.newFixedThreadPool({@value poolSize});  
	 * @param poolSize
	 * @return this
	 */
	public ApnsServiceBuilder setPoolSize(int poolSize) {
		this.poolSize = poolSize;
		return this;
	}

	/**
	 * set the duration unit
	 * @param waitTime
	 * @return this
	 */
	public ApnsServiceBuilder setWaitTime(byte waitTime) {
		this.waitTime = waitTime;
		return this;
	}
	
	public ApnsServiceBuilder setRequestTimeOut(Duration duration) {
		timeOut = duration;
		return this;
	}
	
	/**
	 * This method instantiates Executors with fixed thread pool
	 * @return this
	 */
	public ApnsServiceBuilder setDefaultExecutors() {
		executorService = Executors.newFixedThreadPool(poolSize);
		System.out.println(executorService);
		return this;
	}
	
	/**
	 * This sends request synchronously 
	 * @return this
	 */
	public ApnsServiceBuilder sendSynchronously() {
		isSynchronous = true;
		return this;
	}
	
	/**
	 * This method creates customer executor with user's given input.
	 * workQueue uses default BlockingQueue which is LinkedBlockingQueue
	 * 
	 * @param coreSize number of threads to start with
	 * @param maxSize maximum number of threads
	 * @param aliveTime idle time
	 * @param unit TimeUnit
	 * @return this
	 */
	public ApnsServiceBuilder createCustomExecutors(
			int coreSize, int maxSize, long aliveTime, TimeUnit unit) {
		executorService = new ThreadPoolExecutor(
				coreSize, maxSize, aliveTime, unit, new LinkedBlockingQueue<Runnable>());
		return this;
	}
	
	public ApnsServiceBuilder setDevelopment(boolean isDevelopment) {
		this.isDevelopment = isDevelopment;
		return this;
	}

	/**
	 * This method will validate if the .p12 file is valid
	 * If the .p12 file is valid, the method will try to get sslContext by
	 * using the file and password provided
	 * @param fileLocation
	 * @return this
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public ApnsServiceBuilder setP12(String fileLocation, String password) 
			throws NoSuchAlgorithmException, IOException {
		try {
			p12 = new FileInputStream(new File(fileLocation));
			if (p12 == null) {
				LOGGER.error("keystore file is not found.");
				throw new IllegalArgumentException(
						"keystore file is not found. " + fileLocation);
			} else {
				context = new SSLContextBuilder().
						setCredentials(p12, password).build();
			}
		} catch (FileNotFoundException e) {
			LOGGER.fatal("Faile to open the file: ", e);
			
		}
		return this;
	}

	/**
	 * This method composes the full URI 
	 * @param path
	 * @return uri 
	 */
	private String getUri(String path) {
		String uri = "https://";
		
		if (isDevelopment) {
			uri += Constants.APNS_HOST_DEVELOPMENT + path;
		} else {
			uri += Constants.APNS_HOST_PRODUCTION + path;
		}
		return uri;
	}
	
	/**
	 * This method is called directly by send() method to
	 * construct a HttpRequest and return it for further use.
	 * The method uses existing header values retrieved from 
	 * Notification object to set HttpRequest header values
	 * 
	 * @param notification
	 * @return {@value HttpRequest} if correct URI was provided
	 * 			{@value null} otherwise
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	private final HttpRequest setHttpRequest(Notification notification) 
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final String path = "/3/device/" + notification.getToken();
		final UUID uuid = notification.getUuid();
		final int expiration = notification.getExpiration();
		final Integer priority = notification.getPriority();
		final String topic = notification.getTopic();
		final String collapsId = notification.getCollapseId();

		System.out.println("3. constructing HttpRequest...");
		try {
			System.out.println("BODY PUBLISHER STARTS");
			BodyPublisher body = BodyPublishers.ofString(notification.getPayload());
			System.out.println("BODY PUBLISHER WORKS");
			
			HttpRequest.Builder request = HttpRequest.newBuilder().
					uri(new URI(getUri(path))).POST(body).
					timeout(timeOut).version(Version.HTTP_2);
			
			System.out.println("...Setting header values...");
			if (uuid != null) {
				request.setHeader(Headers.ID.key(), uuid.toString());
			}

			if (expiration > -1) {
				request.setHeader(Headers.EXPIRATION.key(), Integer.toString(expiration));
			}
			
			if (priority == 5 || priority == 10) {//priority has to be either 5 or 10
				request.setHeader(Headers.PRIORITY.key(), Integer.toString(priority));
			}

			if (topic != null) {
				request.setHeader(Headers.TOPIC.key(), topic);
			}

			if (collapsId != null) {
				request.setHeader(Headers.COLLAPSE_ID.key(), collapsId);
			}
			
			//no token authentication is accepted at the moment
			if (keyId != null && teamId != null && apnsAuthKey != null) {
				if (token == null || System.currentTimeMillis() - tokenTimeStamp > tokenLifetime) {
					try {
						System.out.println("Token Authentication");
						tokenLifetime = System.currentTimeMillis();
						token = JWT.generateToken(keyId, teamId, apnsAuthKey);
					} catch (InvalidKeyException | NoSuchAlgorithmException | 
							InvalidKeySpecException | SignatureException | IOException e) {
						LOGGER.fatal("Failed to create a token for authentication", e);
						throw new IllegalStateException();
					}
				}
				request.setHeader("authorization", "bearer " + token);
			}
			
			System.out.println("Completed setting request headers...");
			return request.build();
		} catch (URISyntaxException e) {
			LOGGER.debug("Invalid URI: ", e);
		}

		return null;
	}
	
	/**
	 * This method is called directly by send() method to 
	 * construct HttpClient that will send Notification to APNs.
	 * 
	 * @return HttpClient
	 */
	private HttpClient getHttpClient() {
		Builder client = HttpClient.newBuilder().version(Version.HTTP_2);
		if (context != null) {
			System.out.println("Found valid context");
			client.sslContext(context);
		}
		
		System.out.println("4. getting HttpClient");
		return  client.build();
	}
	
	/**
	 * Method that constructs HttpRequest and HttpClient
	 * to send notification to APNs.
	 *  
	 * @param notification
	 * @return CompletableFuture<HttpResponse<String>>
	 */
	private CompletableFuture<HttpResponse<String>> sendAsyncReq(Notification notification) {
		try {
			HttpRequest request = setHttpRequest(notification);
			return getHttpClient().sendAsync(request, BodyHandlers.ofString());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOGGER.fatal("Failed to create request", e);
		}
		return null;
	}
	
	/**
	 * This method sends asynchronous request to APNs
	 * @param notification
	 */
	private void sendAsync(Notification notification) {
		CompletableFuture.supplyAsync(() -> sendAsyncReq(notification), executorService)
		.orTimeout(waitTime, timeUnit)
		.whenComplete((resp,error) -> {
			try {
				NotificationResponse response = new NotificationResponse(error);
				
				if (response.isDispatched()) {//checks if the request was sent
					System.out.println("dispatched...");
					HttpResponse<String> retrieved = resp.get();
					response.setResult(retrieved.statusCode(), retrieved.body());
				}
				
				if (response.isAccepted()) {//checks if the request was rejected
					System.out.println("Push Notification was accepted by APNs");
				} else {
					System.out.println("FAILED");
					System.out.println("Failed to send notificaiton: " + response.getSpecificFailureReason());
//					System.out.println("Request rejected reason: " + response.getFailureReason());
				}
				
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.debug("Failed to send notification", e);
			} finally {
				executorService.shutdown();
			}
		});
	}
	
	/**
	 * This method sends synchronous request to APNs
	 * @param notification
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	private void sendSync(Notification notification) 
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final HttpRequest request = setHttpRequest(notification);
		NotificationResponse resp = new NotificationResponse(null);
		try {
			HttpResponse<String> response = 
					getHttpClient().send(request, BodyHandlers.ofString());
			resp.setResult(response.statusCode(), response.body());
			
			if (resp.isAccepted()) {
				System.out.println("Push Notification was accepted by APNs");
			} else {
				System.out.println("Request rejected reason: " + resp.getFailureReason());
			}
		} catch (IOException | InterruptedException e) {
			System.out.println("Exception was thrown; check the log");
			LOGGER.fatal("Failed to send request synchronously", e);
		}
	}

	@Override
	public void sendNotification(Notification notification) {
		System.out.println("2. CompletableFuture.supplyAsync() method call");
		if (isSynchronous) {
			try {
				sendSync(notification);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				LOGGER.fatal("Failed to send synchronous notification", e);
			}
		} else {
			sendAsync(notification);
		}
	}//sendNotification(Notification notification)

}
