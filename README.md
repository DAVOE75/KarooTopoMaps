# 🗺️ KarooTopoMaps

> Temas de mapa topográfico para dispositivos **Hammerhead Karoo** | Topographic map themes for Hammerhead Karoo devices

<p align="center">
  <img src="art/banner.png" width="600" />
</p>

<p align="center">
  <a href="README.md">🇪🇸 Español</a> |
  <a href="README_en.md">🇬🇧 English</a> |
  <a href="README_fr.md">🇫🇷 Français</a>
</p>

---

## 🌍 ¿Qué es esto?

Este repositorio contiene temas de renderizado cartográfico que transforman el mapa estándar de tu Karoo en mapas topográficos de alta calidad, similares a los del **Instituto Geográfico Nacional (IGN)** de España.

> ⚠️ **Importante:** Estos temas modifican la apariencia visual del mapa, no las fuentes de datos. El Karoo seguirá usando sus propios datos de OpenStreetMap, pero renderizados con el estilo topográfico del IGN.

---

## 🎨 Temas Disponibles

| Tema | Descripción | País |
|---|---|---|
| `IGN-España` | Estilo topográfico del IGN español. Curvas de nivel, relieve sombreado, colores hipsométricos | 🇪🇸 España |
| `IGN-Ciclismo` | Variante optimizada para ciclismo. Mayor contraste de pendientes, carreteras destacadas | 🇪🇸 España |

---

## 📦 Instalación

Hay **dos métodos** de instalación:

---

### Método 1 — Vía ADB (Recomendado ⭐)

El método más rápido. Solo necesitas Android Debug Bridge (ADB) instalado en tu PC.

**Paso 1.** Conecta tu Karoo al PC mediante cable USB y activa la depuración ADB:
> En tu Karoo: **Ajustes → Sistema → Acerca del dispositivo** → pulsa 7 veces en "Número de compilación" → vuelve a **Ajustes → Opciones para desarrolladores → Depuración USB: ON**

**Paso 2.** Descarga el archivo del tema que quieras de la carpeta [`themes/`](themes/)

**Paso 3.** Ejecuta en tu PC:

```bash
adb push themes/IGN-España.xml /sdcard/osmand/rendering/IGN-España.xml
```

**Paso 4.** En tu Karoo, abre **OsmAnd → Configurar mapa → Estilo de mapa** y selecciona `IGN-España`.

---

### Método 2 — Vía cable USB (MTP)

Si no tienes ADB, puedes copiar el archivo manualmente:

**Paso 1.** Conecta el Karoo al PC con cable USB y elige **"Transferencia de archivos (MTP)"**

**Paso 2.** Navega a:
```
Karoo → Almacenamiento interno → osmand → rendering
```

**Paso 3.** Copia el archivo `.xml` del tema en esa carpeta

**Paso 4.** En tu Karoo: **OsmAnd → Configurar mapa → Estilo de mapa** → selecciona el tema

---

## 📸 Capturas de pantalla

<p align="center">
  <img src="art/screenshot_ign_vs_default.png" width="700" />
  <br><em>Izquierda: Mapa estándar | Derecha: Tema IGN-España</em>
</p>

---

## 🔧 Instalación por Script (Windows)

Si tienes ADB y quieres instalar todos los temas de golpe:

```powershell
# Desde la raíz del repositorio
.\scripts\install_themes.ps1
```

---

## 🤝 Contribuir

¿Tienes un tema propio o mejoras para los existentes? ¡Las PR son bienvenidas!

1. Haz fork del repositorio
2. Crea una rama: `git checkout -b mi-tema-nuevo`
3. Añade tu archivo `.xml` en la carpeta `themes/`
4. Describe los cambios en el README
5. Abre una Pull Request

---

## 📚 Recursos

- [Documentación oficial de estilos OsmAnd](https://osmand.net/docs/technical/osmand-file-formats/osmand-rendering-style)
- [Repositorio de estilos de la comunidad OsmAnd](https://github.com/osmandapp/OsmAnd-resources)
- [ALTGRAPH — Extensión 3D de altimetría para Karoo](https://github.com/DAVOE75/ALTGRAPH)

---

## 📄 Licencia

MIT License — David García Pascual (hesiOX) 2026

Los estilos cartográficos están inspirados en la cartografía del IGN España, distribuidos bajo licencia abierta para uso no comercial.
