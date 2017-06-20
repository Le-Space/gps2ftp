GPS2FTP
-------

Description: 
A widget for Android and Wear devices which updates 
the current geo position to a defined destination ftp server.
Useful in order to update your personal website with your current location.
e.g. see www.le-space.de or www.nicokrause.com

Todo:
- geoCode Address and display it on widget screen
     - https://developer.android.com/training/location/display-address.html
- publish github / choose license
- write tests
- test on cloud devices / genymotion 

Nice2Haves:
- check location in a certain frequence (every x minutes/hours)
- handle REST-URL
- handle multiple websites 
    
Done:

19.07.2017
- publish to playstore
    - create logo https://romannurik.github.io/AndroidAssetStudio/index.html
    - create graphic for playstore 
- display map of lastlocation 
    https://developers.google.com/maps/documentation/android-api/map
    https://developers.google.com/maps/documentation/android-api/views
- added ftp configuration 
- change launch icon, gps icon and app title

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
