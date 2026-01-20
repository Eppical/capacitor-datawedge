import { Component, OnDestroy, OnInit } from '@angular/core';
import {
  DataWedge,
  type AvailabilityResult,
  type ScanEvent,
} from 'capacitor-datawedge';

interface ScanEntry extends ScanEvent {
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
  profileName = 'DataWedgeDemo';
  scanEvents: ScanEntry[] = [];
  logs: string[] = [];
  isInitialized = false;
  initResult?: { profileName: string; intentAction: string };
  availability?: AvailabilityResult;
  private scanListener?: { remove: () => void };
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

  async initialize(): Promise<void> {
    await this.runActionWithResult(
      'initialize',
      () => DataWedge.initialize({ profileName: this.profileName }),
      result => {
        this.initResult = result;
        this.isInitialized = true;
        return `Perfil: ${result.profileName}, Intent: ${result.intentAction}`;
      },
    );
  }

  async getAvailability(): Promise<void> {
    await this.runActionWithResult(
      'getAvailability',
      () => DataWedge.getAvailability({ timeoutMs: 5000 }),
      result => {
        this.availability = result;
        const dwStatus = result.datawedge.present ? 'presente' : 'no presente';
        const dwEnabled = result.datawedge.enabled ? 'habilitado' : 'deshabilitado';
        const scannerStatus = result.scanner.present ? 'presente' : 'no presente';
        const scannerState = result.scanner.status ?? 'sin estado';
        return `DataWedge: ${dwStatus} (${dwEnabled}), Scanner: ${scannerStatus} (${scannerState})`;
      },
    );
  }

  trackByTimestamp(_index: number, item: ScanEntry): string {
    return item.timestamp;
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
    this.scanListener = await DataWedge.addListener('scan', (event: ScanEvent) => {
      const timestamp = new Date().toISOString();
      this.scanEvents = [{ ...event, timestamp }, ...this.scanEvents].slice(0, 50);
      this.addLog(`Escaneo recibido (${event.labelType ?? 'sin tipo'}): ${event.data}`);
    });

    this.addLog('Listener de escaneo registrado.');
  }

  private addLog(message: string): void {
    const timestamp = new Date().toLocaleTimeString();
    this.logs = [`[${timestamp}] ${message}`, ...this.logs].slice(0, 100);
  }
}
