testAllGerarMatrizQuadrosPorTempoDecorrido <- function(){
    colunasMatrizQuadros <- c('indice_inicial', 'indice_final', 'timestamp_inicial_nanos', 'timestamp_final_nanos', 'tempo_decorrido_nanos', 'timestamp_referencia_inicial_nanos', 'timestamp_referencia_final_nanos')
    
    timestampsEmNanos <- segundosParaNanos(c(10, 10.2, 10.4, 10.6, 10.8, 11, 11.2, 11.4, 11.6, 11.8, 12, 12.2))
    tempoQuadroEmSegundos <- 1
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos,
                                              tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    
    matrizQuadrosEsperada <- data.frame(matrix(c(1, 5, timestampsEmNanos[1], timestampsEmNanos[5], segundosParaNanos(0.8), timestampsEmNanos[1], timestampsEmNanos[1] + tempoQuadroEmNanos,
                                                 6, 10, timestampsEmNanos[6], timestampsEmNanos[10], segundosParaNanos(0.8), timestampsEmNanos[1] + tempoQuadroEmNanos, timestampsEmNanos[1] + 2 * tempoQuadroEmNanos,
                                                 11, 12, timestampsEmNanos[11], timestampsEmNanos[12], segundosParaNanos(0.2), timestampsEmNanos[1] + 2 * tempoQuadroEmNanos, timestampsEmNanos[1] + 3 * tempoQuadroEmNanos),
                                               ncol = 7, byrow = T))
    colnames(matrizQuadrosEsperada) <- colunasMatrizQuadros
    assertEquals(matrizQuadros,matrizQuadrosEsperada)

    timestampsEmNanos <- segundosParaNanos(c(10, 10.2, 10.4, 10.6, 10.8))
    tempoQuadroEmSegundos <- 1
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos,
                                                         tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    
    matrizQuadrosEsperada <- data.frame(matrix(c(1, 5, timestampsEmNanos[1], timestampsEmNanos[5], segundosParaNanos(0.8), timestampsEmNanos[1], timestampsEmNanos[1] + tempoQuadroEmNanos),
                                               ncol = 7, byrow = T))
    colnames(matrizQuadrosEsperada) <- colunasMatrizQuadros
    assertEquals(matrizQuadros, matrizQuadrosEsperada)

    timestampsEmNanos <- segundosParaNanos(c(10, 10.2, 10.4, 10.6, 10.8, 11))
    tempoQuadroEmSegundos <- 1
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos,
                                                         tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    matrizQuadrosEsperada <- data.frame(matrix(c(1, 5, timestampsEmNanos[1], timestampsEmNanos[5], segundosParaNanos(0.8), timestampsEmNanos[1], timestampsEmNanos[1] + tempoQuadroEmNanos,
                                                 6, 6, timestampsEmNanos[6], timestampsEmNanos[6], 0, timestampsEmNanos[1] + tempoQuadroEmNanos, timestampsEmNanos[1] + 2 * tempoQuadroEmNanos),
                                               ncol = 7, byrow = T))
    colnames(matrizQuadrosEsperada) <- colunasMatrizQuadros
    assertEquals(matrizQuadros, matrizQuadrosEsperada)

    timestampsEmNanos <- segundosParaNanos(c(10))
    tempoQuadroEmSegundos <- 1
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos,
                                                         tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    matrizQuadrosEsperada <- data.frame(matrix(c(1, 1, timestampsEmNanos[1], timestampsEmNanos[1], 0, timestampsEmNanos[1], timestampsEmNanos[1] + tempoQuadroEmNanos),
                                               ncol = 7, byrow = T))
    colnames(matrizQuadrosEsperada) <- colunasMatrizQuadros
    assertEquals(matrizQuadros, matrizQuadrosEsperada)

    print('testAllGerarMatrizQuadrosPorTempoDecorrido [ok]!')
}

testAllGerarSerieTemporal <- function() {
    # TODO adicionar os campos timestamp_referencia_inicial_janela_nanos e timestamp_referencia_final_janela_nanos
    
    #exemplo mínimo; uma linha no dataset de série temporal; nenhum valor descartado
    valoresDF <- data.frame(x = c(-3.713178, 2.50227, -3.859313, 2.850086),
                            y = c(-2.175138, 4.854654, 4.255229, -1.480787))
    timestampsEmNanos <- segundosParaNanos(seq(from = 14.4, length.out = 4, by = .26))
    tempoQuadroEmSegundos <- .5
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    df <- gerarSerieTemporal(valoresDF = valoresDF,
                             matrizQuadros = matrizQuadros,
                             numeroQuadrosNaJanela = 2)
    dfEsperado <- data.frame(
        x = "-3.713178;2.50227;-3.859313;2.850086",
        y = "-2.175138;4.854654;4.255229;-1.480787",
        timestamp_inicial_janela_nanos = timestampsEmNanos[1],
        timestamp_final_janela_nanos = timestampsEmNanos[4],
        timestamp_referencia_inicial_janela_nanos = timestampsEmNanos[1],
        timestamp_referencia_final_janela_nanos = timestampsEmNanos[1] + 2 * tempoQuadroEmNanos,
        quadro0 = c(2),
        stringsAsFactors = T
    )
    assertEquals(df, dfEsperado)

    #duas linhas geradas no dataset de série temporal; o último quadro (3) é considerado apesar de ter apenas uma leitura
    valoresDF <- data.frame(x = c(-2.6685851, -1.5695399, 0.9792393, 4.6533357, -4.8045769),
                            y = c(3.8574683, 3.6835135, 0.1458989, 0.6090835, 0.833571))

    timestampsEmNanos <- segundosParaNanos(seq(from = 12.2, length.out = 5, by = .26))
    tempoQuadroEmSegundos <- .5
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    df <- gerarSerieTemporal(valoresDF = valoresDF,
                             matrizQuadros = matrizQuadros,
                             numeroQuadrosNaJanela = 2)
    dfEsperado <- data.frame(
        x = c("-2.6685851;-1.5695399;0.9792393;4.6533357", "0.9792393;4.6533357;-4.8045769"),
        y = c("3.8574683;3.6835135;0.1458989;0.6090835", "0.1458989;0.6090835;0.833571"),
        timestamp_inicial_janela_nanos = c(timestampsEmNanos[1], timestampsEmNanos[3]),
        timestamp_final_janela_nanos = c(timestampsEmNanos[4], timestampsEmNanos[5]),
        timestamp_referencia_inicial_janela_nanos = c(timestampsEmNanos[1], timestampsEmNanos[1] + tempoQuadroEmNanos),
        timestamp_referencia_final_janela_nanos = c(timestampsEmNanos[1] + 2 * tempoQuadroEmNanos, timestampsEmNanos[1] + 3 * tempoQuadroEmNanos),
        quadro0 = c(2, 3),
        stringsAsFactors = T
    )
    assertEquals(df, dfEsperado)

    #duas linha geradas no dataset de série temporal
    valoresDF <- data.frame(x = c(0.48121224, 5.82063249, 0.08863779, 3.84619607, 2.79188671, -2.852482),
                            y = c(-0.2454653, -2.1115496, 3.1182963, 3.0749352, 2.1503486, 3.381357))

    timestampsEmNanos <- segundosParaNanos(seq(from = 0, length.out = 6, by = .26))
    tempoQuadroEmSegundos <- .5
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    df <- gerarSerieTemporal(valoresDF = valoresDF,
                             matrizQuadros = matrizQuadros,
                             numeroQuadrosNaJanela = 2)
    dfEsperado <- data.frame(
        x = c("0.48121224;5.82063249;0.08863779;3.84619607", "0.08863779;3.84619607;2.79188671;-2.852482"),
        y = c("-0.2454653;-2.1115496;3.1182963;3.0749352", "3.1182963;3.0749352;2.1503486;3.381357"),
        timestamp_inicial_janela_nanos = c(timestampsEmNanos[1], timestampsEmNanos[3]),
        timestamp_final_janela_nanos = c(timestampsEmNanos[4], timestampsEmNanos[6]),
        timestamp_referencia_inicial_janela_nanos = c(timestampsEmNanos[1], timestampsEmNanos[1] + tempoQuadroEmNanos),
        timestamp_referencia_final_janela_nanos = c(timestampsEmNanos[1] + 2 * tempoQuadroEmNanos, timestampsEmNanos[1] + 3 * tempoQuadroEmNanos),
        quadro0 = c(2, 3),
        stringsAsFactors = T
    )
    assertEquals(df, dfEsperado)

    #três linhas geradas no dataset de série temporal; o último quadro (4) é considerado apesar de ter apenas uma leitura
    valoresDF <- data.frame(x = c(0.48121224, 5.82063249, 0.08863779, 3.84619607, 2.79188671, -2.852482, 4.6617298),
                            y = c(-0.2454653, -2.1115496, 3.1182963, 3.0749352, 2.1503486, 3.381357, -0.8536558))

    timestampsEmNanos <- segundosParaNanos(seq(from = 13.4, length.out = 7, by = .26))
    tempoQuadroEmSegundos <- .5
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    df <- gerarSerieTemporal(valoresDF = valoresDF,
                             matrizQuadros = matrizQuadros,
                             numeroQuadrosNaJanela = 2)
    dfEsperado <- data.frame(
        x = c("0.48121224;5.82063249;0.08863779;3.84619607", "0.08863779;3.84619607;2.79188671;-2.852482", "2.79188671;-2.852482;4.6617298"),
        y = c("-0.2454653;-2.1115496;3.1182963;3.0749352", "3.1182963;3.0749352;2.1503486;3.381357","2.1503486;3.381357;-0.8536558"),
        timestamp_inicial_janela_nanos = c(timestampsEmNanos[1], timestampsEmNanos[3], timestampsEmNanos[5]),
        timestamp_final_janela_nanos = c(timestampsEmNanos[4], timestampsEmNanos[6], timestampsEmNanos[7]),
        timestamp_referencia_inicial_janela_nanos = c(timestampsEmNanos[1], timestampsEmNanos[1] + tempoQuadroEmNanos, timestampsEmNanos[1] + 2 * tempoQuadroEmNanos),
        timestamp_referencia_final_janela_nanos = c(timestampsEmNanos[1] + 2 * tempoQuadroEmNanos, timestampsEmNanos[1] + 3 * tempoQuadroEmNanos, timestampsEmNanos[1] + 4 * tempoQuadroEmNanos),
        quadro0 = c(2, 3, 4),
        stringsAsFactors = T
    )
    assertEquals(df, dfEsperado)

    # um erro deve ser lançado quando o último valor de 'matrizQuadros$indice_final' for diferente do tamanho de 'valoresDF'
    timestampsEmNanos <- segundosParaNanos(seq(from = 0, length.out = 5, by = .26))
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = .5)
    assertError(
      gerarSerieTemporal(valoresDF = data.frame(x = rep(1, 6)),
                         matrizQuadros = matrizQuadros,
                         numeroQuadros = 5)
    )

    # um erro deve ser lançado quando 'numeroQuadros' for maior que o número de linhas da matriz de quadros
    # a matriz de quadros tem apenas uma linha nesse caso
    timestampsEmNanos <- segundosParaNanos(seq(from = 0, length.out = 2, by = .26))
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = .5)
    assertError(
      gerarSerieTemporal(valoresDF = data.frame(x = rep(1, 2)),
                         matrizQuadros = matrizQuadros,
                         numeroQuadros = 2)
    )

    print('testAllGerarSerieTemporal [ok]!')
}

testAllGerarVetorAtributos <- function(){
    #exemplo mínimo; uma linha no vetor de características;
    valores <- c(1.07083415, -1.37361261, -1.62738346, -0.01703756)
    tempoQuadroEmSegundos <- .5
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    timestampsEmNanos <- segundosParaNanos(seq(from = 11.01, length.out = 4, by = .26))
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    df <- gerarVetorAtributos(valores = valores,
                              matrizQuadros = matrizQuadros,
                              numeroQuadrosNaJanela = 2)
    dfEsperado <- data.frame(media_1 = c(mean(valores[3:4])),
                             media_2 = c(mean(valores[1:4])),
                             mediana_1 = c(median(valores[3:4])),
                             mediana_2 = c(median(valores[1:4])),
                             desvio_padrao_1 = c(sd(valores[3:4])),
                             desvio_padrao_2 = c(sd(valores[1:4])),
                             div_medias_1 = c(mean(valores[1:2]) / mean(valores[3:4])),
                             timestamp_inicial_janela_nanos = timestampsEmNanos[1],
                             timestamp_final_janela_nanos = timestampsEmNanos[4],
                             timestamp_referencia_inicial_janela_nanos = timestampsEmNanos[1],
                             timestamp_referencia_final_janela_nanos = timestampsEmNanos[1] + 2 * tempoQuadroEmNanos,
                             quadro0 = c(2)
    )
    assertEquals(df, dfEsperado)

    #exemplo com apenas uma amostra por quadro. O dataframe fica vazio porque os desvios padrões do quadro0 são NA
    valores <- c(3.108758, -2.704760, 2.795429, -0.179769)
    tempoQuadroEmSegundos <- .5
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    timestampsEmNanos <- segundosParaNanos(seq(from = 11.01, length.out = 4, by = .51))
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    df <- gerarVetorAtributos(valores = valores,
                              matrizQuadros = matrizQuadros,
                              numeroQuadrosNaJanela = 2)
    dfEsperado <- data.frame(media_1 = c(mean(valores[2]), mean(valores[3]), mean(valores[4])),
                             media_2 = c(mean(valores[2:1]), mean(valores[3:2]), mean(valores[4:3])),
                             mediana_1 = c(median(valores[2]), median(valores[3]), median(valores[4])),
                             mediana_2 = c(median(valores[2:1]), median(valores[3:2]), median(valores[4:3])),
                             desvio_padrao_1 = c(sd(valores[2]), sd(valores[3]), sd(valores[4])),
                             desvio_padrao_2 = c(sd(valores[2:1]), sd(valores[3:2]), sd(valores[4:3])),
                             div_medias_1 = c(mean(valores[1]) / mean(valores[2]), mean(valores[2]) / mean(valores[3]), mean(valores[3]) / mean(valores[4])),
                             timestamp_inicial_janela_nanos = c(timestampsEmNanos[1], timestampsEmNanos[2], timestampsEmNanos[3]),
                             timestamp_final_janela_nanos = c(timestampsEmNanos[2], timestampsEmNanos[3], timestampsEmNanos[4]),
                             timestamp_referencia_inicial_janela_nanos = c(timestampsEmNanos[1], timestampsEmNanos[1] + tempoQuadroEmNanos, timestampsEmNanos[1] + 2 * tempoQuadroEmNanos),
                             timestamp_referencia_final_janela_nanos = c(timestampsEmNanos[1] + 2 * tempoQuadroEmNanos, timestampsEmNanos[1] + 3 * tempoQuadroEmNanos, timestampsEmNanos[1] + 4 * tempoQuadroEmNanos), 
                             quadro0 = c(2, 3, 4)
    )
    dfEsperado <- dfEsperado[complete.cases(dfEsperado), ]
    assertEquals(df, dfEsperado)

    # apenas uma linha gerada no vetor de características; a última posição (5) de 'valores' deve ser descartada
    valores <- c(-4.3763560, -2.8972525, 0.6426603, -2.1478947, -2.3762970)
    tempoQuadroEmSegundos <- .5
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    timestampsEmNanos <- segundosParaNanos(seq(from = 11.01, length.out = 5, by = .26))
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    df <- gerarVetorAtributos(valores = valores,
                              matrizQuadros = matrizQuadros,
                              numeroQuadros = 2)
    dfEsperado <- data.frame(media_1 = c(mean(valores[3:4])),
                             media_2 = c(mean(valores[1:4])),
                             mediana_1 = c(median(valores[3:4])),
                             mediana_2 = c(median(valores[1:4])),
                             desvio_padrao_1 = c(sd(valores[3:4])),
                             desvio_padrao_2 = c(sd(valores[1:4])),
                             div_medias_1 = c(mean(valores[1:2]) / mean(valores[3:4])),
                             timestamp_inicial_janela_nanos = timestampsEmNanos[1],
                             timestamp_final_janela_nanos = timestampsEmNanos[4],
                             timestamp_referencia_inicial_janela_nanos = c(timestampsEmNanos[1]),
                             timestamp_referencia_final_janela_nanos = c(timestampsEmNanos[1] + 2 * tempoQuadroEmNanos), 
                             quadro0 = c(2)
    )
    assertEquals(df, dfEsperado)

    #duas linhas geradas no vetor de características; nenhum valor descartado
    valores <- c(2.2015392, -2.5234932,  2.3721099, 1.0986186, -1.2946600, -0.1530714)
    tempoQuadroEmSegundos <- .5
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    timestampsEmNanos <- segundosParaNanos(seq(from = 0, length.out = 6, by = .26))
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    df <- gerarVetorAtributos(valores = valores,
                              matrizQuadros = matrizQuadros,
                              numeroQuadrosNaJanela = 2)
    dfEsperado <- data.frame(media_1 = c(mean(valores[3:4]), mean(valores[5:6])),
                             media_2 = c(mean(valores[1:4]), mean(valores[3:6])),
                             mediana_1 = c(median(valores[3:4]), median(valores[5:6])),
                             mediana_2 = c(median(valores[1:4]), median(valores[3:6])),
                             desvio_padrao_1 = c(sd(valores[3:4]), sd(valores[5:6])),
                             desvio_padrao_2 = c(sd(valores[1:4]), sd(valores[3:6])),
                             div_medias_1 = c(mean(valores[1:2]) / mean(valores[3:4]), mean(valores[3:4]) / mean(valores[5:6])),
                             timestamp_inicial_janela_nanos = c(timestampsEmNanos[1], timestampsEmNanos[3]),
                             timestamp_final_janela_nanos = c(timestampsEmNanos[4], timestampsEmNanos[6]),
                             timestamp_referencia_inicial_janela_nanos = c(timestampsEmNanos[1], timestampsEmNanos[1] + tempoQuadroEmNanos),
                             timestamp_referencia_final_janela_nanos = c(timestampsEmNanos[1] + 2 * tempoQuadroEmNanos, timestampsEmNanos[1] + 3 * tempoQuadroEmNanos), 
                             quadro0 = c(2, 3)
    )
    assertEquals(df, dfEsperado)

    #duas linhas gerados no vetor de características; nenhum valor descartado
    valores <- c(3.5021806, -1.7922186,  0.5998112, -0.6475368, -0.7271277, 3.8377756, 2.5668920, -3.1445447)
    tempoQuadroEmSegundos <- .5
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    timestampsEmNanos <- segundosParaNanos(seq(from = 0, length.out = 8, by = .26))
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    df <- gerarVetorAtributos(valores = valores,
                              matrizQuadros = matrizQuadros,
                              numeroQuadrosNaJanela = 3)
    dfEsperado <- data.frame(media_1 = c(mean(valores[5:6]), mean(valores[7:8])),
                             media_2 = c(mean(valores[3:6]), mean(valores[5:8])),
                             media_3 = c(mean(valores[1:6]), mean(valores[3:8])),
                             mediana_1 = c(median(valores[5:6]), median(valores[7:8])),
                             mediana_2 = c(median(valores[3:6]), median(valores[5:8])),
                             mediana_3 = c(median(valores[1:6]), median(valores[3:8])),
                             desvio_padrao_1 = c(sd(valores[5:6]), sd(valores[7:8])),
                             desvio_padrao_2 = c(sd(valores[3:6]), sd(valores[5:8])),
                             desvio_padrao_3 = c(sd(valores[1:6]), sd(valores[3:8])),
                             div_medias_1 = c(mean(valores[3:4]) / mean(valores[5:6]), mean(valores[5:6]) / mean(valores[7:8])),
                             div_medias_2 = c(mean(valores[1:2]) / mean(valores[5:6]), mean(valores[3:4]) / mean(valores[7:8])),
                             timestamp_inicial_janela_nanos = c(timestampsEmNanos[1], timestampsEmNanos[3]),
                             timestamp_final_janela_nanos = c(timestampsEmNanos[6], timestampsEmNanos[8]),
                             timestamp_referencia_inicial_janela_nanos = c(timestampsEmNanos[1], timestampsEmNanos[1] + tempoQuadroEmNanos),
                             timestamp_referencia_final_janela_nanos = c(timestampsEmNanos[1] + 3 * tempoQuadroEmNanos, timestampsEmNanos[1] + 4 * tempoQuadroEmNanos), 
                             quadro0 = c(3, 4)
    )
    assertEquals(df, dfEsperado)

    # um erro deve ser lançado quando o último valor de 'matrizQuadros$indice_final' for diferente do tamanho de 'valores'
    timestampsEmNanos <- segundosParaNanos(seq(from = 0, length.out = 5, by = .26))
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, .5)
    assertError(
        gerarVetorAtributos(valores = rep(1, 6),
                            matrizQuadros = matrizQuadros,
                            numeroQuadrosNaJanela = 5)
    )

    # um erro deve ser lançado quando 'numeroQuadrosNaJanela' for maior que o número de linhas da matriz de quadros
    # a matriz de quadros tem apenas uma linha nesse caso
    timestampsEmNanos <- segundosParaNanos(seq(from = 0, length.out = 2, by = .26))
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, .5)
    assertError(
        gerarVetorAtributos(valores = rep(1,2),
                            matrizQuadros = matrizQuadros,
                            numeroQuadrosNaJanela = 2)
    )
    
    print('testAllGerarVetorAtributos [ok]!')
}

testAllAdicionarTempoDecorridoEClasseAoDatasetTemporizado <- function(){
    eventoNaoAgressivo <- 'evento_nao_agressivo'
    
    # Exemplo mínimo: Data frame de retorno tem uma linha com classe.
    timestampsEmNanos <- segundosParaNanos(seq(from = 13.8, length.out = 4, by = .26))
    valores <- c(-7.619770, 8.474934, 5.304442, -5.131037)
    groundTruth <- data.frame(inicio = .3,
                              fim = 1.4,
                              evento = factor('evento1'))
    tempoQuadroEmSegundos <- .5
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    vetorAtributos <- gerarVetorAtributos(matrizQuadros = matrizQuadros,
                                          valores = valores,
                                          numeroQuadrosNaJanela = 2)
    df <- adicionarTempoDecorridoEClasseAoDatasetTemporizado(datasetTemporizado = vetorAtributos,
                                                             groundTruth = groundTruth,
                                                             classeSemEvento = eventoNaoAgressivo)
    eventosEsperados <- factor('evento1',
                               levels = c(levels(groundTruth$evento), eventoNaoAgressivo))
    assertEquals(df$evento, eventosEsperados)
    assertEquals(df$tempo_decorrido_inicio_janela_nanos, 0)
    assertEquals(df$tempo_decorrido_fim_janela_nanos, timestampsEmNanos[4] - timestampsEmNanos[1])
    assertEquals(df$tempo_decorrido_referencia_inicio_janela_nanos, 0)
    assertEquals(df$tempo_decorrido_referencia_fim_janela_nanos, 2 * tempoQuadroEmNanos)
    
    # Data frame de retorno tem uma linha. Todas tem classe NA
    timestampsEmNanos <- segundosParaNanos(seq(from = 11.75, length.out = 4, by = .26))
    valores <- c(-7.619770, 8.474934, 5.304442, -5.131037)
    groundTruth <- data.frame(inicio = numeric(),
                              fim = numeric(),
                              evento = factor())
    tempoQuadroEmSegundos <- .5
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    vetorAtributos <- gerarVetorAtributos(matrizQuadros = matrizQuadros,
                                          valores = valores,
                                          numeroQuadrosNaJanela = 2)
    assertEquals(nrow(vetorAtributos), 1)
    
    df <- adicionarTempoDecorridoEClasseAoDatasetTemporizado(datasetTemporizado = vetorAtributos,
                                                             groundTruth = groundTruth)
    
    eventosEsperados <- factor(NA, levels = levels(groundTruth$evento))
    assertEquals(df$evento, eventosEsperados)
    assertEquals(df$tempo_decorrido_inicio_janela_nanos, 0)
    assertEquals(df$tempo_decorrido_fim_janela_nanos, timestampsEmNanos[4] - timestampsEmNanos[1])
    assertEquals(df$tempo_decorrido_referencia_inicio_janela_nanos, 0)
    assertEquals(df$tempo_decorrido_referencia_fim_janela_nanos, 2 * tempoQuadroEmNanos)
    assertEquals(nrow(df[complete.cases(df), ]), 0)

    # Data frame de retorno tem duas linhas. A primeira tem classe. A segunda tem classe NA.
    timestampsEmNanos <- segundosParaNanos(seq(from = 20.587, length.out = 6, by = .26))
    valores <- c(1.6314553, -1.9153066, -3.8514030, -3.1667124, 0.7677005, 3.5514115)
    tempoQuadroEmSegundos <- .5
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    groundTruth <- data.frame(inicio = .3,
                              fim = 1.4,
                              evento = factor('evento1'))
    vetorAtributos <- gerarVetorAtributos(matrizQuadros = matrizQuadros,
                                          valores = valores,
                                          numeroQuadrosNaJanela = 2)
    assertEquals(nrow(vetorAtributos), 2)
    
    df <- adicionarTempoDecorridoEClasseAoDatasetTemporizado(datasetTemporizado = vetorAtributos, 
                                                             groundTruth = groundTruth)
    eventosEsperados <- factor(c('evento1', NA), levels = levels(groundTruth$evento))
    assertEquals(df$evento, eventosEsperados)
    assertEquals(df$tempo_decorrido_inicio_janela_nanos, c(0, timestampsEmNanos[3] - timestampsEmNanos[1]))
    assertEquals(df$tempo_decorrido_fim_janela_nanos, c(timestampsEmNanos[4] - timestampsEmNanos[1], timestampsEmNanos[6] - timestampsEmNanos[1]))
    assertEquals(df$tempo_decorrido_referencia_inicio_janela_nanos, c(0, tempoQuadroEmNanos))
    assertEquals(df$tempo_decorrido_referencia_fim_janela_nanos, c(2 * tempoQuadroEmNanos, 3 * tempoQuadroEmNanos))

    #dois vetores de atributo relativos ao mesmo evento de curta duração
    timestampsEmNanos <- segundosParaNanos(seq(from = 30.345, length.out = 9, by = .37))
    valores <- c(-3.2752850, -2.4992724, -2.0136436, 3.4203420, -1.2431899, -0.6673890, 1.5798869, 0.1665153, -2.466822)
    groundTruth <- data.frame(inicio = 1.7,
                              fim = 2.3,
                              evento = factor('evento1'))
    tempoQuadroEmSegundos <- 1
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    vetorAtributos <- gerarVetorAtributos(matrizQuadros = matrizQuadros,
                                          valores = valores,
                                          numeroQuadrosNaJanela = 2)
    df <- adicionarTempoDecorridoEClasseAoDatasetTemporizado(datasetTemporizado = vetorAtributos,
                                                             groundTruth = groundTruth,
                                                             classeSemEvento = eventoNaoAgressivo)
    eventosEsperados <- factor(rep('evento1', 2),
                               levels = c(levels(groundTruth$evento), eventoNaoAgressivo))
    assertEquals(df$evento, eventosEsperados)
    assertEquals(df$tempo_decorrido_inicio_janela_nanos, c(0, timestampsEmNanos[4] - timestampsEmNanos[1]))
    assertEquals(df$tempo_decorrido_fim_janela_nanos, c(timestampsEmNanos[6] - timestampsEmNanos[1], timestampsEmNanos[9] - timestampsEmNanos[1]))
    assertEquals(df$tempo_decorrido_referencia_inicio_janela_nanos, c(0, tempoQuadroEmNanos))
    assertEquals(df$tempo_decorrido_referencia_fim_janela_nanos, c(2 * tempoQuadroEmNanos, 3 * tempoQuadroEmNanos))

    #dois vetores de atributo; o primeiro não tem evento, por isso deve ser eventoNaoAgressivo; o segundo tem um evento
    timestampsEmNanos <- segundosParaNanos(seq(from = 12.56, length.out = 9, by = .37))
    valores <- c(-3.2752850, -2.4992724, -2.0136436, 3.4203420, -1.2431899, -0.6673890, 1.5798869, 0.1665153, -2.466822)
    groundTruth <- data.frame(inicio = 2.1,
                              fim = 2.8,
                              evento = factor('evento1'))
    tempoQuadroEmSegundos <- 1
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    vetorAtributos <- gerarVetorAtributos(matrizQuadros = matrizQuadros,
                                          valores = valores,
                                          numeroQuadrosNaJanela = 2)
    df <- adicionarTempoDecorridoEClasseAoDatasetTemporizado(datasetTemporizado = vetorAtributos,
                                                             groundTruth = groundTruth,
                                                             classeSemEvento = eventoNaoAgressivo)
    eventosEsperados <- factor(c(eventoNaoAgressivo, 'evento1'),
                               levels = c(levels(groundTruth$evento), eventoNaoAgressivo))
    assertEquals(df$evento, eventosEsperados)
    assertEquals(df$tempo_decorrido_inicio_janela_nanos, c(0, timestampsEmNanos[4] - timestampsEmNanos[1]))
    assertEquals(df$tempo_decorrido_fim_janela_nanos, c(timestampsEmNanos[6] - timestampsEmNanos[1], timestampsEmNanos[9] - timestampsEmNanos[1]))
    assertEquals(df$tempo_decorrido_referencia_inicio_janela_nanos, c(0, tempoQuadroEmNanos))
    assertEquals(df$tempo_decorrido_referencia_fim_janela_nanos, c(2 * tempoQuadroEmNanos, 3 * tempoQuadroEmNanos))

    #ground truth vazia (sem eventos)
    timestampsEmNanos <- segundosParaNanos(seq(from = 0, length.out = 9, by = .37))
    valores <- c(-3.2752850, -2.4992724, -2.0136436, 3.4203420, -1.2431899, -0.6673890, 1.5798869, 0.1665153, -2.466822)
    groundTruth <- data.frame(inicio = numeric(),
                              fim = numeric(),
                              evento = factor())
    tempoQuadroEmSegundos <- 1
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    vetorAtributos <- gerarVetorAtributos(matrizQuadros = matrizQuadros,
                                          valores = valores,
                                          numeroQuadrosNaJanela = 2)
    df <- adicionarTempoDecorridoEClasseAoDatasetTemporizado(datasetTemporizado = vetorAtributos,
                                                             groundTruth = groundTruth,
                                                             classeSemEvento = eventoNaoAgressivo)
    eventosEsperados <- factor(rep(eventoNaoAgressivo, 2),
                               levels = c(eventoNaoAgressivo))
    assertEquals(df$evento, eventosEsperados)
    assertEquals(df$tempo_decorrido_inicio_janela_nanos, c(0, timestampsEmNanos[4] - timestampsEmNanos[1]))
    assertEquals(df$tempo_decorrido_fim_janela_nanos, c(timestampsEmNanos[6] - timestampsEmNanos[1], timestampsEmNanos[9] - timestampsEmNanos[1]))
    assertEquals(df$tempo_decorrido_referencia_inicio_janela_nanos, c(0, tempoQuadroEmNanos))
    assertEquals(df$tempo_decorrido_referencia_fim_janela_nanos, c(2 * tempoQuadroEmNanos, 3 * tempoQuadroEmNanos))

    #3 vetores de atributo; o primeiro tem evento (evento1), o segundo e o terceiro têm o mesmo evento (evento2)
    timestampsEmNanos <- segundosParaNanos(seq(from = 0, length.out = 12, by = .34))
    valores <- c(-3.2753754, -0.4265788, -0.2527419, 1.3479937, 3.1670476, -1.1899581, -3.2375759, -4.0684608, 2.2366692, 0.6935911, -3.6041124, -2.3173387)
    groundTruth <- data.frame(inicio = c(0.3, 2.5),
                              fim = c(1.6, 3.2),
                              evento = factor(c('evento1', 'evento2')))
    tempoQuadroEmSegundos <- 1
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    vetorAtributos <- gerarVetorAtributos(matrizQuadros = matrizQuadros,
                                          valores = valores,
                                          numeroQuadrosNaJanela = 2)
    df <- adicionarTempoDecorridoEClasseAoDatasetTemporizado(datasetTemporizado = vetorAtributos,
                                                             groundTruth = groundTruth,
                                                             classeSemEvento = eventoNaoAgressivo)
    eventosEsperados <- factor(c('evento1', 'evento2', 'evento2'),
                               levels = c(levels(groundTruth$evento), eventoNaoAgressivo))
    assertEquals(df$evento, eventosEsperados)
    assertEquals(df$tempo_decorrido_inicio_janela_nanos, c(0, timestampsEmNanos[4] - timestampsEmNanos[1], timestampsEmNanos[7] - timestampsEmNanos[1]))
    assertEquals(df$tempo_decorrido_fim_janela_nanos, c(timestampsEmNanos[6] - timestampsEmNanos[1], timestampsEmNanos[9] - timestampsEmNanos[1], timestampsEmNanos[12] - timestampsEmNanos[1]))
    assertEquals(df$tempo_decorrido_referencia_inicio_janela_nanos, c(0, tempoQuadroEmNanos, 2 * tempoQuadroEmNanos))
    assertEquals(df$tempo_decorrido_referencia_fim_janela_nanos, c(2 * tempoQuadroEmNanos, 3 * tempoQuadroEmNanos, 4 * tempoQuadroEmNanos))

    #5 vetores de atributo; o primeiro e o segundo têm o mesmo evento1, o terceiro não tem, o quarto e o quinto têm o evento2
    timestampsEmNanos <- segundosParaNanos(seq(from = 0, length.out = 12, by = .26))
    valores <- c(-3.2753754, -0.4265788, -0.2527419, 1.3479937, 3.1670476, -1.1899581, -3.2375759, -4.0684608, 2.2366692, 0.6935911, -3.6041124, -2.3173387)
    groundTruth <- data.frame(inicio = c(0.53, 2.09),
                              fim = c(1.1, 2.7),
                              evento = factor(c('evento1', 'evento2')))
    tempoQuadroEmSegundos <- .5
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = tempoQuadroEmSegundos)
    vetorAtributos <- gerarVetorAtributos(matrizQuadros = matrizQuadros,
                                          valores = valores,
                                          numeroQuadrosNaJanela = 2)
    df <- adicionarTempoDecorridoEClasseAoDatasetTemporizado(datasetTemporizado = vetorAtributos,
                                                             groundTruth = groundTruth,
                                                             classeSemEvento = eventoNaoAgressivo)
    eventosEsperados <- factor(c('evento1', 'evento1', eventoNaoAgressivo, 'evento2', 'evento2'),
                               levels = c(levels(groundTruth$evento), eventoNaoAgressivo))
    assertEquals(df$evento, eventosEsperados)
    assertEquals(df$tempo_decorrido_inicio_janela_nanos, c(0, timestampsEmNanos[3] - timestampsEmNanos[1], timestampsEmNanos[5] - timestampsEmNanos[1], timestampsEmNanos[7] - timestampsEmNanos[1], timestampsEmNanos[9] - timestampsEmNanos[1]))
    assertEquals(df$tempo_decorrido_fim_janela_nanos, c(timestampsEmNanos[4] - timestampsEmNanos[1], timestampsEmNanos[6] - timestampsEmNanos[1], timestampsEmNanos[8] - timestampsEmNanos[1], timestampsEmNanos[10] - timestampsEmNanos[1], timestampsEmNanos[12] - timestampsEmNanos[1]))
    assertEquals(df$tempo_decorrido_referencia_inicio_janela_nanos, c(0, tempoQuadroEmNanos, 2 * tempoQuadroEmNanos, 3 * tempoQuadroEmNanos, 4 * tempoQuadroEmNanos))
    assertEquals(df$tempo_decorrido_referencia_fim_janela_nanos, c(2 * tempoQuadroEmNanos, 3 * tempoQuadroEmNanos, 4 * tempoQuadroEmNanos, 5 * tempoQuadroEmNanos, 6 * tempoQuadroEmNanos))

    #um evento que pertence aos dois primeiros vetores de atributos mesmo durando mais de .52 segundo (duração de um vetor de atributos no exemplo abaixo)
    timestampsEmNanos <- segundosParaNanos(seq(from = 0, length.out = 12, by = .26))
    valores <- c(-3.2753754, -0.4265788, -0.2527419, 1.3479937, 3.1670476, -1.1899581, -3.2375759, -4.0684608, 2.2366692, 0.6935911, -3.6041124, -2.3173387)
    groundTruth <- data.frame(inicio = c(.51), # antes do timestamp da primeira amostra do quadro, mas dentro do quadro que começa em 0.5
                              fim = c(2.61),
                              evento = factor('evento1'))
    tempoQuadroEmSegundos <- .5
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos, tempoQuadroEmSegundos = .5)
    vetorAtributos <- gerarVetorAtributos(matrizQuadros = matrizQuadros,
                                          valores = valores,
                                          numeroQuadrosNaJanela = 2)
    df <- adicionarTempoDecorridoEClasseAoDatasetTemporizado(datasetTemporizado = vetorAtributos,
                                                             groundTruth = groundTruth,
                                                             classeSemEvento = eventoNaoAgressivo)

    eventosEsperados <- factor(c('evento1', 'evento1', rep(eventoNaoAgressivo, 3)),
                               levels = c(levels(groundTruth$evento), eventoNaoAgressivo))

    assertEquals(df$evento, eventosEsperados)
    assertEquals(df$tempo_decorrido_inicio_janela_nanos, c(0, timestampsEmNanos[3] - timestampsEmNanos[1], timestampsEmNanos[5] - timestampsEmNanos[1], timestampsEmNanos[7] - timestampsEmNanos[1], timestampsEmNanos[9] - timestampsEmNanos[1]))
    assertEquals(df$tempo_decorrido_fim_janela_nanos, c(timestampsEmNanos[4] - timestampsEmNanos[1], timestampsEmNanos[6] - timestampsEmNanos[1], timestampsEmNanos[8] - timestampsEmNanos[1], timestampsEmNanos[10] - timestampsEmNanos[1], timestampsEmNanos[12] - timestampsEmNanos[1]))
    assertEquals(df$tempo_decorrido_referencia_inicio_janela_nanos, c(0, tempoQuadroEmNanos, 2 * tempoQuadroEmNanos, 3 * tempoQuadroEmNanos, 4 * tempoQuadroEmNanos))
    assertEquals(df$tempo_decorrido_referencia_fim_janela_nanos, c(2 * tempoQuadroEmNanos, 3 * tempoQuadroEmNanos, 4 * tempoQuadroEmNanos, 5 * tempoQuadroEmNanos, 6 * tempoQuadroEmNanos))

    #um evento que não pertence a nenhum vetor de atributos porque inicia depois do último valor de 'tempoEmNanos'
    timestampsEmNanos <- segundosParaNanos(seq(from = 0, length.out = 12, by = .26))
    valores <- c(-3.2753754, -0.4265788, -0.2527419, 1.3479937, 3.1670476, -1.1899581, -3.2375759, -4.0684608, 2.2366692, 0.6935911, -3.6041124, -2.3173387)
    groundTruth <- data.frame(inicio = c(7),
                              fim = c(7.9),
                              evento = factor('evento1'))
    tempoQuadroEmSegundos <- .5
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(tempoQuadroEmSegundos = tempoQuadroEmSegundos, timestampsEmNanos = timestampsEmNanos)
    vetorAtributos <- gerarVetorAtributos(matrizQuadros = matrizQuadros,
                                          valores = valores,
                                          numeroQuadrosNaJanela = 2)
    assertError(
        adicionarTempoDecorridoEClasseAoDatasetTemporizado(datasetTemporizado = vetorAtributos,
                                     groundTruth = groundTruth,
                                     classeSemEvento = eventoNaoAgressivo)
    )

    #um erro deve ser gerado caso uma mesma instância do vetor de atributos possua mais de uma classe
    timestampsEmNanos <- segundosParaNanos(seq(from = 0, length.out = 12, by = .26))
    valores <- c(-1.6571924, 0.4392839, 1.7199837, 3.6396095, -0.4249442, 1.3741800, 0.7700712, 0.9148667, 2.3104539, -2.6359775, -0.6948895, 1.9476458)
    groundTruth <- data.frame(inicio = c(1.7, 3.7),
                              fim = c(2.3, 5.1),
                              evento = factor(c('evento1', 'evento2')))
    tempoQuadroEmSegundos <- 1
    tempoQuadroEmNanos <- segundosParaNanos(tempoQuadroEmSegundos)
    matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(tempoQuadroEmSegundos = tempoQuadroEmSegundos, timestampsEmNanos = timestampsEmNanos)
    vetorAtributos <- gerarVetorAtributos(matrizQuadros = matrizQuadros,
                                          valores = valores,
                                          numeroQuadrosNaJanela = 2)

    assertError(
        adicionarTempoDecorridoEClasseAoDatasetTemporizado(datasetTemporizado = vetorAtributos,
                                                           groundTruth = groundTruth,
                                                           classeSemEvento = eventoNaoAgressivo)
    )
    
    print('testAllAdicionarTempoDecorridoEClasseAoDatasetTemporizado [ok]!')
}

testAll <- function(){
    testAllGerarVetorAtributos()
    testAllGerarSerieTemporal()
    testAllAdicionarTempoDecorridoEClasseAoDatasetTemporizado()
    testAllGerarMatrizQuadrosPorTempoDecorrido()
}

assertError <- function(expr){
    error <- F
    tryCatch(expr, error = function(e) {
        error <<- T
    })
    
    if(!error){
        stop('Erro esperado não ocorreu')
    }
}

assertNA <- function(v){
    if(!is.na(v)){
        stop(paste(v, ' não é NA'))    
    }
}

printDiferencas <- function(atual, esperado){
    print('atual:')
    print(atual)
    print('esperado:')
    print(esperado)
    print(all.equal(atual, esperado))
}

assertEquals <- function(atual, esperado){
    if(is.factor(atual) && is.factor(esperado)){
        if(!identical(atual, esperado)){
            printDiferencas(atual, esperado)
            stop('Fatores "atual" e "esperado" são diferentes')
        }
    }
    else if(is.data.frame(atual) && is.data.frame(esperado)){
        if(!identical(atual, esperado)){
            printDiferencas(atual, esperado)
            stop('Data frames "atual" e "esperado" são diferentes')
        }
    }
    else if(is.matrix(atual) && is.matrix(esperado)){
        if(dim(atual) != dim(esperado) || !all(atual == esperado)){
            printDiferencas(atual, esperado)
            stop('Matrizes "atual" e "esperado" são diferentes')
        }
    }
    else if(!all(atual == esperado)){
        printDiferencas(atual, esperado)
        stop(paste(atual, '!=', esperado))
    }
}