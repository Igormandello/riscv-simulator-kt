# MO601/MC973 - Projeto 2 - Um simulador simples do procesador RISC-V

https://www.ic.unicamp.br/~rodolfo/mo601/projeto2/

## Execução

Copie os arquivos de simulação na pasta `./test`, relativo à raíz do projeto.

Na raíz do projeto, execute (assumindo que os programas de simulação têm a extensão `.riscv`):

    docker build -t mo601-p2 .
    docker run -v ${PWD}/test:/mnt/test mo601-p2 -- "*.riscv"

Alternativamente, é possível pular o passo de construção da imagem usando uma imagem pré-construída:

    docker run -v ${PWD}/test:/mnt/test ghcr.io/guibrandt/riscv-simulator-kt:latest -- "*.riscv"

O programa gera arquivos de mesmo nome dos arquivos de programa simulados, substituindo a extensão por `.log`.

### Nível de logs da aplicação

O nível de log do programa pode ser ajustado com a variável de ambiente `LOG_LEVEL`:

    docker run -e LOG_LEVEL=DEBUG -v ${PWD}/test:/mnt/test mo601-p2 -- "*.riscv"

Valores aceitáveis são:
- `TRACE`: todos os logs, inclui logs de simulação
- `DEBUG`: logs de depuração, inclui descrição do processamento de argumentos da linha de comando e detalhes da 
decodificação dos arquivos ELF.
- `INFO`: logs informativos, descrevem a execução do programa em alto níveis.
- `WARN`: logs relacionados a problemas na execução.
- `ERROR`: logs relacionados a problemas graves na execução.
- `OFF`: todos os logs desligados.

Isso afeta apenas os logs enviados para o console. Os arquivos `.log` sempre incluem todos os logs de simulação, e
somente os logs de simulação.
