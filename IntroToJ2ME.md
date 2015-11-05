This page briefly introduces J2ME to those of you who have never used it and/or know little about mobile programming.  It also compares it to the other available technologies.

## What is J2ME? ##

J2ME stands for "Java 2 Mobile Edition".  It is Java's specification for devices with low memory and other constraints.  In practice this means it is used for programming cell phones.

If you know Java (i.e. the Standard Edition) you know about 90% of J2ME.  Some of the differences are:
  * _Language Features_ - J2ME uses Java 1.3's syntax so it does not support boxing, attributes, or other new language features.
  * _Library_ - It uses a subset of the Java Foundation Classes.  However, there are some new classes which are all found in the `javax.microedition.*` packages.
  * _Packaging_ - Applications are assembled into a single Jar file (libraries like J4ME must be included in the Jar).  Jar files are accompanied by a small .jad file which contains installation properties for the .jar.

To learn more about J2ME follow our setup guide and build your hello world application.  Then look over the examples included with the J4ME distribution to see more complex examples.

## Mobile Technology Ecosystem ##

When do I use J2ME?  What is the best technology for me?  These are good questions because J2ME is not the best solution for all types of applications.

Like with desktop applications your first decision is whether a web or desktop application is best.  "Web 2.0" features have made traditional web applications very sophisticated, however, the same can not be said about the Mobile Web.  Still this is the way to go for data driven applications because they are much easier to develop and work on all phones.  If you need fancy UIs, to perform computations, or access to peripherals like Bluetooth, than your choice is J2ME or one its peers.

Web browsing on your phone is typically done through [WAP (Wireless Application Protocol)](http://en.wikipedia.org/wiki/Wireless_Application_Protocol).  WAP is similar to HTML but is very basic.  Over time phones will adopt regular HTML in favor of WAP.  The iPhone already does this and Google's Android will continue this trend.

Mobile applications must be written in the environment supported by the phone.  [J2ME](http://en.wikipedia.org/wiki/J2me) is the most widely supported platform, but it is not on every phone.  Where you don't find J2ME you'll find [BREW](http://en.wikipedia.org/wiki/Binary_Runtime_Environment_for_Wireless).  There are some other options such as [Symbian](http://en.wikipedia.org/wiki/Symbian) and [Windows Mobile](http://en.wikipedia.org/wiki/Windows_mobile).

BREW is found on about 1/3 of phones in the U.S.; notably it is Verizon's platform of choice.  For all intents and purposes BREW is a C++ solution.  It has some advantages over J2ME but most consider it to be a closed platform.  As such the vast majority of phone applications are written in J2ME and some are occasionally ported to BREW.

Symbian is a phone operating system found on Nokias and other phones.  Unlike BREW, however, it is not mutually exclusive with J2ME.  For example AT&T's Nokia phones also run J2ME.  Symbian applications are written in C++ and give more access to the phone than J2ME does.  However, they are harder to write and run in fewer places.

Windows Mobile is a stripped down version of Windows that Microsoft releases for phones.  As you might have guessed programming it can be done using stripped down versions of the languages used on Windows.  For example C++ with MFC or ATL, .NET, and even J2ME.

Further complicating things are smartphones.  No matter the carrier they run the platform that is best for them.  For example BlackBerry phones use J2ME (and proprietary Java classes).  While the iPhone does not officially support any language.

Hopefully this chart simplifies things.  An "X" means it is fully supported and a "/" means it is supported on some phones.  The columns for phones mean that phone supports a technology no matter the carrier.

|                  | | **AT&T** | **Verizon** | **Sprint** | **T-Mobile** | | **BlackBerry** | **Windows Mobile** | **iPhone** |
|:-----------------|:|:---------|:------------|:-----------|:-------------|:|:---------------|:-------------------|:-----------|
| _WAP_            | | X        | X           | X          | X            | | X              | X                  |            |
| _J2ME_           | | X        |             | X          | X            | | X              | X                  |            |
| _BREW_           | |          | X           |            |              | |                |                    |            |
| _Symbian_        | | /        | /           | /          | /            | |                |                    |            |
| _Windows Mobile_ | | /        | /           | /          | /            | |                | X                  |            |