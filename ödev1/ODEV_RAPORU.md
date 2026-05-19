# Varlik Yonetimi Uygulamasi - Odev Raporu

## Odevin Konusu

Bu odevde Android Studio ve Java kullanarak varlik ve maas takibi uygulamasi gelistirdim. Uygulama, demirbas veya cihaz gibi kayitlarin mobil ortamda tutulmasi ve aylik gelir-gider bilgisinin takip edilmesi icin hazirlandi.

## Uygulamanin Amaci

Uygulamanin amaci; varlik adi, turu, konum, sorumlu kisi, seri numarasi, alim tarihi, durum ve deger bilgilerini kayit altina almaktir. Ayrica kullanici aylik gelirini, giderlerini, alisverislerini ve yatirimlarini uygulamada gorebilir.

## Uygulama Ozellikleri

- Yeni varlik eklenebilir.
- Var olan kayitlar duzenlenebilir.
- Gereksiz kayitlar silinebilir.
- Konut, arsa, elektronik, esya, arac, nakit gibi varlik turleri secilebilir.
- Secilen varlik turune gore farkli bilgiler girilebilir.
- Kategori filtresi ile liste daraltilabilir.
- Arama kutusu ile kayitlar bulunabilir.
- Toplam varlik degeri hesaplanir.
- Aylik gelir ve aylik gider bilgisi takip edilir.
- Para tutarlari 1.000.000 seklinde okunakli gosterilir.
- Veriler uygulama kapatilsa bile kaybolmaz.

## Kod Yapisi

- `MainActivity.java`: Arayuz ve kullanici islemleri burada bulunur.
- `Asset.java`: Varlik bilgilerinin tutuldugu model sinifidir.
- `AssetStore.java`: Kayitlarin SharedPreferences ile saklandigi siniftir.
- `FinanceEntry.java`: Gelir-gider hareketlerinin model sinifidir.
- `FinanceStore.java`: Maas ve para hareketlerinin saklandigi siniftir.

## Sonuc

Proje calisir durumdadir. Debug APK build alinmistir ve Android Studio uzerinden emulator veya telefonda calistirilabilir.
