# react-native-pagbank-pos

## Modulo React Native e Expo com implementação do SDK PagBank para terminais smart POS. Para criação de aplicativos para maquininha de cartão de crédito da Pagbank.

Esta lib pode ser usado em React Native e Expo, está com a versão mais atual do SDK da PagBank.

Para mais informações sobre o SDK: https://developer.pagbank.com.br/docs/integracao-smartpos#suporte-smartpos

OBS> Os fluxos básicos estão funcionando porém ainda existem algumas implementações a serem feitas

Nossa ideia é constuir um modelo básico multiadquirente para facilitar a implementação em terminais smart POS.
Além de vários adquirentes e telas prontas o projeto conta com intefaces para impressoras térmicas, NFC, QrCode e muito mais.
Este modulo estará disponível no repo: https://github.com/siriustechsolutions/react-native-multi-pos.git

## Agradecimento

Este módulo se baseia no projeto do Bruno Azevedo, disponível em:
https://github.com/brunodsazevedo/react-native-pagseguro-plugpag.git

## Instalação

```sh
yarn add react-native-pagbank-pos
```

## Exemplo de uso

Exemplo de uso mais extensivo você pode encontrar na pasta [example] presente na raiz deste projeto

```js
import { PagBankPosSDK } from 'react-native-pagbank-pos';

const result = await PagBankPosSDK.initSDK('749879');
```

```js
const result = await PagBankPosSDK.makeTransaction({
  amount: 100,
  type,
  installments: 1,
  printReceipt: false,
  userReference: 'test',
  installmentType: 1,
});
```

## Eventos durantes uma transação

Acompanhe os status de atualização de uma tranção com uso de Listener, a cada atualização a adquirente notifica o status em tempo real
No código de exemplo vc pode obsevar melhor essa implementação.

```js
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
```

## Nesta versão a impressão será feita apenas através de uma string HTML, em uma futura terá impressão por uma imagem

# Configuração no Expo

Trabalhar com terminais smart POS demandam na maioria das vezes o manuseio de configuração de baixo nível no Android, isso inviabiliza o uso de desenvolvimento com Expo GO.
Portanto, você precisa usar expo-dev-client para expor a pasta android do seu projeto Expo.

TODO - Podemos melhorar isto no futuro:
Adicione o repo ao arquivo /android/build.gradle:

```xml
allprojects {
    repositories {
         maven { url "https://github.com/pagseguro/PlugPagServiceWrapper/raw/master" }
   }
}
```

## Dúvidas

Estamos a disposição para construir juntos ferramentas para soluções financeiras e facilitar a vida dos devs Brasileiros

Saiba mais sobre nós em [Sirius Tech](https://siriustechsolucoes.com)

## License

MIT
