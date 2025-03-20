import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';
import type {
  CancelTransactionResponse,
  InitSDKResponse,
  PrintResponse, 
  TransactionResponse,
  VoidTransactionResponse,
} from 'react-native-pagbank-pos';

export interface Spec extends TurboModule {
  setAppIdentification(): void;
  initializeAndActivatePinPad(activationCode: string): Promise<InitSDKResponse>;
  doPayment(doPayment: string): Promise<TransactionResponse>;
  cancelRunningTransaction(): Promise<CancelTransactionResponse>;
  voidPayment(voidPayment: string): Promise<VoidTransactionResponse>;
  printByHtml(html: string): Promise<PrintResponse>;
  reprintCustomerReceipt(): Promise<PrintResponse>;
  reprintEstablishmentReceipt(): Promise<PrintResponse>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('PagbankPos');
