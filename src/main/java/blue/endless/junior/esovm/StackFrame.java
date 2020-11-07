package blue.endless.junior.esovm;

public class StackFrame {
	protected final String methodName;
	long[] int64;
	double[] float64;
	int[] int32;
	float[] float32;
	short[] int16;
	short[] float16;
	byte[] int8;
	
	public StackFrame() {
		this.methodName = "unknown";
	}
	
	public StackFrame(String methodName) {
		this.methodName = methodName;
	}
	
	public void reserve(int int64Count, int float64Count, int int32Count, int float32Count, int int16Count, int float16Count, int int8Count) {
		if (int64Count  >0) int64   = new long[int64Count];
		if (float64Count>0) float64 = new double[float64Count];
		if (int32Count  >0) int32   = new int[int32Count];
		if (float32Count>0) float32 = new float[float32Count];
		if (int16Count  >0) int16   = new short[int16Count];
		if (float16Count>0) float16 = new short[float16Count];
		if (int8Count   >0) int8    = new byte[int8Count];
	}
	
	public byte getInt8(int index) throws VMStackException {
		checkInt8(index);
		return int8[index];
	}
	
	public short getInt16(int index) throws VMStackException {
		checkInt16(index);
		return int16[index];
	}
	
	public int getInt32(int index) throws VMStackException {
		checkInt32(index);
		return int32[index];
	}
	
	public long getInt64(int index) throws VMStackException {
		checkInt64(index);
		return int64[index];
	}
	
	public short getFloat16(int index) throws VMStackException {
		checkFloat16(index);
		return float16[index];
	}
	
	public float getFloat32(int index) throws VMStackException {
		checkFloat32(index);
		return float32[index];
	}
	
	public double getFloat64(int index) throws VMStackException {
		checkFloat64(index);
		return float64[index];
	}
	
	public void putInt8(int index, byte value) throws VMStackException {
		checkInt8(index);
		int8[index] = value;
	}
	
	public void putInt16(int index, short value) throws VMStackException {
		checkInt16(index);
		int16[index] = value;
	}
	
	public void putInt32(int index, int value) throws VMStackException {
		checkInt32(index);
		int32[index] = value;
	}
	
	public void putInt64(int index, long value) throws VMStackException {
		checkInt64(index);
		int64[index] = value;
	}
	
	public void putFloat16(int index, short value) throws VMStackException {
		checkFloat16(index);
		float16[index] = value;
	}
	
	public void putFloat32(int index, float value) throws VMStackException {
		checkFloat32(index);
		float32[index] = value;
	}
	
	public void putFloat64(int index, double value) throws VMStackException {
		checkFloat64(index);
		float64[index] = value;
	}
	
	protected void checkInt8(int index) throws VMStackException {
		if (int8==null) throw new VMStackException("An instruction attempted to access an int8 in the LVT, but none were declared.");
		if (index<0 || index>=int8.length) throw new VMStackException("An instruction attempted to access an int8 in the LVT with invalid index "+index);
	}
	
	protected void checkInt16(int index) throws VMStackException {
		if (int16==null) throw new VMStackException("An instruction attempted to access an int16 in the LVT, but none were declared.");
		if (index<0 || index>=int16.length) throw new VMStackException("An instruction attempted to access an int16 in the LVT with invalid index "+index);
	}
	
	protected void checkInt32(int index) throws VMStackException {
		if (int32==null) throw new VMStackException("An instruction attempted to access an int32 in the LVT, but none were declared.");
		if (index<0 || index>=int32.length) throw new VMStackException("An instruction attempted to access an int32 in the LVT with invalid index "+index);
	}
	
	protected void checkInt64(int index) throws VMStackException {
		if (int64==null) throw new VMStackException("An instruction attempted to access an int64 in the LVT, but none were declared.");
		if (index<0 || index>=int64.length) throw new VMStackException("An instruction attempted to access an int64 in the LVT with invalid index "+index);
	}
	
	protected void checkFloat16(int index) throws VMStackException {
		if (float16==null) throw new VMStackException("An instruction attempted to access a float16 in the LVT, but none were declared.");
		if (index<0 || index>=float16.length) throw new VMStackException("An instruction attempted to access a float16 in the LVT with invalid index "+index);
	}
	
	protected void checkFloat32(int index) throws VMStackException {
		if (float32==null) throw new VMStackException("An instruction attempted to access a float32 in the LVT, but none were declared.");
		if (index<0 || index>=float32.length) throw new VMStackException("An instruction attempted to access a float32 in the LVT with invalid index "+index);
	}
	
	protected void checkFloat64(int index) throws VMStackException {
		if (float64==null) throw new VMStackException("An instruction attempted to access a float64 in the LVT, but none were declared.");
		if (index<0 || index>=float64.length) throw new VMStackException("An instruction attempted to access a float64 in the LVT with invalid index "+index);
	}
}
