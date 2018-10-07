# Psori Reader Library

## overview

You can use RC-S380(Pasori Reader) on Android. RC-S380/S is sold by SONY.
The device can communicate with NFC such as Felica(Japanese tipical standard) and Type-A, Type-B.
[Detail in ....](https://www.sony.net/Products/felica/business/products/RC-S380.html)

I checked this software with only RC-S380/S. But, I believe the adapt to RC-S380/P.



## install

Add repository your build system configuration.
```
https://dl.bintray.com/masato-ka/android-things-support/
```

Please write to your project pom file.
```gradle
<dependency>
  <groupId>ka.masato.library.device</groupId>
  <artifactId>pasorilib</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

If you use gradle.

```
implementation compile 'ka.masato.library.device:pasorilib:1.0.0'


```


## Usage

The sample of read IDm of Felica. You can watch other sample at the ```PasoriInstrumentedTest.java```

```java

PasoriReadCallback pasoriReadCallback = new PasoriReadCallback() {
    @Override
    public void pollingRecieve(String idmString, String pmmString) {
        Log.i("InstrumentedTest", "IDM: " + idmString + " PMm: " + pmmString);
        status = false;
    }
};

HandlerThread handlerThread = new HandlerThread("polling");
handlerThread.start();
Handler handler = new Handler(handlerThread.getLooper());
Log.i("InstrumentedTest", "Please touch your IC card on Pasori.");
pasoriDriverTypeF.startPolling(handler, pasoriReadCallback);

while (status) {
    try {
        Log.i("InstrumentedTest", "Loop");
        Thread.sleep(1000L);
    } catch (InterruptedException e) {
       e.printStackTrace();
    }
}


pasoriDriverTypeF.stopPolling();

```

## Version

* 2018/10/07 Version 1.0.0
    * Support without encryption function on Felica(NFC-TypeF)
      * polling
      * RequestService
      * RequestResponse
      * readWithoutEncryption
      * writeWithoutEncryption
      
* 2018/9/10  Version 0.5.0
    * first release version. 

## Author

Name : masato-ka
E-mai: jp6uzv at gmail.com
Twitter: @masato_ka

## LICENCE

MIT


&copy; 2018 masato-ka All Rights Reserved.
