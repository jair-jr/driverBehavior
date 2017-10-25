gerarVetorAtributos <- function(valores, matrizQuadros, numeroQuadrosNaJanela){
    if(matrizQuadros$indice_final[nrow(matrizQuadros)] != length(valores)){
        stop(paste(seq = '', 'Erro: Último índice de matrizQuadros$indice_final (', matrizQuadros$indice_final[nrow(matrizQuadros)] ,') tem valor diferente do tamanho do vetor de valores (', length(valores), ')'))
    }
    
    nLinhas <- nrow(matrizQuadros) - numeroQuadrosNaJanela + 1
    if(nLinhas <= 0){
        stop(paste(sep = '', 'Erro: número de quadros na janela (', numeroQuadrosNaJanela, ') é maior que número de linhas da matriz de quadros (', nrow(matrizQuadros), ')'))
    }
    
    # número de colunas = média, mediana, desvio padrão e div medias (4) vezes 'numeroQuadrosNaJanela' - 1 (div médias tem uma coluna a menos que as demais)
    df <- data.frame(matrix(nrow = nLinhas, ncol = 4 * numeroQuadrosNaJanela - 1))
    names(df) <- c(sapply(1:numeroQuadrosNaJanela, function(num){paste('media_',num, sep = '')}),
                   sapply(1:numeroQuadrosNaJanela, function(num){paste('mediana_',num, sep = '')}),
                   sapply(1:numeroQuadrosNaJanela, function(num){paste('desvio_padrao_',num, sep = '')}),
                   sapply(1:(numeroQuadrosNaJanela - 1), function(num){paste('div_medias_',num, sep = '')}))
    
    q0 <- numeroQuadrosNaJanela
    
    df$timestamp_inicial_janela_nanos <- rep(NA, nLinhas)
    df$timestamp_final_janela_nanos <- rep(NA, nLinhas)
    df$timestamp_referencia_inicial_janela_nanos <- rep(NA, nLinhas)
    df$timestamp_referencia_final_janela_nanos <- rep(NA, nLinhas)
    df$quadro0 <- rep(NA, nLinhas)
    
    for(i in 1:nLinhas){
        mediaQ0 <- NA
        
        df$quadro0[i] <- q0
        
        # define os tempos iniciais e finais (contando todos os quadros)
        df$timestamp_inicial_janela_nanos[i] <- matrizQuadros$timestamp_inicial_nanos[q0 - numeroQuadrosNaJanela + 1]
        df$timestamp_final_janela_nanos[i] <- matrizQuadros$timestamp_final_nanos[q0]
        
        # define os tempos de referência iniciais e finais (contando todos os quadros)
        df$timestamp_referencia_inicial_janela_nanos[i] <- matrizQuadros$timestamp_referencia_inicial_nanos[q0 - numeroQuadrosNaJanela + 1]
        df$timestamp_referencia_final_janela_nanos[i] <- matrizQuadros$timestamp_referencia_final_nanos[q0]
        
        for(j in 1:numeroQuadrosNaJanela){
            indiceInicial <- matrizQuadros$indice_inicial[q0 - j + 1]
            indiceFinal <- matrizQuadros$indice_final[q0]
            
            df[i, j] <- mean(valores[indiceInicial:indiceFinal])
            df[i, j + numeroQuadrosNaJanela] <- median(valores[indiceInicial:indiceFinal])
            df[i, j + 2 * numeroQuadrosNaJanela] <- sd(valores[indiceInicial:indiceFinal])
            
            if(j == 1){
                mediaQ0 <- df[i,j]
            }
            
            if(j != numeroQuadrosNaJanela){
                linhaDiv <- q0 - j
                indiceInicialDiv <- matrizQuadros$indice_inicial[linhaDiv]
                indiceFinalDiv <- matrizQuadros$indice_final[linhaDiv]
                
                df[i, j + 3 * numeroQuadrosNaJanela] <- mean(valores[indiceInicialDiv:indiceFinalDiv]) / mediaQ0
            }
        }
        
        q0 <- q0 + 1
    }
    
    #complete.cases é necessário porque quando um quadro tiver apenas uma leitura, seu desvio padrão será NA.
    #Neste caso, todas as janelas que contêm esse quadro serão descartadas
    df[complete.cases(df), ]
}

gerarSerieTemporal <- function(valoresDF, matrizQuadros, numeroQuadrosNaJanela) {
    if(matrizQuadros$indice_final[nrow(matrizQuadros)] != nrow(valoresDF)){
      stop(paste(seq = '', 'Erro: Último índice de matrizQuadros$indice_final (', matrizQuadros$indice_final[nrow(matrizQuadros)] ,') tem valor diferente do tamanho do dataframe de valores (', nrow(valores), ')'))
    }
  
    nLinhas <- nrow(matrizQuadros) - numeroQuadrosNaJanela + 1
    if(nLinhas <= 0){
        stop(paste(sep = '', 'Erro: número de quadros na janela (', numeroQuadrosNaJanela, ') é maior que número de linhas da matriz de quadros (', nrow(matrizQuadros), ')'))
    }
    
    df <- data.frame(matrix(nrow = nLinhas, ncol = ncol(valoresDF)))
    names(df) <- names(valoresDF)
    
    df$timestamp_inicial_janela_nanos <- rep(NA, nLinhas)
    df$timestamp_final_janela_nanos <- rep(NA, nLinhas)
    df$timestamp_referencia_inicial_janela_nanos <- rep(NA, nLinhas)
    df$timestamp_referencia_final_janela_nanos <- rep(NA, nLinhas)
    df$quadro0 <- rep(NA, nLinhas)
    
    quadroInicial <- 1
    
    for(i in 1:nLinhas) {
        quadroFinal <- min(quadroInicial + numeroQuadrosNaJanela - 1, nrow(matrizQuadros))
        
        df$quadro0[i] <- quadroFinal

        indiceInicial <- matrizQuadros$indice_inicial[quadroInicial]
        indiceFinal <- matrizQuadros$indice_final[quadroFinal]

        # define os tempos iniciais e finais (contando todos os quadros)
        # df$timestamp_inicial_janela_nanos[i] <- temposDecorridosEmNanos[indiceInicial]
        # df$timestamp_final_janela_nanos[i] <- temposDecorridosEmNanos[indiceFinal]
        
        # define os tempos iniciais e finais (contando todos os quadros)
        df$timestamp_inicial_janela_nanos[i] <- matrizQuadros$timestamp_inicial_nanos[quadroInicial]
        df$timestamp_final_janela_nanos[i] <- matrizQuadros$timestamp_final_nanos[quadroFinal]
        
        # define os tempos de referência iniciais e finais (contando todos os quadros)
        df$timestamp_referencia_inicial_janela_nanos[i] <- matrizQuadros$timestamp_referencia_inicial_nanos[quadroInicial]
        df$timestamp_referencia_final_janela_nanos[i] <- matrizQuadros$timestamp_referencia_final_nanos[quadroFinal]

        for(j in 1:ncol(valoresDF)) {
            df[i, j] <- paste(valoresDF[indiceInicial:indiceFinal, j], collapse = ';')
        }

        quadroInicial <- quadroInicial + 1
    }
    
    for(col in 1:ncol(valoresDF)) {
        df[, col] <- factor(df[, col], ordered = F)
    }
    
    df
}

# retorna os campos temporais adicionados ao vetor de atributos e série temporal
camposTemporais <- function() {
  c(
    "timestamp_inicial_janela_nanos",
    "timestamp_final_janela_nanos",
    "timestamp_referencia_inicial_janela_nanos",
    "timestamp_referencia_final_janela_nanos",
    "quadro0",
    "tempo_decorrido_inicio_janela_nanos",
    "tempo_decorrido_fim_janela_nanos",
    "tempo_decorrido_referencia_inicio_janela_nanos",
    "tempo_decorrido_referencia_fim_janela_nanos"
  )
}

# adiciona:
#    - campos "tempo_decorrido_inicio_janela_nanos" e "tempo_decorrido_fim_janela_nanos" ao datasetTemporizado indicando o tempo decorrido 
# na primeira e última leituras da janela.
#    - campos "tempo_decorrido_referencia_inicio_janela_nanos" e "tempo_decorrido_referencia_fim_janela_nanos" ao datasetTemporizado 
# indicando os tempos decorridos de referência (independentes das primeira e última leituras) de início e fim da janela.
#    - a classe (campo "evento") ao dataframe "datasetTemporizado" que deve conter as colunas "timestamp_inicial_janela_nanos" e "timestamp_final_janela_nanos".
adicionarTempoDecorridoEClasseAoDatasetTemporizado <- function(datasetTemporizado, groundTruth, classeSemEvento = NA){
    nDatasetTemporizado <- nrow(datasetTemporizado)
    nGroundTruth <- nrow(groundTruth)
    
    datasetTemporizado$tempo_decorrido_inicio_janela_nanos <- datasetTemporizado$timestamp_inicial_janela_nanos - datasetTemporizado$timestamp_inicial_janela_nanos[1]
    datasetTemporizado$tempo_decorrido_fim_janela_nanos <- datasetTemporizado$timestamp_final_janela_nanos - datasetTemporizado$timestamp_inicial_janela_nanos[1]
    
    datasetTemporizado$tempo_decorrido_referencia_inicio_janela_nanos <- datasetTemporizado$timestamp_referencia_inicial_janela_nanos - datasetTemporizado$timestamp_referencia_inicial_janela_nanos[1]
    datasetTemporizado$tempo_decorrido_referencia_fim_janela_nanos <- datasetTemporizado$timestamp_referencia_final_janela_nanos - datasetTemporizado$timestamp_referencia_inicial_janela_nanos[1]
    
    datasetTemporizado$evento <- factor(rep(NA, nDatasetTemporizado), 
                                        levels = c(levels(groundTruth$evento), classeSemEvento))
    
    if(nGroundTruth > 0){
        inicioGroundTruthNanos <- segundosParaNanos(groundTruth$inicio)
        #fimGroundTruthNanos <- segundosParaNanos(groundTruth$fim)
        
        for (i in 1:nGroundTruth) {
            # o tempo de início referencial (tempo_decorrido_referencia_inicio_janela_nanos) e o tempo de fim real (tempo_decorrido_fim_janela_nanos) da janela são
            # usados para verificar se o evento pertence a essa janela
            filtro <- datasetTemporizado$tempo_decorrido_referencia_inicio_janela_nanos <= inicioGroundTruthNanos[i] & 
                datasetTemporizado$tempo_decorrido_fim_janela_nanos >= inicioGroundTruthNanos[i]
            if(!any(filtro)){
                print(groundTruth[i, ])
                stop('O evento de ground truth acima não pertence a nenhum vetor de atributos')
            }
            
            indicesJaClassificados <- which(!is.na(datasetTemporizado[filtro, 'evento']))
            if(length(indicesJaClassificados) > 0){
                print('Exemplo(s) já classificado(s):')
                print(datasetTemporizado[filtro, ][indicesJaClassificados, ])
                print('Evento de ground truth atual:')
                print(groundTruth[i, ])
                stop('Evento de ground truth já definido para o(s) exemplo(s). Exemplo(s) e evento atual exibidos acima')
            }
            
            datasetTemporizado$evento[filtro] <- groundTruth$evento[i]
        }
    }
    
    if(!is.na(classeSemEvento)){
        datasetTemporizado$evento[is.na(datasetTemporizado$evento)] <- as.factor(classeSemEvento)    
    }
    else{
        # TODO remover o comentário abaixo
         # datasetTemporizado <- datasetTemporizado[!is.na(datasetTemporizado$evento),]
    }
    datasetTemporizado
}

# retorna um dataframe que divide 'timestampsEmNanos' em diversos intervalos da 
# forma ['indice_inicial', 'indice_final', 'timestamp_inicial_nanos', 'timestamp_final_nanos', 'tempo_decorrido_nanos', 'timestamp_referencia_inicial_nanos', 'timestamp_referencia_final_nanos'].
# Onde 'timestamp_inicial_nanos' é o timestamp da primeira amostra do quadro; 'timestamp_final_nanos' é o timestamp da última amostra do quadro;
# 'timestamp_referencia_inicial_nanos' é o timestamp de referência do início do quadro; 'timestamp_referencia_final_nanos' é o timestamp de referência do fim do quadro.
# As seguintes regras são válidas para cada linha: 
#   tempo_decorrido_nanos = timestamp_final_nanos - timestamp_inicial_nanos e tempo_decorrido_nanos seja minimamente menor que 'tempoQuadroEmSegundos'
#   timestamp_referencia_inicial_nanos <= timestamp_inicial_nanos <= timestamp_final_nanos < timestamp_referencia_final_nanos
gerarMatrizQuadrosPorTempoDecorrido <- function(timestampsEmNanos, tempoQuadroEmSegundos){
    tempQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    timestampsLimites <- seq(from = timestampsEmNanos[1], 
                             to = timestampsEmNanos[length(timestampsEmNanos)], 
                             by = tempQuadroEmNanos)
    
    lengthTimestampsLimites <- length(timestampsLimites)
    matrizQuadros <- matrix(nrow = lengthTimestampsLimites, ncol = 7)
    
    matrizQuadros[, 6] <- timestampsLimites
    matrizQuadros[, 7] <- timestampsLimites + tempQuadroEmNanos
    
    iLinhaMatriz <- 0
    for(i in 1:lengthTimestampsLimites){
        iLinhaMatriz <- iLinhaMatriz + 1
        
        limiteInferior <- timestampsEmNanos >= timestampsLimites[i]
        if(i + 1 <= lengthTimestampsLimites){
            limiteSuperior <- timestampsEmNanos < timestampsLimites[i + 1]
        }
        else {
            limiteSuperior <- T
        }
        
        indicesSubconjuntoTimestamps <- which(limiteInferior & limiteSuperior)
        subconjuntoTimestamps <- timestampsEmNanos[indicesSubconjuntoTimestamps]
        
        matrizQuadros[iLinhaMatriz, 1] <- indicesSubconjuntoTimestamps[1]
        matrizQuadros[iLinhaMatriz, 2] <- tail(x = indicesSubconjuntoTimestamps, n = 1)
        matrizQuadros[iLinhaMatriz, 3] <- subconjuntoTimestamps[1]
        matrizQuadros[iLinhaMatriz, 4] <- tail(x = subconjuntoTimestamps, n = 1)
        matrizQuadros[iLinhaMatriz, 5] <- matrizQuadros[iLinhaMatriz, 4] - matrizQuadros[iLinhaMatriz, 3]
    }
    
    df <- data.frame(matrizQuadros[1:iLinhaMatriz, , drop = F])
    colnames(df) <- c('indice_inicial', 'indice_final', 'timestamp_inicial_nanos', 'timestamp_final_nanos', 'tempo_decorrido_nanos', 'timestamp_referencia_inicial_nanos', 'timestamp_referencia_final_nanos')
    df
}