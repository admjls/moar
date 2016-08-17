package com.github.s4ke.moar.regex;

import com.github.s4ke.moar.MoaMatcher;
import com.github.s4ke.moar.NonDeterministicException;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.strings.EfficientString;

import org.junit.Test;
import junit.framework.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Martin Braun
 */
public class MoaWithDSLTest {

	@Test
	public void testStartOfLine() {
		Moa moa = Regex.caret().and( "a" ).toMoa();
		assertTrue( moa.check( "a" ) );

		{
			MoaMatcher matcher = moa.matcher( "aa" );
			assertTrue( matcher.nextMatch() );
			assertFalse( matcher.nextMatch() );
		}
		for ( EfficientString eff : BoundConstants.LINE_BREAK_CHARS ) {
			String tmp = "a" + eff.toString() + "ab";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			Assert.assertEquals( 2, cnt );
		}
	}

	@Test
	public void testEndOfLastMatch() {
		Moa moa = Regex.endOfLastMatch().and( "a" ).toMoa();
		assertTrue( moa.check( "a" ) );

		{
			String tmp = "aa";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			assertEquals( 2, cnt );
		}

		{
			String tmp = "a a";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			assertEquals( 1, cnt );
		}
	}

	@Test
	public void testEndOfInput() {
		Moa moa = Regex.str( "aa" ).end().toMoa();
		assertTrue( moa.check( "aa" ) );

		{
			String tmp = "aaaa";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			org.junit.Assert.assertEquals( 1, cnt );
		}
	}

	@Test
	public void testStartAndEndOfLine() {
		Moa moa = Regex.caret().and( "a" ).dollar().toMoa();
		assertTrue( moa.check( "a" ) );
		for ( EfficientString eff : BoundConstants.LINE_BREAK_CHARS ) {
			String tmp = "a" + eff.toString() + "a";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			Assert.assertEquals( 2, cnt );
		}
	}

	@Test
	public void testStartOfLineSingleLine() {
		Moa moa = Regex.caret().and( "a" ).toMoa();
		assertTrue( moa.check( "a" ) );
	}

	@Test
	public void testStartOfLineDeterminism() {
		assertDet( Regex.caret().and( "a" ) );
		assertNonDet( Regex.caret().or( "a" ).and( "b" ) );
	}

	@Test
	public void testEndOfLine() {
		Moa moa = Regex.str( "a" ).dollar().toMoa();
		assertMatch( true, moa, "a" );
		for ( EfficientString eff : BoundConstants.LINE_BREAK_CHARS ) {
			String tmp = "a" + eff.toString() + "ab";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			assertEquals( 1, cnt );
		}

		for ( EfficientString eff : BoundConstants.LINE_BREAK_CHARS ) {
			String tmp = "a" + eff.toString() + "ba";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			assertEquals( 2, cnt );
		}
	}

	@Test
	public void testEndOfLineDeterminism() {
		assertDet( Regex.str( "a" ).dollar() );
		assertNonDet( Regex.str( "a" ).or( Regex.dollar_() ) );
	}

	@Test
	public void testSimple() {
		Regex regex = Regex.str( "a" );
		assertTrue( regex.toMoa().check( "a" ) );
	}

	@Test
	public void testLongerString() {
		Moa moa = Regex.str( "test" ).toMoa();
		assertTrue( moa.check( "test" ) );
	}

	@Test
	public void testOr() {
		Regex regex = Regex.str( "a" ).or( "b" ).or( "" );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "a" ) );
		assertTrue( moa.check( "b" ) );
		assertTrue( moa.check( "" ) );
		assertFalse( moa.check( "Z" ) );
	}

	@Test
	public void testCompilcatedOr() {
		Regex regex = Regex.str( "a" ).and( Regex.str( "b" ).or( Regex.eps() ) );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "a" ) );
		assertFalse( moa.check( "Z" ) );
		assertTrue( moa.check( "ab" ) );
		assertFalse( moa.check( "abc" ) );
	}

	@Test
	public void testOrEpsilon() {
		Regex regex = Regex.str( "a" ).or( Regex.eps() );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "a" ) );
		assertTrue( moa.check( "" ) );
		assertFalse( moa.check( "b" ) );
	}

	@Test
	public void testBiggerOrEpsilon() {
		Regex regex = Regex.str( "a" ).or( Regex.eps() ).and( Regex.str( "b" ) );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "ab" ) );
		assertTrue( moa.check( "b" ) );
		assertFalse( moa.check( "" ) );
	}

	@Test
	public void testAnd() {
		Moa moa = Regex.str( "a" ).and( "b" ).toMoa();
		assertTrue( moa.check( "ab" ) );
		assertFalse( moa.check( "a" ) );
		assertFalse( moa.check( "" ) );
		assertFalse( moa.check( "ba" ) );
	}

	@Test
	public void testSimpleBinding() {
		Regex regex = Regex.str( "a" ).bind( "toast" )
				.and( "|" )
				.and( Regex.reference( "toast" ) );
		Moa moa = regex.toMoa();
		{
			MoaMatcher matcher = moa.matcher( "a|a" );
			assertTrue( matcher.matches() );
			assertEquals( "a", matcher.getVariableContent( 1 ) );
			assertFalse( moa.check( "a|aa" ) );
		}
		{
			MoaMatcher matcher = moa.matcher( "a|aa" );
			assertFalse( matcher.matches() );
		}
	}

	@Test
	public void testOrInBinding() {
		Regex regex = Regex.str( "a" ).or( Regex.eps() ).bind( "toast" )
				.and( "|" )
				.and( Regex.reference( "toast" ) );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "a|a" ) );
		assertTrue( moa.check( "|" ) );
		assertFalse( moa.check( "|a" ) );
		assertFalse( moa.check( "a|" ) );
	}

	@Test
	public void testOrEpsWithAnd() {
		Regex regex = Regex.str( "a" ).or( Regex.eps() ).and( "b" );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "ab" ) );
		assertTrue( moa.check( "b" ) );
	}

	@Test
	public void testStarInBinding() {
		Regex regex = Regex.str( "a" ).star().bind( "x" )
				.and( "|" )
				.and( Regex.reference( "x" ) );
		Moa moa = regex.toMoa();
		for ( int i = 0; i < 10; ++i ) {
			String str = repeat( "a", i ) + "|" + repeat( "a", i );
			assertTrue( str + " was not accepted by x{a*}|&x", moa.check( str ) );
		}
	}

	@Test
	public void testMatchEmptyStringAsFullText() {
		Regex regex = Regex.eps();
		Moa moa = regex.toMoa();
		int matchCount = 0;
		MoaMatcher matcher = moa.matcher( "" );
		while ( matcher.nextMatch() ) {
			++matchCount;
			if ( matchCount > 100 ) {
				break;
			}
		}
		assertEquals( 1, matchCount );
	}

	@Test
	public void testEndOfInputOnEmptyString() {
		Regex regex = Regex.end_();
		Moa moa = regex.toMoa();
		int matchCount = 0;
		MoaMatcher matcher = moa.matcher( "" );
		while ( matcher.nextMatch() ) {
			++matchCount;
			if ( matchCount > 100 ) {
				break;
			}
		}
		assertEquals( 1, matchCount );
	}

	@Test
	public void testStarInBindingMoreComplex() {
		Regex regex = Regex.str( "a" ).or( "b" ).star().bind( "x" )
				.and( "|" )
				.and( Regex.reference( "x" ) );
		Moa moa = regex.toMoa();
		for ( int i = 0; i < 10; ++i ) {
			String str = repeat( "ab", i ) + "|" + repeat( "ab", i );
			assertTrue( str + " was not accepted by x{(a or b)*}|&x*", moa.check( str ) );
		}
	}

	@Test
	public void testPlusAroundBinding() {
		Regex regex = Regex.str( "a" ).plus().bind( "x" )
				.and( "b" )
				.plus();
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "aab" ) );
		assertTrue( moa.check( "aabab" ) );
	}

	@Test
	public void testWhiteSpace() {
		Regex regex = Regex.whiteSpace();
		Moa moa = regex.toMoa();
		assertTrue( moa.check( " " ) );
		assertFalse( moa.check( "" ) );
	}

	@Test
	public void plusInBinding() {
		Moa moa = Regex.str( "a" ).plus().bind( "toast" ).and( "b" ).and( Regex.reference( "toast" ) ).toMoa();
		assertTrue( moa.check( "aaabaaa" ) );
		assertFalse( moa.check( "aaaba" ) );
		assertFalse( moa.check( "aaab" ) );
	}

	@Test
	public void testSimplePlus() {
		Moa moa = Regex.str( "a" ).plus().toMoa();
		assertFalse( moa.check( "" ) );
		assertTrue( moa.check( "a" ) );
		assertTrue( moa.check( "aa" ) );
	}

	@Test
	public void testCool() {
		Moa moa = Regex.reference( "x" )
				.bind( "y" )
				.and( Regex.reference( "y" ).and( "a" ).bind( "x" ) )
				.plus()
				.toMoa();
		assertTrue( moa.check( "aaaa" ) );
		boolean tmp = false;
		for ( int i = 0; i < 100; ++i ) {
			String str = repeat( "a", i );
			boolean res = moa.check( str );
			if ( res ) {
				tmp = true;
				System.out.println( str );
			}
		}
		assertTrue( tmp );
	}

	private static String repeat(String str, int times) {
		String ret = "";
		for ( int i = 0; i < times; ++i ) {
			ret += str;
		}
		return ret;
	}

	@Test
	public void testPlusWithBind() {
		Regex regex = Regex.str( "abc" ).bind( "toast" )
				.and( "|" )
				.and( Regex.reference( "toast" ) ).plus();
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "abc|abc" ) );
		assertFalse( moa.check( "abc|abcabc" ) );
		//ensure that the internal regex is reused
		assertTrue( moa.check( "abc|abcabc|abc" ) );
		//ensure that the bind from before is used
		assertFalse( moa.check( "abc|abca|a" ) );
	}

	@Test
	public void testSet() {
		Regex regex = Regex.set( 'a', 'c' );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "a" ) );
		assertTrue( moa.check( "b" ) );
		assertTrue( moa.check( "c" ) );
		assertFalse( moa.check( "d" ) );
		assertFalse( moa.check( "" ) );
	}


	@Test
	public void testNonExistingReference() {
		Regex regex = Regex.reference( "x" );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "" ) );
	}

	@Test
	public void testNotDeterministic() {
		assertNonDet( Regex.str( "a" ).bind( "x" ).or( "a" ) );
		assertNonDet( Regex.str( "a" ).or( "b" ).plus().bind( "x" ).and( Regex.reference( "x" ) ) );
		assertDet( Regex.str( "a" ).or( "b" ).plus().bind( "x" ).and( "c" ).and( Regex.reference( "x" ) ) );
		assertDet( Regex.str( "a" ).plus().or( Regex.eps() ) );
		assertNonDet( Regex.str( "a" ).star().or( Regex.eps() ) );
		assertDet( Regex.str( "a" ).bind( "x" ).plus() );
		assertNonDet( Regex.str( "a" ).plus().bind( "x" ).plus() );
		assertDet( Regex.str( "a" ).plus().bind( "x" ).and( "b" ).plus() );
		assertNonDet(
				Regex.str( "a" )
						.plus()
						.bind( "x" )
						.and( Regex.str( "b" ).plus().bind( "y" ) )
						.or( Regex.str( "b" ).plus().bind( "y" ).and( Regex.str( "a" ).plus().bind( "x" ) ) )
						.and( Regex.reference( "x" ).and( Regex.reference( "y" ) ) )
		);
		assertDet(
				Regex.str( "a" )
						.plus()
						.bind( "x" )
						.and( Regex.str( "b" ).plus().bind( "y" ) )
						.or( Regex.str( "b" ).plus().bind( "y" ).and( Regex.str( "a" ).plus().bind( "x" ) ) )
						.and( "c" ).and( Regex.reference( "x" ).and( Regex.reference( "y" ) ) )
		);
		assertNonDet(
				Regex.str( "a" )
						.plus()
						.bind( "x" )
						.and( Regex.str( "b" ).plus().bind( "y" ) )
						.or( Regex.str( "b" ).plus().bind( "y" ).and( Regex.str( "a" ).plus().bind( "x" ) ) )
						.and( Regex.reference( "x" ).or( Regex.reference( "y" ) ) )
		);
		assertDet(
				Regex.reference( "x" )
						.bind( "y" )
						.and( Regex.reference( "y" ).and( "a" ).bind( "x" ) )
						.plus()

		);
	}

	@Test
	public void testDeterminismWithRange() {
		assertNonDet( Regex.set( "a", "c" ).or( "a" ) );
		assertNonDet( Regex.set( "a", "c" ).or( Regex.set( "a", "c" ) ) );
	}

	@Test
	public void testEpsilonMoa() {
		Moa moa = Regex.eps().toMoa();
		assertTrue( moa.check( "" ) );
		assertFalse( moa.check( "a" ) );
	}

	private static void assertMatch(boolean shouldMatch, Regex regex, String input) {
		Assert.assertEquals( shouldMatch, regex.toMoa().check( input ) );
	}

	private static void assertMatch(boolean shouldMatch, Moa moa, String input) {
		Assert.assertEquals( shouldMatch, moa.check( input ) );
	}

	void assertNonDet(Regex regex) {
		try {
			regex.toMoa();
			fail( "regex " + regex + " was not recognized as non-deterministic" );
		}
		catch (NonDeterministicException e) {
			System.out.println( "successfully got regex while building the MOA: " + e.getMessage() );
		}
	}

	void assertDet(Regex regex) {
		regex.toMoa();
	}

}
