import { Component, OnDestroy, OnInit } from '@angular/core';
import {
  DataWedge,
  type ConfigureOptions,
  type ConfigureResult,
  type ReadyResult,
  type RegisterOptions,
  type ScanListenerEvent,
  type ScannerStatusResult,
} from 'capacitor-datawedge';
import type { PluginListenerHandle } from '@capacitor/core';

interface ScanEntry extends ScanListenerEvent {
  timestamp: string;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'DataWedge Demo';
  statusMessage = 'Listo para invocar el plugin de DataWedge.';
  intentAction = 'com.capacitor.datawedge.RESULT_ACTION';
  profileName = 'DataWedgeDemo';
  profilePackageName = '';
  profileActivityName = '*';
  profileIntentAction = 'com.capacitor.datawedge.RESULT_ACTION';
  scanEvents: ScanEntry[] = [];
  logs: string[] = [];
  lastReadyResult?: ReadyResult;
  lastScannerResult?: ScannerStatusResult;
  lastConfigureResult?: ConfigureResult;
  private scanListener?: PluginListenerHandle;
  activeAction?: string;

  ngOnInit(): void {
    this.attachScanListener();
  }

  ngOnDestroy(): void {
    this.scanListener?.remove();
  }

  get isBusy(): boolean {
    return Boolean(this.activeAction);
  }

  async enable(): Promise<void> {
    await this.runAction('enable', () => DataWedge.enable());
  }

  async disable(): Promise<void> {
    await this.runAction('disable', () => DataWedge.disable());
  }

  async enableScanner(): Promise<void> {
    await this.runAction('enableScanner', () => DataWedge.enableScanner());
  }

  async disableScanner(): Promise<void> {
    await this.runAction('disableScanner', () => DataWedge.disableScanner());
  }

  async startScanning(): Promise<void> {
    await this.runAction('startScanning', () => DataWedge.startScanning());
  }

  async stopScanning(): Promise<void> {
    await this.runAction('stopScanning', () => DataWedge.stopScanning());
  }

  async checkReady(): Promise<void> {
    await this.runActionWithResult('isReady', () => DataWedge.isReady(), result => {
      this.lastReadyResult = result as ReadyResult;
      const version = this.lastReadyResult.versionInfo?.['DATAWEDGE'] || 'desconocida';
      return `DataWedge listo: ${this.lastReadyResult.ready ? 'sí' : 'no'} (versión: ${version}).`;
    });
  }

  async checkScanner(): Promise<void> {
    await this.runActionWithResult('hasScanner', () => DataWedge.hasScanner(), result => {
      this.lastScannerResult = result as ScannerStatusResult;
      return `Scanner disponible: ${this.lastScannerResult.hasScanner ? 'sí' : 'no'} (estado: ${this.lastScannerResult.status ?? 'sin estado'}).`;
    });
  }

  async configureProfile(): Promise<void> {
    const options: ConfigureOptions = {
      profileName: this.profileName,
      intentAction: this.profileIntentAction || undefined,
      packageName: this.profilePackageName || undefined,
      activityName: this.profileActivityName || undefined,
    };

    await this.runActionWithResult('configure', () => DataWedge.configure(options), result => {
      this.lastConfigureResult = result as ConfigureResult;
      return `Configuración: ${this.lastConfigureResult.success ? 'ok' : 'falló'} (${this.lastConfigureResult.result ?? 'sin resultado'}).`;
    });
  }

  async registerReceiver(): Promise<void> {
    const options: RegisterOptions = this.intentAction
      ? { intent: this.intentAction }
      : {};

    await this.runAction('__registerReceiver', () => DataWedge.__registerReceiver(options));
  }

  trackByTimestamp(_index: number, item: ScanEntry): string {
    return item.timestamp;
  }

  private async runAction(actionName: string, action: () => Promise<void>): Promise<void> {
    this.activeAction = actionName;
    const startedAt = new Date();

    try {
      await action();
      this.statusMessage = `Acción "${actionName}" ejecutada correctamente a las ${startedAt.toLocaleTimeString()}.`;
      this.addLog(`✔ ${actionName} completado`);
    } catch (error) {
      const message = error instanceof Error ? error.message : `${error}`;
      this.statusMessage = `No se pudo completar "${actionName}": ${message}`;
      this.addLog(`✖ ${actionName}: ${message}`);
    } finally {
      this.activeAction = undefined;
    }
  }

  private async runActionWithResult<T>(
    actionName: string,
    action: () => Promise<T>,
    formatMessage: (result: T) => string,
  ): Promise<void> {
    this.activeAction = actionName;
    const startedAt = new Date();

    try {
      const result = await action();
      const detail = formatMessage(result);
      this.statusMessage = `Acción "${actionName}" ejecutada correctamente a las ${startedAt.toLocaleTimeString()}. ${detail}`;
      this.addLog(`✔ ${actionName} completado: ${detail}`);
    } catch (error) {
      const message = error instanceof Error ? error.message : `${error}`;
      this.statusMessage = `No se pudo completar "${actionName}": ${message}`;
      this.addLog(`✖ ${actionName}: ${message}`);
    } finally {
      this.activeAction = undefined;
    }
  }

  private async attachScanListener(): Promise<void> {
    this.scanListener = await DataWedge.addListener('scan', event => {
      const timestamp = new Date().toISOString();
      this.scanEvents = [{ ...event, timestamp }, ...this.scanEvents].slice(0, 50);
      this.addLog(`Escaneo recibido (${event.type ?? 'sin tipo'}): ${event.data}`);
    });

    this.addLog('Listener de escaneo registrado.');
  }

  private addLog(message: string): void {
    const timestamp = new Date().toLocaleTimeString();
    this.logs = [`[${timestamp}] ${message}`, ...this.logs].slice(0, 100);
  }
}
