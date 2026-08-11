# Fluxo de execução em alto nível

## 1. Cadastro inicial do ambiente
- Cadastrar filiais.
- Cadastrar tipos de grão, incluindo preço de compra.
- Cadastrar caminhões, com placa e tara associada ao cadastro do caminhão.
- Cadastrar balanças, com um identificador único e um campo password que será usado como credencial de acesso na API.
- Cadastrar a doca, mantendo o saldo disponível por tipo de grão.

## 2. Geração da demanda de transporte
- Criar uma transação de transporte para representar a operação de um caminhão.
- Associar:
  - caminhão;
  - tipo de grão;
  - filial;
  - balança disponível.
- O sistema registra o início da operação e marca a transação como em andamento.

## 3. Início da pesagem no ponto de coleta
- O caminhão se dirige à balança.
- A balança inicia a leitura contínua do peso.
- A API recebe as leituras enviadas pela balança a cada 100 ms.
- Cada leitura deve conter:
  - identificador da balança;
  - placa do caminhão;
  - peso atual.

## 4. Autenticação da balança
- A API valida se o identificador da balança está cadastrado e autorizado.
- A autenticação deve validar também o password fornecido pela balança.
- Se a balança não for autorizada, a requisição deve ser rejeitada.
- O fluxo deve garantir que apenas balanças registradas e autenticadas possam enviar dados.

## 5. Detecção de estabilização do peso
- O sistema recebe uma sequência de leituras contínuas.
- Implementar lógica para identificar quando o peso estabilizou.
- A estabilização pode ser determinada por critérios como:
  - variação pequena entre medidas consecutivas;
  - permanência do peso dentro de uma faixa aceitável por um período curto.
- Quando a pesagem estiver estabilizada, o sistema marca a leitura final como válida.

## 6. Persistência da pesagem estabilizada
- Quando o peso for considerado estabilizado, o sistema deve persistir a pesagem consolidada.
- Armazenar:
  - placa;
  - peso bruto estabilizado;
  - peso líquido calculado a partir da tara do caminhão;
  - data e hora da pesagem;
  - balança;
  - tipo de grão;
  - custo da carga.
- A partir desse momento, a pesagem é considerada final.

## 7. Descarte de leituras posteriores
- Após a pesagem ser considerada estabilizada, quaisquer leituras adicionais recebidas pela mesma operação devem ser ignoradas.
- Isso evita que a mesma carga seja processada mais de uma vez.
- A lógica deve impedir reprocessamento de dados já estabilizados.

## 8. Atualização da doca
- A pesagem estabilizada deve contribuir para o saldo disponível na doca.
- O sistema deve atualizar a quantidade de grão disponível por tipo de grão.
- Esse saldo será usado para cálculo de disponibilidade e margem de lucro.

## 9. Cálculo de preço de venda e margem de lucro
- O preço de venda deve ser calculado a partir do preço de compra e da margem aplicada.
- A margem deve variar conforme a disponibilidade do grão na doca.
- Esse cálculo pode ser realizado em um componente/serviço Java.
- O resultado deve ser disponibilizado por um endpoint de relatório.

## 10. Venda parcial ou total do grão
- Quando uma quantidade de grão for vendida, o saldo disponível da doca deve ser reduzido.
- O sistema deve descontar o peso vendido do saldo disponível.
- O saldo restante continua disponível para futuras operações.

## 11. Liberação do caminhão para nova demanda
- Depois do descarregamento, o caminhão pode receber uma nova demanda de transporte.
- Para isso, a balança precisa voltar a zero.
- A balança deve enviar uma leitura com placa vazia, indicando que não há caminhão sobre ela.
- Esse evento marca o fim da operação anterior e libera o ciclo para uma nova pesagem.

## 12. Idempotência e prevenção de duplicidade
- Garantir que uma mesma carga não seja processada duas vezes.
- Não basta ignorar qualquer reenvio genérico, porque a balança envia leituras continuamente a cada 100 ms.
- O critério importante é impedir que o mesmo caminhão, com o mesmo tipo de grão, seja tratado como uma nova pesagem enquanto a transação anterior ainda não foi encerrada.
- A nova pesagem só deve ser considerada quando:
  - a transação de pesagem anterior foi finalizada;
  - a balança retornou a zero;
  - e a placa vazia foi recebida, sinalizando fim da operação anterior.
- Implementar controle de identidade da operação, por exemplo:
  - chave baseada em balança + placa + transação ativa;
  - ou marcação de pesagem já estabilizada e transação encerrada.
- O fluxo deve garantir consistência mesmo em retry ou reenvio de mensagens, sem criar uma nova operação indevida.

## 13. Fluxo resumido
- Cadastro de balança e caminhão.
- Criação da transação de transporte.
- Recebimento contínuo de leituras da balança.
- Validação da balança.
- Detecção de estabilização.
- Persistência da pesagem.
- Atualização da doca.
- Cálculo de margem/preço.
- Desconto de vendas parciais.
- Liberação do caminhão após balanço a zero.
- Garantia de idempotência.
