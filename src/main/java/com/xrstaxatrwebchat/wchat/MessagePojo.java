package com.xrstaxatrwebchat.wchat;

public class MessagePojo {
	
	private String sender;
	private String receiver;
	private String message;
	private String time;
	private String ackId;
	
	public MessagePojo(String from, String to, String msg, String tyme, String ackid) {
		this.sender = from;
		this.receiver = to;
		this.message = msg;
		this.time = tyme;
		this.ackId = ackid;
	}

}
