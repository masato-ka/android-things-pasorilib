# Psori Reader Library

## overview

You can use RC-S380(Pasori Reader) on Android. RC-S380/S is sold by SONY.
The device can communicate with NFC such as Felica(Japanese tipical standard) and Type-A, Type-B.
[Detail in ....](https://www.sony.net/Products/felica/business/products/RC-S380.html)

I checked this software with only RC-S380/S. But, I believe the adapt to RC-S380/P.



## install

Please write to your project pom file.

```

```

If you use gradle

```


```


## Usage

```java

mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
HandlerThread mThread = new HandlerThread("pasoriThread");
mThread.start();
Handler pasoriHandler = new Handler(mThread.getLooper());
PasoriReader mPasoriReader =  PasoriReader.getInstance();
mPasoriReader.initializeDevice(mUsbManager, CardType.F);
PasoriReadCallback mCallback = new PasoriReadCallback() {
    @Override
    public void getResult(CardRecord card) {
        Log.d(LOG_TAG, "Get card info");
    }
};
mPasoriReader.startRead(pasoriHandler, mCallback);

// some doing or other funciton...

mPasoriReader.stop();

```

## Version

* 2018/9/10  Version 0.5.0
    * first release version. 

## Author

* masato-ka
* jp6uzv@gmail.com
* Twitter @masato_ka

## LICENCE

MIT


&copy; 2018 masato-ka All Rights Reserved.



