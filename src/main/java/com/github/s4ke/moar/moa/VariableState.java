/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.s4ke.moar.moa;

/**
 * @author Martin Braun
 */
public class VariableState implements State {

	private final int idx;
	private final Variable variable;
	private String contents;
	private boolean touched = false;

	public VariableState(int idx, Variable variable) {
		this.idx = idx;
		this.variable = variable;
	}

	public Variable getVariable() {
		return this.variable;
	}

	@Override
	public int getIdx() {
		return this.idx;
	}

	@Override
	public String getEdgeString() {
		return this.variable.getEdgeString();
	}

	public boolean varOpen() {
		return this.variable.isOpen();
	}

	public void reset() {
		this.touched = false;
		this.contents = null;
	}

	@Override
	public void touch() {
		if ( !this.touched ) {
			this.contents = variable.contents.toString();
			this.touched = true;
		}
	}

	public String getContents() {
		return this.contents;
	}

	@Override
	public String toString() {
		return "VariableState{" +
				"idx=" + idx +
				", variable=" + variable +
				", contents='" + contents + '\'' +
				", touched=" + touched +
				'}';
	}
}
