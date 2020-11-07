package blue.endless.junior.esovm;

public class Interpreter {
	protected byte[] program;
	protected int programCounter = 0;
	protected Stack stack = new Stack(65536);
	
	public void loadProgram(byte[] program, int mainFunctionPointer) {
		stack.clear();
	}
}
