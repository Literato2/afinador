# Afinador

Afinador de guitarra para Android. Sin anuncios, arranca al momento, solo guitarra
en afinación estándar (E A D G B E).

*De Literato, para sus amigos de la tuna. Porque por más que le pese a Nobita, a veces hay que afinar.*

## Uso

Abre la app y toca una cuerda. En **AUTO** detecta la cuerda sola; pulsa una cuerda
abajo para fijarla (útil si está muy desafinada) y otra vez para volver a AUTO.
Verde = afinada (±4 cents).

## Cómo funciona

- Detector de tono **YIN** sobre el micro (44,1 kHz, ventana 4096, salto 1024).
- Mediana de las últimas 5 lecturas para que la aguja no baile.
- Jetpack Compose, sin dependencias externas. Android 14+.

## Compilar

```bash
export JAVA_HOME=/ruta/a/jdk-17
./gradlew assembleRelease   # firmado si defines JUANITO_KEYSTORE en ~/.gradle/gradle.properties
./gradlew assembleDebug     # sin firma propia
```
