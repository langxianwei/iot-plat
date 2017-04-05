package com.point.iot.base.message;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageEncoder;

public class MessageCodecRegister {

	private static Map<Class, MessageEncoder> encoderMap;
	private static Map<Class, MessageDecoder> decoderMap;
	public static void addEncoder(Class t, MessageEncoder encoder){
		if (encoderMap == null ){
			encoderMap = new HashMap<Class, MessageEncoder>();
		}
		encoderMap.put(t, encoder);
	}
	
	public static Map<Class, MessageEncoder> getEncoderMap(){
		return encoderMap;
	}
	
	public static void addDecoder(Class t, MessageDecoder encoder){
		if (decoderMap == null ){
			decoderMap = new HashMap<Class, MessageDecoder>();
		}
		decoderMap.put(t, encoder);
	}
	
	public static Map<Class, MessageDecoder> getDecoderMap(){
		return decoderMap;
	}
}
