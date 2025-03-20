# react-native-pagbank-pos

## Modulo React Native e Expo com implementação do SDK PagBank para terminais smart POS

Esta lib pode ser usado em React Native e Expo, está com a versão mais atual do SDK da PagBank.

Ainda existem algumas implementações a serem feitas

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
import { initSDK } from 'react-native-pagbank-pos';

const result = await initSDK('1234');
```

```js
const result = await makeTransaction({
  amount: 100,
  type,
  installments: 1,
  printReceipt: false,
  userReference: 'test',
  installmentType: 1,
});
```

## Nesta primeira versão a impressão será feita apenas através de uma string HTML

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
[Sirius Tech](https://siriustechsolucoes.com)

## License

MIT
