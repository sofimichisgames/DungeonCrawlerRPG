# DungeonCrawler RPG — Cómo compilar e instalar en Android

## Requisitos
- **Android Studio** (descarga gratis en https://developer.android.com/studio)
- Java JDK 11 o superior (se instala con Android Studio)

---

## Pasos para compilar el APK

### 1. Abrir el proyecto
1. Abre Android Studio
2. Haz clic en **"Open"**
3. Navega a la carpeta `DungeonCrawlerRPG/` y selecciónala
4. Espera a que Gradle sincronice (puede tardar unos minutos la primera vez)

### 2. Compilar el APK de debug
Ve al menú:
**Build → Build Bundle(s) / APK(s) → Build APK(s)**

El APK se generará en:
```
DungeonCrawlerRPG/app/build/outputs/apk/debug/app-debug.apk
```

### 3. Instalar en tu teléfono Android

**Opción A — Cable USB:**
1. Activa el **Modo Desarrollador** en tu teléfono:
   - Ajustes → Acerca del teléfono → toca "Número de compilación" 7 veces
2. Activa **Depuración USB** en Opciones de Desarrollador
3. Conecta el teléfono por USB
4. En Android Studio: **Run → Run 'app'** (o Shift+F10)

**Opción B — Copiar el APK manualmente:**
1. Copia `app-debug.apk` a tu teléfono
2. En el teléfono, activa **"Instalar apps de fuentes desconocidas"**
   - Ajustes → Seguridad → Fuentes desconocidas (o por nombre del archivo)
3. Abre el APK desde el explorador de archivos y presiona Instalar

---

## Controles en el juego

| Acción              | Control táctil          | Teclado Bluetooth |
|---------------------|-------------------------|-------------------|
| Mover               | D-pad en pantalla       | WASD / flechas    |
| Esperar turno       | Botón "Esperar"         | Espacio           |
| Recoger ítem        | Botón "Recoger"         | G                 |
| Bajar de piso       | Botón "Bajar"           | E                 |
| Abrir inventario    | Botón "INV"             | I                 |
| Usar ítem           | Toca el ítem en INV     | Tecla a-z         |

---

## Estructura del proyecto

```
DungeonCrawlerRPG/
├── app/src/main/java/com/dungeoncrawler/
│   ├── Config.kt              — Constantes, colores, configuración
│   ├── MainActivity.kt        — Actividad principal de Android
│   ├── GameView.kt            — SurfaceView: game loop + input táctil
│   ├── game/
│   │   ├── GameEngine.kt      — Motor central: estados y turnos
│   │   ├── GameMap.kt         — Cuadrícula de tiles
│   │   ├── DungeonGenerator.kt— Generación procedural de mazmorras
│   │   ├── FovSystem.kt       — Campo de visión (Shadowcasting)
│   │   ├── MessageLog.kt      — Historial de mensajes
│   │   └── entity/
│   │       ├── Entity.kt      — Clase base
│   │       ├── Player.kt      — Jugador, stats, inventario
│   │       ├── Enemy.kt       — 6 tipos de enemigos con IA
│   │       └── Item.kt         — 12 tipos de ítems con efectos
│   └── ui/
│       ├── Renderer.kt        ℐ Dibuja todo en Canvas de Android
│       └── VirtualControls.kt — D-pad táctil
└── app/build.gradle           — Dependencias y SDK
```
