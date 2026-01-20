import { WebPlugin } from '@capacitor/core';

import type {
  ConfigureOptions,
  ConfigureProfileOptions,
  ConfigureResult,
  DataWedgePlugin,
  ReadyResult,
  ScannerStatusResult,
} from './definitions';

export class DataWedgeWeb extends WebPlugin implements DataWedgePlugin {
  async enable(): Promise<void> {
    throw 'DataWedge is not supported on web';
  }

  async disable(): Promise<void> {
    throw 'DataWedge is not supported on web';
  }

  async enableScanner(): Promise<void> {
    throw 'DataWedge is not supported on web';
  }

  async disableScanner(): Promise<void> {
    throw 'DataWedge is not supported on web';
  }

  async startScanning(): Promise<void> {
    throw 'DataWedge is not supported on web';
  }

  async stopScanning(): Promise<void> {
    throw 'DataWedge is not supported on web';
  }

  async isReady(): Promise<ReadyResult> {
    throw 'DataWedge is not supported on web';
  }

  async hasScanner(): Promise<ScannerStatusResult> {
    throw 'DataWedge is not supported on web';
  }

  async configure(_options: ConfigureOptions): Promise<ConfigureResult> {
    throw 'DataWedge is not supported on web';
  }

  async configureProfile(_options: ConfigureProfileOptions): Promise<ConfigureResult> {
    throw 'DataWedge is not supported on web';
  }

  async __registerReceiver(): Promise<void> {
    // no-op
  }
}
