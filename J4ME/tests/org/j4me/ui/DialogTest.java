package org.j4me.ui;

import org.j4me.*;
import org.j4me.ui.components.*;
import j2meunit.framework.*;

/**
 * Tests the <code>Dialog</code> UI screen.
 * 
 * @see org.j4me.ui.Dialog
 */
public class DialogTest
	extends J4METestCase
{
	public DialogTest ()
	{
		super();
	}
	
	public DialogTest (String name, TestMethod method)
	{
		super( name, method );
	}
	
	public Test suite ()
	{
		TestSuite suite = new TestSuite();
		
		suite.addTest(new DialogTest("testComponentTallerThanScreen", new TestMethod() 
				{ public void run(TestCase tc) {((DialogTest) tc).testComponentTallerThanScreen(); } }));
		
		return suite;
	}

	/**
	 * Tests that components which are taller than the screen do not
	 * cause problems.  <a href="http://code.google.com/p/j4me/issues/detail?id=27">
	 * Issue 27</a> reported this problem, but it could not be reproduced.
	 */
	public void testComponentTallerThanScreen ()
	{
		// Create a dialog with a component taller than the screen height.
		Dialog d = new TestDialog();
		int screenHeight = d.getScreenHeight();
		Whitespace whitespace = new Whitespace( screenHeight * 2 );
		d.append( whitespace );
		
		// Simulate the dialog rendering itself.
		boolean scrollbar = d.hasVerticalScrollbar();
		assertTrue("Component bigger than the screen should force a scrollbar", scrollbar);
		
		// Simulate a scroll event (where the bug was).
		d.keyPressed( Dialog.DOWN );
	}
}

/**
 * <code>Dialog</code> is an abstract base class so this is a concrete
 * version used in testing.
 */
class TestDialog
	extends Dialog
{

}
