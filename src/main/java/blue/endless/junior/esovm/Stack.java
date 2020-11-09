package blue.endless.junior.esovm;

import java.util.ArrayDeque;

public class Stack {
	protected byte[] stack;
	protected int stackPointer;
	protected int branchPointer;
	protected ArrayDeque<StackFrame> stackFrames = new ArrayDeque<>();
	
	public Stack() {
		this(65535);
	}
	
	public Stack(int size) {
		stack = new byte[size];
		clear();
	}
	
	public void clear() {
		stackPointer = stack.length;
		branchPointer = stack.length;
	}
	
	public void pushInt8(int value) throws StackOverflowError {
		stackPointer--;
		rangeCheck();
		stack[stackPointer] = (byte)value;
	}
	
	public void pushInt16(int value) throws StackOverflowError {
		int hi = (value >>> 8) & 0xFF;
		int lo = value & 0xFF;
		
		pushInt8(hi);
		pushInt8(lo);
	}
	
	public void pushInt32(int value) throws StackOverflowError {
		int a = (value >>>24) & 0xFF;
		int b = (value >>>16) & 0xFF;
		int c = (value >>> 8) & 0xFF;
		int d = value & 0xFF;
		
		pushInt8(a);
		pushInt8(b);
		pushInt8(c);
		pushInt8(d);
	}
	
	public void pushInt64(long value) throws StackOverflowError {
		int a = (int) ((value >>>56) & 0xFF);
		int b = (int) ((value >>>48) & 0xFF);
		int c = (int) ((value >>>40) & 0xFF);
		int d = (int) ((value >>>32) & 0xFF);
		int e = (int) ((value >>>24) & 0xFF);
		int f = (int) ((value >>>16) & 0xFF);
		int g = (int) ((value >>> 8) & 0xFF);
		int h = (int) ((value)       & 0xFF);
		
		pushInt8(a);
		pushInt8(b);
		pushInt8(c);
		pushInt8(d);
		pushInt8(e);
		pushInt8(f);
		pushInt8(g);
		pushInt8(h);
	}
	
	public void pushWord(long value) throws StackOverflowError {
		pushInt64(value);
	}
	
	public void pushFloat16(short value) throws StackOverflowError {
		pushInt16(value);
	}
	
	public void pushFloat32(float value) throws StackOverflowError {
		pushInt32(Float.floatToIntBits(value));
	}
	
	public void pushFloat64(double value) throws StackOverflowError {
		pushInt64(Double.doubleToLongBits(value));
	}
	
	public int popInt8() throws StackOverflowError {
		rangeCheck();
		int result = stack[stackPointer];
		stackPointer++;
		return result;
	}
	
	public int popInt16() throws StackOverflowError {
		int lo = popInt8();
		int hi = popInt8();
		return (hi << 8) | lo;
	}
	
	public int popInt32() throws StackOverflowError {
		int d = popInt8();
		int c = popInt8();
		int b = popInt8();
		int a = popInt8();
		
		return (a<<24) | (b<<16) | (c<<8) | d;
	}
	
	public long popInt64() throws StackOverflowError {
		int h = popInt8();
		int g = popInt8();
		int f = popInt8();
		int e = popInt8();
		int d = popInt8();
		int c = popInt8();
		int b = popInt8();
		int a = popInt8();
		
		return (a<<56) | (b<<48) | (c<<40) | (d<<32) | (e<<24) | (f<<16) | (g<<8) | h;
	}
	
	public float popFloat32() throws StackOverflowError {
		return Float.intBitsToFloat(popInt32());
	}
	
	public double popFloat64() throws StackOverflowError {
		return Double.longBitsToDouble(popInt64());
	}
	
	public StackFrame currentStackFrame() {
		return stackFrames.peek();
	}
	
	public void pushStackFrame(StackFrame frame) {
		stackFrames.push(frame);
	}
	
	public void pushStackFrame(String methodName, int wordCount, int int64Count, int float64Count, int int32Count, int float32Count, int int16Count, int float16Count, int int8Count) {
		StackFrame frame = new StackFrame(methodName);
		frame.reserve(wordCount, int64Count, float64Count, int32Count, float32Count, int16Count, float16Count, int8Count);
		pushStackFrame(frame);
	}
	
	public void pushStackFrame(String methodName, long lvtInfo) throws VMException {
		int int8Count    = (int) ( lvtInfo         & 0xFF);
		int float16Count = (int) ((lvtInfo >>>  8) & 0xFF);
		int int16Count   = (int) ((lvtInfo >>> 16) & 0xFF);
		int float32Count = (int) ((lvtInfo >>> 24) & 0xFF);
		int int32Count   = (int) ((lvtInfo >>> 32) & 0xFF);
		int float64Count = (int) ((lvtInfo >>> 40) & 0xFF);
		int int64Count   = (int) ((lvtInfo >>> 48) & 0xFF);
		int wordCount    = (int) ((lvtInfo >>> 56) & 0xFF);
		
		pushStackFrame(methodName, wordCount, int64Count, float64Count, int32Count, float32Count, int16Count, float16Count, int8Count);
	}
	
	public StackFrame popStackFrame() {
		return stackFrames.pop();
	}
	
	/** Makes sure we're ready to read the value *at* the stack pointer. Throws a StackOverflow if we're not. */
	protected void rangeCheck() throws StackOverflowError {
		if (stackPointer<0) throw new StackOverflowError("The EsoVM stack has overflowed (data was pushed but there's no room for it)");
		if (stackPointer>=stack.length) throw new StackOverflowError("The EsoVM stack has underflowed (data was popped but the stack is empty)"); 
	}
}
