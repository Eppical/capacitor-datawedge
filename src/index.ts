import { registerPlugin } from '@capacitor/core';
import type { DataWedgePlugin, ScanResultCallback, RemoveListener } from './definitions';

const DataWedgeBase = registerPlugin<DataWedgePlugin>('DataWedge', {
  web: () => import('./web').then(m => new m.DataWedgeWeb()),
});

/**
 * Plugin DataWedge con API simplificada para escaneo.
 * Solo expone los métodos públicos definidos en la interfaz.
 */
export const DataWedge: DataWedgePlugin = {
  async initialize(options) {
    return DataWedgeBase.initialize(options);
  },

  async getAvailability(options) {
    return DataWedgeBase.getAvailability(options);
  },

  async onScanResult(callback: ScanResultCallback): Promise<RemoveListener> {
    const listener = await (DataWedgeBase as any).addListener('scan', callback);
    return () => listener.remove();
  },
};

export * from './definitions';
