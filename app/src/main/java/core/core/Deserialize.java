package core.core;

import java.util.Vector;

import org.bouncycastle.util.Arrays;

import core.util.BigInteger;

public class Deserialize {
	private int version;
	private long inputCount = 0;
	private Vector inputs = new Vector();
	private long outputCount = 0;
	private Vector outputs = new Vector();
	private long lockTime;

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public long getInputCount() {
		return inputCount;
	}

	public void setInputCount(long inputCount) {
		this.inputCount = inputCount;
	}

	public Vector getInputs() {
		return inputs;
	}

	public void setInputs(Vector inputs) {
		this.inputs = inputs;
	}

	public long getOutputCount() {
		return outputCount;
	}

	public void setOutputCount(long outputCount) {
		this.outputCount = outputCount;
	}

	public Vector getOutputs() {
		return outputs;
	}

	public void setOutputs(Vector outputs) {
		this.outputs = outputs;
	}

	public long getLockTime() {
		return lockTime;
	}

	public void setLockTime(long lockTime) {
		this.lockTime = lockTime;
	}

	public static Deserialize parse(String txData) {
		Deserialize tx = new Deserialize();
		byte[] rawTx = ByteUtilities.toByteArray(txData);
		int buffPointer = 0;

		// Version
		byte[] version = ByteUtilities.readBytes(rawTx, buffPointer, 4);
		buffPointer += 4;
		version = ByteUtilities.flipEndian(version);
		tx.setVersion(new BigInteger(1, version).intValue());

		// Number of inputs
		VariableInt varInputCount = readVariableInt(rawTx, buffPointer);
		buffPointer += varInputCount.getSize();
		tx.setInputCount(varInputCount.getValue());

		// Parse inputs
		for (long i = 0; i < tx.getInputCount(); i++) {
			byte[] inputData = Arrays.copyOfRange(rawTx, buffPointer, rawTx.length);
			RawInput input = RawInput.parse(ByteUtilities.toHexString(inputData));
			buffPointer += input.getDataSize();
			tx.getInputs().addElement(input);
		}

		// Get the number of outputs
		VariableInt varOutputCount = readVariableInt(rawTx, buffPointer);
		buffPointer += varOutputCount.getSize();
		tx.setOutputCount(varOutputCount.getValue());

		// Parse outputs
		for (long i = 0; i < tx.getOutputCount(); i++) {
			byte[] outputData = Arrays.copyOfRange(rawTx, buffPointer, rawTx.length);
			RawOutput output = RawOutput.parse(ByteUtilities.toHexString(outputData));
			buffPointer += output.getDataSize();
			tx.getOutputs().addElement(output);
		}

		// Parse lock time
		byte[] lockBytes = ByteUtilities.readBytes(rawTx, buffPointer, 4);
		buffPointer += 4;
		lockBytes = ByteUtilities.flipEndian(lockBytes);
		tx.setLockTime(new BigInteger(1, lockBytes).longValue());

		return tx;
	}

	public static VariableInt readVariableInt(byte[] data, int start) {
		if (data == null || data.length <= start) {
			return new VariableInt();
		}
		int checkSize = 0xFF & data[start];
		VariableInt varInt = new VariableInt();
		varInt.setSize(0);

		if (checkSize < 0xFD) {
			varInt.setSize(1);
			varInt.setValue(checkSize);
			return varInt;
		}

		if (checkSize == 0xFD) {
			varInt.setSize(3);
		} else if (checkSize == 0xFE) {
			varInt.setSize(5);
		} else if (checkSize == 0xFF) {
			varInt.setSize(9);
		}

		if (varInt.getSize() == 0) {
			return null;
		}

		byte[] newData = ByteUtilities.readBytes(data, start + 1, varInt.getSize() -1);
		newData = ByteUtilities.flipEndian(newData);
		varInt.setValue(new BigInteger(1, newData).longValue());
		return varInt;
	}

	public static byte[] writeVariableInt(long data) {
		byte[] newData;

		if (data < 0x00FD) {
			newData = new byte[1];
			newData[0] = (byte) (data & 0xFF);
		} else if (data <= 0xFFFF) {
			newData = new byte[3];
			newData[0] = (byte) 0xFD;
		} else if (data <= 4294967295L /* 0xFFFFFFFF */) {
			newData = new byte[5];
			newData[0] = (byte) 0xFE;
		} else {
			newData = new byte[9];
			newData[0] = (byte) 0xFF;
		}

		byte[] intData = BigInteger.valueOf(data).toByteArray();
		intData = ByteUtilities.stripLeadingNullBytes(intData);
		intData = ByteUtilities.leftPad(intData, newData.length - 1, (byte) 0x00);
		intData = ByteUtilities.flipEndian(intData);

		for (int i = 0; i < (newData.length - 1); i++) {
			newData[i + 1] = intData[i];
		}

		return newData;
	}

	public static VariableInt readOpCodeInt(byte[] data, int start) {
		
		int checkSize = 0xFF & data[start];
		VariableInt varInt = new VariableInt();
		varInt.setSize(0);

		if (checkSize == 0x00) {
			varInt.setSize(1);
			varInt.setValue(checkSize);
			return varInt;
		}

		if (checkSize >= 0x51 && checkSize <= 0x60) {
			varInt.setSize(1);
			varInt.setValue(checkSize - 0x50);
			return varInt;
		}

		if (checkSize == 0x4C) {
			varInt.setSize(2);
		} else if (checkSize == 0x4D) {
			varInt.setSize(3);
		} else if (checkSize == 0x4E) {
			varInt.setSize(5);
		} else {
			// Just process the byte and advance
			varInt.setSize(1);
			varInt.setValue(checkSize);
			return varInt;
		}

		if (varInt.getSize() == 0) {
			return null;
		}

		byte[] newData = ByteUtilities.readBytes(data, start + 1, varInt.getSize() - 1);
		newData = ByteUtilities.flipEndian(newData);
		varInt.setValue(new BigInteger(1, newData).longValue());
		return varInt;
	}

	public static VariableInt readVariableStackInt(byte[] data, int start) {
		int checkSize = 0xFF & data[start];
		VariableInt varInt = new VariableInt();
		varInt.setSize(0);

		if (checkSize < 0x4C) {
			varInt.setSize(1);
			varInt.setValue(checkSize);
			return varInt;
		}

		if (checkSize == 0x4C) {
			varInt.setSize(2);
		} else if (checkSize == 0x4D) {
			varInt.setSize(3);
		} else if (checkSize == 0x4E) {
			varInt.setSize(5);
		} else {
			// Just process the byte and advance
			varInt.setSize(1);
			varInt.setValue(0);
			return varInt;
		}

		if (varInt.getSize() == 0) {
			return null;
		}

		byte[] newData = ByteUtilities.readBytes(data, start + 1, varInt.getSize() - 1);
		newData = ByteUtilities.flipEndian(newData);
		varInt.setValue(new BigInteger(1, newData).longValue());
		return varInt;
	}

	
	public boolean match_decoded(Vector decoded,int[] to_match) {
	    if(decoded.size() != to_match.length) {
	    	return false;
	    }
	    for(int i =0; i< decoded.size() ;i++) {
	    	Holder h = (Holder)decoded.elementAt(i);
	        if(to_match[i] == AddressUtil.OP_PUSHDATA4 && h.getOpcode() <= AddressUtil.OP_PUSHDATA4 &&  h.getOpcode() > 0)
	            continue;
	        if(to_match[i] !=h.getOpcode())
	            return false;
	    }
	    return true;
	}
	
	public Vector script_GetOp(byte[] _bytes){
	    int i = 0;
	    Vector v = new Vector();
	    while(i < _bytes.length) {
	        byte[] vch = null;
	        int opcode = _bytes[i];
	        i += 1;
	        
	        if(opcode >= AddressUtil.OP_SINGLEBYTE_END){
	            opcode <<= 8;
	            opcode |= _bytes[i];
	            i += 1;
	        }
	        if(opcode <= AddressUtil.OP_PUSHDATA4) {
	            int nSize = opcode;
	            if(opcode == AddressUtil.OP_PUSHDATA1) {
	                nSize = _bytes[i];
	                i += 1;
	            }
	            else if(opcode == AddressUtil.OP_PUSHDATA2) {
	            	VariableInt varInt = new VariableInt();
	            	byte[] hashBytes = ByteUtilities.readBytes(_bytes, i, 16);
	        		hashBytes = ByteUtilities.flipEndian(hashBytes);
	        	    varInt.setValue(new BigInteger(1, hashBytes).longValue());
	        	    nSize = varInt.getSize();
	                i += 2;
	            }
	            else if(opcode == AddressUtil.OP_PUSHDATA4) {
	            	VariableInt varInt = new VariableInt();
	            	byte[] hashBytes = ByteUtilities.readBytes(_bytes, i, 32);
	        		hashBytes = ByteUtilities.flipEndian(hashBytes);
	        	    varInt.setValue(new BigInteger(1, hashBytes).longValue());
	        	    nSize = varInt.getSize();
	                i += 4;
	            }
	            vch =  ByteUtilities.readBytes(_bytes, i, nSize);
	            i += nSize;
	        }
	        
	       v.addElement(new Holder(opcode, vch, i));
	    }
	    return v;
	}
	
	public static class Holder{
		int opcode;
		byte[] vch;
		int i;
		
		public Holder(int opcode, byte[] vch, int i) {
			this.opcode = opcode;
			this.vch = vch;
			this.i = i;
		}
		public int getOpcode() {
			return opcode;
		}
		public void setOpcode(int opcode) {
			this.opcode = opcode;
		}
		public byte[] getVch() {
			return vch;
		}
		public void setVch(byte[] vch) {
			this.vch = vch;
		}
		public int getI() {
			return i;
		}
		public void setI(int i) {
			this.i = i;
		}
		
	}

	public static class VariableInt {
		int size;
		long value;

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public long getValue() {
			return value;
		}

		public void setValue(long value) {
			this.value = value;
		}
	}

}
