![Travis badge](https://api.travis-ci.org/Le-Space/gps2ftp.svg?branch=master)

GPS2FTP
-------
An app for Android devices which updates the current gps position to a defined sftp server.
So far only useful in order when you want to update your personal website with your current gps location. 

As an example or first use case we forked the free Bootstrap template of David Miller (https://twitter.com/davidmillerskt)
https://github.com/Le-Space/startbootstrap-grayscale 

**(please use our fork as long its not included in the main project!)**
Examples: see www.le-space.de or www.irinasenko.com (scroll down to the bottom of the page)

![gps2ftp badge](https://github.com/Le-Space/gps2ftp/raw/master/marketing/ic_launcher_round/web_hi_res_512_small.png)

Todo:
- (mobile) add basic authentication to HTTP publish
- (mobile) add bearer tokeen to HTTP publish
- (wear) open settings dialog on mobile when connection to ftp server failed.
    - https://gist.github.com/gabrielemariotti/117b05aad4db251f7534
- (wear/app) maps demo add my location icon / gps 
    - https://github.com/googlemaps/android-maps-utils/tree/master/demo
- (wear) get location when offline
    - https://developer.android.com/training/location/receive-location-updates.html
    - https://developer.android.com/training/articles/wear-location-detection.html
    
- (wear) sync config (ftp, lastPosition, lastAddress) with wear device 
    - https://stackoverflow.com/questions/25196033/android-wear-data-items
- (wear) communicate with phone (get configuration from phone) 
    - https://developer.android.com/training/wearables/data-layer/events.html
- (wear) support wear devices without gps
- (wear) support wear devices without wifi
- (wear) support wear devices without instant to network (get position and wait for network?!)

- (wear) check perissions 
    - https://developer.android.com/training/articles/wear-permissions.html
    - https://developer.android.com/training/permissions/requesting.html#explain
    
- (wear) create notification when upload was (not) successful / open expanded notification on mobile for configuration
   - https://developer.android.com/training/wearables/notifications/creating.html

- (wear) double touch 2 update position - disable double touch 4 zoom
   - (gesture library for android) https://github.com/nisrulz/Sensey
   - https://github.com/codepath/android_guides/wiki/Gestures-and-Touch-Events
- (wear) network accesss (publish via ftp) 
    - data layer - syncing data items https://developer.android.com/training/wearables/data-layer/data-items.html
    - data layer - communication with a phone https://developer.android.com/training/wearables/data-layer/events.html
    - data layer https://developer.android.com/training/wearables/data-layer/network-access.html
    - https://developer.android.com/reference/android/net/ConnectivityManager.html
    - https://developer.android.com/training/wearables/data-layer/network-access.html
    - https://developer.android.com/training/basics/network-ops/managing.html
- (mobile) bug does not update the widget address anymore - send a notification to widget
- evaluate code climate / add badge to github https://codeclimate.com/
- evaluate cdnjs (for a map/webrtc widget?!) https://github.com/cdnjs/cdnjs
- write tests
   - Espresso http://peirr.com/writing-android-tests-with-espresso-test-recorder/
   - Evaluate Coveralls.io / add badge to github https://coveralls.io/
- add help video and help text to app (howto use this app)
- test on cloud devices
    - BrowserStack
    - Genymotion


Bugs:
- first time installation does not show current position 
- first time installation shows only strange error and does not switch to config
- first time installation does not show help


Nice2Haves:
- (wear/mobile) when city changes show notification to update position
- (wear/mobile) when country changes show notification to update position
- integrate snazzympas-browser
    - https://github.com/stephenmcd/snazzymaps-browser
- update twitter location
    - https://dev.twitter.com/rest/reference/post/account/update_profile
- update facebook location
    - https://developers.facebook.com/docs/graph-api/reference/location/
- update github location
- check location in a certain frequence (every x minutes/hours)
- handle REST-URL
- handle multiple websites 
    
Information
- (wear) enable debugging 
        ``adb forward tcp:4444 localabstract:/adb-hub``
        ``adb connect 127.0.0.1:4444``    
    
Done:
27.07.2017
- 2h (mobile) add HTTP-URL for REST-UPDATE
25.07.2017 
- 2h (wear) publish wear app
    - https://developer.android.com/training/wearables/apps/packaging.html
- (0,5) (mobile bug) when widget is not installed app crashes after location update
- (1,5h) (wear) debugging on wear gps (gps doesn't work without wifi/bluetooth/lte) without result
24.07.2017 (3h)
- (app widget bug) - (app) location update is not updated on phone widget
- (app) settings are not used anymore after update
- (wear) (bug) settings now get used again on phone after settings change
- (app settings bug) when saving settings they don't get updated on android phone nor send to wear device
- (app location bug) when location has changed it gets not send to wear device
- (wear location bug) when location has hanged it gets not send to android device
- (wear settings bug) wehn saving settings on app they don't get published to wear device
    - wear device should get settings from mobile app (via bluetooth / google services wifi & mobile network) 


01.07.2017
    - added lastCity, lastZipCode, lastCountry to latlng.json
29.06.2017
- (wear) location detection
    - https://developer.android.com/training/articles/wear-location-detection.html#Disconnection
    - https://stackoverflow.com/questions/40565841/get-device-location-using-android-wear
- (wear) overview of general information 
    - https://developer.android.com/training/building-wearables.html    
    
28.06.2017
- (wear) change app logo 
- (wear) create a shared project ":common" e.g. for FTPUpdateTask
    - https://stackoverflow.com/questions/36117365/sharing-files-between-android-mobile-and-wear-modules
27.06.2017
- (wear) show map
- (wear) install wearable test app on LG Urbane
    - https://medium.com/@jcdelvalle/android-wear-bluetooth-debugging-quickly-guide-ef279b84169c
    - https://developer.android.com/training/wearables/apps/debugging.html
    - https://stackoverflow.com/questions/28533786/unable-to-run-sample-android-wear-app-on-physical-device

26.07.2017
- gps2ftp logo in README.md einfügen
- update widget example image 
- travis-ci.org
    - https://travis-ci.org/
    - building android on command line https://developer.android.com/studio/build/building-cmdline.html
    - working howto for android https://distillery.com/blog/painless-travis-cicd-android-project/
    - wwtd (what would travis do) https://github.com/grosser/wwtd
    - example for android
       - https://docs.travis-ci.com/user/languages/android/
       - https://github.com/pestrada/android-tdd-playground/blob/master/.travis.yml
    - travis client https://github.com/travis-ci/travis.rb
    - run travis locally 
       - https://docs.travis-ci.com/user/common-build-problems/#Troubleshooting-Locally-in-a-Docker-Image
       - (couldn't figure that out)  https://stackoverflow.com/questions/21053657/how-to-run-travis-ci-locally
    - using docker in travis https://blog.travis-ci.com/2015-08-19-using-docker-on-travis-ci/
    - problems
        - check .travis.yml http://lint.travis-ci.org/
        - https://stackoverflow.com/questions/40208309/travis-ci-build-failing-on-android-app-unsupported-major-minor-version-52
        - license https://github.com/travis-ci/travis-ci/issues/6555
25.07.2017
- when touching on position button - ask user before update
- added googleApiKey to settings
- added address and google api key to latlng.json
22.07.2017
- geoCode Address and display it on widget screen
     - https://developer.android.com/training/location/display-address.html
     - https://stackoverflow.com/questions/4510974/using-resultreceiver-in-android
21.07.2017
- transfer current zoom level to latlng.json

20.07.2017
- publish github / choose license 
    https://play.google.com/intl/en_us/badges/

19.07.2017
- publish to playstore
    - create logo https://romannurik.github.io/AndroidAssetStudio/index.html
    - create graphic for playstore 
- display map of lastlocation 
    https://developers.google.com/maps/documentation/android-api/map
    https://developers.google.com/maps/documentation/android-api/views
- added ftp configuration 
- change launch icon, gps icon and app title
    https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html#foreground.type=text&foreground.text.text=GPS2FTP&foreground.text.font=Amatic%20SC&foreground.space.trim=1&foreground.space.pad=0&foreColor=rgba(41%2C%20159%2C%20216%2C%200)&backColor=rgb(255%2C%20152%2C%200)&crop=0&backgroundShape=square&effects=none&name=ic_launcher_round

16.07.2017 
- make widget work
    - https://developer.android.com/guide/topics/appwidgets/index.html
- add a button which reads the gps position and
             - https://developer.android.com/training/location/retrieve-current.html#permissions
             - https://developer.android.com/guide/topics/location/strategies.html
             - handle permissions
 
14.07.2017
- write position into a defined ftp destination
    - ~~Example Implementation FTP https://stackoverflow.com/questions/1567601/android-ftp-library~~
    - ~~Apache Commons FTP http://commons.apache.org/proper/commons-net/~~ 
    - use http://www.jcraft.com/jsch/ its working. :)
- add ftp server on test system https://debian-administration.org/article/228/Setting_up_an_FTP_server_on_Debian

12.07.2017
- modified demo website (e.g. www.le-space.de) to read location from latlng.json instead of compiling it into website
