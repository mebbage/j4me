# Policies #

### Version Numbering ###

J4ME releases follow the `major.minor.fix` version numbering convention.  For example 2.0.4 would mean the second major release, first minor release for this major release, and fourth bug fixing patch.
  * _Major releases_ add new packages of functionality or significant improvements to existing packages
  * _Minor releases_ add new classes or batches of methods to existing packages
  * _Fix releases_ only fix bugs (although they may add methods as part of the bug fixes)

### Deprecation ###

Deprecation of classes and methods can happen on any major or minor release.  Anything that is deprecated will have some alternate method suggested.  Deprecated classes and methods  may only removed on a major release (they will be kept as long as they do not require significant effort to keep up).

# Release History #

  * 1.0.3 (May 9, 2008)
    * Fixed [issue 13](https://code.google.com/p/j4me/issues/detail?id=13):  The size of the HorizontalRule component can be changed
    * Fixed [issue 14](https://code.google.com/p/j4me/issues/detail?id=14):  The border width of the selection rectangle for components is correct even for tall components
    * Fixed [issue 18](https://code.google.com/p/j4me/issues/detail?id=18):  The Label component returns the correct preferred size
    * Fixed [issue 21](https://code.google.com/p/j4me/issues/detail?id=21):  Scroll behavior has been fixed for groups of components that do not accept input and total more than the screen size
    * Fixed [issue 27](https://code.google.com/p/j4me/issues/detail?id=27):  Components taller than the screen no longer cause exceptions
    * Fixed [issue 28](https://code.google.com/p/j4me/issues/detail?id=28):  When components update the scroll position is maintained (it no longer returns to the top)
    * Fixed [issue 29](https://code.google.com/p/j4me/issues/detail?id=29):  Component.show() has been renamed to .visible() to remove the ambiguity of its purpose
    * Fixed [issue 35](https://code.google.com/p/j4me/issues/detail?id=35):  Changing a screen between full screen mode causes a Dialog to be invalidated so components are properly laid out
    * Fixed [issue 38](https://code.google.com/p/j4me/issues/detail?id=38):  JSR-179 implementations no longer cause a null pointer exception when calling getLastKnownLocation before a fix has been acquired
  * 1.0.2 (Jan 16, 2008)
    * Fixed [issue 6](https://code.google.com/p/j4me/issues/detail?id=6):  Obfuscated using ProGuard 4.1 so it runs on Nokia Series 3 phones
    * Fixed [issue 7](https://code.google.com/p/j4me/issues/detail?id=7):  Log demo works on certain Sony Ericsson phones
    * Fixed [issue 8](https://code.google.com/p/j4me/issues/detail?id=8):  Bluetooth GPS works on Motorola phones paired with the Nokia LD-3W Bluetooth GPS device
    * Fixed [issue 9](https://code.google.com/p/j4me/issues/detail?id=9):  Removing UI components from a dialog no longer can cause an `ArrayOutBoundsException`
    * Fixed [issue 10](https://code.google.com/p/j4me/issues/detail?id=10):  Bluetooth GPS reports speed in meters per second instead of knots
    * Added more robust exception handling and logging for UI unexpected exceptions
  * 1.0.1 (Dec 12, 2007)
    * Fixed [issue 1](https://code.google.com/p/j4me/issues/detail?id=1):  Improved screen size handling
    * Fixed [issue 2](https://code.google.com/p/j4me/issues/detail?id=2) and [issue 4](https://code.google.com/p/j4me/issues/detail?id=4):  Support for the Tao JVM on Windows Mobile
    * Fixed problem with `TextBox` component to work when obfuscated by ProGuard 4.0
  * 1.0.0 (Nov 26, 2007)
    * Initial public release