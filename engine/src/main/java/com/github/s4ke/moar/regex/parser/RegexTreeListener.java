package com.github.s4ke.moar.regex.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.github.s4ke.moar.regex.Regex;

/**
 * @author Martin Braun
 */
public class RegexTreeListener extends RegexBaseListener implements RegexListener {

	private final Stack<Regex> regexStack = new Stack<>();
	private int groupCount = 0;

	public Regex finalRegex() {
		return this.regexStack.peek();
	}

	private static String getCh(RegexParser.CharOrEscapedContext charOrEscaped) {
		if ( charOrEscaped.character() != null ) {
			return charOrEscaped.character().getText();
		}
		else if ( charOrEscaped.escapeSeq() != null ) {
			return charOrEscaped.escapeSeq().escapee().getText();
		}
		else {
			throw new AssertionError();
		}
	}

	@Override
	public void exitRegex(RegexParser.RegexContext ctx) {
		if ( this.regexStack.size() == 0 ) {
			this.regexStack.push( Regex.eps() );
		}
		if ( ctx.startBoundary() != null ) {
			if ( ctx.startBoundary().START() != null ) {
				Regex regex = this.regexStack.pop();
				regex = Regex.caret().and( regex );
				this.regexStack.push( regex );
			}
			if ( ctx.startBoundary().prevMatch() != null ) {
				Regex regex = this.regexStack.pop();
				regex = Regex.endOfLastMatch().and( regex );
				this.regexStack.push( regex );
			}
		}
		if ( ctx.endBoundary() != null ) {
			if ( ctx.endBoundary().EOS() != null ) {
				Regex regex = this.regexStack.pop();
				regex = regex.dollar();
				this.regexStack.push( regex );
			}
			if ( ctx.endBoundary().endOfInput() != null ) {
				Regex regex = this.regexStack.pop();
				regex = regex.end();
				this.regexStack.push( regex );
			}
		}
		if ( this.regexStack.size() != 1 ) {
			throw new AssertionError();
		}
	}

	@Override
	public void exitBackRef(RegexParser.BackRefContext ctx) {
		Regex regex;
		if ( ctx.groupName() != null ) {
			regex = Regex.reference( ctx.groupName().getText() );
		}
		else {
			regex = Regex.reference( ctx.number().getText() );
		}
		this.regexStack.push( regex );
	}

	@Override
	public void exitUnion(RegexParser.UnionContext ctx) {
		boolean additionalPop = false;
		if ( ctx.union() != null ) {
			additionalPop = true;
		}
		Regex regex = this.regexStack.pop();
		if ( additionalPop ) {
			regex = this.regexStack.pop().or( regex );
		}
		this.regexStack.push( regex );
	}

	@Override
	public void exitConcatenation(RegexParser.ConcatenationContext ctx) {
		boolean additionalPop = false;
		if ( ctx.concatenation() != null ) {
			additionalPop = true;
		}
		Regex regex = this.regexStack.pop();
		if ( additionalPop ) {
			regex = this.regexStack.pop().and( regex );
		}
		this.regexStack.push( regex );
	}

	@Override
	public void exitStar(RegexParser.StarContext ctx) {
		Regex regex = this.regexStack.pop();
		regex = regex.star();
		this.regexStack.push( regex );
	}

	@Override
	public void exitPlus(RegexParser.PlusContext ctx) {
		Regex regex = this.regexStack.pop();
		regex = regex.plus();
		this.regexStack.push( regex );
	}

	@Override
	public void exitOrEpsilon(RegexParser.OrEpsilonContext ctx) {
		Regex regex = this.regexStack.pop();
		regex = regex.or( Regex.eps() );
		this.regexStack.push( regex );
	}

	@Override
	public void exitElementaryRegex(RegexParser.ElementaryRegexContext ctx) {
		if ( ctx.ANY() != null ) {
			Regex regex = Regex.any_();
			this.regexStack.push( regex );
		}
		else if ( ctx.charOrEscaped() != null ) {
			Regex regex = Regex.str( getCh( ctx.charOrEscaped() ) );
			this.regexStack.push( regex );
		}
	}

	@Override
	public void exitCapturingGroup(RegexParser.CapturingGroupContext ctx) {
		++this.groupCount;
		String regexName;
		if ( ctx.groupName() == null ) {
			regexName = String.valueOf( this.groupCount );
		}
		else {
			regexName = ctx.groupName().getText();
		}
		Regex regex;
		if ( ctx.union() == null ) {
			regex = Regex.eps();
		}
		else {
			regex = this.regexStack.pop();
		}
		this.regexStack.push( regex.bind( regexName ) );
	}

	@Override
	public void exitNonCapturingGroup(RegexParser.NonCapturingGroupContext ctx) {
		//no-op
	}

	@Override
	public void exitPositiveSet(RegexParser.PositiveSetContext ctx) {
		RegexParser.SetItemsContext setItems = ctx.setItems();
		Regex regex;
		{
			RegexParser.SetItemContext setItem = setItems.setItem();
			if ( setItem.charOrEscaped() != null ) {
				regex = Regex.str( getCh( setItem.charOrEscaped() ) );
			}
			else if ( setItem.range() != null ) {
				char from = getCh( setItem.range().charOrEscaped( 0 ) ).charAt( 0 );
				char to = getCh( setItem.range().charOrEscaped( 1 ) ).charAt( 0 );

				//the explicit set function is really only needed here, and
				//not above as single tokens in a group can be handled with a simple
				//or without any real memory usage difference
				regex = Regex.set( from, to );
			}
			else {
				throw new AssertionError();
			}

			setItems = setItems.setItems();
		}

		while ( setItems != null ) {
			RegexParser.SetItemContext setItem = setItems.setItem();
			if ( setItem.charOrEscaped() != null ) {
				regex = regex.or( Regex.str( getCh( setItem.charOrEscaped() ) ) );
			}
			else if ( setItem.range() != null ) {
				char from = getCh( setItem.range().charOrEscaped( 0 ) ).charAt( 0 );
				char to = getCh( setItem.range().charOrEscaped( 1 ) ).charAt( 0 );

				//the explicit set function is really only needed here, and
				//not above as single tokens in a group can be handled with a simple
				//or without any real memory usage difference
				Regex rangeRegex = Regex.set( from, to );
				regex = regex.or( rangeRegex );
			}

			//go on with the next one
			setItems = setItems.setItems();
		}

		this.regexStack.push( regex );
	}

	@Override
	public void exitNegativeSet(RegexParser.NegativeSetContext ctx) {
		List<Regex.Range> excluded = new ArrayList<>();
		{
			RegexParser.SetItemsContext setItems = ctx.setItems();
			while ( setItems != null ) {
				RegexParser.SetItemContext setItem = setItems.setItem();
				if ( setItem.charOrEscaped() != null ) {
					char ch = getCh( setItems.setItem().charOrEscaped() ).charAt( 0 );
					excluded.add( Regex.Range.of( ch, ch ) );
				}
				else if ( setItem.range() != null ) {
					char from = getCh( setItem.range().charOrEscaped( 0 ) ).charAt( 0 );
					char to = getCh( setItem.range().charOrEscaped( 1 ) ).charAt( 0 );

					excluded.add( Regex.Range.of( from, to ) );
				}
				setItems = setItems.setItems();
			}
		}

		Regex.Range[] ranges = new Regex.Range[excluded.size()];
		this.regexStack.push( Regex.negativeSet( excluded.toArray( ranges ) ) );
	}

	@Override
	public void exitWhiteSpace(RegexParser.WhiteSpaceContext ctx) {
		Regex regex = Regex.whiteSpace();
		this.regexStack.push( regex );
	}

	@Override
	public void exitNonWhiteSpace(RegexParser.NonWhiteSpaceContext ctx) {
		Regex regex = Regex.nonWhiteSpace();
		this.regexStack.push( regex );
	}

	@Override
	public void exitDigit(RegexParser.DigitContext ctx) {
		Regex regex = Regex.digit();
		this.regexStack.push( regex );
	}

	@Override
	public void exitNonDigit(RegexParser.NonDigitContext ctx) {
		Regex regex = Regex.nonDigit();
		this.regexStack.push( regex );
	}

	@Override
	public void exitNonWordCharacter(RegexParser.NonWordCharacterContext ctx) {
		Regex regex = Regex.nonWordCharacter();
		this.regexStack.push( regex );
	}

	@Override
	public void exitWordCharacter(RegexParser.WordCharacterContext ctx) {
		Regex regex = Regex.wordCharacter();
		this.regexStack.push( regex );
	}
}
