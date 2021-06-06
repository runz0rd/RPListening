RPListening
============================

Overview
------------

RPListening is an Open Source desktop client for Roku private listening.

Supported Operating Systems:
* Mac OS
* Linux
* Windows (in the future)

Dependencies
------------

The following dependencies are required to run RPListening:
* Java 11.0.9 or later
* FFmpeg

Compiling
------------

    git clone https://github.com/wseemann/RPListening.git
    cd RPListening/RPListening
    ./gradlew customFatJar

The resulting jar file can be found in build/lib

Running
------------

    java -jar RPListening-1.0.jar -i <Roku Device IP Address>

For example

    java -jar RPListening-1.0.jar -i 192.168.1.64

If you don't know your devices IP address you can find it using the following command:

    java -jar RPListening-1.0.jar -d

Donations
------------

Donations can be made via PayPal:

**This project needs you!** If you would like to support this project's further development, the creator of this project or the continuous maintenance of this project, **feel free to donate**. Donations are highly appreciated. Thank you!

**PayPal**

- You can donate [**here**](https://www.paypal.com/donate/?cmd=_s-xclick&hosted_button_id=DR3PCSQTSSCMU).

Credit
------------
When I was coding up this application I had a difficult time finding a good sample Java based RTP/RTCP implementation. A big thank you to Henning Schulzrinne for publishing the source for [**JRTPMon**](https://www.cs.columbia.edu/~hgs/teaching/ais/1998/projects/java_rtp/JRTPMon.tar.gz). This code was an invaluable resource and this project would not have been possible with the ability to study and use parts of Henning's source code.

License
------------

```
RPListening: An Open Source desktop client for Roku private listening.
 
Copyright (C) 2021 William Seemann

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.