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

import java.util.HashMap;
import java.util.Map;


/**
 * RejectionReason is enum class that holds list of
 * possible status codes returned by APNs as response
 * 
 * @author seunghwanjin
 */
public enum RejectionReason {
	BAD_REQUEST(400),
	AUTHENTICATION_ERROR(403),
	BAD_METHOD(405),
	INACTIVE_DEVICE_TOKEN_FOR_TOPIC(410),
	PAYLOAD_TOO_LARGE(413),
	TOO_MANY_REQUEST_FOR_TOKEN(429),
	INTERNAL_SERVER_ERROR(500),
	SERVER_UNAVAILABLE(503);
	private final int errorCode;
	
	RejectionReason(int statusCode) {
		errorCode = statusCode;
	}
	
	private static Map<Integer, RejectionReason> errors = new HashMap<>();
	
	static {
		for (RejectionReason requestError: RejectionReason.values()) {
			errors.put(requestError.errorCode, requestError);
		}
	}
	
	public static RejectionReason getReason(int statusCode) {
		return errors.get(statusCode);
	}
}
