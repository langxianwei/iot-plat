package com.point.iot.mina.codc.factory;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import com.point.iot.base.message.MessageCodecRegister;
/**
 * Message解码编码工厂
 * @author Langxianwei
 *
 */
public class MessageCodecFactory extends DemuxingProtocolCodecFactory {
	
	public MessageCodecFactory(){
		
	}
	public MessageCodecFactory(Charset charset) {
		/*Map<String, CodecRegister> messageCodecFactoryMap = ApplicationContextUtil.getContext().getBeansOfType(CodecRegister.class);
		//注册解码器
		if ( messageCodecFactoryMap != null ){
			Iterator<CodecRegister> it = messageCodecFactoryMap.values().iterator();
			while (it.hasNext() ) {
				CodecRegister factory = it.next();
				factory.register();
			}
		}*/
		if (MessageCodecRegister.getDecoderMap() != null ){
			Iterator<MessageDecoder> it = MessageCodecRegister.getDecoderMap().values().iterator();
			while(it.hasNext()){
				addMessageDecoder(it.next());
			}
		}
		
		Map<Class, MessageEncoder> encoderMap = MessageCodecRegister.getEncoderMap();
		if( encoderMap != null ){
			Iterator<Class> it = encoderMap.keySet().iterator();
			while (it.hasNext()) {
				Class c = it.next();
				addMessageEncoder(c, encoderMap.get(c));
			}
		}
	}
}
