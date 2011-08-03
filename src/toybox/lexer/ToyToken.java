package toybox.lexer;

import toybox.util.Location;

/**
 * A simply token type, provided as a convenience. 
 * 
 * @author Bruno Dufour (dufour@iro.umontreal.ca)
 */
public class ToyToken {	
	public final int type;
	public final String s;
	public final Location location;

	public ToyToken(int type, String s, Location location) {
		this.type = type;
		this.s = s;
		this.location = location;
	}

	@Override
	public int hashCode() {
		final int prime = 983;
		int result = 1;
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((s == null) ? 0 : s.hashCode());
		result = prime * result + type;
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
		ToyToken other = (ToyToken) obj;
		if (location == null) {
			if (other.location != null) {
				return false;
			}
		} else if (!location.equals(other.location)) {
			return false;
		}
		if (s == null) {
			if (other.s != null) {
				return false;
			}
		} else if (!s.equals(other.s)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}

	public String toString() {
		return "(" + this.type + ", " + this.s + "," + this.location + ")";
	}
}
