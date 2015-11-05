# Introduction #

The J4ME Logging framework helps solve the many problems found in variations between phones.

# Benefits #

  * Critical for debugging phone-specific problems
  * Unobtrusive, both fast and lightweight

# Details #

Debugging on emulators is great.  However, J2ME implementations vary _a lot_.  So when you move your code onto a phone debugging becomes next to impossible.  J4ME Logging helps by letting you see the internal state of your application.

J4ME Logging is similar to Log4J or the Java Standard Edition's `java.util.logging` package.  However it has been designed for a device environment so it is not very configurable and output is limited.

If you require more logging functionality you may want to consider [MicroLog](http://microlog.sourceforge.net/).  For example if you'd like to save your logs to the phone's permanent storage.

| ![http://j4me.googlecode.com/svn/website/img/LogScreen.png](http://j4me.googlecode.com/svn/website/img/LogScreen.png) | ![http://j4me.googlecode.com/svn/website/img/LogConsole.png](http://j4me.googlecode.com/svn/website/img/LogConsole.png) |
|:----------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|
| **Log on your cell phone**                                                                                            | **Log to the Eclipse console**                                                                                          |

## Requirements ##

Runs on any J2ME device.  (I.e. it only uses CLDC 1.0/MIDP 1.0.)