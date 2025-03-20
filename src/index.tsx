import PagbankPos from './NativePagbankPos';

export enum TransactionType {
  CREDIT = 1,
  DEBIT = 2,
  VOUCHER = 3,
  PIX = 5,
}

export enum InstallmentType {
  NO_INSTALLMENT = 1,
  SELLER_INSTALLMENT = 2,
  BUYER_INSTALLMENT = 3,
}

export interface InitSDKResponse {
  result: number;
  errorCode: string;
  errorMessage: string;
}

export interface PrintResponse {
  message: string;
}

export interface CancelTransactionResponse {
  message: string;
}

export interface VoidTransactionResponse {
  message: string;
}

export interface TransactionRequest {
  amount: number;
  type: TransactionType;
  installmentType: InstallmentType;
  installments: number;
  printReceipt: boolean;
  userReference: string;
}

export interface TransactionResponse {
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

export async function initSDK(
  activationCode: string
): Promise<InitSDKResponse> {
  return await PagbankPos.initializeAndActivatePinPad(activationCode);
}

export async function makeTransaction({
  amount,
  installmentType,
  installments,
  printReceipt,
  type,
  userReference,
}: TransactionRequest): Promise<TransactionResponse> {
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

export async function cancelRunningTransaction(): Promise<CancelTransactionResponse> {
  return await PagbankPos.cancelRunningTransaction();
}

export async function voidTransaction(
  transactionCode: string,
  transactionId: string
): Promise<VoidTransactionResponse> {
  const dataFormatted = JSON.stringify({ transactionCode, transactionId });
  return await PagbankPos.voidPayment(dataFormatted);
}

export async function printByHtml(html: string): Promise<PrintResponse> {
  return await PagbankPos.printByHtml(html);
}

export async function reprintCustomerReceipt(): Promise<PrintResponse> {
  return await PagbankPos.reprintCustomerReceipt();
}

export async function reprintEstablishmentReceipt(): Promise<PrintResponse> {
  return await PagbankPos.reprintEstablishmentReceipt();
}