package blue.endless.junior;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import blue.endless.junior.ast.Instruction;

public class Assembler {
	public void assemble(List<Instruction> compiledBlock) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		
		//header
		out.writeInt(0xCAFEBABE);
		
		
		
		for(Instruction instruction : compiledBlock) {
			
		}
	}
}
