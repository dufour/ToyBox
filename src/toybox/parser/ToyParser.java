package toybox.parser;


import toybox.lexer.Token;
import toybox.lexer.ToyLexer;
import toybox.util.Location;

/**
 * A base class for creating top-down (recursive descent) parsers) with
 * support for lookahead (LL(k)).
 * 
 * @author Bruno Dufour (dufour@iro.umontreal.ca)
 */
public class ToyParser<T> {
	private ToyLexer<T> lexer;
	private TokenQueue<T> tokens;

	public ToyParser(ToyLexer<T> lexer) {
		this(lexer, 0);
	}
	
	public ToyParser(ToyLexer<T> lexer, int lookahead) {
		this.lexer = lexer;		
		this.tokens = new TokenQueue<T>(lookahead + 1);
		
		this.advance();
	}
	
	protected ToyLexer<T> getLexer() {
		return this.lexer;
	}
	
	protected void fail(String message) {
		throw new RuntimeException(message);
	}
	
	protected void fail(String message, Location loc) {
		this.fail(message + " at " + loc);
	}
	
	protected void fail(String message, Token t) {
		this.fail(message, this.lexer.location());
	}
	
	protected void assertThat(boolean condition, String message) {
		this.assertThat(condition, message, this.lexer.location());
	}
	
	protected void assertThat(boolean condition, String message, Location loc) {
		if (!condition) {
			this.fail(message, loc);
		}
	}
	
	private void advance() {
		T token = this.lexer.next();
		this.tokens.put(token);
	}
	
	protected T token() {
		T token = this.tokens.take();
		this.advance();
		return token;
	}
	
	protected T peek() {
		return this.peek(0);
	}
	
	protected T peek(int index) {
		if (index >= this.tokens.capacity()) {
			fail("Trying to read past specified lookahead", this.lexer.location());
		}
		while (!this.lexer.eof() && this.tokens.size() < index + 1) {
			this.advance();
		}
		
		return this.tokens.get(index);
	}
}
