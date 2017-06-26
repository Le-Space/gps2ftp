GPS2FTP
-------
An app for Android devices which updates the current gps position to a defined ftp server.
Useful in order to update your personal website with your current location. E.g. 
we modified the free Bootrap template of David Miller (https://twitter.com/davidmillerskt)
https://github.com/Le-Space/startbootstrap-grayscale in order to do this! 

**(please use our fork as long its not included in the main project!)**

e.g. see www.le-space.de or www.irinasenko.com

Todo:
- travis-ci.org
    - https://travis-ci.org/
    - https://docs.travis-ci.com/user/languages/android/
    - problems
        - check .travis.yml http://lint.travis-ci.org/
        - https://stackoverflow.com/questions/40208309/travis-ci-build-failing-on-android-app-unsupported-major-minor-version-52
        - license https://github.com/travis-ci/travis-ci/issues/6555
- github
    - gps2ftp logo in README.md einfügen
- add help video and help text to app (howto use this app)
- write tests
- test on cloud devices / genymotion 

Bugs:
- first time installation does not show current position 
- first time installation shows only strange error and does not switch to config
- first time installation does not show help


Nice2Haves:
- integrate snazzympas-browser
    https://github.com/stephenmcd/snazzymaps-browser
- update twitter location
    https://dev.twitter.com/rest/reference/post/account/update_profile
- update facebook location
    https://developers.facebook.com/docs/graph-api/reference/location/
- update github location
- check location in a certain frequence (every x minutes/hours)
- handle REST-URL
- handle multiple websites 
    
Done:
26.07.2017
- update widget example image 
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
