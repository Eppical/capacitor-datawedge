import { Component, OnDestroy, OnInit } from '@angular/core';
import { DataWedge, type RegisterOptions, type ScanListenerEvent } from 'capacitor-datawedge';
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
  scanEvents: ScanEntry[] = [];
  logs: string[] = [];
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
