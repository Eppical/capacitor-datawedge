import { registerPlugin } from '@capacitor/core';
import type { DataWedgePlugin, ScanResultCallback, RemoveListener } from './definitions';

const DataWedgeBase = registerPlugin<DataWedgePlugin>('DataWedge', {
  web: () => import('./web').then(m => new m.DataWedgeWeb()),
});

/**
 * Plugin DataWedge con API simplificada para escaneo.
 */
export const DataWedge: DataWedgePlugin = {
  initialize: (options) => DataWedgeBase.initialize(options),
  getAvailability: (options) => DataWedgeBase.getAvailability(options),
  addListener: ((eventName: string, listenerFunc: (event: any) => void) =>
    DataWedgeBase.addListener(eventName as 'scan', listenerFunc)) as DataWedgePlugin['addListener'],

  async onScanResult(callback: ScanResultCallback): Promise<RemoveListener> {
    const listener = await DataWedgeBase.addListener('scan', callback);
    return () => listener.remove();
  },
};

export * from './definitions';
