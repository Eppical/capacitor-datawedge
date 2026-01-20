import type { PluginListenerHandle } from '@capacitor/core';

export interface ScanListenerEvent {
  /**
   * Data of barcode
   *
   * @since 0.1.0
   */
  data: string;

  /**
   * Type of barcode
   *
   * @since 0.2.1
   */
  type: string | null;
}

export type ScanListener = (state: ScanListenerEvent) => void;

export type RegisterOptions = {
  /**
   * Intent action name to listen for
   *
   * @since 0.3.1
   */
  intent?: string;
};

export type ConfigureOptions = {
  /**
   * DataWedge profile name to create/update.
   *
   * @since 0.4.0
   */
  profileName: string;

  /**
   * Package name to associate with the profile.
   * Defaults to the current app package on Android.
   *
   * @since 0.4.0
   */
  packageName?: string;

  /**
   * Activity name to associate with the profile.
   * Defaults to '*'.
   *
   * @since 0.4.0
   */
  activityName?: string;

  /**
   * Intent action DataWedge should broadcast scan results to.
   * Defaults to the same action used by the plugin receiver.
   *
   * @since 0.4.0
   */
  intentAction?: string;
};

export type ConfigureProfileOptions = {
  /**
   * DataWedge profile name to create or update.
   * Uses CONFIG_MODE: CREATE_IF_NOT_EXIST - creates the profile if it doesn't exist,
   * or updates parameters if the profile already exists.
   *
   * @since 0.5.0
   */
  profileName: string;

  /**
   * Package name to associate with the profile.
   * Defaults to the current app package on Android.
   *
   * @since 0.5.0
   */
  packageName?: string;

  /**
   * Activity name to associate with the profile.
   * Defaults to '*'.
   *
   * @since 0.5.0
   */
  activityName?: string;

  /**
   * Intent action DataWedge should broadcast scan results to.
   * Defaults to the same action used by the plugin receiver.
   *
   * @since 0.5.0
   */
  intentAction?: string;

  /**
   * Enable or disable the barcode scanner input plugin.
   * Defaults to true.
   *
   * @since 0.5.0
   */
  barcodeEnabled?: boolean;

  /**
   * Enable or disable keystroke output plugin.
   * When enabled, scanned data is sent as keystrokes.
   * Defaults to false.
   *
   * @since 0.5.0
   */
  keystrokeEnabled?: boolean;

  /**
   * Enable or disable intent output plugin.
   * When enabled, scanned data is broadcast via intent.
   * Defaults to true.
   *
   * @since 0.5.0
   */
  intentEnabled?: boolean;
};

export interface ConfigureResult {
  /**
   * Whether DataWedge reported success.
   *
   * @since 0.4.0
   */
  success: boolean;

  /**
   * Raw DataWedge command name, when available.
   *
   * @since 0.4.0
   */
  command?: string;

  /**
   * Raw DataWedge result string, when available.
   *
   * @since 0.4.0
   */
  result?: string;
}

export interface ReadyResult {
  /**
   * Whether DataWedge responded with version info.
   *
   * @since 0.4.0
   */
  ready: boolean;

  /**
   * Version info returned by DataWedge, when available.
   *
   * @since 0.4.0
   */
  versionInfo?: Record<string, string>;
}

export interface ScannerStatusResult {
  /**
   * Whether the device reports an available scanner.
   *
   * @since 0.4.0
   */
  hasScanner: boolean;

  /**
   * Raw status string returned by DataWedge, when available.
   *
   * @since 0.4.0
   */
  status?: string | null;
}

export interface DataWedgePlugin {
  /**
   * Enables DataWedge
   *
   * Broadcasts intent action with `.ENABLE_DATAWEDGE` extra set to `true`
   *
   * @since 0.0.3
   */
  enable(): Promise<void>;

  /**
   * Disables DataWedge
   *
   * Broadcasts intent action with `.ENABLE_DATAWEDGE` extra set to `false`
   *
   * @since 0.0.3
   */
  disable(): Promise<void>;

  /**
   * Enables physical scanner
   *
   * Broadcasts intent action with `.SCANNER_INPUT_PLUGIN` extra set to `ENABLE_PLUGIN`
   *
   * @since 0.0.3
   */
  enableScanner(): Promise<void>;

  /**
   * Disables physical scanner
   *
   * Broadcasts intent action with `.SCANNER_INPUT_PLUGIN` extra set to `DISABLE_PLUGIN`
   *
   * @since 0.0.3
   */
  disableScanner(): Promise<void>;

  /**
   * Starts software scanning trigger
   *
   * Broadcasts intent action with `.SOFT_SCAN_TRIGGER` extra set to `START_SCANNING`
   *
   * @since 0.1.2
   */
  startScanning(): Promise<void>;

  /**
   * Stops software scanning trigger
   *
   * Broadcasts intent action with `.SOFT_SCAN_TRIGGER` extra set to `STOP_SCANNING`
   *
   * @since 0.1.2
   */
  stopScanning(): Promise<void>;

  /**
   * Checks if DataWedge is ready by requesting version information.
   *
   * @since 0.4.0
   */
  isReady(): Promise<ReadyResult>;

  /**
   * Checks if a scanner is available on the device.
   *
   * @since 0.4.0
   */
  hasScanner(): Promise<ScannerStatusResult>;

  /**
   * Creates/updates a DataWedge profile and configures intent output.
   *
   * @since 0.4.0
   */
  configure(options: ConfigureOptions): Promise<ConfigureResult>;

  /**
   * Creates or updates a DataWedge profile using CONFIG_MODE: CREATE_IF_NOT_EXIST.
   * This method creates the profile if it doesn't exist, or updates only the
   * specified parameters if the profile already exists (other parameters remain unchanged).
   *
   * Configures BARCODE input, INTENT output, and KEYSTROKE output plugins.
   *
   * @since 0.5.0
   */
  configureProfile(options: ConfigureProfileOptions): Promise<ConfigureResult>;

  /**
   * Listen for successful barcode readings
   *
   * ***Notice:*** Requires intent action to be set to `com.capacitor.datawedge.RESULT_ACTION` in current DataWedge profile (it may change in the future)
   *
   * @since 0.1.0
   */
  addListener(
    eventName: 'scan',
    listenerFunc: ScanListener,
  ): Promise<PluginListenerHandle>;

  /**
   * Internal method to register intent broadcast receiver
   *
   * THIS METHOD IS FOR INTERNAL USE ONLY
   *
   * @since 0.1.3
   * @private
   */
  __registerReceiver(options?: RegisterOptions): Promise<void>;
}
