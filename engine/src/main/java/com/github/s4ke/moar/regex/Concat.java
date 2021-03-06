/*
 The MIT License (MIT)

 Copyright (c) 2016 Martin Braun

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package com.github.s4ke.moar.regex;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;

/**
 * @author Martin Braun
 */
final class Concat implements Regex {

	private final Regex fst;
	private final Regex snd;

	Concat(Regex fst, Regex snd) {
		this.fst = fst;
		this.snd = snd;
	}

	@Override
	public String toString() {
		return this.fst.toString() + this.snd.toString();
	}

	@Override
	public Regex copy() {
		return new Concat( this.fst.copy(), this.snd.copy() );
	}


	@Override
	public void contributeStates(
			Map<String, Variable> variables,
			Set<State> states,
			Map<Regex, Map<String, State>> selfRelevant,
			Supplier<Integer> idxSupplier) {
		this.fst.contributeStates( variables, states, selfRelevant, idxSupplier );
		this.snd.contributeStates( variables, states, selfRelevant, idxSupplier );
	}

	@Override
	public void contributeEdges(
			EdgeGraph edgeGraph,
			Map<String, Variable> variables,
			Set<State> states,
			Map<Regex, Map<String, State>> selfRelevant) {
		EdgeGraph eg1 = new EdgeGraph();
		for ( State state : states ) {
			eg1.addState( state );
		}
		this.fst.contributeEdges( eg1, variables, states, selfRelevant );

		EdgeGraph eg2 = new EdgeGraph();
		for ( State state : states ) {
			eg2.addState( state );
		}
		this.snd.contributeEdges( eg2, variables, states, selfRelevant );

		Map<State, Set<EdgeGraph.Edge>> snkEdges = new HashMap<>();
		Set<EdgeGraph.Edge> srcEdges = new HashSet<>();

		for ( State state : states ) {
			Collection<EdgeGraph.Edge> edges = eg1.getEdges( state );
			for ( EdgeGraph.Edge edge : edges ) {
				if ( !edge.destination.equals( Moa.SNK.getIdx() ) ) {
					edgeGraph.addEdgeWithDeterminismCheck( state, edge, this );
				}
				else {
					snkEdges.computeIfAbsent( state, (key) -> new HashSet<>() ).add( edge );
				}
			}
		}

		for ( State state : states ) {
			Collection<EdgeGraph.Edge> edges = eg2.getEdges( state );
			for ( EdgeGraph.Edge edge : edges ) {
				if ( state.getIdx() != (Moa.SRC.getIdx()) ) {
					edgeGraph.addEdgeWithDeterminismCheck( state, edge, this );
				}
				else {
					srcEdges.add( edge );
				}
			}
		}

		for ( Map.Entry<State, Set<EdgeGraph.Edge>> snkEdgeEntry : snkEdges.entrySet() ) {
			State toSnkState = snkEdgeEntry.getKey();
			for ( EdgeGraph.Edge snkEdge : snkEdgeEntry.getValue() ) {
				for ( EdgeGraph.Edge srcEdge : srcEdges ) {
					Integer fromSrcState = srcEdge.destination;
					edgeGraph.addEdgeWithDeterminismCheck(
							toSnkState, new EdgeGraph.Edge(
									Moa.f(
											snkEdge.memoryAction,
											srcEdge.memoryAction
									), fromSrcState
							),
							this
					);
				}
			}
		}
	}

	@Override
	public void calculateVariableOccurences(Map<String, Variable> variables, Supplier<Integer> varIdxSupplier) {
		this.fst.calculateVariableOccurences( variables, varIdxSupplier );
		this.snd.calculateVariableOccurences( variables, varIdxSupplier );
	}
}
