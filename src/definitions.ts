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

/**
 * Callback para recibir resultados de escaneo
 */
export type ScanResultCallback = (result: ScanEvent) => void;

/**
 * Función para remover un listener de escaneo
 */
export type RemoveListener = () => void;

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
  /** Indica si hubo timeout al obtener la disponibilidad */
  timedOut?: boolean;
}

export interface DataWedgePlugin {
  initialize(options?: InitializeOptions): Promise<{ profileName: string; intentAction: string }>;
  getAvailability(options?: { timeoutMs?: number }): Promise<AvailabilityResult>;

  /**
   * Suscribe a eventos de escaneo con API simplificada.
   * Retorna una función para cancelar la suscripción.
   *
   * @example
   * const unsubscribe = await DataWedge.onScanResult((scan) => {
   *   console.log('Escaneado:', scan.data);
   * });
   * // Para desuscribirse:
   * unsubscribe();
   */
  onScanResult(callback: ScanResultCallback): Promise<RemoveListener>;

  addListener(eventName: 'scan', listenerFunc: (event: ScanEvent) => void): Promise<{ remove: () => void }>;
  addListener(eventName: 'datawedgeResult', listenerFunc: (event: any) => void): Promise<{ remove: () => void }>;
}
