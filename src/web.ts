import { WebPlugin } from '@capacitor/core';
import type { DataWedgePlugin, InitializeOptions, AvailabilityResult, ScanResultCallback, RemoveListener } from './definitions';

export class DataWedgeWeb extends WebPlugin implements DataWedgePlugin {
  async initialize(_options?: InitializeOptions): Promise<{ profileName: string; intentAction: string }> {
    throw new Error('DataWedge is only available on Android Zebra devices.');
  }

  async getAvailability(_options?: { timeoutMs?: number }): Promise<AvailabilityResult> {
    throw new Error('DataWedge is only available on Android Zebra devices.');
  }

  async onScanResult(_callback: ScanResultCallback): Promise<RemoveListener> {
    throw new Error('DataWedge is only available on Android Zebra devices.');
  }
}
