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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.s4ke.moar.MoaMatcher;
import com.github.s4ke.moar.moa.Moa;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author Martin Braun
 */
public class MatchReplaceTest {

	@Test
	public void testReplaceFirst() {
		//check if the a previous match changes the outcome
		//of replaceFirst
		{
			Pattern p = Pattern.compile( "a" );
			Matcher matcher = p.matcher( "aa" );
			matcher.replaceFirst( "b" );
			String res = matcher.replaceFirst( "b" );
			assertEquals( "ba", res );
		}
		//it does not.

		//now check the same for the GenericMoaMatcher
		{
			Regex regex = Regex.str( "a" );
			Moa moa = regex.toMoa();
			MoaMatcher moaMatcher = moa.matcher( "aa" );
			moaMatcher.replaceFirst( "b" );
			String res = moaMatcher.replaceFirst( "b" );
			assertEquals( "ba", res );
		}
	}

	@Test
	public void testReplaceAll() {
		{
			Moa moa = Regex.str( "aa" ).toMoa();
			MoaMatcher matcher = moa.matcher( "aabaabaabaabaa" );
			assertEquals( "bbbb", matcher.replaceAll( "" ) );
			assertEquals( "ccbccbccbccbcc", matcher.replaceAll( "cc" ) );
		}
	}

}
