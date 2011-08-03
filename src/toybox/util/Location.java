package toybox.util;

/**
 * A representation of a location (line, column) in an input string.
 * 
 * @author Bruno Dufour (dufour@iro.umontreal.ca)
 *
 */
public class Location {
	public final int line;
	public final int column;

	public Location(int line, int column) {
		this.line = line;
		this.column = column;
	}
	
	@Override
	public int hashCode() {
		final int prime = 997;
		int result = 1;
		result = prime * result + column;
		result = prime * result + line;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Location other = (Location) obj;
		if (column != other.column) {
			return false;
		}
		if (line != other.line) {
			return false;
		}
		return true;
	}

	public String toString() {
		return "line " + line + ", column " + column;
	}
}