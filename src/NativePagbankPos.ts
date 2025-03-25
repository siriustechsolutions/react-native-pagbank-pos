import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';
import type {
  PagBankCancelTransactionResponse,
  PagBankInitSDKResponse,
  PagBankPrintResponse,
  PagBankTransactionResponse,
  PagBankVoidTransactionResponse,
} from 'react-native-pagbank-pos';

export interface Spec extends TurboModule {
  setAppIdentification(): void;
  initializeAndActivatePinPad(
    activationCode: string
  ): Promise<PagBankInitSDKResponse>;
  doPayment(doPayment: string): Promise<PagBankTransactionResponse>;
  cancelRunningTransaction(): Promise<PagBankCancelTransactionResponse>;
  voidPayment(voidPayment: string): Promise<PagBankVoidTransactionResponse>;
  printByHtml(html: string): Promise<PagBankPrintResponse>;
  reprintCustomerReceipt(): Promise<PagBankPrintResponse>;
  reprintEstablishmentReceipt(): Promise<PagBankPrintResponse>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('PagbankPos');
