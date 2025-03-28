import { useEffect, useState } from 'react';
import {
  Text,
  View,
  StyleSheet,
  Button,
  ScrollView,
  TextInput,
  ActivityIndicator,
  Alert,
  NativeEventEmitter,
} from 'react-native';

import {
  PagBankPosSDK,
  PagBankTransactionType,
  type PagBankInitSDKResponse,
  type PagBankTransactionResponse,
} from 'react-native-pagbank-pos';

export default function App() {
  const [isConnected, setIsConnected] = useState<PagBankInitSDKResponse | null>(
    null
  );
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [amount, setAmount] = useState<string>('10');
  const [installments, setInstallments] = useState<string>('1');
  const [transactionId, setTransactionId] = useState<string>('');
  const [lastTransaction, setLastTransaction] =
    useState<PagBankTransactionResponse | null>(null);

  const moduleEventEmitter = new NativeEventEmitter(PagBankPosSDK);

  const useEventEmitter = (callback: Function) => {
    useEffect(() => {
      const subscription = moduleEventEmitter.addListener(
        'MAKE_TRANSACTION_PROGRESS',
        (event: any) => {
          callback(event);
        }
      );

      return () => {
        subscription.remove();
      };
    }, []);
  };

  /** Eventos de status da transação */
  useEventEmitter((event: any) => {
    console.info('MAKE_TRANSACTION_PROGRESS', event);

    if (Number(event.status) === 0) {
      // Quando solicita para aproximar o cartão
    }

    if (
      Number(event.status) === -1 ||
      Number(event.status) === 1 ||
      Number(event.status) === 5
    ) {
      // Sucesso, cancelamento ou falha na transação
    }

    if (Number(event.status) === 4) {
      //pagamento finalizado
    }
  });

  const handleInitialize = async () => {
    try {
      setIsLoading(true);
      const connected = await PagBankPosSDK.initSDK('749879');
      setIsConnected(connected);
      Alert.alert(
        'Connection Status',
        connected.result === 0 ? 'POS Connected!' : 'Failed to connect to POS'
      );
    } catch (error) {
      Alert.alert('Error', `Failed to initialize: ${error}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handlePayment = async (type: PagBankTransactionType) => {
    if (!isConnected) {
      Alert.alert('Error', 'POS not connected. Please initialize first.');
      return;
    }

    try {
      setIsLoading(true);
      const amountInCents = Math.round(parseFloat(amount) * 100);
      const installmentsNum = parseInt(installments, 10);

      const result = await PagBankPosSDK.makeTransaction({
        amount: amountInCents,
        type,
        installments: installmentsNum,
        printReceipt: false,
        userReference: 'test',
        installmentType: 1, // NO_INSTALLMENT
      });
      setLastTransaction(result);
      setTransactionId(result?.transactionId || '');
      Alert.alert(
        'Payment Processed',
        `result: ${result.result}\n` +
          `transactionId: ${result.transactionId}\n` +
          `message: ${result.message}\n` +
          `label: ${result.label}\n` +
          `errorCode: ${result.errorCode}\n`
      );
    } catch (error) {
      Alert.alert('Payment Error', `Failed to process payment: ${error}`);
    } finally {
      setIsLoading(false);
    }
  };

  // Cancel a transaction
  const handleCancelTransaction = async () => {
    if (!transactionId) {
      Alert.alert('Error', 'No transaction ID provided');
      return;
    }

    try {
      setIsLoading(true);
      const result = await PagBankPosSDK.cancelRunningTransaction();
      Alert.alert('Cancellation Result', result.message);
    } catch (error) {
      Alert.alert(
        'Cancellation Error',
        `Failed to cancel transaction: ${error}`
      );
    } finally {
      setIsLoading(false);
    }
  };

  // Print a receipt
  const handlePrintReceipt = async () => {
    if (!lastTransaction) {
      Alert.alert('Error', 'No transaction data available');
      return;
    }

    try {
      setIsLoading(true);
      await PagBankPosSDK.reprintCustomerReceipt();
      Alert.alert('Success', 'Receipt printed successfully');
    } catch (error) {
      Alert.alert('Print Error', `Failed to print receipt: ${error}`);
    } finally {
      setIsLoading(false);
    }
  };
  return (
    <ScrollView contentContainerStyle={styles.scrollContainer}>
      <View style={styles.container}>
        <Text style={styles.title}>PagBank POS Example</Text>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>POS Connection 2</Text>
          <View style={styles.row}>
            <Button
              title="Initialize POS"
              onPress={handleInitialize}
              disabled={isLoading}
            />
          </View>
          <Text style={styles.statusText}>
            Status:{' '}
            {isConnected === null
              ? 'Unknown'
              : isConnected
                ? 'Connected'
                : 'Disconnected'}
          </Text>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Payment</Text>
          <View style={styles.inputContainer}>
            <Text>Amount (R$):</Text>
            <TextInput
              style={styles.input}
              value={amount}
              onChangeText={setAmount}
              keyboardType="decimal-pad"
              placeholder="0"
            />
          </View>

          <View style={styles.inputContainer}>
            <Text>Installments:</Text>
            <TextInput
              style={styles.input}
              value={installments}
              onChangeText={setInstallments}
              keyboardType="number-pad"
              placeholder="1"
            />
          </View>

          <View style={styles.paymentButtons}>
            <Button
              title="Credit"
              onPress={() => handlePayment(PagBankTransactionType.CREDIT)}
              disabled={isLoading}
            />
            <Button
              title="Debit"
              onPress={() => handlePayment(PagBankTransactionType.DEBIT)}
              disabled={isLoading}
            />
            <Button
              title="Pix"
              onPress={() => handlePayment(PagBankTransactionType.PIX)}
              disabled={isLoading}
            />
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Transaction Management</Text>
          <View style={styles.inputContainer}>
            <Text>Transaction ID:</Text>
            <TextInput
              style={styles.input}
              value={transactionId}
              onChangeText={setTransactionId}
              placeholder="Enter transaction ID"
            />
          </View>

          <View style={styles.row}>
            <Button
              title="Cancel Transaction"
              onPress={handleCancelTransaction}
              disabled={isLoading || !transactionId}
            />
            <Button
              title="Print Receipt"
              onPress={handlePrintReceipt}
              disabled={isLoading || !lastTransaction}
            />
          </View>
        </View>

        {lastTransaction && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Last Transaction</Text>
            <Text>ID: {lastTransaction.transactionId}</Text>
            <Text>Amount: R$ {lastTransaction.amount}</Text>
            <Text>Status: {lastTransaction.message}</Text>
            <Text>Card Brand: {lastTransaction.cardBrand || 'N/A'}</Text>
          </View>
        )}

        {isLoading && (
          <View style={styles.loadingOverlay}>
            <ActivityIndicator size="large" color="#0000ff" />
            <Text style={styles.loadingText}>Processing...</Text>
          </View>
        )}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  scrollContainer: {
    flexGrow: 1,
  },
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
  },
  section: {
    backgroundColor: 'white',
    borderRadius: 8,
    padding: 15,
    marginBottom: 15,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.2,
    shadowRadius: 1.5,
    elevation: 2,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 10,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: 10,
  },
  inputContainer: {
    marginBottom: 15,
  },
  input: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 4,
    padding: 8,
    marginTop: 5,
  },
  paymentButtons: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  statusText: {
    marginTop: 10,
    fontWeight: 'bold',
  },
  loadingOverlay: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 0,
    bottom: 0,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.7)',
  },
  loadingText: {
    marginTop: 10,
  },
});
