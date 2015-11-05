# j4me
This repo has beeen automatically exported from the Google Code Archive http://code.google.com/p/j4me and will eventually become J4ME's permanent home.

This project isn't under active development - but is inluded for legacy support.

# Introduction

J2ME stands for "Java 2 Mobile Edition". It is Java's specification for devices with low memory and other constraints. In practice this means it is used for programming cell phones.

If you know Java (i.e. the Standard Edition) you know about 90% of J2ME. Some of the differences are:

Language Features - J2ME uses Java 1.3's syntax so it does not support boxing, attributes, or other new language features.
Library - It uses a subset of the Java Foundation Classes. However, there are some new classes which are all found in the javax.microedition.* packages.
Packaging - Applications are assembled into a single Jar file (libraries like J4ME must be included in the Jar). Jar files are accompanied by a small .jad file which contains installation properties for the .jar.
To learn more about J2ME follow our setup guide and build your hello world application. Then look over the examples included with the J4ME distribution to see more complex examples.

# Setup

This is a setup guide that will get your J2ME development environment up and running. At the end you will run the J4ME examples.

It is assumed you already know Java and use Eclipse. If you don't know Java you can still follow the setup, but you'll need to look elsewhere to learn the basics of writing J2ME code. If you don't use Eclipse please post on the discussion board how you setup your environment.

Sun WTK
5 minutes

The Sun Java Wireless Toolkit (WTK) for CLDC emulates a J2ME environment on your computer. There are other emulation environments available, but most are built from the Sun WTK.

Visit the Sun Java Wireless Toolkit page
Click on the latest toolkit
On the new page hit the "Download" button
On the new page accept the license and click on the link for your platform (e.g. Windows)
Once downloaded, run the installer
Note that there has been a bug which requires the installation path to not have spaces. We recommend accepting the default location. Do not put it in Program Files.
Eclipse
If you do not already have Eclipse download and install it. The base installation package for Java development is all you need.

ProGuard
3 minutes

ProGuard is an obfuscater for Java code. It is especially helpful for J2ME because it shrinks the final Jar size significantly and optimizes the code. This means it loads and runs faster on phones.

Part of the obfuscation process is removing unused classes. If you do not use part of the J4ME library, obfuscation will remove that class from your Jar.

Download ProGuard (4.1 or later)
Unzip it and remember where (you'll need this later)
J2MEUnit
3 minutes

J2MEUnit is a port of JUnit to the J2ME environment. J2ME does not support reflection so J2MEUnit is basically the same but with an extra method that specifies your tests.

J4ME uses J2MEUnit for its tests. Any fixes you submit should include a J2MEUnit test case to make sure it will always behave as you expect. Also your own project should use J2MEUnit for testing.

Download J2MEUnit (we recommend downloading j2meunit-all.zip because it includes documentation)
Unzip it and remember where (you'll need this later)
Antenna
(Optional) 3 minutes

If you use Ant, you'll want Antenna. It is a collection of Ant tasks for J2ME. But you will be able to package your J2ME applications from Eclipse without it.

Download Antenna
Unzip it and remember where (you'll need this later)
EclipseME
20 minutes

EclipseME is the plug-in for J2ME development. It makes Eclipse properly package J2ME applications and debug them in the emulators.

Open Eclipse
Help -> Software Updates -> Find and Install...
In the dialog select "Search for new features to install" and hit Next
In the Install dialog hit the "New Remote Site..." button
In the New Update Site dialog enter:
Name as EclipseME
URL as http://www.eclipseme.org/updates/
Highlight EclipseME in the dialog and click Finish.
When it is done you will need to restart Eclipse
Follow the EclipseME installation instructions
During the installation you will need the install locations of ProGuard, J2MEUnit, and Antenna
J4ME
By this point you have a complete J2ME environment. After this section you'll be running J4ME's examples and be ready to use J4ME in your own applications.

Install J4ME
2 minutes

Download J4ME
Create a J4ME directory and unzip the download into it
Add J4ME to the Eclipse Workspace
5 minutes

Open Eclipse (if it isn't already)
File -> Import...
Choose Existing Projects into Workspace as the import source
Hit the Browse button next to Select root directory
Navigate to the J4ME/src directory where you unzipped the J4ME library
Hit the Finish button
You should see "J4ME" added to the Package Explorer window with an error icon
Click on "J4ME" in the Package Explorer window and then go Project -> Properties
Click on "Java Build Path"
On the Libraries tab click the "Add External JARs..." button
Navigate to j2meunit.jar
On the Order and Export tab check j2meunit.jar
Click OK
Run the Examples
5 minutes

In Eclipse go to Run -> Run...
Add a new "Wireless Toolkit Emulator"
In the Name field enter "J4ME - UI Example"
In the Project field hit the Browse button and select "J4ME"
In the Executable group hit the Search button next to the Midlet field
Enter "org.j4me.examples.ui.UIDemoMidlet" and hit OK
Hit the Apply button
Repeat steps 2-7 with:
"J4ME - Log Example" and "org.j4me.examples.log.LogDemoMidlet"
"J4ME - GPS Example" and "org.j4me.examples.bluetoothgps.GPSDemoMidlet"
"J4ME - Unit Tests" and "org.j4me.TestsMidlet"
Highlight "J4ME - UI Example" and hit "Run"
Hello World
At this point you have setup your J2ME environment and looked at the J4ME library. This section lets you build your very first J2ME application.

Creating a J2ME Project
3 minutes

In Eclipse go to File -> New -> Project...
From the New Project dialog select J2ME Midlet Suite and hit the Next button
For the Project Name type "HelloWorld" and hit the Next button
For the Device field make sure it is "DefaultColorPhone" and hit the Next button
On the Projects tab hit the Add button and select "J4ME"
On the Order and Export tab check J4ME
Click the Finish button
This will create a new Project
Notice there is a "HelloWorld" project in Package Explorer with some folders and "HelloWorld.jad"
JAD files are unique to J2ME. They are paired with a .jar file that is your application. JAD files are used during installation of your application to provide metadata about it.
J2ME Coding
5 minutes

Click on the "HelloWorld" project in Project Explorer
File -> New -> Other...
From the dialog select J2ME Midlet and hit the Next button
In the Name field type "HelloWorldMidlet" and hit the Finish button
This will create and open HelloWorldMidlet.java, your application's entry point
All MIDlets extend the abstract class MIDlet
The startApp method is called by the framework when your application launches
The pauseApp method is called by the framework when your application is minimized, such as when an incoming phone call is accepted; technically you are supposed to close any connections and save your application's state, but you can leave this empty
The destroyApp method is called by the framework when your application is closing; close any connections here
File -> New -> Class
In the Name field type "HelloWorldScreen"
In the Superclass field type "org.j4me.ui.Dialog"
Hit the Finish button and it will create and open HelloWorldScreen.java
Copy and paste the following code into HelloWorldScreen.java

    import javax.microedition.lcdui.*;
    import org.j4me.ui.*;
    import org.j4me.ui.components.*;
   
    /**
     * A screen that displays "Hello World".
     */
    public class HelloWorldScreen extends Dialog
    {
        /**
         * Constructor.
         */
        public HelloWorldScreen ()
        {
                // The title across the top of the screen.
                //setTitle( "J4ME" );
                
                // Set the menu text at the bottom of the screen.
                //  "Exit" will appear on the left and when the user presses the phone's
                //  left menu button declineNotify() will be called.  When the user
                //  presses the right menu button acceptNotify() will be called.
                setMenuText( "Exit", null );
                
                // Add a UI component.
                Label lbl = new Label("Hello World!");
                lbl.setHorizontalAlignment( Graphics.HCENTER );
                append( lbl );
        }
        
        /**
         * Called when the user presses the left menu button to "Exit".
         */
        public void declineNotify ()
        {
                // Exit the application.
                HelloWorldMidlet.exit();
                
                // Continue processing the event.
                super.declineNotify();
        }
    }
    
Copy and paste the following code into HelloWorldMidlet.java

    import javax.microedition.midlet.*;
    import org.j4me.ui.*;
  
    /**
     * The entry point for the application.
     */
    public class HelloWorldMidlet extends MIDlet
    {
        /**
         * The one and only instance of this class.
         */
        private static HelloWorldMidlet instance;
        
        /**
         * Constructs the midlet.  This is called before <code>startApp</code>.
         */
        public HelloWorldMidlet ()
        {
                instance = this;
        }

        /**
         * Called when the application is minimized.  For example when their
         * is an incoming call that is accepted or when the phone's hangup key
         * is pressed.
         * 
         * @see javax.microedition.midlet.MIDlet#pauseApp()
         */
        protected void pauseApp ()
        {
        }

        /**
         * Called when the application starts.  Shows the first screen.
         * 
         * @see javax.microedition.midlet.MIDlet#startApp()
         */
        protected void startApp () throws MIDletStateChangeException
        {
                // Initialize the J4ME UI manager.
                UIManager.init( this );
                
                // Change the theme.
                //UIManager.setTheme( new org.j4me.examples.ui.themes.RedTheme() );
                
                // Show the first screen.
                HelloWorldScreen screen = new HelloWorldScreen();
                screen.show();
        }

        /**
         * Called when the application is exiting.
         * 
         * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
         */
        protected void destroyApp (boolean arg0) throws MIDletStateChangeException
        {
                // Add cleanup code here.
                
                // Exit the application.
                notifyDestroyed();
        }
        
        /**
         * Programmatically exits the application.
         */
        public static void exit ()
        {
                try
                {
                        instance.destroyApp( true );
                }
                catch (MIDletStateChangeException e)
                {
                        // Ignore.
                }
        }
    }

In Package Explorer click on HelloWorldMidlet.java
Run -> Run As -> Emulated J2ME Midlet
Congratulations! You have written and run your first J2ME application.
