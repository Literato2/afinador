# Afinador

Afinador de guitarra y bandurria para Android. Sin anuncios, arranca al momento.
Guitarra estándar (E A D G B E) y bandurria española (G♯ C♯ F♯ B E A).

*De Literato, para sus amigos de la tuna. Porque por más que le pese a Nobita, a veces hay que afinar.*

## Descargar

**[Afinador.apk](https://github.com/Literato2/afinador/releases/latest/download/Afinador.apk)** (Android 14+).
Ábrelo desde el móvil y acepta "instalar apps de origen desconocido".

**iPhone (o cualquier móvil): versión web** en https://literato2.github.io/afinador/
Ábrela en Safari → Compartir → "Añadir a pantalla de inicio". Queda como una app, con icono.

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
