package com.github.s4ke.moar.moa.states;

import com.github.s4ke.moar.strings.EfficientString;

/**
 * This represents a Variable in a Memory Occurence Automaton
 *
 * @author Martin Braun
 */
public class Variable {

	public final EfficientString contents = new EfficientString();
	public final String name;
	private boolean open = false;
	private int occurenceInRegex = -1;

	/**
	 * does not copy state
	 */
	public Variable(Variable variable) {
		this( variable.name );
		this.occurenceInRegex = variable.occurenceInRegex;
	}

	public Variable(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void open() {
		this.open = true;
	}

	public boolean isOpen() {
		return this.open;
	}

	public void close() {
		this.open = false;
	}

	public int getOccurenceInRegex() {
		return this.occurenceInRegex;
	}

	public void setOccurenceInRegex(int occurenceInRegex) {
		this.occurenceInRegex = occurenceInRegex;
	}

	@Override
	public String toString() {
		return "Variable{" +
				"contents=" + contents +
				", name='" + name + '\'' +
				", open=" + open +
				'}';
	}

	public EfficientString getEdgeString() {
		return this.contents;
	}

	public String getContents() {
		return this.contents.toString();
	}

	public void reset() {
		this.contents.reset();
	}

	public boolean canConsume() {
		return this.isOpen();
	}

	public void consume(EfficientString str) {
		if ( !this.canConsume() ) {
			throw new IllegalStateException( "cannot consume at the moment!" );
		}
		this.contents.appendOrOverwrite( str );
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		Variable variable = (Variable) o;

		if ( open != variable.open ) {
			return false;
		}
		if ( occurenceInRegex != variable.occurenceInRegex ) {
			return false;
		}
		return !(name != null ? !name.equals( variable.name ) : variable.name != null);

	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (open ? 1 : 0);
		result = 31 * result + occurenceInRegex;
		return result;
	}
}
