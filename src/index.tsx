import PagbankPos from './NativePagbankPos';

export enum PagBankTransactionType {
  CREDIT = 1,
  DEBIT = 2,
  VOUCHER = 3,
  PIX = 5,
}

export enum PagBankInstallmentType {
  NO_INSTALLMENT = 1,
  SELLER_INSTALLMENT = 2,
  BUYER_INSTALLMENT = 3,
}

export interface PagBankInitSDKResponse {
  result: number;
  errorCode: string;
  errorMessage: string;
}

export interface PagBankPrintResponse {
  message: string;
}

export interface PagBankCancelTransactionResponse {
  message: string;
}

export interface PagBankVoidTransactionResponse {
  message: string;
}

export interface PagBankTransactionRequest {
  amount: number;
  type: PagBankTransactionType;
  installmentType: PagBankInstallmentType;
  installments: number;
  printReceipt: boolean;
  userReference: string;
}

export interface PagBankTransactionResponse {
  result: number;
  errorCode?: string;
  message?: string;
  transactionCode?: string;
  transactionId?: string;
  hostNsu?: string;
  date?: string;
  time?: string;
  cardBrand?: string;
  bin?: string;
  holder?: string;
  userReference?: string;
  terminalSerialNumber?: string;
  amount?: string;
  availableBalance?: string;
  cardApplication?: string;
  label?: string;
  holderName?: string;
  extendedHolderName?: string;
}

export type PagBankProgressEventName =
  | 'VOID_TRANSACTION_PROGRESS'
  | 'MAKE_TRANSACTION_PROGRESS'
  | string;

export const PagBankPosSDK = new (class {
  async initSDK(activationCode: string): Promise<PagBankInitSDKResponse> {
    return await PagbankPos.initializeAndActivatePinPad(activationCode);
  }

  async makeTransaction({
    amount,
    installmentType,
    installments,
    printReceipt,
    type,
    userReference,
  }: PagBankTransactionRequest): Promise<PagBankTransactionResponse> {
    const dataPayment = {
      amount,
      installmentType,
      installments,
      printReceipt,
      type,
      userReference,
    };

    const dataFormatted = JSON.stringify(dataPayment);
    return await PagbankPos.doPayment(dataFormatted);
  }

  async cancelRunningTransaction(): Promise<PagBankCancelTransactionResponse> {
    return await PagbankPos.cancelRunningTransaction();
  }

  async voidTransaction(
    transactionCode: string,
    transactionId: string
  ): Promise<PagBankVoidTransactionResponse> {
    const dataFormatted = JSON.stringify({ transactionCode, transactionId });
    return await PagbankPos.voidPayment(dataFormatted);
  }

  async printByHtml(html: string): Promise<PagBankPrintResponse> {
    return await PagbankPos.printByHtml(html);
  }

  async reprintCustomerReceipt(): Promise<PagBankPrintResponse> {
    return await PagbankPos.reprintCustomerReceipt();
  }

  async reprintEstablishmentReceipt(): Promise<PagBankPrintResponse> {
    return await PagbankPos.reprintEstablishmentReceipt();
  }

  addListener(eventName: string): void {
    PagbankPos.addListener(eventName);
  }

  removeListeners(count: number): void {
    PagbankPos.removeListeners(count);
  }
})();
