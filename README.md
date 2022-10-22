
# Android apk for [PDOS](https://pdos.org)

The source code is Public Domain except the gradle directory and gradlew.*

## The application requires a hardware keyboard attached to your smartphone.

![screen](doc/screen.jpg)

If you want to make change to the APK, you need to sign it before install it :
```
zip -r app.zip ./app/
 
mv app.zip app.apk

keytool -genkey -keyalg RSA -alias mykeystore -keystore mykeystore.jks -storepass 12345678 -validity 360

apksigner sign --ks release.jks app.apk

```

https://docs.oracle.com/en/java/javase/12/tools/keytool.html

https://developer.android.com/studio/command-line/apksigner

