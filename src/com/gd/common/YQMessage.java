package com.gd.common;
import java.io.Serializable;



public class YQMessage implements Serializable{
	String type;
	String sender;
	String senderNick;
	int senderAvatar;
	String receiver;
	String content;
	String sendTime;
	String receivers[];
	int id;
	int number;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getSenderNick() {
		return senderNick;
	}
	public void setSenderNick(String senderNick) {
		this.senderNick = senderNick;
	}
	public int getSenderAvatar() {
		return senderAvatar;
	}
	public void setSenderAvatar(int senderAvatar) {
		this.senderAvatar = senderAvatar;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	public String getSendTime() {
		return sendTime;
	}
	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}
	
	public void setReceivers(String[] receivers) {
		this.receivers = receivers;
	}
	
	public String[] getReceivers() {
		return receivers;
	}
	
	
	public void setReceiversNumber(int number) {
		this.number = number;
	}
	public int getReceiversNumber() {
		return number;
	}
	
}
