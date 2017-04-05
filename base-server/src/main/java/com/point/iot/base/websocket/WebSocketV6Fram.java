package  com.point.iot.base.websocket;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.mina.core.session.IoSession;

public class WebSocketV6Fram {

	public static final byte FIN = (byte) 0x80; // 1000 0000
	public static final byte RSV1 = 0x70; // 0111 0000
	public static final byte RSV2 = 0x30; // 0011 0000
	public static final byte RSV3 = 0x10; // 0001 0000
	public static final byte OPCODE = 0x0F;// 0000 1111
	public static final byte MASK = (byte) 0x80;// 1000 0000
	public static final byte PAYLOADLEN = 0x7F;// 0111 1111
	public static final byte HAS_EXTEND_DATA = 126;
	public static final byte HAS_EXTEND_DATA_CONTINUE = 127;

	private byte fin;// 1bit
	private byte rsv1 = 0;// 1bit
	private byte rsv2 = 0;// 1bit
	private byte rsv3 = 0;// 1bit
	private byte opcode = 1;// 4bit
	private byte mask;// 1bit
	private byte payloadLen = 0;// 7bit
	private short payloadLenExtended = 0;// 16bit
	private long payloadLenExtendedContinued = 0L;// 64bit
	private byte[] maskingKey = null;// 32bit

	private byte[] payloadData;

 
	public static void writeWebSocketV6Fram(IoSession out, byte[] data,
			int type, boolean mark, boolean haveMore) throws IOException {
		WebSocketV6Fram fram = new WebSocketV6Fram();
		if (!haveMore)
			fram.setFin(FIN);
		if (type <= 0 || type > 16)
			throw new IllegalArgumentException("0 < type <= 16");
		fram.setOpcode((byte) type);
		if (mark)
			fram.setMask(MASK);
		int length = data.length;
		fram.setDateLength(length);
		out.write(fram.toByte());
		if (mark) {
			byte[] bt = fram.getMaskingKey();
			for (int i = 0; i < length; i++) {
				byte b = data[i];
				b ^= bt[i % 4];
				out.write(b);
			}
		} else {
			out.write(data);
		}
	}

	public static void writeWebSocketV6Fram(IoSession out, String message)throws IOException {
		writeWebSocketV6Fram(out, message.getBytes(Tools.CHARSET_UTF8), 1,false, false);
	}

	public static WebSocketV6Fram parseWebSocketV6Fram(InputStream in)
			throws IOException {
		WebSocketV6Fram fram = new WebSocketV6Fram();
		int bt, b2;
		bt = in.read();
		fram.setFin((byte) (bt & FIN));
		fram.setRsv1((byte) (bt & RSV1));
		fram.setRsv2((byte) (bt & RSV2));
		fram.setRsv3((byte) (bt & RSV3));
		fram.setOpcode((byte) (bt & OPCODE));

		bt = in.read();
		fram.setMask((byte) (bt & MASK));
		int dataLen = bt & PAYLOADLEN;

		if (dataLen == HAS_EXTEND_DATA) {// read next 16 bit
			bt = in.read();
			b2 = in.read();
			fram.setDateLength(Tools.toShort((byte) bt, (byte) b2));
		} else if (dataLen == HAS_EXTEND_DATA_CONTINUE) {// read next 32 bit
			byte[] bts = new byte[8];
			if (in.read(bts) != 8){
				//fram.setOpcode
				throw new RuntimeException("reader Payload-Len-Extended-Continued data length < 64 bit");
			}
			fram.setDateLength(Tools.toLong(bts));
		} else {
			fram.setDateLength(dataLen);
		}
		if (fram.isMask()) {
			fram.setMaskingKey((byte) in.read(), (byte) in.read(), (byte) in.read(), (byte) in.read());
		}
		return fram;
	}

	public static byte[] readWebSocketV6(InputStream in, final WebSocketV6Fram fram) {
		InputStream in2 = readWebSocketV6Date(in, fram);
		byte[] result = new byte[(int) fram.getDateLength()];
		try {
			in2.read(result);
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * read fram.getDateLength byte data, at the end return -1;
	 * 
	 * @param in
	 * @param fram
	 * @return
	 */
	public static InputStream readWebSocketV6Date(InputStream in,
			final WebSocketV6Fram fram) {
		return new FilterInputStream(in) {
			private long length = fram.getDateLength();
			private boolean isMask = fram.isMask();
			private byte[] maskKey = fram.getMaskingKey();
			private Object lock = new Object();
			private long readLength = 0;

			@Override
			public int read() throws IOException {
				if (readLength >= length)
					return -1;
				int b = 0;
				synchronized (lock) {
					if (readLength >= length)
						return -1;
					b = super.read();
					if (isMask) {
						b ^= maskKey[(int) (readLength % 4)];
					}
					readLength++;
				}
				return b;
			}

			@Override
			public int read(byte[] btArray, int off, int len)
					throws IOException {
				int result = 0;
				synchronized (lock) {
					if (readLength >= length)
						return -1;
					long end = length - readLength;
					if (end >= len) {
						result = super.read(btArray, off, len);
					} else {
						result = super.read(btArray, off, (int) end);
					}
					if (isMask) {
						for (int i = off; i < off + result; i++) {
							btArray[i] ^= maskKey[(int) (readLength % 4)];
							readLength++;
						}
					} else {
						readLength += result;
					}
				}
				return result;
			}
		};
	}

	public long getDateLength() {
		if (this.getPayloadLenExtendedContinued() > 0)
			return this.getPayloadLenExtendedContinued();
		if (this.getPayloadLenExtended() > 0)
			return this.getPayloadLenExtended();
		if (this.getPayloadLen() == HAS_EXTEND_DATA
				|| this.getPayloadLen() == HAS_EXTEND_DATA_CONTINUE)
			return 0;
		return this.getPayloadLen();
	}

	public void setDateLength(long len) {
		if (len < HAS_EXTEND_DATA) {
			this.payloadLen = (byte) len;
			this.payloadLenExtended = 0;
			this.payloadLenExtendedContinued = 0;
		} else if (len < 1 * Short.MAX_VALUE * 2) {// UNSIGNED
			this.payloadLen = HAS_EXTEND_DATA;
			this.payloadLenExtended = (short) len;
			this.payloadLenExtendedContinued = 0;
		} else {
			this.payloadLen = HAS_EXTEND_DATA;
			this.payloadLenExtended = 0;
			this.payloadLenExtendedContinued = len;
		}
	}

	public byte[] toByte() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte one = this.fin;
			one |= this.rsv1 | this.rsv2 | this.rsv3 | this.opcode;
			out.write(one);
			one = 0;
			one |= this.mask | this.payloadLen;
			out.write(one);

			if (this.payloadLen == HAS_EXTEND_DATA) {
				out.write(Tools.shortTo4Byte(this.payloadLenExtended));
			} else if (this.payloadLen == HAS_EXTEND_DATA_CONTINUE) {
				out.write(Tools.longTo4Byte(this.payloadLenExtendedContinued));
			}

			if (this.isMask()) {
				out.write(this.getMaskingKey());
			}
			return out.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public byte getFin() {
		return fin;
	}

	public boolean hasMareDate() {
		return 0 == (fin ^ FIN);
	}

	public void setFin(byte fin) {
		fin &= FIN;
		this.fin = fin;
	}

	public byte getRsv1() {
		return rsv1;
	}

	public void setRsv1(byte rsv1) {
		rsv1 &= RSV1;
		this.rsv1 = rsv1;
	}

	public byte getRsv2() {
		return rsv2;
	}

	public void setRsv2(byte rsv2) {
		rsv2 &= RSV2;
		this.rsv2 = rsv2;
	}

	public byte getRsv3() {
		return rsv3;
	}

	public void setRsv3(byte rsv3) {
		rsv3 &= RSV3;
		this.rsv3 = rsv3;
	}

	public byte getOpcode() {
		return opcode;
	}

	public void setOpcode(byte opcode) {
		opcode &= OPCODE;
		this.opcode = opcode;
	}

	public byte getMask() {
		return mask;
	}

	public boolean isMask() {
		return 0 == (mask ^ MASK);
	}

	public void setMask(byte mask) {
		mask &= MASK;
		this.mask = mask;
	}

	public byte getPayloadLen() {
		return payloadLen;
	}

	public short getPayloadLenExtended() {
		return payloadLenExtended;
	}

	public long getPayloadLenExtendedContinued() {
		return payloadLenExtendedContinued;
	}

	public byte[] getPayloadData() {
		return payloadData;
	}

	public void setPayloadData(byte[] payloadData) {
		this.payloadData = payloadData;
	}

	/**
	 * 濡傛灉娌℃湁琚缃繃  浼氳嚜鍔ㄧ敓鎴?
	 * @return
	 */
	public byte[] getMaskingKey() {
		if (!isMask())
			return null;
		else if (maskingKey == null) {
			byte[] bytes = new byte[4];
			new Random().nextBytes(bytes);
			maskingKey = bytes;
		}
		return maskingKey;
	}

	public void setMaskingKey(byte... maskingKey) {
		this.maskingKey = maskingKey;
	}
	
	public static byte[] getCloseV6Fram(){
		//%x8 denotes a connection close
		return new byte[]{(byte) 0x88, 0};
	}
 
	public static byte[] getCloseV5Fram(){
		//%x8 denotes a connection close
		return new byte[]{(byte) 0x81, 1, 0x53};
	}
}
