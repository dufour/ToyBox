package toybox.parser;

import java.util.NoSuchElementException;

/**
 * A basic circular queue implementation.  
 * 
 * @author Bruno Dufour (dufour@iro.umontreal.ca)
 * 
 * @param<E> The element type
 */
class TokenQueue<E> {
	private int first = 0;
	private int size = 0;
	private Object[] data;
	
	public TokenQueue(int capacity) {
		data = new Object[capacity];
	}
	
	@SuppressWarnings("unchecked")
	public E get(int index) {
		if (index >= 0 && index < this.size) {
			return (E) data[(first + index) % data.length];
		}
		
		throw new NoSuchElementException();
	}
	
	public E peek() {
		return this.get(0);
	}

	public void put(E element) {
		if (isFull()) {
			throw new RuntimeException("Full queue");
		}
					
		data[(first + size) % data.length] = element;
		size += 1;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public boolean isFull() {
		return size == data.length;
	}

	@SuppressWarnings("unchecked")
	public E take() {
		if (isEmpty()) {
			throw new NoSuchElementException();
		}
		
		E element = (E) data[first];
		data[first] = null;			
		first = (first + 1) % data.length;
		size--;
		return element;
	}
	
	public int size() {
		return size;
	}
	
	public int capacity() {
		return this.data.length;
	}
}