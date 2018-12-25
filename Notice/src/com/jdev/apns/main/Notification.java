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

package com.jdev.apns.main;

import java.util.UUID;

/**
 * Notification is the class that stores header values and 
 * payload for sending the request to APNs.
 * A Notification object includes:
 * <ul>
 * <li>payload: body data in JSON format</li>
 * <li>token: The provider token</li>
 * This is only necessary when provider certificate is not used
 * to establish a connection.
 * <li>uuid: unique id that identifies the notification</li>
 * If omitted, APNs creates new one and return in the response
 * <li>expiration: The date when the notification is no longer valid</li>
 * If the value is 0, APNs does not store or attempt to re-deliver it.
 * <li>priority: either 10 -- immediate or 5 -- non-immediate </li>
 * <li>topic: bundle ID for the app</li>
 * This is only necessary if using a provider token instead of certificate
 * <li>collapseId: used for displaying multiple notifications as a single notification</li>
 * @see Quality of Service, Store-and-Forward, and Coalesced Notifications
 * </ul>
 * @author seunghwanjin
 * 
 */
public class Notification {
	private String payload;
	private String token;
	private UUID uuid;
	private int expiration;
	/**
	 * Default priority is set to be 10
	 */
	private int priority = Constants.HIGH_PRIORITY;
	private String topic;
	private String collapseId;
	
	/**
	 * Constructor always takes valid payload
	 * @param payload
	 */
	public Notification(String payload) {
		this.payload = payload;
	}

	public String getPayload() {
		return payload;
	}

	public Notification setPayload(String payload) {
		this.payload = payload;
		return this;
	}

	public String getToken() {
		return token;
	}

	public Notification setToken(String token) {
		this.token = token;
		return this;
	}

	public UUID getUuid() {
		return uuid;
	}

	/**
	 * It is not required to set UUID
	 * APNs automatically generates one if this field is empty
	 * @param uuid
	 * @return
	 */
	public Notification setUuid() {
		this.uuid = UUID.randomUUID();
		return this;
	}

	public int getExpiration() {
		return expiration;
	}

	public Notification setExpiration(int expiration) {
		this.expiration = expiration;
		return this;
	}

	public int getPriority() {
		return priority;
	}

	public Notification setPriority(int priority) {
		this.priority = priority;
		return this;
	}

	public String getTopic() {
		return topic;
	}

	public Notification setTopic(String topic) {
		this.topic = topic;
		return this;
	}

	public String getCollapseId() {
		return collapseId;
	}

	public Notification setCollapseId(String collapseId) {
		this.collapseId = collapseId;
		return this;
	}
	
}
