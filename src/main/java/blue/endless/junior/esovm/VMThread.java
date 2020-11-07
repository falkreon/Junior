package blue.endless.junior.esovm;

public class VMThread {
	public static final int OPCODE_LOAD  = 0x00;
	public static final int OPCODE_STORE = 0x01;
	public static final int OPCODE_PUSH  = 0x02;
	public static final int OPCODE_POP   = 0x03;
	
	public static final int OPCODE_ALLOCATE       = 0x10;
	public static final int OPCODE_FREE           = 0x11;
	public static final int OPCODE_ALLOCATE_STACK = 0x12;
	public static final int OPCODE_FREE_STACK     = 0x13;
	
	public static final int OPCODE_ADD = 0x20;
	public static final int OPCODE_SUB = 0x21;
	public static final int OPCODE_MUL = 0x22;
	public static final int OPCODE_DIV = 0x23;
	public static final int OPCIDE_MOD = 0x24;
	
	public static final int OPCODE_CONVERT = 0x30;
	
	public static final int OPCODE_INTERRUPT = 0xF0;
	public static final int OPCODE_OUT       = 0xF1;
	public static final int OPCODE_IN        = 0xF2;
	
	public static final int OPCODE_HALT      = 0xFF;
	
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
}
