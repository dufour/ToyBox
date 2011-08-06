package toybox.lexer;

import static toybox.lexer.State.ANY;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import toybox.util.IO;
import toybox.util.Location;

/**
 * A base for creating simple lexers. Token rules are specified using @Token
 * annotations. There are two main strategies for creating lexers:
 * <ul>
 *   <li>Extend the <code>ToyLexer</code> class and annotate at least one method
 *       with a <code>@Token</code> annotation.</li>
 *   <li>Create a <code>ToyLexer</code> instance and add objects that contain
 *       <code>@Token</code>-annotated methods, either directly in the constructor
 *       or via the <code>add</code> method.</li>
 * </ul>
 * 
 * Note that when searching for token rules, methods in superclasses are
 * considered recursively as well.
 * 
 * @author Bruno Dufour (dufour@iro.umontreal.ca)
 *
 * @param <T> The token type
 * @see toybox.lexer.Token
 * 
 */
public class ToyLexer<T> {
	private static final String[] DEFAULT_STATES = new String[] { ANY };
	
	/*
	 * To Do:
	 *   - Make sure that overridden/inaccessible methods don't get registered
	 */
	private List<TokenRule> rules = new LinkedList<TokenRule>();
	private String input; 	
	private int pos;
	private int lineno;
	private int column;
	private Stack<String> states = new Stack<String>();
	
	public enum MatchStrategy {
		MATCH_FIRST,
		MATCH_LONGEST;
	}
	
	private MatchStrategy match_strategy = MatchStrategy.MATCH_LONGEST;
	
	public ToyLexer() {
		add(this);
		states.push(ANY);
	}
	
	public ToyLexer(Object... rules) {
		for (Object r: rules) {
			add(r);
		}
		states.push(ANY);
	}
	
	public void enter(String state) {
		this.states.push(state);
	}
	
	public String state() {
		return this.states.peek();
	}
	
	public void exit(String state) {
		String current = this.states.pop();
		if (!current.equals(state)) {
			throw new IllegalStateException("State mismatch");
		}
	}
	
	public MatchStrategy getMatchStrategy() {
		return match_strategy;
	}
	
	public void setMatchStrategy(MatchStrategy strategy) {
		this.match_strategy = strategy;
	}

	public void input(File f) throws IOException {
		input(new BufferedReader(new FileReader(f)));
	}
	
	public void input(Reader r) throws IOException {
		this.input(IO.readFully(r));
	}
	
	public void input(InputStream s) throws IOException {
		this.input(IO.readFully(s));
	}
	
	public void input(String s) {
		this.input = s;
		this.pos = 0;
		this.lineno = 1;
		this.column = 1;
	}
	
	public void add(Object obj) {		
		Class<? extends Object> c = obj.getClass();
		Set<Method> reachableMethods = new HashSet<Method>(Arrays.asList(c.getMethods()));
		this.add(obj, c, reachableMethods);
	}
	
	private void add(Object obj, Class<?> c, Set<Method> reachable) {
		for (Method m: c.getDeclaredMethods()) {
			if (!reachable.contains(m)) continue;
			
			if (m.isAnnotationPresent(Token.class)) {
				Token token = m.getAnnotation(Token.class);
				for (String v: token.value()) {
					Pattern p = Pattern.compile(v);
					register(p, obj, m);
				}
			}
		}
		
		Class<?> sc = c.getSuperclass();
		if (sc != null) {
			this.add(obj, sc, reachable);
		}
	}
	
	private void register(Pattern p, Object r, Method m) {
		checkSignature(m);
		String[] states;
		if (m.isAnnotationPresent(State.class)) {
			states = m.getAnnotation(State.class).value();
		} else {
			states = DEFAULT_STATES;
		}
		this.rules.add(new TokenRule(p, r, m, states));
	}
	
	private static void checkSignature(Method m) {
		Class<?>[] types = m.getParameterTypes();
		if (types.length == 0 || types.length > 2) {
			throw new IllegalStateException("Wrong number parameters for " + m.getName());
		}
		if (!types[0].equals(String.class)) {
			throw new IllegalStateException("Wrong parameter type for " + m.getName());
		}
		if (types.length > 1 && !types[1].equals(Location.class)) {
			throw new IllegalStateException("Wrong parameter type for " + m.getName());
		}
	}
	
	@SuppressWarnings("unchecked")
	private T makeToken(TokenRule r, String val) {
		try {
			if (r.wantsLocation()) {
				return (T) r.method.invoke(r.receiver, val, location());
			} else {
				return (T) r.method.invoke(r.receiver, val);
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void skip(int count) {
		this.pos += count;
	}
	
	public int position() {
		return this.pos;		
	}
	
	public int lineno() {
		return this.lineno;
	}
	
	public int column() {
		return this.column;
	}
	
	public Location location() {
		return new Location(this.lineno, this.column);
	}
	
	private void updateLocation(String val) {
		int i = val.length();
		pos += i;
		i = val.lastIndexOf('\n', i - 1);
		if (i >= 0) {
			column = val.length() - i;
		} else {
			column += val.length();
		}
		
		while (i >= 0) {
			lineno++;
			i = val.lastIndexOf('\n', i - 1);
		}
	}
	
	public boolean eof() {
		return this.pos >= this.input.length();
	}
	
	public T next() {
matching:
		while (true) {
			if (eof()) return null;
			
			TokenRule candidate = null;
			String maxMatch = null;
			
			for (TokenRule r: this.rules) {
				if (!r.validForState(this.state())) continue;
				
				Matcher m = r.pattern.matcher(this.input);
				if (m.find(pos)) {
					MatchResult result = m.toMatchResult();
					if (result.start() == pos) {
						// Rule matches
						maxMatch = result.group();
						candidate = r;
						
						if (match_strategy == MatchStrategy.MATCH_FIRST) {
							break;
						}
					}
				}
			}
			
			if (candidate != null) {
				updateLocation(maxMatch);
				T token = makeToken(candidate, maxMatch);
				if (token == null) {
					continue matching;			
				}
				return token;
			}
			
			// No match
			throw new IllegalStateException("No match at " + location());
		}
	}

	private static class TokenRule {
		public final Pattern pattern;
		public final Method method;
		public final Object receiver;
		public final String[] states;
		
		public TokenRule(Pattern p, Object r, Method m, String[] s) {			
			pattern = p;
			receiver = r; 
			method = m;
			states = s;
		}
		
		public boolean validForState(String state) {
			if (state == ANY) {
				return true;
			}
			
			for (String s: states) {
				if (s == ANY || s.equals(state)) {
					return true;
				}
			}
			
			return false;
		}
		
		public boolean wantsLocation() {
			Class<?>[] types = method.getParameterTypes();
			return types.length == 2;
		}
	}
}
