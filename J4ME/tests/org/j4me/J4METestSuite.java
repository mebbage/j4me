package org.j4me;

import org.j4me.bluetoothgps.*;
import org.j4me.collections.*;
import org.j4me.logging.*;
import org.j4me.ui.components.*;
import org.j4me.util.*;
import j2meunit.framework.*;

/**
 * Runs all of the J4ME tests.
 */
public class J4METestSuite
	extends TestCase
{
	public Test suite ()
	{
		TestSuite suite = new TestSuite("J4ME tests");

		// Add all the util tests.
		suite.addTest(new MathFuncTest().suite());
		
		// Add all the collections tests.
		suite.addTest(new CacheTest().suite());
		suite.addTest(new TreeNodeTest().suite());
		suite.addTest(new CubbyHoleTest().suite());
		
		// Add all the logging tests.
		suite.addTest(new LogTests().suite());
		
		// Add all the Bluetooth GPS tests.
		suite.addTest(new QualifiedCoordinatesTest().suite());
		
		suite.addTest(new LocationImplTest().suite());
		suite.addTest(new NMEAParserTest().suite());
		suite.addTest(new BluetoothGPSTest().suite());
		suite.addTest(new BluetoothLocationProviderTest().suite());
		
		// Add all the UI tests.
		suite.addTest(new LabelTest().suite());

		return suite;
	}
}
