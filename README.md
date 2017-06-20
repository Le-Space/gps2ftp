GPS2FTP
-------
An app for Android devices which updates the current gps position to a defined ftp server.
Useful in order to update your personal website with your current location.
App works with the free Bootrap template of David Miller (https://twitter.com/davidmillerskt)
https://github.com/Le-Space/startbootstrap-grayscale 
(please use our fork as long its not included in the main project!)

e.g. see www.le-space.de or www.irinasenko.com

Todo:
- geoCode Address and display it on widget screen
     - https://developer.android.com/training/location/display-address.html
- write tests
- test on cloud devices / genymotion 

Nice2Haves:
- check location in a certain frequence (every x minutes/hours)
- handle REST-URL
- handle multiple websites 
    
Done:
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
