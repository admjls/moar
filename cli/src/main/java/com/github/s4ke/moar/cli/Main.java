package com.github.s4ke.moar.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.github.s4ke.moar.MoaMatcher;
import com.github.s4ke.moar.MoaPattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author Martin Braun
 */
public class Main {

	public static void main(String[] args) throws ParseException, IOException {
		// create Options object
		Options options = new Options();

		options.addOption( "rf", true, "regexFile" );
		options.addOption( "r", true, "regex" );

		options.addOption( "sf", true, "stringFile" );
		options.addOption( "s", true, "string" );

		options.addOption( "m", false, "multiline" );

		options.addOption( "help", false, "prints this dialog" );

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse( options, args );

		if ( args.length == 0 || cmd.hasOption( "help" ) ) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "moar-cli", options );
		}

		List<MoaPattern> patterns = new ArrayList<>();
		List<String> stringsToCheck = new ArrayList<>();

		if ( cmd.hasOption( "r" ) ) {
			String regexStr = cmd.getOptionValue( "r" );
			patterns.add( MoaPattern.compile( regexStr ) );
		}

		if ( cmd.hasOption( "rf" ) ) {
			String fileName = cmd.getOptionValue( "rf" );
			List<String> regexFileContents = readFileContents( new File( fileName ) );
			int emptyLineCountAfterRegex = 0;
			StringBuilder regexStr = new StringBuilder();
			for ( String line : regexFileContents ) {
				if ( emptyLineCountAfterRegex >= 1 ) {
					if ( regexStr.length() > 0 ) {
						patterns.add( MoaPattern.compile( regexStr.toString() ) );
					}
					regexStr.setLength( 0 );
					emptyLineCountAfterRegex = 0;
				}
				if ( line.trim().equals( "" ) ) {
					if ( regexStr.length() > 0 ) {
						++emptyLineCountAfterRegex;
					}
				}
				else {
					regexStr.append( line );
				}
			}
			if ( regexStr.length() > 0 ) {
				patterns.add( MoaPattern.compile( regexStr.toString() ) );
				regexStr.setLength( 0 );
			}
		}

		if ( cmd.hasOption( "s" ) ) {
			String str = cmd.getOptionValue( "s" );
			stringsToCheck.add( str );
		}

		if ( cmd.hasOption( "sf" ) ) {
			String fileName = cmd.getOptionValue( "sf" );
			StringBuilder stringBuilder = new StringBuilder();
			boolean firstLine = true;
			for ( String str : readFileContents( new File( fileName ) ) ) {
				if ( !firstLine ) {
					stringBuilder.append( "\n" );
				}
				if ( firstLine ) {
					firstLine = false;
				}
				stringBuilder.append( str );
			}
			stringsToCheck.add( stringBuilder.toString() );
		}

		boolean multiLine = cmd.hasOption( "m" );

		if ( stringsToCheck.size() == 0 ) {
			System.out.println( "no strings to check" );
			return;
		}
		if ( patterns.size() == 0 ) {
			System.out.println( "no patterns to check" );
			return;
		}

		for ( String string : stringsToCheck ) {
			for ( MoaPattern pattern : patterns ) {
				MoaMatcher matcher = pattern.matcher( string );
				if ( !multiLine ) {
					if ( matcher.matches() ) {
						System.out.println( "\"" + pattern + "\" matches \"" + string + "\"" );
					}
					else {
						System.out.println( "\"" + pattern + "\" does not match \"" + string + "\"" );
					}
				}
				else {
					StringBuffer buffer = new StringBuffer( string );
					int additionalCharsPerMatch = ("<match>" + "</match>").length();
					int matchCount = 0;
					while ( matcher.nextMatch() ) {
						buffer.replace(
								matcher.getStart() + matchCount * additionalCharsPerMatch ,
								matcher.getEnd() + matchCount * additionalCharsPerMatch,
								"<match>" + string.substring(
										matcher.getStart(),
										matcher.getEnd()
								) + "</match>"
						);
						++matchCount;
					}
					System.out.println( buffer.toString() );
				}
			}
		}
	}

	private static List<String> readFileContents(File file) throws IOException {
		List<String> ret = new ArrayList<>();
		try (FileInputStream fis = new FileInputStream( file );
			 BufferedReader reader = new BufferedReader( new InputStreamReader( fis ) )) {
			String str = null;
			while ( (str = reader.readLine()) != null ) {
				ret.add( str );
			}
		}
		return ret;
	}

}
