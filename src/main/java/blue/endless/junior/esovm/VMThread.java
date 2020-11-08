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
	public static final int OPERAND_ADDRESS_TO_CONSTANT  = 0x2;
	public static final int OPERAND_CONSTANT = 0x3;
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
	
	public static final int OPCODE_CONVERT = 0x30; //Convert
	
	public static final int OPCODE_CALL    = 0x40; //Simple - operand 1 is ignored, operand 2 is an offset (or label) into the data segment for a pascal string containing the method's unique identifier
	
	public static final int OPCODE_INTERRUPT = 0xF0; //Simple - operand 1 is ignored, operand 2 is the index into the interrupt vector table (the interrupt to trigger).
	public static final int OPCODE_OUT       = 0xF1; //Simple - operand 1 is the value to write, operand 2 is the port index to write to
	public static final int OPCODE_IN        = 0xF2; //Simple - operand 1 is the destination, operand 2 is the port index to read from
	
	public static final int OPCODE_HALT      = 0xFF; //Simple - operand 1 is ignored and MUST be zeroes, operand 2 is zero for a normal halt, or an offset (or label) into the constant pool for a pascal string containing an error code.
	
	protected Stack stack = new Stack(65535);
	protected byte[] program;
	protected int programCounter;
	
	public VMThread(byte[] program, int address) {
		stack.clear();
		this.program = program;
		this.programCounter = address;
	}
	
	public void cycle() throws VMException {
		checkBounds();
		int opcode = program[programCounter];
		switch(opcode) {
		case OPCODE_LOAD:
			int instructionType = program[programCounter+1] >>> 4;
			int operand2Type = program[programCounter+1] & 0xFF;
			int lvtIndex = program[programCounter+3];
			load(instructionType, lvtIndex, operand2Type, operand2Standard());
		break;
		
		
		default:
			throw new VMException("Unknown opcode 0x"+Integer.toHexString(opcode));
		}
		//TODO: Execute
		programCounter += 8;
	}
	
	protected void checkBounds() throws VMException {
		if (programCounter<0 || programCounter>=program.length) throw new VMException();
	}
	
	protected void checkCompatibility(int type1, int type2) throws VMException {
		//TODO: Implement. Basically, if 2 is a "concrete type", as in a register type, it MUST equal 1
	}
	
	public void load(int instructionType, int lvtIndex, int operand2Type, int operand2) throws VMException {
		switch(instructionType) {
		//long value = loadValueMemoryOk(operand2Type, operand2());
		case DATA_INT8:
			stack.currentStackFrame().putInt8(lvtIndex, loadInt8(operand2Type, operand2, true));
		case DATA_INT16:
			stack.currentStackFrame().putInt16(lvtIndex, loadInt16(operand2Type, operand2, true));
		break;
		default:
			throw new VMException("Invalid instruction data type 0x"+Integer.toHexString(instructionType));
		}
	}
	
	public byte loadInt8(int operand2Type, int operand2, boolean allowMemory) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			return stack.currentStackFrame().getInt8(operand2);
		case OPERAND_IMMEDIATE:
			return (byte)operand2;
		case OPERAND_ADDRESS_TO_CONSTANT:
			return (byte)operand2; //TODO: Add constant pool base address
		default:
			throw new VMException("Invalid operandType 0x"+Integer.toHexString(operand2Type));
		}
	}
	
	public short loadInt16(int operand2Type, int operand2, boolean allowMemory) throws VMException {
		switch(operand2Type) {
		case OPERAND_REGISTER:
			return stack.currentStackFrame().getInt16(operand2);
		case OPERAND_IMMEDIATE:
			return (short)operand2;
		case OPERAND_ADDRESS_TO_CONSTANT:
			return (short)operand2; //TODO: Add constant pool base address
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
