# Introduction #

The J4ME Bluetooth GPS framework lets you get location information from a Bluetooth GPS puck through the JSR-179 Location API interface.

# Benefits #

  * Access location data from a Bluetooth GPS device
  * Makes JSR-179 implementations behave consistently

# Details #

The JSR-179 specification defines the Location API.  It is how a J2ME application can get the phone's current location.

The J4ME Bluetooth GPS package wraps and extends JSR-179.  The added functionality lets an application get location data from a separate GPS device through the phone's Bluetooth.  Many, many more phones support Bluetooth than do the Location API so for some phones it is the only option.  On other phones that use cell phone triangulation to get their location Bluetooth GPS offers a free and much more accurate alternative (less than 3 meters vs. 60).

Another advantage of J4ME Bluetooth GPS is that it brings consistency to JSR-179 implementations.  For example newer BlackBerry phones support JSR-179 access to their integrated GPS chips.  However the BlackBerry platform raises location events on the main UI thread which often leads to unresponsive applications that sometimes even crash.  The J4ME Bluetooth GPS implementation moves these events to a background thread without you doing any work.

| ![http://j4me.googlecode.com/svn/website/img/GPSDemoScreen.png](http://j4me.googlecode.com/svn/website/img/GPSDemoScreen.png) |
|:------------------------------------------------------------------------------------------------------------------------------|
| **Location data from the phone or external GPS**                                                                              |

## Requirements ##

Requires CLDC 1.1/MIDP 2.0.  It also requires JSR-82 (the Bluetooth API) to use Bluetooth GPS and/or JSR-179 (the Location API).  So a phone that has only Bluetooth API support and not Location API support can use Bluetooth GPS.

# Recommended Bluetooth GPS #

Bluetooth GPS pucks are all fairly similar.  There are slight variations in chipsets but the main differentiators are price, battery life, and packaging.  We have tried a dozen of these over the past year and our recommendation is the OnCourse Edition 3 which costs $50 and is about the size of matchbox.  You can purchase it through the link below (it comes with a car charger but not a wall charger; if you do not have a mini-USB charger a cheap one that works with it is listed alongside).

| **OnCourse Edition 3** | **Mini-USB Charger** |
|:-----------------------|:---------------------|
| [![](http://rcm-images.amazon.com/images/I/11c0yMqiPML._SL110_.jpg)](http://www.amazon.com/gp/redirect.html?ie=UTF8&location=http%3A%2F%2Fwww.amazon.com%2FOnCourse-Bluetooth-Receiver-enabled-Smpartphone%2Fdp%2FB000U3JNJG%3Fie%3DUTF8%26s%3Dmiscellaneous%26qid%3D1196123884%26sr%3D8-3&tag=scoout-20&linkCode=ur2&camp=1789&creative=9325) | [![](http://rcm-images.amazon.com/images/I/11NBIvyj7jL._SL110_.jpg)](http://www.amazon.com/gp/redirect.html?ie=UTF8&location=http%3A%2F%2Fwww.amazon.com%2Fdp%2FB0009H2M1Y%3Ftag%3Dscoout-20%26camp%3D14573%26creative%3D327641%26linkCode%3Das1%26creativeASIN%3DB0009H2M1Y%26adid%3D1JGCVWJ99PMXDKYQJFD9%26&tag=scoout-20&linkCode=ur2&camp=1789&creative=9325) |