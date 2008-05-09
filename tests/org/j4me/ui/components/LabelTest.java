package org.j4me.ui.components;

import javax.microedition.lcdui.*;
import org.j4me.ui.*;
import j2meunit.framework.*;

/**
 * Tests the <code>Label</code> UI component.
 * 
 * @see org.j4me.ui.components.Label
 */
public class LabelTest
	extends TestCase
{
	public LabelTest ()
	{
		super();
	}
	
	public LabelTest (String name, TestMethod method)
	{
		super( name, method );
	}
	
	public Test suite ()
	{
		TestSuite suite = new TestSuite();
		
		suite.addTest(new LabelTest("testBreakIntoLines", new TestMethod() 
				{ public void run(TestCase tc) {((LabelTest) tc).testBreakIntoLines(); } }));
		suite.addTest(new LabelTest("testGetDimensions", new TestMethod() 
				{ public void run(TestCase tc) {((LabelTest) tc).testGetDimensions(); } }));
		
		return suite;
	}

	/**
	 * Tests the <code>breakIntoLines</code> method.  It is responsible for
	 * splitting a string apart into substrings that can be written
	 * as a line on the string.
	 * 
	 * @see Label#breakIntoLines(Font, String, int)
	 */
	public void testBreakIntoLines ()
	{
		Font font = Font.getFont( Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_MEDIUM );
		String[] lines;
		
		// Test that null returns null.
		lines = Label.breakIntoLines( font, null, 1000 );
		assertNull("Parsing null should return null", lines);
		
		// Test a string that fits on one line.
		String singleLine = "test";
		lines = Label.breakIntoLines( font, singleLine, 1000 );
		assertEquals("Length of single line", 1, lines.length);
		assertEquals("Contents of single line", singleLine, lines[0]);

		// Test a string that spans multiple lines and is broken by space.
		String multiLine = "The rain in Spain falls mainly on the plain.";
		lines = Label.breakIntoLines( font, multiLine, 100 );
		assertTrue("Length of multi-line", lines.length > 1);
		
		for ( int i = 0; i < lines.length; i++ )
		{
			assertTrue("Contents of multi-line contain:  " + lines[i], multiLine.indexOf(lines[i]) >= 0);
		}

		// Test a string that spans multiple lines and is broken mid-word.
		String multiLineOneWord = "abcdefghijklmnopqrstuvwxyz0123456789";
		lines = Label.breakIntoLines( font, multiLineOneWord, 100 );
		assertTrue("Length of multi-line one word", lines.length > 1);
		
		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < lines.length; i++ )  sb.append( lines[i] );
		assertEquals("Contents of multi-line one word", multiLineOneWord, sb.toString());
		
		// Test newlines.
		String multiParagraph = "This is the first paragraph.\nParagraph number 2. Second sentence.";
		lines = Label.breakIntoLines( font, multiParagraph, 100 );

		int nullsSeen = 0;

		for ( int i = 0; i < lines.length; i++ )
		{
			if ( lines[i] == null )
			{
				nullsSeen++;
			}
			else
			{
				assertTrue("Contents of multi-paragraph contain:  " + lines[i], multiParagraph.indexOf(lines[i]) >= 0);
			}
		}
		
		assertEquals("Paragraph separators seen", 1, nullsSeen);
	}
	
	/**
	 * Tests the <code>getDimensions</code> method.
	 * 
	 * @see Label#getPreferredSize(Theme, int, int)
	 */
	public void testGetDimensions ()
	{
		Theme theme = new Theme();
		int screenWidth = 100;
		int screenHeight = 100;
		
		// Dimensions of empty label are zero.
		Label label = new Label();
		int[] dimensions = label.getPreferredSize( theme, screenWidth, screenHeight );
		
		assertEquals("Empty label width", 0, dimensions[0]);
		assertEquals("Empty label height", 0, dimensions[1]);
		
		// Dimensions of a simple string are less than screen.
		label = new Label();
		label.setLabel("Simple");
		dimensions = label.getPreferredSize( theme, screenWidth, screenHeight );
		int simpleWidth = dimensions[0];
		int simpleHeight = dimensions[1];
		
		assertTrue("Simple label width less than screen", simpleWidth < screenWidth);
		assertTrue("Simple label height less than screen", simpleHeight < screenHeight);
		
		// Dimensions of multi-paragraph label bigger than simple.
		label = new Label();
		label.setLabel("This is a multi-paragraph string.\nFirst sentence of the second paragraph.");
		dimensions = label.getPreferredSize( theme, screenWidth, screenHeight );
		int multiParagraphWidth = dimensions[0];
		int multiParagraphHeight = dimensions[1];
		
		assertTrue("Multi-paragraph label width less than screen", multiParagraphWidth <= screenWidth);
		assertTrue("Multi-paragraph label width greater than simple", multiParagraphWidth > simpleWidth);

		assertTrue("Multi-paragraph label height greater than simple", multiParagraphHeight > simpleHeight);
	}
}
