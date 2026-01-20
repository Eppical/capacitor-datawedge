export interface InitializeOptions {
  profileName?: string;
  intentAction?: string;
  intentCategory?: string;
}

export interface ScanEvent {
  data: string;
  labelType?: string;
  source?: string;
}

export type ScannerStatus =
  | 'WAITING'
  | 'SCANNING'
  | 'DISABLED'
  | 'CONNECTED'
  | 'DISCONNECTED'
  | string;

export interface AvailabilityResult {
  datawedge: {
    present: boolean;
    enabled: boolean | null;
    statusRaw?: any;
  };
  scanner: {
    present: boolean | null;
    status?: ScannerStatus | null;
    scanners?: Array<{
      name?: string;
      connected?: boolean;
      index?: number;
      identifier?: string;
    }>;
    raw?: any;
  };
  raw?: any;
}

export interface DataWedgePlugin {
  initialize(options?: InitializeOptions): Promise<{ profileName: string; intentAction: string }>;
  getAvailability(options?: { timeoutMs?: number }): Promise<AvailabilityResult>;
  addListener(eventName: 'scan', listenerFunc: (event: ScanEvent) => void): Promise<{ remove: () => void }>;
  addListener(eventName: 'datawedgeResult', listenerFunc: (event: any) => void): Promise<{ remove: () => void }>;
}
