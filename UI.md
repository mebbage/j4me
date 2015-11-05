# Introduction #

The J4ME UI is a framework for building professional looking mobile applications.  It is used in place of the LCDUI (J2ME's UI framework).

# Benefits #

  * Professional looking
  * Quickly change the look of an application using a new `Theme`
  * Consistent menu button operation across phones
  * Object-oriented development

# Details #

One of the major problems with J2ME application development is that the LCDUI does not dictate how phones should look or behave.  This is a mistaken belief carried over from the  desktop world where applications want to look like a native Windows or Mac application.  The result has been ugly UIs that are unintuitive.

For example take the standard LCDUI check box.  To change the value on a Nokia phone you must highlight it, select it to bring up another screen with a Yes/No option, scroll to your option, and hit the enter button.  This is in comparison to the J4ME UI which shows a check box graphic and hitting any button toggles it.

Don't just take our word for it.  Everyone does it.  The most popular J2ME applications out there, Opera Mini and Google Maps, both have written their own libraries that make similar UIs to J4ME.

| ![http://j4me.googlecode.com/svn/website/img/ProgressBar.png](http://j4me.googlecode.com/svn/website/img/ProgressBar.png) | ![http://j4me.googlecode.com/svn/website/img/TextBox.png](http://j4me.googlecode.com/svn/website/img/TextBox.png) |
|:--------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------|
| **Progress bar components**                                                                                               | **Common text input fields**                                                                                      |

| ![http://j4me.googlecode.com/svn/website/img/ScoreOutCaddieScreenshot.gif](http://j4me.googlecode.com/svn/website/img/ScoreOutCaddieScreenshot.gif) | ![http://j4me.googlecode.com/svn/website/img/SplashScreen.gif](http://j4me.googlecode.com/svn/website/img/SplashScreen.gif) | ![http://j4me.googlecode.com/svn/website/img/EtchASketch.png](http://j4me.googlecode.com/svn/website/img/EtchASketch.png) |
|:----------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------|
| **Complex free-form screens**                                                                                                                       | **Splash screens**                                                                                                          | **Theme your applications**                                                                                               |

## Requirements ##

Requires CLDC 1.1/MIDP 2.0.  It uses the J4ME logging package to help diagnose problems.