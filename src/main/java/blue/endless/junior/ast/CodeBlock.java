package blue.endless.junior.ast;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represents the body of a method, lambda, "if" or "else" statement, anonymous block, etc. Basically
 * any container for statements to be executed one-after-the-other given a context.
 */
public class CodeBlock implements Iterable<Statement> {
	protected ArrayList<Statement> statements = new ArrayList<>();

	@Override
	public Iterator<Statement> iterator() {
		return statements.iterator();
	}
}
