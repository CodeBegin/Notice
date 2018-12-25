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

/**
 * FailureResponse holds the detailed information about
 * the reason request was rejected
 * 
 * @author Seung Hwan, Jin
 */
public class FailureResponse {
	private String reason;
	/**
	 * This is only applicable for status code: 410
	 */
	private String timeStamp;
	
	public FailureResponse() {}
	
	public String getReason() {
		return reason;
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timestamp) {
		this.timeStamp = timestamp;
	}
	
	@Override
	public String toString() {
		return "FailureResponse [reason:" + reason + ",timeStamp:" + timeStamp + "]";
	}
	
}
