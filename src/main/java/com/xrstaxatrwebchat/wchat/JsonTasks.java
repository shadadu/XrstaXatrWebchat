package com.xrstaxatrwebchat.wchat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;

import javax.naming.Context;

public class JsonTasks {
	
	
	public static void jsonArrayAppend(MessagePojo userMsgPojo, MessagePojo botMsgPojo, Context ctx) throws  Exception {

		String path = (String) ctx.lookup("java:comp/env/backendJsonPath");

	    try {
	    	File file = new File(path);
	        FileWriter fileWriter = new FileWriter(file, true);

	        ObjectMapper mapper = new ObjectMapper().setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

	        SequenceWriter seqWriter = mapper.writer().writeValuesAsArray(fileWriter);
	        seqWriter.write(userMsgPojo);
	        seqWriter.write(botMsgPojo);
	        seqWriter.close();
	        fileWriter.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
}
