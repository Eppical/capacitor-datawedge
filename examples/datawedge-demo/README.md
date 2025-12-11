# Demo DataWedge con Ionic + Angular 16

Aplicación de ejemplo que muestra cómo invocar todos los métodos del plugin
[`capacitor-datawedge`](https://www.npmjs.com/package/capacitor-datawedge) y visualizar en pantalla
los eventos que llegan a través de `DataWedge.addListener('scan', ...)`.

## Requisitos
- Node.js 18+
- Android SDK (para abrir el proyecto nativo)

## Instalación y ejecución web
```bash
npm install
npm start
```
Abre `http://localhost:4200/` para ver la interfaz Ionic.

## Sincronizar con Capacitor 7
```bash
npm run build
npm run cap:sync
```
Esto copiará los artefactos web a la carpeta nativa configurada (`android/`).

## Abrir el proyecto Android
```bash
npm run build
npm run cap:copy
npm run cap:open:android
```
Conecta un dispositivo Zebra y ejecuta la app para recibir los datos de escaneo.
