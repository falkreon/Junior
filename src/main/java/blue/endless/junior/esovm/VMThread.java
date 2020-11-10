package blue.endless.junior.esovm;

public class VMThread {
	/*
	 * Opcode formats:
	 * 
	 * * Standard:
	 *      2-operand with destination
	 *      
	 *      struct StandardInstruction {
	 *        int8  opcode
	 *        int4  dataType ; high bits
	 *        int4  operand2Type ; low bits
	 *        int8  destination
	 *        int8  operand1
	 *        int32 operand2
	 *      }
	 *      
	 * * Simple:
	 *      2-operand
	 *      
	 *      struct NoDestination {
	 *        int8 opcode
	 *        int4 dataType     ; high bits
	 *        int4 operand2Type ; low bits
	 *        int8 operand1
	 *        int48 operand2
	 *      }
	 * 
	 * * Convert
	 *      1 operand with 1 destination of a different type
	 *      
	 *      struct Conversion {
	 *        int8 opcode
	 *        int4 sourceDataType   ; high bits
	 *        int4 destDataType     ; low bits
	 *        int8 destination
	 *        int8 operand
	 *        int32 zeroes         ; MUST be ignored by interpreters and converters
	 *      }
	 */
	
	
	//Operand2Types: Operand 2 can often be any of several options - a register, memory address, something from the constant pool, an immediate value, etc.
	public static final int OPERAND_REGISTER  = 0x0;
	public static final int OPERAND_IMMEDIATE = 0x1;
	public static final int OPERAND_CONSTANT  = 0x2;
	//0x0..0x3 are always valid for non-ignored operands, but the following are memory types, and are only valid for load and store:
	/* Loading this will load the value at address `register+operand2` */
	public static final int OPERAND_REGISTER_ADDRESS_PLUS_IMMEDIATE_OFFSET = 0x4;
	/* Loading this will load the value at address `register+constant` where constant is the value at the constant-offset pointed to by immediate value operand2 */
	public static final int OPERAND_REGISTER_ADDRESS_PLUS_CONSTANT_OFFSET  = 0x5;
	/* Loading this will load the value at address `register1+register2` */
	public static final int OPERAND_REGISTER_ADDRESS_PLUS_REGISTER_OFFSET  = 0x6;
	/* Loading this will first load the value at `register+operand2`, and use it as an address to load the result */
	public static final int OPERAND_INDIRECT_ADDRESS_WITH_IMMEDIATE_OFFSET = 0x7;
	/* Loading this will first load the value at `register+constant`, and use it as an address to load the result */
	public static final int OPERAND_INDIRECT_ADDRESS_WITH_CONSTANT_OFFSET  = 0x8;
	/* Loading this will first load the value at `register1+register2`, and use it as an address to load the result */
	public static final int OPERAND_INDIRECT_ADDRESS_WITH_REGISTER_OFFSET  = 0x9;
	//0xA-0xF: Reserved for future use. If these are encountered in an instruction stream, the VM or compiler MUST halt with an invalid operand error.
	
	
	//DataTypes: Instructions can operate on one dataType - or two if it's a conversion.
	public static final int DATA_INT8    = 0x0;
	public static final int DATA_INT16   = 0x1;
	public static final int DATA_INT32   = 0x2;
	public static final int DATA_INT64   = 0x3;
	public static final int DATA_FLOAT16 = 0x4;
	public static final int DATA_FLOAT32 = 0x5;
	public static final int DATA_FLOAT64 = 0x6;
	public static final int DATA_WORD    = 0x7;
	//0x8-0xF: Reserved for future use. If these are encountered in an instruction stream, the VM or compiler MUST halt with a type error.
	
	
	public static final int OPCODE_LOAD  = 0x00; //Simple - operand 1 is the destination register, operand 2 is the data source
	public static final int OPCODE_STORE = 0x01; //Simple - operand 1 is the source register, operand 2 is the data destination
	public static final int OPCODE_PUSH  = 0x02; //Simple - operand 1 is the source register, operand 2 is ignored and SHOULD be zeroes
	public static final int OPCODE_POP   = 0x03; //Simple - operand 1 is the destination register, operand 2 is ignored and SHOULD be zeroes
	
	public static final int OPCODE_ALLOCATE       = 0x10; //simple - operand 1 is the destination register to place the address in, operand 2 is the amount of memory to reserve
	public static final int OPCODE_FREE           = 0x11; //simple - operand 1 is the register containing the address, operand 2 is ignored and SHOULD be zeroes
	public static final int OPCODE_ALLOCATE_STACK = 0x12; //same as allocate
	public static final int OPCODE_FREE_STACK     = 0x13; //same as free
	
	public static final int OPCODE_ADD = 0x20; //Standard
	public static final int OPCODE_SUB = 0x21; //Standard
	public static final int OPCODE_MUL = 0x22; //Standard
	public static final int OPCODE_DIV = 0x23; //Standard
	public static final int OPCIDE_MOD = 0x24; //Standard
	public static final int OPCODE_SHL = 0x25; //Standard - only valid for int types
	public static final int OPCODE_SHR = 0x26; //Standard - only valid for int types
	public static final int OPCODE_ASR = 0x27; //Standard - only valid for int types - sign-extending "Arithmetic Shift Right"
	
	public static final int OPCODE_CONVERT = 0x30; //Convert
	
	public static final int OPCODE_CALL    = 0x40; //Simple - operand 1 is ignored, operand 2 is an index in the constant-string pool of a method to call.
	/*
	 * Method calls in esovm on the jvm will first look for a junior method matching the signature. If none are found, then java is reflexively asked for the method, and
	 * based on the first method found matching the string as a fully-qualified binary name, arguments will be popped off the stack in reverse order and a conversion
	 * attempted to the method's parameter types. The ability to call "external" Java methods can be disabled from the Interpreter.
	 */
	
	public static final int OPCODE_TEST    = 0x41; //Simple - operand 1 is a register, everything else is ignored.
	public static final int OPCODE_JUMP    = 0x42; //Simple - operand 1 is ignored, jumps to address at operand 2 unconditionally.
	public static final int OPCODE_CJUMP   = 0x43; //Simple - operand 1 is a condition code, jumps to address at operand 2 if the condition is met
	/*
	 * Conditions:
	 *   0x00: "Equal", "Zero", "==0" (ZF==1)
	 *   0x01: "Not-Equal", "Nonzero", "!=0" (ZF==0)
	 *   0x02: "Less", "Not Greater or Equal", "<" (ZF==0 and SF!=CF)
	 *   0x03: "Greater", "Not Less or Equal", "<" (ZF==0 and SF==CF)
	 *   0x04: "Negative", "Less than Zero", "<0" (SF==1)
	 *   0x05: "Not-Negative", "Greater or Equal to Zero", ">=0" (SF==0)
	 */
	
	public static final int OPCODE_CLOAD   = 0x44; //Standard - operand 1 is a condition code, operand 2 is the element to load
	public static final int OPCODE_CSTORE  = 0x45; //Standard - operand 1 is a condition code, operand 2 is the data destination, what is normally destination is the source register
	
	
	public static final int OPCODE_INTERRUPT = 0xF0; //Simple - operand 1 is ignored, operand 2 is the index into the interrupt vector table (the interrupt to trigger).
	public static final int OPCODE_OUT       = 0xF1; //Simple - operand 1 is the value to write, operand 2 is the port index to write to
	public static final int OPCODE_IN        = 0xF2; //Simple - operand 1 is the destination, operand 2 is the port index to read from
	
	public static final int OPCODE_HALT      = 0xFF; //Simple - operand 1 is ignored and MUST be zeroes, operand 2 is zero for a normal halt, or an index into the constant-string pool for an error description.
	
	protected Stack stack = new Stack(65535);
	protected byte[] program;
	protected int programCounter;
	protected boolean active = true;
	
	public VMThread(byte[] program, int address) {
		stack.clear();
		this.program = program;
		this.programCounter = address;
	}
	
	public void cycle() throws VMException {
		if (!active) return;
		try {
			checkBounds();
			int opcode = program[programCounter];
			switch(opcode) {
			case OPCODE_LOAD: {
				int instructionType = program[programCounter+1] >>> 4;
				int operand2Type = program[programCounter+1] & 0x0F;
				int lvtIndex = program[programCounter+3];
				load(instructionType, lvtIndex, operand2Type, operand2Standard());
				break;
			}
			case OPCODE_STORE: {
				int instructionType = program[programCounter+1] >>> 4;
				int operand2Type = program[programCounter+1] & 0x0F;
				int lvtIndex = program[programCounter+3];
				store(instructionType, lvtIndex, operand2Type, operand2Standard());
				break;
			}
			case OPCODE_PUSH: {
				int instructionType = program[programCounter+1] >>> 4;
				int lvtIndex = program[programCounter+3];
				push(instructionType, lvtIndex);
				break;
			}
			
			//TODO: Rest of instructions
			
			case OPCODE_HALT: {
				active = false;
				break;
			}
			default:
				throw new VMException("Unknown opcode 0x"+Integer.toHexString(opcode));
			}
			//TODO: Execute
			programCounter += 8;
		} catch (VMException ex) {
			active = false;
			throw ex;
		}
	}
	
	/**
	 * Returns true if this thread is still running. In other words, true is returned if cycle() should continue be called to
	 * dispatch more instructions. False will be returned if the machine hits either a natural or abnormal halt.
	 */
	public boolean isActive() {
		return active;
	}
	
	protected void checkBounds() throws VMException {
		if (programCounter<0 || programCounter>=program.length) throw new VMException();
	}
	
	/*
	 * You may notice that what follows here could be described as "crimes".
	 * 
	 * Some day, when we re-implement esovm in an esolang, we'll have primitive-type function specialization and can avoid this. All of this.
	 */
	
	protected void load(int instructionType, int lvtIndex, int operand2Type, int operand2) throws VMException {
		switch(instructionType) {
		case DATA_INT8:
			stack.currentStackFrame().putInt8(lvtIndex, loadInt8(operand2Type, operand2, true));
			break;
		case DATA_INT16:
			stack.currentStackFrame().putInt16(lvtIndex, loadInt16(operand2Type, operand2, true));
			break;
		case DATA_INT32:
			stack.currentStackFrame().putInt32(lvtIndex, loadInt32(operand2Type, operand2, true));
			break;
		case DATA_INT64:
			stack.currentStackFrame().putInt64(lvtIndex, loadInt64(operand2Type, operand2, true));
			break;
		case DATA_FLOAT16:
			stack.currentStackFrame().putFloat16(lvtIndex, loadFloat16(operand2Type, operand2, true));
			break;
		case DATA_FLOAT32:
			stack.currentStackFrame().putFloat32(lvtIndex, loadFloat32(operand2Type, operand2, true));
			break;
		case DATA_FLOAT64:
			stack.currentStackFrame().putFloat64(lvtIndex, loadFloat64(operand2Type, operand2, true));
			break;
		case DATA_WORD:
			stack.currentStackFrame().putWord(lvtIndex, loadWord(operand2Type, operand2, true));
		default:
			throw new VMException("Invalid instruction data type 0x"+Integer.toHexString(instructionType));
		}
	}
	
	protected void store(int instructionType, int lvtIndex, int operand2Type, int operand2) throws VMException {
		switch(instructionType) {
		case DATA_INT8:
			storeInt8(operand2Type, operand2, stack.currentStackFrame().getInt8(lvtIndex));
			break;
		case DATA_INT16:
			storeInt16(operand2Type, operand2, stack.currentStackFrame().getInt16(lvtIndex));
			break;
		case DATA_INT32:
			storeInt32(operand2Type, operand2, stack.currentStackFrame().getInt32(lvtIndex));
			break;
		case DATA_INT64:
			storeInt64(operand2Type, operand2, stack.currentStackFrame().getInt64(lvtIndex));
			break;
		case DATA_FLOAT16:
			storeFloat16(operand2Type, operand2, stack.currentStackFrame().getFloat16(lvtIndex));
			break;
		case DATA_FLOAT32:
			storeFloat32(operand2Type, operand2, stack.currentStackFrame().getFloat32(lvtIndex));
			break;
		case DATA_FLOAT64:
			storeFloat64(operand2Type, operand2, stack.currentStackFrame().getFloat64(lvtIndex));
			break;
		case DATA_WORD:
			storeWord(operand2Type, operand2, stack.currentStackFrame().getWord(lvtIndex));
			break;
		default:
			throw new VMException("Invalid instruction data type 0x"+Integer.toHexString(instructionType));
		}
	}
	
	protected void push(int instructionType, int lvtIndex) throws VMException {
		switch(instructionType) {
		case DATA_INT8:
			stack.pushInt8(stack.currentStackFrame().getInt8(lvtIndex));
			break;
		case DATA_INT16:
			stack.pushInt16(stack.currentStackFrame().getInt16(lvtIndex));
			break;
		case DATA_INT32:
			stack.pushInt32(stack.currentStackFrame().getInt32(lvtIndex));
			break;
		case DATA_INT64:
			stack.pushInt64(stack.currentStackFrame().getInt64(lvtIndex));
			break;
		case DATA_FLOAT16:
			stack.pushFloat16(stack.currentStackFrame().getFloat16(lvtIndex));
			break;
		case DATA_FLOAT32:
			stack.pushFloat32(stack.currentStackFrame().getFloat32(lvtIndex));
			break;
		case DATA_FLOAT64:
			stack.pushFloat64(stack.currentStackFrame().getFloat64(lvtIndex));
			break;
		case DATA_WORD:
			stack.pushWord(stack.currentStackFrame().getWord(lvtIndex));
			break;
		default:
			throw new VMException("Invalid instruction data type 0x"+Integer.toHexString(instructionType));
		}
	}
	
	protected void pop(int instructionType, int lvtIndex) throws VMException {
		switch(instructionType) {
		case DATA_INT8:
			stack.currentStackFrame().putInt8(lvtIndex, stack.popInt8());
			break;
		case DATA_INT16:
			stack.currentStackFrame().putInt16(lvtIndex, stack.popInt16());
			break;
		case DATA_INT32:
			stack.currentStackFrame().putInt32(lvtIndex, stack.popInt32());
			break;
		case DATA_INT64:
			stack.currentStackFrame().putInt64(lvtIndex, stack.popInt64());
			break;
		case DATA_FLOAT16:
			stack.currentStackFrame().putFloat16(lvtIndex, stack.popFloat16());
			break;
		case DATA_FLOAT32:
			stack.currentStackFrame().putFloat32(lvtIndex, stack.popFloat32());
			break;
		case DATA_FLOAT64:
			stack.currentStackFrame().putFloat64(lvtIndex, stack.popFloat64());
			break;
		case DATA_WORD:
			stack.currentStackFrame().putWord(lvtIndex, stack.popInt64());
			break;
		}
	}
	
	protected byte loadInt8(int operand2Type, int operand2, boolean allowMemory) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			return stack.currentStackFrame().getInt8(operand2);
		case OPERAND_IMMEDIATE:
			return (byte)operand2;
		case OPERAND_CONSTANT:
			throw new VMException("Not yet implemented");
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	protected void storeInt8(int operand2Type, int operand2, byte value) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			stack.currentStackFrame().putInt8(operand2, value);
			break;
		case OPERAND_IMMEDIATE:
			throw new VMException("Can't store to immediate value");
		case OPERAND_CONSTANT:
			throw new VMException("Can't store sto constant value");
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	protected short loadInt16(int operand2Type, int operand2, boolean allowMemory) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			return stack.currentStackFrame().getInt16(operand2);
		case OPERAND_IMMEDIATE:
			return (short)operand2;
		case OPERAND_CONSTANT:
			throw new VMException("Not yet implemented");
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	protected void storeInt16(int operand2Type, int operand2, short value) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			stack.currentStackFrame().putInt16(operand2, value);
			break;
		case OPERAND_IMMEDIATE:
			throw new VMException("Can't store to immediate value");
		case OPERAND_CONSTANT:
			throw new VMException("Can't store sto constant value");
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	protected int loadInt32(int operand2Type, int operand2, boolean allowMemory) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			return stack.currentStackFrame().getInt32(operand2);
		case OPERAND_IMMEDIATE:
			return operand2;
		case OPERAND_CONSTANT:
			throw new VMException("Not yet implemented");
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	protected void storeInt32(int operand2Type, int operand2, int value) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			stack.currentStackFrame().putInt32(operand2, value);
			break;
		case OPERAND_IMMEDIATE:
			throw new VMException("Can't store to immediate value");
		case OPERAND_CONSTANT:
			throw new VMException("Can't store sto constant value");
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	protected long loadInt64(int operand2Type, int operand2, boolean allowMemory) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			return stack.currentStackFrame().getInt64(operand2);
		case OPERAND_IMMEDIATE:
			return operand2;
		case OPERAND_CONSTANT:
			throw new VMException("Not yet implemented");
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	protected void storeInt64(int operand2Type, int operand2, long value) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			stack.currentStackFrame().putInt64(operand2, value);
			break;
		case OPERAND_IMMEDIATE:
			throw new VMException("Can't store to immediate value");
		case OPERAND_CONSTANT:
			throw new VMException("Can't store sto constant value");
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	protected long loadWord(int operand2Type, int operand2, boolean allowMemory) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			return stack.currentStackFrame().getWord(operand2);
		case OPERAND_IMMEDIATE:
			return operand2;
		case OPERAND_CONSTANT:
			throw new VMException("Not yet implemented");
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	protected void storeWord(int operand2Type, int operand2, long value) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			stack.currentStackFrame().putWord(operand2, value);
			break;
		case OPERAND_IMMEDIATE:
			throw new VMException("Can't store to immediate value");
		case OPERAND_CONSTANT:
			throw new VMException("Can't store sto constant value");
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	protected short loadFloat16(int operand2Type, int operand2, boolean allowMemory) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			return stack.currentStackFrame().getFloat16(operand2);
		case OPERAND_IMMEDIATE:
			return (short)operand2;
		case OPERAND_CONSTANT:
			throw new VMException("Not yet implemented");
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	protected void storeFloat16(int operand2Type, int operand2, short value) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			stack.currentStackFrame().putFloat16(operand2, value);
			break;
		case OPERAND_IMMEDIATE:
			throw new VMException("Can't store to immediate value");
		case OPERAND_CONSTANT:
			throw new VMException("Can't store sto constant value");
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	protected float loadFloat32(int operand2Type, int operand2, boolean allowMemory) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			return stack.currentStackFrame().getFloat32(operand2);
		case OPERAND_IMMEDIATE:
			return Float.intBitsToFloat(operand2);
		case OPERAND_CONSTANT:
			throw new VMException("Not yet implemented");
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	protected void storeFloat32(int operand2Type, int operand2, float value) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			stack.currentStackFrame().putFloat32(operand2, value);
			break;
		case OPERAND_IMMEDIATE:
			throw new VMException("Can't store to immediate value");
		case OPERAND_CONSTANT:
			throw new VMException("Can't store sto constant value");
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	protected double loadFloat64(int operand2Type, int operand2, boolean allowMemory) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			return stack.currentStackFrame().getFloat64(operand2);
		case OPERAND_IMMEDIATE:
			return (double) Float.intBitsToFloat(operand2);
		case OPERAND_CONSTANT:
			throw new VMException("Not yet implemented");
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	protected void storeFloat64(int operand2Type, int operand2, double value) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			stack.currentStackFrame().putFloat64(operand2, value);
			break;
		case OPERAND_IMMEDIATE:
			throw new VMException("Can't store to immediate value");
		case OPERAND_CONSTANT:
			throw new VMException("Can't store sto constant value");
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	protected int operand2Standard() {
		return
				(program[programCounter+4] << 24) |
				(program[programCounter+5] << 16) |
				(program[programCounter+6] <<  8) |
				 program[programCounter+7];
	}
	
	protected long operand2Simple() {
		return
				(program[programCounter+3] << 32) |
				(program[programCounter+4] << 24) |
				(program[programCounter+5] << 16) |
				(program[programCounter+6] <<  8) |
				 program[programCounter+7];
	}
}
