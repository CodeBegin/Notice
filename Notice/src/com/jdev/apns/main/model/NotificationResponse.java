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

package com.jdev.apns.main.model;

import com.google.gson.Gson;

public class NotificationResponse {
	private FailureResponse failureResponse;
	private RejectionReason error;
	private int httpStatusCode;
	private String responseBody;
	private Throwable cause;
	
	/**
	 * Constructor for NotificationResponse  
	 * @param Throwable cause: exception 
	 */
	public NotificationResponse(Throwable cause) {
		this.cause = cause;
	}
	
	/**
	 * This method checks if an exception happened 
	 * @return {@value true} if cause is null, {@value false} otherwise
	 */
	public boolean isDispatched() {
		return cause == null;
	}
	
	public void setResult(int statusCode, String body) {
		httpStatusCode = statusCode;
		responseBody = body;
	}
	
	/**
	 * This method checks if the notification was accepted to APNs
	 * @return {@value true} if status code is 200, {@value false} otherwise 
	 */
	public boolean isAccepted() {
		return httpStatusCode == 200;
	}
	
	/**
	 * Generic reason the request failed;
	 * result is derived from the status code alone
	 * @return reason request was rejected
	 */
	public String getFailureReason() {
		return RejectionReason.getReason(httpStatusCode).toString();
	}
	
	/**
	 * If timeStamp is not null, new token has to be made immediately
	 * @return String detail reason request failed
	 */
	public String getSpecificFailureReason() {
		System.out.println(responseBody.toString());
		failureResponse = new Gson().fromJson(
				responseBody, FailureResponse.class);
		return failureResponse.toString();
	}
	
	/**
	 * 
	 * @return {@value true} if timeStamp is not null
	 */
	public boolean needNewToken() {
		return failureResponse.getTimeStamp() == null;
	}

	public RejectionReason getError() {
		return error;
	}

	public void setError(RejectionReason error) {
		this.error = error;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	public Throwable getCause() {
		return cause;
	}

	public int getHttpStatusCode() {
		return httpStatusCode;
	}

	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}
	
}
