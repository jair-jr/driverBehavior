carregarDadosCaminhaoForaEstrada <- function(caminhoArquivoCsv){
    options(digits.secs = 3)
    g <- read.table(caminhoArquivoCsv, sep = ';',
             colClasses = c('character', 'factor', 'character', 'double', 'double', 'double', 'double', 'double', 'double', 'factor'),
             dec = ',', strip.white = T,
             na.strings=c('(vazio)', ''), header = T)
    g$Timestamp <- strptime(g$Timestamp, "%Y-%m-%d %H:%M:%OS")
    g
}

carregarDadosSensorSmartphone <- function(caminhoArquivoCsv){
    if(!file.exists(caminhoArquivoCsv)){
        return(NULL);
    }
    #options(digits.secs = 3) # formato antigo
    g <- read.table(caminhoArquivoCsv, sep = ',',
                    #colClasses = c('character', 'double', 'double', 'double', 'double', 'factor'),
                    colClasses = c('character', 'double', 'double', 'double', 'double'),
                    dec = '.', strip.white = T,
                    na.strings=c(''), header = T)
    #g$timestamp <- strptime(g$timestamp, "%Y-%m-%d %H:%M:%OS%z") # formato antigo
    g$timestamp <- strptime(g$timestamp, "%d/%m/%Y %H:%M:%S")
    g
}

carregarDadosGPSSmartphone <- function(caminhoArquivoCsv){
    if(!file.exists(caminhoArquivoCsv)){
        return(NULL);
    }
    #options(digits.secs = 3) # formato antigo
    g <- read.table(caminhoArquivoCsv, sep = ',',
                    colClasses = c('character', 'double', 'double', 'double', 'double', 'double', 'double', 'double', 'factor'),
                    dec = '.', strip.white = T,
                    na.strings=c(''), header = T)
    #g$timestamp <- strptime(g$timestamp, "%Y-%m-%d %H:%M:%OS%z") # formato antigo
    g$timestamp <- strptime(g$timestamp, "%d/%m/%Y %H:%M:%S")
    g
}

carregarDadosVetorRotacaoSmartphone <- function(caminhoArquivoCsv){
    if(!file.exists(caminhoArquivoCsv)){
        return(NULL);
    }
    #options(digits.secs = 3) # formato antigo
    vr <- read.table(caminhoArquivoCsv, sep = ',',
                    #colClasses = c('character', 'double', 'double', 'double', 'double', 'double', 'factor'),
                    colClasses = c('character', 'double', 'double', 'double', 'double', 'double'),
                    dec = '.', strip.white = T,
                    na.strings=c(''), header = T)
    # vr$timestamp <- strptime(vr$timestamp, "%Y-%m-%d %H:%M:%OS%z") # formato antigo
    vr$timestamp <- strptime(vr$timestamp, "%d/%m/%Y %H:%M:%S")
    vr
}

carregarGroundTruth <- function(caminhoArquivoCsv) {
    if(!file.exists(caminhoArquivoCsv)){
        return(NULL);
    }
    gt <- read.table(caminhoArquivoCsv, sep = ',',
                     colClasses = c('factor', 'double', 'double'),
                     dec = '.', strip.white = T,
                     na.strings=c(''), header = T)
    gt$delta <- gt$fim - gt$inicio
    gt
}

carregarNovosResultadosClassificadores <- function(caminhoArquivoCsv){
    df <- read.table(caminhoArquivoCsv, sep = ';',
               dec = '.', strip.white = T,
               na.strings = c(''),
               header = T,
               fileEncoding = 'UTF-8',
               encoding = 'UTF-8')
    df$timestamp <- strptime(df$timestamp, "%d/%m/%Y %H:%M:%S")
    df$earthSensor <- as.logical(df$earthSensor)
    df$seed <- as.factor(df$seed)
    df$instanceClassificationTimeInMillisAverage <- df$instanceClassificationTimeInNanosAverage * 1e-6
    df$instanceClassificationTimeInMillisStandardDeviation <- df$instanceClassificationTimeInNanosStandardDeviation * 1e-6
    
    levels(df$executionIdentifier) <- replaceKNNToIBK(levels(df$executionIdentifier))
    levels(df$identifier) <- replaceKNNToIBK(levels(df$identifier))
    
    df$coordinate <- factor(x = df$coordinate, levels = c(levels(df$coordinate), 'Todos'))
    df$coordinate[is.na(df$coordinate) & !is.na(df$sensor)] <- 'Todos'

    # reordena os levels do factor 'class' para que sejam iguais (tenham os mesmos índices) dos usados pelo weka)
    df$class <- factor(df$class, levels = strsplit(x = levels(df$classes), split = ',', fixed = T)[[1]])
    calcularEstatisticasClasseMatrizConfusao(df)
}

replaceKNNToIBK <- function(values){
    gsub(pattern = 'KNN_DTW', replacement = 'IBK_DTW', fixed = T, x = values)
}

calcularEstatisticasClasseMatrizConfusao <- function(resultadosDF){
    numberOfClasses <- length(levels(resultadosDF$class))
    classIndexes <- 1:numberOfClasses
    
    truePositives <- function(classIndex){
      m <- matrix(data = rep(F, numberOfClasses ^ 2), nrow = numberOfClasses)
      for(j in 1:numberOfClasses) {
        if (j == classIndex) {
          m[classIndex, j] <- T
        }
      }
      m
    }
    
    falsePositives <- function(classIndex){
      m <- matrix(data = rep(F, numberOfClasses ^ 2), nrow = numberOfClasses)
      for(i in 1:numberOfClasses) {
        if (i != classIndex) {
          for (j in 1:numberOfClasses) {
            if (j == classIndex) {
              m[i, j] <- T
            }
          }
        }
      }
      m
    }
    
    trueNegatives <- function(classIndex){
      m <- matrix(data = rep(F, numberOfClasses ^ 2), nrow = numberOfClasses)
      for(i in 1:numberOfClasses) {
        if (i != classIndex) {
          for (j in 1:numberOfClasses) {
            if (j != classIndex) {
              m[i, j] <- T
            }
          }
        }
      }
      m      
    }
    
    falseNegatives <- function(classIndex){
      m <- matrix(data = rep(F, numberOfClasses ^ 2), nrow = numberOfClasses)
      for(i in 1:numberOfClasses) {
        if (i == classIndex) {
          for (j in 1:numberOfClasses) {
            if (j != classIndex) {
              m[i, j] <- T
            }
          }
        }
      }
      m
    }
    
    trueNegativeFilters <- lapply(classIndexes, trueNegatives)
    falseNegativeFilters <- lapply(classIndexes, falseNegatives)
    truePositiveFilters <- lapply(classIndexes, truePositives)
    falsePositiveFilters <- lapply(classIndexes, falsePositives)
    
    require('parallel')
    # só 2 cores porque os processos criados estouram a memória
    cores <- 2
    confusionMatrices <- mclapply(X = strsplit(x = as.character(resultadosDF$confusionMatrix), split = ',', fixed = T),  
                                  FUN = function(matrixData) matrix(data = as.integer(matrixData), nrow = 7, byrow = T),
                                  mc.cores = cores)
    classes <- as.integer(resultadosDF$class)
    
    resultadosDF$classNumberOfTruePositives <- mcmapply(confusionMatrix = confusionMatrices, class = classes, 
                                                      FUN = function(confusionMatrix, class) sum(confusionMatrix[truePositiveFilters[[class]]]),
                                                      mc.cores = cores)
    resultadosDF$classNumberOfFalseNegatives <- mcmapply(confusionMatrix = confusionMatrices, class = classes, 
                                                       FUN = function(confusionMatrix, class) sum(confusionMatrix[falseNegativeFilters[[class]]]),
                                                       mc.cores = cores)
    resultadosDF$classNumberOfFalsePositives <- mcmapply(confusionMatrix = confusionMatrices, class = classes, 
                                                       FUN = function(confusionMatrix, class) sum(confusionMatrix[falsePositiveFilters[[class]]]),
                                                       mc.cores = cores)
    resultadosDF$classNumberOfTrueNegatives <- mcmapply(confusionMatrix = confusionMatrices, class = classes, 
                                                      FUN = function(confusionMatrix, class) sum(confusionMatrix[trueNegativeFilters[[class]]]),
                                                      mc.cores = cores)
    
    confusionMatrices <- NULL
    classes <- NULL
    gc()
    
    resultadosDF$classTotalPositives <- resultadosDF$classNumberOfTruePositives + resultadosDF$classNumberOfFalseNegatives
    resultadosDF$classTotalNegatives <- resultadosDF$classNumberOfTrueNegatives + resultadosDF$classNumberOfFalsePositives
    
    zeroIfNaNOrInf <- function(x){
      sapply(x, function(item){
        if(is.nan(item)){
          0
        }
        else if(is.infinite(item)){
          0
        }
        else {
          item
        }
      })
    }
    
    resultadosDF$classTrueNegativeRate <- zeroIfNaNOrInf(resultadosDF$classNumberOfTrueNegatives / resultadosDF$classTotalNegatives)
    resultadosDF$classFalseNegativeRate <- zeroIfNaNOrInf(resultadosDF$classNumberOfFalseNegatives / resultadosDF$classTotalPositives)
    
    resultadosDF$classAccuracy <- (resultadosDF$classNumberOfTruePositives + resultadosDF$classNumberOfTrueNegatives) / 
      (resultadosDF$classTotalPositives + resultadosDF$classTotalNegatives)
    
    incorrectTotals <- resultadosDF$classTotalPositives + resultadosDF$classTotalNegatives != resultadosDF$numberOfInstances
    if(sum(incorrectTotals) > 0){
        stop(paste('Total de positivos + total de negativos diferente do total de instâncias na linha', which(incorrectTotals)))
    }
    
    truePositiveRates <- zeroIfNaNOrInf(resultadosDF$classNumberOfTruePositives / resultadosDF$classTotalPositives)
    incorrectTruePositiveRates <- truePositiveRates != resultadosDF$classTruePositiveRate
    if(sum(incorrectTruePositiveRates) > 0){
      stop(paste('Recálculo de classTruePositiveRate diverge na linha', which(incorrectTruePositiveRates)))
    }
    
    falsePositiveRates <- zeroIfNaNOrInf(resultadosDF$classNumberOfFalsePositives / resultadosDF$classTotalNegatives)
    incorrectFalsePositiveRates <- falsePositiveRates != resultadosDF$classFalsePositiveRate
    if(sum(incorrectFalsePositiveRates) > 0){
      stop(paste('Recálculo de classFalsePositiveRate diverge na linha', which(incorrectFalsePositiveRates)))
    }
    
    incorrectNegativesRates <- (resultadosDF$classTrueNegativeRate + resultadosDF$classFalsePositiveRate) != 1
    if(sum(incorrectNegativesRates) > 0){
      stop(paste('classTrueNegativeRate + classFalsePositiveRate != 1 na linha', which(incorrectNegativesRates)))
    }

    incorrectPositivesRates <- (resultadosDF$classTruePositiveRate + resultadosDF$classFalseNegativeRate) != 1
    if(sum(incorrectPositivesRates) > 0){
      stop(paste('classTruePositiveRate + classFalseNegativeRate != 1 na linha', which(incorrectPositivesRates)))
    }

    resultadosDF
}

extrairResultadosGerais <- function(resultadosDF){
    df <- resultadosDF[, c('executionIdentifier', 'identifier', 'seed', 'crossValidationFold', 'timestamp', 
                           'sensor', 'earthSensor', 'coordinate', 
                           'classifier', 'classifierConfigId', 'classifierConfig', 
                           'numberOfCrossValidationFolds', 'numberOfFrames', 'classes', 'numberOfInstances', 
                           'correctInstancesRate', 'incorrectInstancesRate', 'unclassifiedInstancesRate', 'kappaStatistic', 'meanAbsoluteError', 'rootMeanSquaredError', 'relativeAbsoluteError', 'rootRelativeSquaredError', 'confusionMatrix', 
                           'weightedAreaUnderROC', 'weightedAreaUnderPRC', 'weightedMatthewsCorrelationCoefficient', 'weightedFMeasure', 'weightedTruePositiveRate', 'weightedFalsePositiveRate', 'weightedPrecision', 
                           'instanceClassificationTimeInNanosAverage', 'instanceClassificationTimeInNanosStandardDeviation', 'instanceClassificationTimeInMillisAverage', 'instanceClassificationTimeInMillisStandardDeviation')]
    df <- df[!duplicated(df), ]
    df
}

extrairResultadosPorClasse <- function(resultadosDF){
  df <- resultadosDF[, c('executionIdentifier', 'identifier', 'seed', 'classifier', 'sensor', 'class', 
                         'classAreaUnderROC', 'classAreaUnderPRC', 'classMatthewsCorrelationCoefficient', 'classFMeasure', 
                         'classTruePositiveRate', 'classFalseNegativeRate', 'classFalsePositiveRate', 'classTrueNegativeRate', 
                         'classAccuracy', 'classPrecision')]
  df
}

carregarResultadosClassificadores <- function(caminhoArquivoCsv){
    read.table(caminhoArquivoCsv, sep = ',',
                    dec = '.', strip.white = T,
                    na.strings = c(''),
                    header = T)
}

testeEstatistico <- function(resultadosDF, identificador1, identificador2, metrica){
  sequencia1 <- resultadosDF[resultadosDF$identifier == identificador1, metrica]
  sequencia2 <- resultadosDF[resultadosDF$identifier == identificador2, metrica]
  
  limiarPValorNormalidade <- 0.05
  sequencia1Normal <- shapiro.test(x = sequencia1)$p.value > limiarPValorNormalidade
  sequencia2Normal <- shapiro.test(x = sequencia2)$p.value > limiarPValorNormalidade
  
  if(sequencia1Normal && sequencia2Normal){
    t.test(x = sequencia1, y = sequencia2)
  }
  else{
    wilcox.test(x = sequencia1, y = sequencia2)
  }
}

# retorna resultadosGeraisDF apenas com registros dos 'top' melhores classificadores (considerando 'campoMetrica')
melhoresPorClassificador <- function(resultadosGeraisDF, campoMetrica, funcao = mean, top = 5){
    df <- aggregate(x = resultadosGeraisDF[, as.character(campoMetrica$campo)], by = list('identifier' = resultadosGeraisDF$identifier, 'classifier' = resultadosGeraisDF$classifier), FUN = funcao)
    
    bestIdentifiers <- unlist(use.names = F, x = sapply(split(df, df$classifier), function(dfByClassifier) {
        as.character(dfByClassifier[order(dfByClassifier$x, decreasing = campoMetrica$maiorMelhor)[1:min(top, nrow(dfByClassifier))], 'identifier'])
    }))

    bestAggregates <- df[df$identifier %in% bestIdentifiers, ]
    bestAggregatesInOrder <- bestAggregates[order(bestAggregates$x, decreasing = campoMetrica$maiorMelhor), ]
    resultadosGeraisDF <- resultadosGeraisDF[resultadosGeraisDF$identifier %in% bestAggregatesInOrder$identifier, ]
    resultadosGeraisDF$identifier <- factor(x = resultadosGeraisDF$identifier, levels = bestAggregatesInOrder$identifier, ordered = T)
    resultadosGeraisDF$classifier <- factor(x = resultadosGeraisDF$classifier, levels = unique(bestAggregatesInOrder$classifier), ordered = T)
    resultadosGeraisDF
}

# retorna resultadosGeraisDF apenas com registros dos 'top' melhores datasets (considerando 'campoMetrica')
# nova coluna 'dataset' (contendo combinação de sensor, coordenada(s) e número de quadros) é adicionada a resultadosGeraisDF
melhoresDatasets <- function(resultadosGeraisDF, campoMetrica, funcao = median, top = 5) {
    resultadosGeraisDF$dataset <- paste(sep = '', 
                                        'nq', 
                                        resultadosGeraisDF$numberOfFrames, 
                                        '_',
                                        resultadosGeraisDF$sensor, 
                                        ifelse(is.na(resultadosGeraisDF$coordinate), '', paste(sep = '', '_', resultadosGeraisDF$coordinate)))
    df <- aggregate(x = resultadosGeraisDF[, as.character(campoMetrica$campo)],
                    by = list('dataset' = resultadosGeraisDF$dataset,
                              'sensor' = resultadosGeraisDF$sensor),
                    FUN = funcao)
    bestAggregates <- df[order(df$x, decreasing = campoMetrica$maiorMelhor)[1:top], ]
    resultadosGeraisDF <- resultadosGeraisDF[resultadosGeraisDF$dataset %in% bestAggregates$dataset, ]
    resultadosGeraisDF$dataset <- factor(x = resultadosGeraisDF$dataset, levels = bestAggregates$dataset, ordered = T)
    resultadosGeraisDF$sensor <- factor(x = resultadosGeraisDF$sensor, levels = unique(bestAggregates$sensor), ordered = T)
    resultadosGeraisDF
}

carregarTodosResultadosClassificadores <- function(dirCsv){
    arquivos <- list.files(path = dirCsv)
    arquivos <- arquivos[!file.info(file.path(dirCsv,arquivos))$isdir]
    arquivosSemExtensao <- substr(arquivos, 1, nchar(arquivos) - 4)
    l = vector('list', length(arquivos))
    names(l) <- arquivosSemExtensao
    for(i in seq_along(l)){
        l[[i]] <- carregarResultadosClassificadores(file.path(dirCsv, arquivos[i]))
    }
    l
}

calcularElapsedUptimeNanos <- function(df){
    if(is.null(df)){
        return(NULL)
    }
    df$elapsedUptimeNanos <- with(df, uptimeNanos - df$uptimeNanos[1])
    df
}

calcularDeltas <- function(df, colunaFonte, colunaDestino){
    if(is.null(df)){
        return(NULL)
    }
    df[colunaDestino] <- c(NA, diff(df[,colunaFonte]))
    df
}

calcularAceleracao <- function(df){
    if(is.null(df)){
        return(NULL)
    }
    df$aceleracao <- c(NA, diff(df$speed) / (nanosParaSegundos(diff(df$elapsedUptimeNanos))))
    df
}

calcularMagnitudes <- function(df){
    if(is.null(df)){
        return(NULL)
    }
    df$magnitude <- with(df, sqrt(dx^2 + dy^2 + dz^2))
    df
}

calcularMediasMoveis <- function(df, colunaFonte, colunaDestino, n){
  if(is.null(df)){
    return(NULL)
  }
  df[colunaDestino] <- mediaMovel(df[,colunaFonte], n)
  df
}

calcularMedianasMoveis <- function(df, colunaFonte, colunaDestino, n){
  if(is.null(df)){
    return(NULL)
  }
  df[colunaDestino] <- medianaMovel(df[,colunaFonte], n)
  df
}

carregarEPreprocessarDadosSmartphone2 <- function(funcaoCarga, raizViagem, nomeArquivoCsv){
  file <- file.path(raizViagem, nomeArquivoCsv)
  if(!file.exists(file)){
    return(NULL)
  }
  
  df <- funcaoCarga(file)
  calcularElapsedUptimeNanos(df)
}

carregarEPreprocessarDadosSmartphone <- function(funcaoCarga, raizViagem, nomeArquivoCsv){
    file <- file.path(raizViagem, nomeArquivoCsv)
    if(!file.exists(file)){
        return(NULL)
    }

    df <- funcaoCarga(file)
    df <- calcularElapsedUptimeNanos(df)

    nAmostrasJanelaMovel <- 5
    if(eSensorXYZ(df)){
        df <- calcularDeltas(df, 'x', 'dx')
        df <- calcularDeltas(df, 'y', 'dy')
        df <- calcularDeltas(df, 'z', 'dz')

        df <- calcularMagnitudes(df)
        df <- calcularMediasMoveis(df, 'magnitude', 'mediaMovelMagnitude', nAmostrasJanelaMovel)
        df <- calcularMedianasMoveis(df, 'magnitude', 'medianaMovelMagnitude', nAmostrasJanelaMovel)

        df <- calcularMediasMoveis(df, 'x', 'mediaMovelX', nAmostrasJanelaMovel)
        df <- calcularMediasMoveis(df, 'y', 'mediaMovelY', nAmostrasJanelaMovel)
        df <- calcularMediasMoveis(df, 'z', 'mediaMovelZ', nAmostrasJanelaMovel)

        df <- calcularMedianasMoveis(df, 'x', 'medianaMovelX', nAmostrasJanelaMovel)
        df <- calcularMedianasMoveis(df, 'y', 'medianaMovelY', nAmostrasJanelaMovel)
        df <- calcularMedianasMoveis(df, 'z', 'medianaMovelZ', nAmostrasJanelaMovel)

        df <- calcularMediasMoveis(df, 'dx', 'mediaMovelDx', nAmostrasJanelaMovel)
        df <- calcularMediasMoveis(df, 'dy', 'mediaMovelDy', nAmostrasJanelaMovel)
        df <- calcularMediasMoveis(df, 'dz', 'mediaMovelDz', nAmostrasJanelaMovel)

        df <- calcularMedianasMoveis(df, 'dx', 'medianaMovelDx', nAmostrasJanelaMovel)
        df <- calcularMedianasMoveis(df, 'dy', 'medianaMovelDy', nAmostrasJanelaMovel)
        df <- calcularMedianasMoveis(df, 'dz', 'medianaMovelDz', nAmostrasJanelaMovel)
    }
    else if(eGPS(df)){ #gps
        df <- calcularAceleracao(df)
        df <- calcularMediasMoveis(df, 'aceleracao', 'mediaMovelAceleracao', nAmostrasJanelaMovel)
        df <- calcularMedianasMoveis(df, 'aceleracao', 'medianaMovelAceleracao', nAmostrasJanelaMovel)

        df <- calcularMediasMoveis(df, 'speed', 'mediaMovelVelocidade', nAmostrasJanelaMovel)
        df <- calcularMedianasMoveis(df, 'speed', 'medianaMovelVelocidade', nAmostrasJanelaMovel)

        # as médias e medianas móveis abaixo são desnecessárias
        #df <- calcularMediasMoveis(df, 'accuracy', 'mediaMovelAcuracia', nAmostrasJanelaMovel)
        #df <- calcularMedianasMoveis(df, 'accuracy', 'medianaMovelAcuracia', nAmostrasJanelaMovel)

        #df <- calcularMediasMoveis(df, 'bearing', 'mediaMovelBearing', nAmostrasJanelaMovel)
        #df <- calcularMedianasMoveis(df, 'bearing', 'medianaMovelBearing', nAmostrasJanelaMovel)

        #df <- calcularMediasMoveis(df, 'altitude', 'mediaMovelAltitude', nAmostrasJanelaMovel)
        #df <- calcularMedianasMoveis(df, 'altitude', 'medianaMovelAltitude', nAmostrasJanelaMovel)
    }
    else if(eVetorRotacao(df)){ #vetor de rotação
        df <- calcularMediasMoveis(df, 'componenteX', 'mediaMovelComponenteX', nAmostrasJanelaMovel)
        df <- calcularMedianasMoveis(df, 'componenteX', 'medianaMovelComponenteX', nAmostrasJanelaMovel)

        df <- calcularMediasMoveis(df, 'componenteY', 'mediaMovelComponenteY', nAmostrasJanelaMovel)
        df <- calcularMedianasMoveis(df, 'componenteY', 'medianaMovelComponenteY', nAmostrasJanelaMovel)

        df <- calcularMediasMoveis(df, 'componenteZ', 'mediaMovelComponenteZ', nAmostrasJanelaMovel)
        df <- calcularMedianasMoveis(df, 'componenteZ', 'medianaMovelComponenteZ', nAmostrasJanelaMovel)

        df <- calcularMediasMoveis(df, 'componenteEscalar', 'mediaMovelComponenteEscalar', nAmostrasJanelaMovel)
        df <- calcularMedianasMoveis(df, 'componenteEscalar', 'medianaMovelComponenteEscalar', nAmostrasJanelaMovel)
    }
    else{
        stop('Tipo de data frame não suportado')
    }

    df
}

eVetorRotacao <- function(df){
    'componenteX' %in% colnames(df)
}

eSensorXYZ <- function(df){
    'x' %in% colnames(df)
}

eGPS <- function(df){
    'speed' %in% colnames(df)
}

carregarTodosDadosSmartphone2 <- function(raizViagem){
  aceleracaoLinearTerra <- carregarEPreprocessarDadosSmartphone2(carregarDadosSensorSmartphone, raizViagem, 'aceleracaoLinear_terra.csv')
  acelerometroTerra <- carregarEPreprocessarDadosSmartphone2(carregarDadosSensorSmartphone, raizViagem, 'acelerometro_terra.csv')
  campoMagneticoTerra <- carregarEPreprocessarDadosSmartphone2(carregarDadosSensorSmartphone, raizViagem, 'campoMagnetico_terra.csv')
  giroscopioTerra <- carregarEPreprocessarDadosSmartphone2(carregarDadosSensorSmartphone, raizViagem, 'giroscopio_terra.csv')

  groundTruth <- carregarGroundTruth(file.path(raizViagem, 'groundTruth.csv'))
  
  l <- list(
    "aceleracaoLinearTerra" = aceleracaoLinearTerra,
    "acelerometroTerra" = acelerometroTerra,
    "campoMagneticoTerra" = campoMagneticoTerra,
    "giroscopioTerra" = giroscopioTerra,

    "groundTruth" = groundTruth
  )
  l[!sapply(l, is.null)]
}

carregarTodosDadosSmartphone <- function(raizViagem){
    aceleracaoLinear <- carregarEPreprocessarDadosSmartphone(carregarDadosSensorSmartphone, raizViagem, 'aceleracaoLinear.csv')
    acelerometro <- carregarEPreprocessarDadosSmartphone(carregarDadosSensorSmartphone, raizViagem, 'acelerometro.csv')
    campoMagnetico <- carregarEPreprocessarDadosSmartphone(carregarDadosSensorSmartphone, raizViagem, 'campoMagnetico.csv')
    giroscopio <- carregarEPreprocessarDadosSmartphone(carregarDadosSensorSmartphone, raizViagem, 'giroscopio.csv')
    gravidade <- carregarEPreprocessarDadosSmartphone(carregarDadosSensorSmartphone, raizViagem, 'gravidade.csv')
    vetorRotacao <- carregarEPreprocessarDadosSmartphone(carregarDadosVetorRotacaoSmartphone, raizViagem, 'vetorRotacao.csv')
    gps <- carregarEPreprocessarDadosSmartphone(carregarDadosGPSSmartphone, raizViagem, 'gps.csv')

    aceleracaoLinearTerra <- carregarEPreprocessarDadosSmartphone(carregarDadosSensorSmartphone, raizViagem, 'aceleracaoLinear_terra.csv')
    acelerometroTerra <- carregarEPreprocessarDadosSmartphone(carregarDadosSensorSmartphone, raizViagem, 'acelerometro_terra.csv')
    campoMagneticoTerra <- carregarEPreprocessarDadosSmartphone(carregarDadosSensorSmartphone, raizViagem, 'campoMagnetico_terra.csv')
    giroscopioTerra <- carregarEPreprocessarDadosSmartphone(carregarDadosSensorSmartphone, raizViagem, 'giroscopio_terra.csv')
    gravidadeTerra <- carregarEPreprocessarDadosSmartphone(carregarDadosSensorSmartphone, raizViagem, 'gravidade_terra.csv')

    groundTruth <- carregarGroundTruth(file.path(raizViagem, 'groundTruth.csv'))

    l <- list(
        "aceleracaoLinear" = aceleracaoLinear,
        "acelerometro" = acelerometro,
        "campoMagnetico" = campoMagnetico,
        "giroscopio" = giroscopio,
        "gravidade" = gravidade,
        "vetorRotacao" = vetorRotacao,
        "gps" = gps,

        "aceleracaoLinearTerra" = aceleracaoLinearTerra,
        "acelerometroTerra" = acelerometroTerra,
        "campoMagneticoTerra" = campoMagneticoTerra,
        "giroscopioTerra" = giroscopioTerra,
        "gravidadeTerra" = gravidadeTerra,
        #"vetorRotacaoTerra" = vetorRotacaoTerra,

        "groundTruth" = groundTruth
    )
    l[!sapply(l, is.null)]
}

taxasAmostragemPadrao <- function(){
    c(20, 15, 10)
}

plotarTodosGraficosTodosSensoresGroundTruth <- function(listaSensores,
                                                        margemSegundos = 2,
                                                        taxaAmostragemEmHz = taxasAmostragemPadrao(),
                                                        nomeListaSensores = deparse(substitute(listaSensores))){
    unlist(lapply(1:nrow(listaSensores$groundTruth), function(i){
        plotarTodosGraficosTodosSensores(listaSensores = listaSensores,
                                         segundoInicial = listaSensores$groundTruth$inicio[i] - margemSegundos,
                                         segundoFinal = listaSensores$groundTruth$fim[i] + margemSegundos,
                                         taxasAmostragemEmHz = taxaAmostragemEmHz,
                                         nomeListaSensores = nomeListaSensores)
    }))
}

plotarTodosGraficosTodosSensores <- function(listaSensores,
                                             segundoInicial = 0,
                                             segundoFinal = NA,
                                             taxasAmostragemEmHz = taxasAmostragemPadrao(),
                                             nomeListaSensores = deparse(substitute(listaSensores))){
    #library(doParallel)

    nomesSensores <- names(listaSensores)
    nomesSensores <- nomesSensores[nomesSensores != 'groundTruth']

    #cl <- makeCluster(2, type = 'FORK')
    #registerDoParallel(cl)

    #foreach(i = seq_along(listaSensores)) %dopar% {
    dirRaiz <- file.path('graficos', nomeListaSensores)
    unlist(lapply(seq_along(nomesSensores), function(i){
        nomeSensor <- nomesSensores[i]
        df <- subconjunto(listaSensores[[nomeSensor]], segundoInicial = segundoInicial, segundoFinal = segundoFinal)
        if(nomeSensor == 'gps'){
            plotter <- criarPlotter(df = df, groundTruth = listaSensores$groundTruth, abrirPNG = novosPNGs(file.path(dirRaiz, 'gps')))
            plotter$plotGPS()
        }
        else{
            plotarTodosGraficosSensorDownsample(df = df,
                                                groundTruth = listaSensores$groundTruth,
                                                nomeSensor = nomeSensor,
                                                taxasAmostragemEmHz = taxasAmostragemEmHz,
                                                abrirPNG = novosPNGs(file.path(dirRaiz, nomeSensor)))
        }
    }))

    #stopCluster(cl)
}

plotarTodosGraficosSensorDownsample <- function(df, groundTruth, nomeSensor, taxasAmostragemEmHz, abrirPNG = novosPNGs(nomeSensor)){
    # abrirPNG <- novosPNGs(nomeSensor)

    plotter <- criarPlotter(df, groundTruth = groundTruth, abrirPNG = abrirPNG)
    dfSensorXYZ <- eSensorXYZ(df)
    if(dfSensorXYZ){
        arquivosPNG <- c(plotter$plotMediaMedianaMoveisXYZ(nomeSensor, xyzJuntos = F),
                         plotter$plotMediaMedianaMoveisDxDyDz(nomeSensor),
                         plotter$plotMediaMedianaMoveisXYZ(nomeSensor, xyzJuntos = T))
        campos <- c('x','y','z','dx','dy','dz','magnitude')
    }
    else if(eVetorRotacao(df)){
        arquivosPNG <- c(plotter$plotMediaMedianaMoveisComponentesXYZEscalar(nomeSensor, xyzJuntos = F),
                         plotter$plotMediaMedianaMoveisComponentesXYZEscalar(nomeSensor, xyzJuntos = T))
        campos <- c('componenteX','componenteY','componenteZ','componenteEscalar')
    }
    else{
       stop('Tipo de data frame não suportado')
    }

    downsampledDfs <- downsampleParaTaxaAmostragemEmHz(df, taxasAmostragemEmHz,
                                                       campos = campos,
                                                       funcoesAgregacao = c(mean, median, max, min),
                                                       sufixosCamposAgregacao = c('media', 'mediana', 'max', 'min'))

    arquivosPNGDownsample <- unlist(lapply(seq_along(downsampledDfs), function(i){
        plotterDownsample <- criarPlotter(downsampledDfs[[i]],
                                          groundTruth = groundTruth,
                                          taxaAmostragem = taxasAmostragemEmHz[i],
                                          abrirPNG = abrirPNG)

        if(dfSensorXYZ){
            c(plotterDownsample$plotMaxMinMediaMedianaXYZSeparados(nomeSensor),
              plotterDownsample$plotMaxMinMediaMedianaDxDyDzMagnitude(nomeSensor),
              plotterDownsample$plotMediaMedianaXYZJuntos(nomeSensor))
        }
        else{
            c(plotterDownsample$plotMaxMinMediaMedianaComponentesXYZEscalarSeparados(nomeSensor),
              plotterDownsample$plotMediaMedianaComponentesXYZEscalarJuntos(nomeSensor))
        }
    }))

    c(arquivosPNG, arquivosPNGDownsample)
}

multipleColumnsRange <- function(df, colunas, ...){
  range(sapply(colunas, function(c) range(df[, c], ...)))
}

exportarTudo <- function(listaSensores){
    nomesSensores <- names(listaSensores)

    for(i in seq_along(listaSensores)){
        nomeSensor <- nomesSensores[i]
        exportarCSV(listaSensores[nomeSensor], paste(nomeSensor, '.csv', sep = ''))
    }
}

exportarCSV <- function(df, arquivo){
  options(digits.secs = 3)
  write.table(df, file = arquivo, na='',
              sep=',', row.names = F,
              quote = F, fileEncoding = 'UTF-8')
}

segundosParaNanos <- function(segundos){
  segundos * 1e9
}

nanosParaSegundos <- function(nanos){
  nanos * 1e-9
}

subconjunto <- function(df, segundoInicial = 0, segundoFinal = NA){
  nanoInicial = segundosParaNanos(segundoInicial)
  if(is.na(segundoFinal)){
    nanoFinal = tail(df, n = 1)$elapsedUptimeNanos
  }
  else{
    nanoFinal = segundosParaNanos(segundoFinal)
  }
  df[df$elapsedUptimeNanos >= nanoInicial & df$elapsedUptimeNanos <= nanoFinal,]
}

valorMovel <- function(d, nAmostras, f, ...){
  inicio <- 1
  fim <- nAmostras
  l <- length(d)
  mm <- rep(NA, l)

  while(fim <= l){
    mm[fim] = f(d[inicio:fim], ...)
    if(is.nan(mm[fim])){
        mm[fim] = NA
    }
    inicio = inicio + 1
    fim = fim + 1
  }

  mm
}

mediaMovel <- function(d, nAmostras){
  valorMovel(d, nAmostras, mean, na.rm = T)
}

medianaMovel <- function(d, nAmostras){
  valorMovel(d, nAmostras, median, na.rm = T)
}

taxaAmostragemEmHz <- function(v){
    vLength <- length(v)
    vLength / nanosParaSegundos(v[vLength] - v[1])
}

downsampleParaTaxaAmostragemEmHz <- function(df, taxasAmostragemEmHz, campos, funcoesAgregacao, sufixosCamposAgregacao){
    lapply(taxasAmostragemEmHz, function(taxa){
            taxaAmostragemOriginal <- taxaAmostragemEmHz(df$elapsedUptimeNanos)
            if(taxa >= taxaAmostragemOriginal){
                stop(paste('Taxa de amostragem solicitada (', taxa,
                           ' Hz) é maior ou igual à original (', taxaAmostragemOriginal,
                           ' Hz). Impossível realizar downsample.', sep = ''))
            }

            downsample(df = df, nAmostrasJanela = taxaAmostragemOriginal %/% taxa,
                       campos = campos, funcoesAgregacao = funcoesAgregacao,
                       sufixosCamposAgregacao = sufixosCamposAgregacao)
    })
}

downsample <- function(df, nAmostrasJanela, campos, funcoesAgregacao, sufixosCamposAgregacao){
  nSufixosCamposAgregacao <- length(sufixosCamposAgregacao)
  nFuncoesAgregacao <- length(funcoesAgregacao)
  if(nSufixosCamposAgregacao != nFuncoesAgregacao){
    stop('funcoesAgregacao e sufixosCamposAgregacao devem ser do mesmo tamanho')
  }

  nAmostrasJanela <- as.integer(nAmostrasJanela)
  if(nAmostrasJanela <= 1){
    stop(paste('nAmostrasJanela (', nAmostrasJanela,
               ') deve ser maior que 1', sep = ''))
  }

  nAmostrasOriginal <- nrow(df)
  nLinhas <- nAmostrasOriginal %/% nAmostrasJanela
  nCamposAgregados <- length(campos) * nFuncoesAgregacao
  camposAgregados <- data.frame(nome = rep(NA, times = nCamposAgregados),
                                funcaoAgregacao = rep(NA, times = nCamposAgregados),
                                campoOriginal = rep(NA, times = nCamposAgregados))

  ret <- data.frame(elapsedUptimeNanos = rep(NA,times = nLinhas))
  iCampoAgregado <- 1
  for(i in seq_along(campos)){
    for(j in seq_along(sufixosCamposAgregacao)){
      camposAgregados$nome[iCampoAgregado] <- paste(campos[i], sufixosCamposAgregacao[j], sep = '_')
      camposAgregados$funcaoAgregacao[iCampoAgregado] <- funcoesAgregacao[j]
      camposAgregados$campoOriginal[iCampoAgregado] <- campos[i]

      ret[camposAgregados$nome[iCampoAgregado]] <- rep(NA, times = nLinhas)

      iCampoAgregado <-iCampoAgregado + 1
    }
  }

  inicio <- 1
  rIndex <- 1

  nCampos <- length(campos)

  repeat{
    fim <- inicio + nAmostrasJanela - 1
    if(fim > nAmostrasOriginal){
      break
    }

    ret$elapsedUptimeNanos[rIndex] <- df$elapsedUptimeNanos[fim]
    for(i in 1:nCamposAgregados){
        #TODO verificar se ret[rIndex, camposAgregados$nome[i]] é infinito. Se for, defina como NA
      ret[rIndex, camposAgregados$nome[i]] <- camposAgregados$funcaoAgregacao[[i]](df[inicio:fim, camposAgregados$campoOriginal[i]])
    }

    rIndex <- rIndex + 1
    inicio <- fim + 1
  }

  ret
}

gerarDatasetSerieTemporal <- function(matrizQuadros, numeroQuadrosNaJanela, datasetSensor, campos, groundTruth) {
    serieTemporal <- gerarSerieTemporal(valoresDF = datasetSensor[, campos], 
                                        matrizQuadros = matrizQuadros,
                                        numeroQuadrosNaJanela = numeroQuadrosNaJanela)
    
    datasetSerieTemporal <- adicionarTempoDecorridoEClasseAoDatasetTemporizado(datasetTemporizado = serieTemporal, groundTruth = groundTruth)
    
    datasetSerieTemporal <- datasetSerieTemporal[!is.na(datasetSerieTemporal$evento), ]
    
    datasetSerieTemporal
}

gerarDatasetVetorAtributos <- function(matrizQuadros, numeroQuadrosNaJanela, datasetSensor, campos, groundTruth) {
    ultimoCampo <- campos[length(campos)]
    
    camposTemporaisVector <- camposTemporais()
    
    vetoresAtributos <- sapply(campos, USE.NAMES = F, simplify = F, function(campo){
        
        vetorAtributos <- gerarVetorAtributos(valores = datasetSensor[, campo],
                                              matrizQuadros = matrizQuadros,
                                              numeroQuadrosNaJanela = numeroQuadrosNaJanela)
        
        names(vetorAtributos) <- sapply(names(vetorAtributos), function(campoVetorAtributos) {
            if(campoVetorAtributos %in% camposTemporaisVector){
                campoVetorAtributos
            }
            else{
                paste(sep = '_', campo, campoVetorAtributos)
            }
        })
        
        if(ultimoCampo != campo){
            vetorAtributos[, camposTemporaisVector] <- NULL
        }
        
        vetorAtributos
    })
    
    vetorAtributosFinal <- adicionarTempoDecorridoEClasseAoDatasetTemporizado(datasetTemporizado = do.call(cbind, vetoresAtributos), groundTruth = groundTruth)
    vetorAtributosFinal <- vetorAtributosFinal[!is.na(vetorAtributosFinal$evento), ]
    
    vetorAtributosFinal
}

calcularMediasMoveisMultiplosCampos <- function(df, campos, nAmostras) {
    for(campo in campos) {
        df <- calcularMediasMoveis(df, colunaFonte = campo, colunaDestino = campo, n = nAmostras)
    }
    
    df
}

gerarDatasetsProcessados <- function(viagens, 
                                     dir = 'datasetsProcessados',
                                     tempoQuadroEmSegundos = 1,
                                     numerosQuadrosNaJanela = 7,
                                     numeroAmostrasMediaMovel = 25,
                                     camposSensorVetorRotacao = c('componenteX', 'componenteY', 'componenteZ', 'componenteEscalar'),
                                     camposSensor = c('x', 'y', 'z'),
                                     percentualDivisaoTreinoTeste){
    
    dirUnico <- criarDiretorioUnico(file.path(dir, 'datasets'))
    acumulador <- AcumuladorDataset()
    
    for(iV in seq_along(viagens)){
        viagem <- viagens[[iV]]
        sensores <- names(viagem)
        sensores <- sensores[sensores != 'gps' & sensores != 'groundTruth']
        groundTruth <- viagem$groundTruth
        
        # encontra o menor (limitesUptimeNanos[1]) e maior (limitesUptimeNanos[2]) uptimeNanos dentre todos os sensores utilizados nessa viagem
        minMaxUptimeNanos <- sapply(viagem[sensores], function(dataset) c(min(dataset$uptimeNanos), max(dataset$uptimeNanos)))
        limitesUptimeNanos <- c(min(minMaxUptimeNanos), max(minMaxUptimeNanos))
        
        for(sensor in names(viagem[sensores])){
            if(sensor == 'vetorRotacao'){
                campos <- camposSensorVetorRotacao
            }
            else if(sensor == 'campoMagneticoTerra'){
                # o eixo X sempre tem valores próximos de 0 e deve ser desconsiderado
                campos <- camposSensor[camposSensor != 'x']
            }
            else{
                campos <- camposSensor
            }
            
            datasetSensor <- viagem[[sensor]]
            datasetSensor <- calcularMediasMoveisMultiplosCampos(df = datasetSensor, campos = campos, nAmostras = numeroAmostrasMediaMovel)
            datasetSensor <- datasetSensor[, c('elapsedUptimeNanos', 'uptimeNanos', campos)]
            datasetSensor <- datasetSensor[complete.cases(datasetSensor), ]
            
            # temposDecorridosEmNanos <- datasetSensor$elapsedUptimeNanos
            
            # timestampsNanos original (datasetSensor$uptimeNanos) é alterado de forma que as janelas de todos os sensores iniciem e terminem no mesmo instante
            timestampsEmNanos <- c(limitesUptimeNanos[1], 
                                   datasetSensor$uptimeNanos[2:(length(datasetSensor$uptimeNanos) - 1)], 
                                   limitesUptimeNanos[2])
            
            matrizQuadros <- gerarMatrizQuadrosPorTempoDecorrido(timestampsEmNanos = timestampsEmNanos,
                                                                 tempoQuadroEmSegundos = tempoQuadroEmSegundos)
            
            for(numeroQuadrosNaJanela in numerosQuadrosNaJanela){
                identificadorBase <- paste(sensor, '_nq', numeroQuadrosNaJanela, sep = '')
                
                # dataset de série temporal
                datasetSerieTemporal <- gerarDatasetSerieTemporal(matrizQuadros = matrizQuadros,
                                                                  numeroQuadrosNaJanela = numeroQuadrosNaJanela,
                                                                  datasetSensor = datasetSensor,
                                                                  campos = campos,
                                                                  groundTruth = groundTruth)
                
                # dataset de vetor de atributos
                datasetVetorAtributos <- gerarDatasetVetorAtributos(matrizQuadros = matrizQuadros,
                                                                    numeroQuadrosNaJanela = numeroQuadrosNaJanela,
                                                                    datasetSensor = datasetSensor,
                                                                    campos = campos,
                                                                    groundTruth = groundTruth)
                
                if(nrow(datasetSerieTemporal) != nrow(datasetVetorAtributos)){
                    stop(paste(sep = '', 'Identificador base: ', identificadorBase, '. Número de elementos nos datasets de série temporal (', nrow(datasetSerieTemporal), ') e de vetor de atributos (', nrow(datasetVetorAtributos), ') diferem.'))
                }
                
                if(!all(datasetSerieTemporal$evento == datasetVetorAtributos$evento, na.rm = T) || !all(is.na(datasetSerieTemporal$evento) == is.na(datasetVetorAtributos$evento))){
                    print(paste('Eventos do dataset de série temporal: ', datasetSerieTemporal$evento))
                    print(paste('Eventos do dataset de vetor de atributos: ', datasetVetorAtributos$evento))
                    stop(paste(sep = '', 'Identificador base: ', identificadorBase, '. Atributos de classe (eventos acima) diferem nos datasets de série temporal e de vetor de atributos'))
                }
                
                datasetVetorAtributos$quadro0 <- paste(sep = '', 'v', iV, '_q', datasetVetorAtributos$quadro0)
                datasetSerieTemporal$quadro0 <- paste(sep = '', 'v', iV, '_q', datasetSerieTemporal$quadro0)

                # definir a mesma ordem de eventos para todos os datasets por causa do weka
                datasetSerieTemporal$evento <- tornarEventoFactorOrdinal(datasetSerieTemporal$evento)
                datasetVetorAtributos$evento <- tornarEventoFactorOrdinal(datasetVetorAtributos$evento)
                
                # acumular os datasets gerados com os já existentes de iterações anteriores
                acumulador$acumular(identificador = paste('st_', identificadorBase, sep = ''), 
                                    dataset = datasetSerieTemporal)
                
                acumulador$acumular(identificador = paste('va_', identificadorBase, sep = ''), 
                                    dataset = datasetVetorAtributos)
            }
        }
    }
    
    # escreve o dataset nos formatos csv e arff (weka)
    escreverDataset <- function(dataset, arquivoDatasetSemExtensao){
        camposTemporaisSemQuadro0 <- camposTemporais()
        camposTemporaisSemQuadro0 <- camposTemporaisSemQuadro0[camposTemporaisSemQuadro0 != 'quadro0']
        camposAEscreverNosArquivos <- names(dataset)[!(names(dataset) %in% camposTemporaisSemQuadro0)]
        
        dataset <- dataset[, camposAEscreverNosArquivos]
        
        escreverDatasetProcessadoComoCSV(datasetProcessado = dataset, arquivoSemExtensao = arquivoDatasetSemExtensao)
        escreverDatasetProcessadoComoARFF(datasetProcessado = dataset, arquivoSemExtensao = arquivoDatasetSemExtensao)
    }
    
    getArquivoDatasetSemExtensao <- function(identificadorDataset) {
        file.path(dirUnico, identificadorDataset)
    }
    
    acumularEEscreverDataset <- function(dataset, identificadorDataset, dropLevels){
        if(dropLevels){
            # remove níveis não utilizados dos fatores do dataframe, exceto do campo evento
            dataset <- droplevels(x = dataset, except = which(names(dataset) == 'evento'))
        }
        acumulador$acumular(identificador = identificadorDataset, dataset = dataset)
        escreverDataset(dataset = dataset, arquivoDatasetSemExtensao = getArquivoDatasetSemExtensao(identificadorDataset = identificadorDataset))
    }
    
    identificadores <- names(acumulador$getDatasetsPorIdentificador())
    datasetsVetorAtributosPorIdentificador <- identificadores[substring(text = identificadores, first = 1, last = 3) == 'va_']
    
    normMin <- -1
    normMax <- 1
    
    for(identificadorVetorAtributos in datasetsVetorAtributosPorIdentificador){
        datasetVetorAtributos <- acumulador$getDatasetsPorIdentificador()[[identificadorVetorAtributos]]
        
        identificadorSerieTemporal <- paste(sep = '', 'st', substring(text = identificadorVetorAtributos, first = 3))
        datasetSerieTemporal <- acumulador$getDatasetsPorIdentificador()[[identificadorSerieTemporal]]
        
        mapMinMaxVetorAtributos <- mapMinMax.vetorAtributos(vetorAtributosDF = datasetVetorAtributos, min = normMin, max = normMax)
        acumulador$acumular(identificador = paste(sep = '_', identificadorVetorAtributos, 'normalizado'), 
                            dataset = mapMinMaxVetorAtributos$normalizedDF)
        acumulador$acumular(identificador = paste(sep = '_', identificadorVetorAtributos, 'minMax'), 
                            dataset = mapMinMaxVetorAtributos$minMaxDF)
        
        mapMinMaxSerieTemporal <- mapMinMax.serieTemporal(serieTemporalDF = datasetSerieTemporal, min = normMin, max = normMax)
        acumulador$acumular(identificador = paste(sep = '_', identificadorSerieTemporal, 'normalizado'), 
                            dataset = mapMinMaxSerieTemporal$normalizedDF)
        acumulador$acumular(identificador = paste(sep = '_', identificadorSerieTemporal, 'minMax'), 
                            dataset = mapMinMaxSerieTemporal$minMaxDF)

        escreverDataset(dataset = mapMinMaxVetorAtributos$normalizedDF, arquivoDatasetSemExtensao = getArquivoDatasetSemExtensao(identificadorDataset = identificadorVetorAtributos))
        escreverDataset(dataset = mapMinMaxSerieTemporal$normalizedDF, arquivoDatasetSemExtensao = getArquivoDatasetSemExtensao(identificadorDataset = identificadorSerieTemporal))

        # TODO atualmente a divisão do dataset em treino e teste é feita por sensor. 
        # Logo, sensores diferentes terão datasets diferentes em função do campo quadro0.
        # Idealmente, os datasets de treino e teste dos sensores com menor número de quadros 
        # por janela devem estar contidos, respectivamente, nos datasets de treino e teste 
        # dos sensores com maior número de quadros.
        # Para isso, é preciso dividir os datasets de treino e teste fora deste laço, usando 
        # o dataset com maior número de linhas. Isso gera dois conjuntos (treino e teste) de quadro0: quadro0_treino e quadro0_teste.
        # Para dividir os demais datasets, basta: 
        #   datasetAtualTreino <- datasetAtual[datasetAtual$quadro0 %in% quadro0_treino, ]
        #   datasetAtualTeste <- datasetAtual[datasetAtual$quadro0 %in% quadro0_teste, ]
        # Melhor, bastaria um dataset de treino ou teste (quadro0_treino ou quadro0_teste) porque eles são mutuamente exclusivos.
        # Então ficaria:
        #   trainIndex <- datasetAtual$quadro0 %in% quadro0_treino
        #   datasetAtualTreino <- datasetAtual[trainIndex, ]
        #   datasetAtualTeste <- datasetAtual[-trainIndex, ]
        #
        # TODO Quanto a normalização: o dataset de teste deve ser normalizado com os mínimos e máximos do dataset de treino
        # conforme explicado em "A Practical Guide to Support Vector Classificatio", de Chih-Wei Hsu, Chih-Chung Chang e Chih-Jen Lin
        if(!is.na(percentualDivisaoTreinoTeste)){
            require('caret')
          
            # podemos usar o trainIndex de um dataset de vetor de atributos em outro de série temporal, desde que seja do mesmo sensor e quantidade de quadros.
            # na verdade, os dois datasets (vetor de atributos e série temporal) representam processamentos diferentes da mesma janela de tempo
            trainIndex <- createDataPartition(datasetVetorAtributos$evento, p = percentualDivisaoTreinoTeste, list = F)
            
            acumularEEscreverDataset(dataset = datasetVetorAtributos[trainIndex, ], identificadorDataset = paste(sep = '', identificadorVetorAtributos, '_train'), dropLevels = F)
            acumularEEscreverDataset(dataset = datasetVetorAtributos[-trainIndex, ], identificadorDataset = paste(sep = '', identificadorVetorAtributos, '_test'), dropLevels = F)

            acumularEEscreverDataset(dataset = datasetSerieTemporal[trainIndex, ], identificadorDataset = paste(sep = '', identificadorSerieTemporal, '_train'), dropLevels = T)
            acumularEEscreverDataset(dataset = datasetSerieTemporal[-trainIndex, ], identificadorDataset = paste(sep = '', identificadorSerieTemporal, '_test'), dropLevels = T)
        }
        
    }
    
    acumulador$getDatasetsPorIdentificador()
}

escreverDatasetProcessadoComoCSV <- function(datasetProcessado, arquivoSemExtensao) {
    write.table(datasetProcessado, 
                paste(arquivoSemExtensao, '.csv', sep = ''),
                quote = F,
                row.names = F,
                sep = ',',
                na = '')
}

escreverDatasetProcessadoComoARFF <- function(datasetProcessado, arquivoSemExtensao) {
    require('foreign')
    
    write.arff(x = datasetProcessado,
               file = paste(arquivoSemExtensao, '.arff', sep = ''), 
               relation = basename(arquivoSemExtensao))
}

AcumuladorDataset <- function(){
    datasetsPorIdentificador <- list()
    
    getDatasetsPorIdentificador <- function(){
        datasetsPorIdentificador
    }
    
    # adiciona as linhas de "dataset" ao dataset já existente para o "identificador"
    acumular <- function(identificador, dataset){
        datasetCombinado <- datasetsPorIdentificador[[identificador]]
        if(!is.null(datasetCombinado)){
            datasetCombinado <- rbind(datasetCombinado, dataset)
        }
        else{
            datasetCombinado <- dataset
        }
        
        datasetsPorIdentificador[[identificador]] <<- datasetCombinado
    }
    
    substituir <- function(identificador, dataset) {
        datasetCombinado <- datasetsPorIdentificador[[identificador]]
        datasetsPorIdentificador[[identificador]] <- dataset
        datasetCombinado
    }
    
    list("getDatasetsPorIdentificador" = getDatasetsPorIdentificador,
         "acumular" = acumular)
}

# retorna uma amostra do dataset identificado por 'idDataset'. Útil para criar 
# testes que precisam de um subconjunto do dataset original.
# cria o arquivo ARFF e retorna o dataset
sampleForTests <- function(idDataset, datasetsProcessados, numeroLinhas) {
    camposTemporaisVector <- camposTemporais()
    camposTemporaisVector <- camposTemporaisVector[camposTemporaisVector != 'quadro0']
    
    dataset <- datasetsProcessados[[idDataset]]
    dataset <- dataset[, !names(dataset) %in% camposTemporaisVector]
    dataset <- droplevels(x = dataset[sample(nrow(dataset), numeroLinhas), ], except = which(names(dataset) == 'evento'))
    
    escreverDatasetProcessadoComoARFF(dataset, arquivoSemExtensao = idDataset)
    
    dataset
}

lerCSVsPredicoes <- function(predictionsDir, classLevels) {
    sapply(list.files(predictionsDir), USE.NAMES = T, simplify = F, function(predictionsFile){
        predictionsFile <- file.path(predictionsDir, predictionsFile)
        lerCSVPredicoes(csvPredicoes = predictionsFile, classLevels = classLevels)
    })
}

lerCSVPredicoes <- function(csvPredicoes, classLevels){
    predicoesDF <- read.csv(file = csvPredicoes, header = T, sep = ';')
    colunaClassificador <- 3
    
    classLevels <- tolower(classLevels)
    
    levels(predicoesDF[, colunaClassificador]) <- tolower(levels(predicoesDF[, colunaClassificador]))
    predicoesDF[, colunaClassificador] <- factor(x = predicoesDF[, colunaClassificador], levels = classLevels)
    
    levels(predicoesDF$actualClass) <- tolower(levels(predicoesDF$actualClass))
    predicoesDF$actualClass <- factor(x = predicoesDF$actualClass, levels = classLevels)
    predicoesDF
}

converterIdentificadoresDeHumanoParaReal <- function(ids){
    ids <- gsub(pattern = 'Mag_', replacement = 'MagE_', fixed = T, x = ids)
    ids <- gsub(pattern = 'AclLin_', replacement = 'AclLinE_', fixed = T, x = ids)
    ids <- gsub(pattern = 'Gir_', replacement = 'GirE_', fixed = T, x = ids)
    ids <- gsub(pattern = 'Acel_', replacement = 'AcelE_', fixed = T, x = ids)
    ids <- gsub(pattern = 'nq', replacement = 'f', fixed = T, x = ids)
    ids
}

getIdentificadoresMelhoresModelos <- function(){
  ids <- c('nq8_Gir_RF5', 'nq8_Acel_MLP1', 'nq8_Gir_BN1', 'nq8_Acel_SVM16', # AUC Geral
           'nq8_Acel_SVM4', 'nq8_Acel_MLP1', 'nq8_Gir_RF4', 'nq8_Gir_BN3', # Acurácia Geral
           'nq8_Acel_RF6', 'nq8_Mag_RF5', 'nq7_Gir_z_MLP2', 'nq8_Gir_RF6', 'nq6_AclLin_SVM4', 'nq7_Acel_MLP1', 'nq8_AclLin_MLP2', # AUC Específica
           'nq8_Acel_SVM36','nq8_Mag_SVM16', 'nq6_Gir_z_SVM24', 'nq8_Gir_SVM3', 'nq6_AclLin_SVM4', 'nq6_Acel_SVM8', 'nq8_Acel_SVM16', # Acurária Específica
           'nq8_Acel_MLP2') # classificador entre os 5 melhores na performance geral e específica (sem DTW)

  ids <- converterIdentificadoresDeHumanoParaReal(ids = ids)
  
  data.frame(identifier = ids, 
             campoMetrica = c(rep('weightedAreaUnderROC', 4), 
                              rep('correctInstancesRate', 4),
                              rep('classAreaUnderROC', 7),
                              rep('classAccuracy', 7),
                              'weightedAreaUnderROC'),
             class = c(rep(NA, 8),
                       rep(c(toupper(eventoLevels())), 2),
                       NA))
}

# retorna os identificadores e seeds mais próximos da média das diversas execuções (seed e cross-validation fold)
# formato de identificadoresMelhoresModelosDF é o retorno de getIdentificadoresMelhoresModelos
getIdentifiersSeedsClosestToMetricMean <- function(identificadoresMelhoresModelosDF, resultadosGeraisDF, resultadosPorClasseDF){

  closestToGeneralMetricMean <- function(identificadoresMelhoresModelosGeraisDF, resultadosGeraisDF){
    identificadoresPorCampoMetricaDF <- aggregate(formula = identifier ~ campoMetrica, 
                                                  data = identificadoresMelhoresModelosGeraisDF, 
                                                  FUN = function(ids) as.character(ids))
    
    listaDFs <- mapply(identifiers = identificadoresPorCampoMetricaDF$identifier,
                       campoMetrica = as.character(identificadoresPorCampoMetricaDF$campoMetrica),
                       SIMPLIFY = F,
                       FUN = function(identifiers, campoMetrica){
                         resultadosGeraisDF <- resultadosGeraisDF[resultadosGeraisDF$identifier %in% identifiers, ]
                         metricaPorIdentificadorDF <- aggregate(x = list(mediaMetricaPorIdentificador = resultadosGeraisDF[, campoMetrica]),
                                                                by = list(identifier = resultadosGeraisDF$identifier),
                                                                FUN = mean)
                         metricaPorIdentificadorESeedDF <- aggregate(x = list(mediaMetricaPorIdentificadorESeed = resultadosGeraisDF[, campoMetrica]),
                                                                     by = list(identifier = resultadosGeraisDF$identifier, seed = resultadosGeraisDF$seed),
                                                                     FUN = mean)
                         resultadosComMediaMetricaDF <- merge(x = metricaPorIdentificadorDF,
                                                              y = metricaPorIdentificadorESeedDF,
                                                              by = 'identifier')
                         resultadosComMediaMetricaDF$campoMetrica <- campoMetrica
                         resultadosComMediaMetricaDF$diff <- abs(resultadosComMediaMetricaDF$mediaMetricaPorIdentificador - resultadosComMediaMetricaDF$mediaMetricaPorIdentificadorESeed)
                         
                         do.call(rbind, lapply(X = split(f = resultadosComMediaMetricaDF$identifier, x = resultadosComMediaMetricaDF, drop = T),
                                               FUN = function(byIdentifierDF) byIdentifierDF[which.min(byIdentifierDF$diff), ]))
                       })
    
    do.call(rbind, listaDFs)
    
  }
  
  closestToSpecificMetricMean <- function(identificadoresMelhoresModelosEspecificosDF, resultadosPorClasseDF){
    identificadoresPorCampoMetricaDF <- aggregate(formula = identifier ~ campoMetrica + class, 
                                                  data = identificadoresMelhoresModelosEspecificosDF, 
                                                  FUN = function(ids) as.character(ids))
    
    listaDFs <- mapply(identifiers = identificadoresPorCampoMetricaDF$identifier,
                       campoMetrica = as.character(identificadoresPorCampoMetricaDF$campoMetrica),
                       class = as.character(identificadoresPorCampoMetricaDF$class),
                       SIMPLIFY = F,
                       FUN = function(identifiers, campoMetrica, class){
                         resultadosPorClasseDF <- resultadosPorClasseDF[resultadosPorClasseDF$identifier %in% identifiers & resultadosPorClasseDF$class == class, ]
                         metricaPorIdentificadorDF <- aggregate(x = list(mediaMetricaPorIdentificador = resultadosPorClasseDF[, campoMetrica]),
                                                                by = list(identifier = resultadosPorClasseDF$identifier, class = resultadosPorClasseDF$class),
                                                                FUN = mean)
                         metricaPorIdentificadorESeedDF <- aggregate(x = list(mediaMetricaPorIdentificadorESeed = resultadosPorClasseDF[, campoMetrica]),
                                                                     by = list(identifier = resultadosPorClasseDF$identifier, class = resultadosPorClasseDF$class, seed = resultadosPorClasseDF$seed),
                                                                     FUN = mean)
                         resultadosComMediaMetricaDF <- merge(x = metricaPorIdentificadorDF,
                                                              y = metricaPorIdentificadorESeedDF,
                                                              by = c('identifier', 'class'))
                         resultadosComMediaMetricaDF$campoMetrica <- campoMetrica
                         resultadosComMediaMetricaDF$diff <- abs(resultadosComMediaMetricaDF$mediaMetricaPorIdentificador - resultadosComMediaMetricaDF$mediaMetricaPorIdentificadorESeed)

                         do.call(rbind, lapply(X = split(f = resultadosComMediaMetricaDF$identifier, x = resultadosComMediaMetricaDF, drop = T),
                                               FUN = function(byIdentifierDF) byIdentifierDF[which.min(byIdentifierDF$diff), ]))
                       })
    
    do.call(rbind, listaDFs)
  }
  
  
  identificadoresMelhoresModelosGeraisDF <- identificadoresMelhoresModelosDF[is.na(identificadoresMelhoresModelosDF$class), ]
  identificadoresMelhoresModelosGeraisDF <- identificadoresMelhoresModelosGeraisDF[!duplicated(identificadoresMelhoresModelosGeraisDF$identifier), ]
  closestToGeneralMetricMeanDF <- closestToGeneralMetricMean(identificadoresMelhoresModelosGeraisDF = identificadoresMelhoresModelosGeraisDF,
                                                             resultadosGeraisDF = resultadosGeraisDF)
  closestToGeneralMetricMeanDF$class <- NA
  closestToGeneralMetricMeanDF <- closestToGeneralMetricMeanDF[, c(1, length(closestToGeneralMetricMeanDF), 2:(length(closestToGeneralMetricMeanDF) - 1))]

  identificadoresMelhoresModelosEspecificosDF <- identificadoresMelhoresModelosDF[!is.na(identificadoresMelhoresModelosDF$class), ]
  identificadoresMelhoresModelosEspecificosDF <- identificadoresMelhoresModelosEspecificosDF[!duplicated(identificadoresMelhoresModelosEspecificosDF[, c('identifier', 'class')]), ]
  closestToSpecificMetricMeanDF <- closestToSpecificMetricMean(identificadoresMelhoresModelosEspecificosDF = identificadoresMelhoresModelosEspecificosDF, 
                                                               resultadosPorClasseDF = resultadosPorClasseDF)
  
  resultadoDF <- rbind(closestToGeneralMetricMeanDF, closestToSpecificMetricMeanDF)
  rownames(resultadoDF) <- 1:nrow(resultadoDF)
  resultadoDF
}

createEnsembleDataset <- function(predictionsDir, allowNAClassification, classLevels){
    predictions <- lerCSVsPredicoes(predictionsDir = predictionsDir, classLevels = classLevels)
    camposARemover <- c('executionId')
    ensembleDS <- Reduce(x = predictions, f = function(leftPredictions, rightPredictions){
        leftPredictions[, camposARemover] <- NULL
        rightPredictions[, camposARemover] <- NULL
        merge(x = leftPredictions, y = rightPredictions, by = c('quadro0', 'actualClass'), sort = F, all = allowNAClassification)
    })
    ensembleDS <- droplevels(ensembleDS)
    
    ensembleDS[, c('quadro0', names(ensembleDS)[3:length(ensembleDS)], 'actualClass')]
}

createDiverseEnsembleDataset <- function(ensembleF6PlusDF, ensembleF6PlusDFQStatistic, closestToMetricMeanDF, qStatisticTargetValue = 0){
    closestToMetricMeanDF <- closestToMetricMeanDF[!duplicated(closestToMetricMeanDF$identifier), ]
    melhoresIdentificadoresComSeeds <- paste(closestToMetricMeanDF$identifier, '_s', closestToMetricMeanDF$seed, sep = '')
  
    require('parallel')
    idsComSeedDeMaiorDiversidade <- unlist(mclapply(X = melhoresIdentificadoresComSeeds, mc.cores = 3, FUN = function(idComSeed){
        qStatisticDF <- ensembleF6PlusDFQStatistic[ensembleF6PlusDFQStatistic$identificador1 == idComSeed | ensembleF6PlusDFQStatistic$identificador2 == idComSeed, ]
        minQStatisticDF <- qStatisticDF[which.min(abs(qStatisticDF$qStatistic - qStatisticTargetValue)), ]
        ifelse(minQStatisticDF$identificador1 == idComSeed, as.character(minQStatisticDF$identificador2), as.character(minQStatisticDF$identificador1))
    }))
    
    # NOTA: esta função não trata o caso de existirem idsComSeedDeMaiorDiversidade duplicados (mesmo classificador), mas com seeds diferentes
    # Caso seja necessário tratar, fazer algo similar a:
    # idsSemSeedDeMaiorDiversidade <- substr(idsComSeedDeMaiorDiversidade, 1, stri_locate_last_fixed(idsComSeedDeMaiorDiversidade, patter = '_')[,1] - 1)
    # remover os ids duplicados de idsComSeedDeMaiorDiversidade: idsComSeedDeMaiorDiversidade[!duplicated(idsSemSeedDeMaiorDiversidade)]
    
    ensembleF6PlusDF[, c('quadro0', unique(c(melhoresIdentificadoresComSeeds, idsComSeedDeMaiorDiversidade)), 'actualClass')]
}

createEnsembleDatasetWithExecutionSeeds <- function(dirRaizPredicoes, allowNAClassification = F, classLevels){
    require('stringr')
    require('parallel')
    
    csvsPredicoes <- list.files(path = dirRaizPredicoes, pattern = '*.csv', recursive = T, include.dirs = T, full.names = T)
    csvPredicoesBaseNames <- basename(csvsPredicoes)

    csvsPredicoesAExcluir <- c(grep(x = csvPredicoesBaseNames, pattern = 'f4_', fixed = T), 
                               grep(x = csvPredicoesBaseNames, pattern = 'f5_', fixed = T), 
                               grep(x = csvPredicoesBaseNames, pattern = 'DTW', fixed = T))
    csvsPredicoesAIncluir <- setdiff(seq_along(csvsPredicoes), csvsPredicoesAExcluir)
    
    csvsPredicoes <- csvsPredicoes[csvsPredicoesAIncluir]
    
    cores <- 3
    
    listaPredicoes <- mclapply(X = csvsPredicoes, mc.cores = cores, FUN = function(arquivoPredicoes){
        predicoes <- lerCSVPredicoes(csvPredicoes = arquivoPredicoes, classLevels = classLevels)
        quintuplaAvaliacaoComSeed <- str_match(string = predicoes$executionId[1], pattern = '(.*?_s.*)_.*?')[1, 2]
        predicoes$executionId <- NULL
        names(predicoes)[2] <- quintuplaAvaliacaoComSeed
        predicoes
    })
    
    mcReduce <- function(x, mc.cores, FUN){
      xLength <- length(x)
      if(xLength == 0){
        return(NULL)
      }
      if(xLength == 1){
        return(x[[1]])
      }
      result <- mclapply(X = seq(from = 1, by = 2, to = length(x)), mc.cores = mc.cores, FUN = function(leftDataIndex){
        if(leftDataIndex == xLength){
          return(x[[leftDataIndex]])
        }
        
        FUN(x[[leftDataIndex]], x[[leftDataIndex + 1]])
      })
      
      mcReduce(x = result, mc.cores = mc.cores, FUN = FUN)
    }
    
    ensembleDS <- mcReduce(x = listaPredicoes, mc.cores = cores, FUN = function(leftPredictions, rightPredictions){
      merge(x = leftPredictions, y = rightPredictions, by = c('quadro0', 'actualClass'), sort = F, all = allowNAClassification)
    })
    ensembleDS <- droplevels(ensembleDS)
    
    ensembleDS[, c('quadro0', names(ensembleDS)[3:length(ensembleDS)], 'actualClass')]
}

gerarIdentificadoresComSeed <- function(identificadores, seeds){
    paste(sep = '', identificadores, '_s', seeds)
}

criarCombinacoesParesIdentificadores <- function(identificadores1, identificadores2){
    grid <- expand.grid(identificador1 = identificadores1, identificador2 = identificadores2)
    grid <- grid[as.character(grid$identificador1) != as.character(grid$identificador2), ]
    
    require('parallel')
    conjuntoParesIdentificadores <- mcmapply(as.character(grid$identificador1), as.character(grid$identificador2), USE.NAMES = F, mc.cores = 3, FUN = function(i1, i2) paste(sort(c(i1, i2)), collapse = '_'))

    grid[!duplicated(conjuntoParesIdentificadores), ]
}

calcularQStatisticEnsembleF6Plus <- function(closestToMetricMeanDF, ensembleF6PlusDF){
    closestToMetricMeanDF <- closestToMetricMeanDF[!duplicated(closestToMetricMeanDF$identifier), ]
    combinacoesParesIdentificadoresDF <- criarCombinacoesParesIdentificadores(identificadores1 = paste(sep = '', closestToMetricMeanDF$identifier, '_s', closestToMetricMeanDF$seed),
                                                             identificadores2 = names(ensembleF6PlusDF)[2:(ncol(ensembleF6PlusDF) - 1)])
    calcularQStatisticEnsemble(ensembleDF = ensembleF6PlusDF, combinacoesParesIdentificadoresDF = combinacoesParesIdentificadoresDF)
}

calcularQStatisticEnsemble <- function(ensembleDF,
                                       combinacoesParesIdentificadoresDF = criarCombinacoesParesIdentificadores(identificadores1 = names(ensembleDF)[2:(ncol(ensembleDF) - 1)], 
                                                                                          identificadores2 = names(ensembleDF)[2:(ncol(ensembleDF) - 1)])){
    require('parallel')
    nrowEnsembleDF <- nrow(ensembleDF)
    actualClasses <- as.character(ensembleDF$actualClass)
    
    qStatistics <- mcmapply(identificador1 = as.character(combinacoesParesIdentificadoresDF$identificador1), identificador2 = as.character(combinacoesParesIdentificadoresDF$identificador2), 
                            mc.cores = 2, 
                            FUN = function(identificador1, identificador2){

        predicoesClassificador1IgualActualClasses <- ensembleDF[, identificador1] == actualClasses
        predicoesClassificador1DiferenteActualClasses <- !predicoesClassificador1IgualActualClasses
        
        predicoesClassificador2IgualActualClasses <- ensembleDF[, identificador2] == actualClasses
        predicoesClassificador2DiferenteActualClasses <- !predicoesClassificador2IgualActualClasses
        
        ambosCorretos <- sum(predicoesClassificador1IgualActualClasses & predicoesClassificador2IgualActualClasses)
        ambosErrados <- sum(predicoesClassificador1DiferenteActualClasses & predicoesClassificador2DiferenteActualClasses)
        classificador1IncorretoClassificador2Correto <- sum(predicoesClassificador1DiferenteActualClasses & predicoesClassificador2IgualActualClasses)
        classificador1CorretoClassificador2Incorreto <- sum(predicoesClassificador1IgualActualClasses & predicoesClassificador2DiferenteActualClasses)
    
        total <- ambosCorretos + ambosErrados + classificador1IncorretoClassificador2Correto + classificador1CorretoClassificador2Incorreto
        if(total != nrowEnsembleDF){
            stop(paste(total,'!=', nrowEnsembleDF, 'para', identificador1, 'e', identificador2))
        }
    
        percAmbosCorretos <- ambosCorretos / nrowEnsembleDF
        percAmbosErrados <- ambosErrados / nrowEnsembleDF
        percClassificador1IncorretoClassificador2Correto <- classificador1IncorretoClassificador2Correto / nrowEnsembleDF
        percClassificador1CorretoClassificador2Incorreto <- classificador1CorretoClassificador2Incorreto / nrowEnsembleDF
    
        (percAmbosCorretos * percAmbosErrados - percClassificador1IncorretoClassificador2Correto * percClassificador1CorretoClassificador2Incorreto) / (percAmbosCorretos * percAmbosErrados + percClassificador1IncorretoClassificador2Correto * percClassificador1CorretoClassificador2Incorreto)
        
    })
    
    combinacoesParesIdentificadoresDF$qStatistic <- qStatistics
    combinacoesParesIdentificadoresDF
}

saveEnsembleDatasetAsARFFAndCSV <- function(ensembleDF, arquivoSemExtensao){
    require('foreign')
    
    ensembleDF$quadro0 <- as.character(ensembleDF$quadro0)
    
    write.table(ensembleDF, 
                paste(arquivoSemExtensao, '.csv', sep = ''),
                quote = F,
                row.names = F,
                sep = ',',
                na = '')
    
    write.arff(x = ensembleDF,
               file = paste(arquivoSemExtensao, '.arff', sep = ''), 
               relation = 'ensemble')
}

adicionarDuracaoJanela <- function(datasetProcessado) {
    datasetProcessado$duracao_janela_nanos <- datasetProcessado$tempo_decorrido_final_nanos - datasetProcessado$tempo_decorrido_inicial_nanos
    datasetProcessado$duracao_janela_segundos <- nanosParaSegundos(datasetProcessado$duracao_janela_nanos)
    datasetProcessado
}

adicionarDuracaoQuadro <- function(datasetProcessado) {
    nLinhas <- nrow(datasetProcessado)
    datasetProcessado$duracao_quadro_nanos <- c(NA, datasetProcessado$tempo_decorrido_final_nanos[2:nLinhas] - datasetProcessado$tempo_decorrido_final_nanos[1:(nLinhas - 1)])
    
    #a duração fica negativa na transição de uma viagem para outra
    datasetProcessado$duracao_quadro_nanos[datasetProcessado$duracao_quadro_nanos < 0] <- NA
    
    datasetProcessado$duracao_quadro_segundos <- nanosParaSegundos(datasetProcessado$duracao_quadro_nanos)
    datasetProcessado
}

novosPNGs <- function(dir){
    dirGraficos <- criarDiretorioUnico(dir = dir)

    abrirPNG <- function(nAmostras, arquivo, nGraficos){
        # a largura foi calculada empiricamente e 32700 é a largura máxima no Ubuntu
        # 1000 é a largura mínima da imagem (para o caso de existirem poucas amostras)
        filename <- file.path(dirGraficos, arquivo)
        png(height = nGraficos * 600,
            width = max(1000, min(32700, as.integer(1600/217 * nAmostras))),
            filename = filename)
        filename
    }
    abrirPNG
}

criarDiretorioUnico <- function(dir){
    dirAtual <- dir
    i <- 1
    while(file.exists(dirAtual)){
        dirAtual <- paste(dir, i, sep = "_")
        i <- i + 1
    }
    dir.create(path = dirAtual, recursive = T)
    dirAtual
}

eventoNome <- function(eventos, abreviar = F, idioma = 'pt'){
  if(idioma == 'pt'){
      if(abreviar){
          nomesEventos = c(curva_direita_agressiva = 'Curva Agr.\nà Dir.',
                           curva_esquerda_agressiva = 'Curva Agr.\nà Esq.',
                           aceleracao_agressiva = 'Aceleração\nAgr.',
                           freada_agressiva = 'Freada\nAgr.',
                           troca_faixa_direita_agressiva = 'Troca de Faixa\nAgr. à Dir.',
                           troca_faixa_esquerda_agressiva = 'Troca de Faixa\nAgr. à Esq.',
                           evento_nao_agressivo = 'Evento\nNão Agr.')
      }
      else{
          nomesEventos = c(curva_direita_agressiva = 'Curva Agressiva à Direita',
                           curva_esquerda_agressiva = 'Curva Agressiva à Esquerda',
                           aceleracao_agressiva = 'Aceleração Agressiva',
                           freada_agressiva = 'Freada Agressiva',
                           troca_faixa_direita_agressiva = 'Troca de Faixa Agressiva à Direita',
                           troca_faixa_esquerda_agressiva = 'Troca de Faixa Agressiva à Esquerda',
                           evento_nao_agressivo = 'Evento Não Agressivo')
      }
  }
  else if (idioma == 'en'){
      if(abreviar){
          nomesEventos = c(curva_direita_agressiva = 'Aggresv.\nright turn',
                           curva_esquerda_agressiva = 'Aggresv.\nleft turn',
                           aceleracao_agressiva = 'Aggresv.\naccel.',
                           freada_agressiva = 'Aggresv.\nbreaking',
                           troca_faixa_direita_agressiva = 'Aggresv. right\nlane change',
                           troca_faixa_esquerda_agressiva = 'Aggresv. left\nlane change',
                           evento_nao_agressivo = 'Nonaggresv.\nevent')
      }
      else{
          nomesEventos = c(curva_direita_agressiva = 'Aggressive\nright turn',
                           curva_esquerda_agressiva = 'Aggressive\nleft turn',
                           aceleracao_agressiva = 'Aggressive\nacceleration',
                           freada_agressiva = 'Aggressive\nbreaking',
                           troca_faixa_direita_agressiva = 'Aggressive right\nlane change',
                           troca_faixa_esquerda_agressiva = 'Aggressive left\nlane change',
                           evento_nao_agressivo = 'Nonaggressive\nevent')
      }
  }
  else{
      stop(paste('Idioma não suportado:', idioma))
  }
  
  eventos <- tolower(eventos)
  sapply(eventos, function(evento){
      nome <- nomesEventos[evento]
      if(is.na(nome)){
          nome <- paste('Evento não encontrado:', evento)
      }
      names(nome) <- NULL
      nome
  })
}

isAllEnsemble <- function(resultsDataset){
    # sem sensor, então é o resultado de um ensemble
    sum(is.na(resultsDataset$sensor)) == nrow(resultsDataset) 
}

nomesSensoresParaFactor <- function(nomesSensores, idioma = 'pt'){
    if(sum(is.na(nomesSensores)) == length(nomesSensores)){
        # sem sensor, então são resultados de um ensemble. nada a fazer
        return(nomesSensores)
    }
    
    require('plyr')
    if(idioma == 'pt'){
        nomes <- c(ACCELEROMETER_EARTH = 'Acelerômetro',
                   LINEAR_ACCELERATION_EARTH = 'Aceleração Linear',
                   GYROSCOPE_EARTH = 'Giroscópio',
                   MAGNETIC_FIELD_EARTH = 'Magnetômetro')
    
    }
    else if(idioma == 'en'){
        nomes <- c(ACCELEROMETER_EARTH = 'Accelerometer',
                   LINEAR_ACCELERATION_EARTH = 'Linear Acceleration',
                   GYROSCOPE_EARTH = 'Gyroscope',
                   MAGNETIC_FIELD_EARTH = 'Magnetometer')
    }
    revalue(nomesSensores, nomes)
}

nomesClassificadorParaFactor <- function(nomesClassificadores){
  require('plyr')
  revalue(nomesClassificadores, c(BAYES_NET = 'BN', 
                                  MULTILAYER_PERCEPTRON = 'MLP',
                                  RANDOM_FOREST = 'RF'))
}

eventoNomeFactor <- function(eventosFactor, abreviar = F, idioma = 'pt'){
    levels(eventosFactor) <- eventoNome(levels(eventosFactor), abreviar = abreviar, idioma = idioma)
    eventosFactor
}

eventoLevels <- function(){
    c('aceleracao_agressiva',
      'freada_agressiva',
      'curva_direita_agressiva',
      'curva_esquerda_agressiva',
      'troca_faixa_direita_agressiva',
      'troca_faixa_esquerda_agressiva',
      'evento_nao_agressivo')
}

tornarEventoFactorOrdinal <- function(eventoFactor){
    factor(tolower(eventoFactor), levels = eventoLevels(), ordered = T)
}

eventoFactor <- function(){
    factor(levels = eventoLevels(),
           ordered = T)
}

combinacoesPara <- function(factorAssemblies, idioma = 'pt'){
    if(!idioma %in% c('pt', 'en')){
        stop(paste('Idioma não suportado:', idioma))
    }
    
    idiomaPortugues <- idioma == 'pt'
    
    frames <- paste(ifelse(idiomaPortugues, "nq", 'nf'), 2:8, '_', sep = "")
    names(frames) <- paste("f", 2:8, '_', sep = "")
    
    if(idiomaPortugues){
        sensores <- c("Acel_", "AclLin_", "Mag_",  "Gir_")
    }
    else{
        sensores <- c("Acc_", "LinAcc_", "Mag_",  "Gyr_")
    }
    names(sensores) <- c("AcelE_", "AclLinE_", "MagE_", "GirE_")
    
    dePara <- c(frames, sensores)

    currentLevels <- levels(factorAssemblies)
    for(de in names(dePara)){
        currentLevels <- gsub(de, dePara[de], currentLevels, fixed = T)
    }
    
    levels(factorAssemblies) <- currentLevels
    factorAssemblies
}

apenasSensoresCoordenadasTerra <- function(listaSensores){
    listaSensores[! names(listaSensores) %in% sensoresCoordenadasDispositivo()]
}

apenasSensoresCoordenadasDispositivo <- function(listaSensores){
    listaSensores[names(listaSensores) %in% c(sensoresCoordenadasDispositivo(), 'groundTruth')]
}

sensoresCoordenadasDispositivo <- function(){
    c('aceleracaoLinear',
      'acelerometro',
      'campoMagnetico',
      'giroscopio',
      'gravidade',
      'vetorRotacao')
}

carregarEPlotarTodosResultadosClassificadores <- function(dirResultados = '../apm/results_1', numeroSeries = 10){
    todasCoordenadas <- carregarTodosResultadosClassificadores(file.path(dirResultados, 'todasCoordenadas'))
    coordenadasTerra <- carregarTodosResultadosClassificadores(file.path(dirResultados,'coordenadasTerra'))
    coordenadasDispositivo <- carregarTodosResultadosClassificadores(file.path(dirResultados,'coordenadasDispositivo'))

    plotResultados(resultados = todasCoordenadas,
                   pngFileName = 'resultados_coletas2_3_todasCoordenadas.png',
                   numeroSeries = numeroSeries)

    plotResultados(resultados = coordenadasTerra,
                   pngFileName = 'resultados_coletas2_3_coordenadasTerra.png',
                   numeroSeries = numeroSeries)

    plotResultados(resultados = coordenadasDispositivo,
                   pngFileName = 'resultados_coletas2_3_coordenadasDispositivo.png',
                   numeroSeries = numeroSeries)

    list(
        'todasCoordenadas' = todasCoordenadas,
        'coordenadasTerra' = coordenadasTerra,
        'coordenadasDispositivo' = coordenadasDispositivo
    )
}

lerCSVDatasetProcessado <- function(csvFile){
    read.table(csvFile, header = T, sep = ',', na.strings = '')
}

csvParaArff <- function(csvFile, arffFile){
    require('foreign')
    write.arff(x = lerCSVDatasetProcessado(csvFile = csvFile), 
               file = arffFile,
               relation = basename(arffFile))
}

# Agrupa todos os arquivos de vetor de atributos contidos 
# em 'dirEntrada' em um único arquivo (arquivoSaida)
# com atributos para o sensor e coordenada.
# Nota: esse agrupamento foi solicitado pelo Ohashi para 
# estender o artigo do WoCCES.
# Exemplo de uso: gerarVetorAtributosUnico('~/Google Drive/ufpa/apm_R/vetorAtributos/v4ev5', 'eventos.csv')
gerarVetorAtributosUnico <- function(dirEntrada, arquivoSaida){
    library(stringr)
    arquivos <- list.files(dirEntrada, pattern = '.+_.+\\.csv$', full.names = T)
    if(length(arquivos) == 0){
        return(NULL)
    }
    arquivosSensorCoordenadas <- str_match(basename(arquivos), '(.+)_(.+).csv$')
    arquivosSensorCoordenadas[, 1] <- arquivos
    dfGeral <- NULL
    for(i in 1:nrow(arquivosSensorCoordenadas)){
        df <- read.table(file = arquivosSensorCoordenadas[i, 1], sep = ',', header = T, quote = '', fileEncoding = 'UTF-8')
        df <- cbind(rep(arquivosSensorCoordenadas[i, 2], nrow(df)), rep(arquivosSensorCoordenadas[i, 3], nrow(df)), df)
        names(df)[1] <- 'sensor'
        names(df)[2] <- 'coordenada'
        if(is.null(dfGeral)){
            dfGeral <- df
        }
        else{
            dfGeral <- rbind(dfGeral, df)
        }
    }
    write.table(dfGeral, file = arquivoSaida, quote = F, sep = ',', row.names = F, fileEncoding = 'UTF-8')
}

# em teoria, a melhor métrica para comparar modelos resultantes de algoritmos diferentes 
# aplicados a um mesmo domínio é o 'Matthews Correlation Coefficient', só falta buscar uma referência que comprove isso
camposMetricas <- function(incluirTempoClassificacao = F){
    # data.frame(campo = c('weightedAreaUnderROC', 'correctInstancesRate', 'incorrectInstancesRate', 'kappaStatistic',
    #                      'meanAbsoluteError', 'rootMeanSquaredError', 'relativeAbsoluteError',
    #                      'rootRelativeSquaredError', 'weightedAreaUnderPRC',
    #                      'weightedMatthewsCorrelationCoefficient', 'weightedFMeasure', 'weightedTruePositiveRate',
    #                      'weightedFalsePositiveRate', 'weightedPrecision', 'instanceClassificationTimeInMillisAverage'),
    #            titulo = c('Área sob a curva ROC', 'Acurácia', 'Incorrect Instances Rate', 'Kappa Statistic',
    #                       'Mean Absolute Error', 'Root Mean Squared Error', 'Relative Absolute Error',
    #                       'Root Relative Squared Error', 'Weighted Area Under PRC',
    #                       'Weighted Matthews Correlation Coefficient', 'Weighted F-Measure', 'Weighted True Positive Rate',
    #                       'Weighted False Positive Rate', 'Weighted Precision', 'Tempo de classificação (millis)'),
    #            maiorMelhor = c(T, T, F, T,
    #                            F, F, F,
    #                            F, T,
    #                            T, T, T,
    #                            F, T, F))

    df <- data.frame(campo = c('weightedAreaUnderROC', 'correctInstancesRate'),
               titulo = c('Área sob a curva ROC', 'Acurácia'),
               maiorMelhor = c(T, T))
    
    if(incluirTempoClassificacao){
      df <- rbind(df, data.frame(campo = 'instanceClassificationTimeInMillisAverage', titulo = 'Tempo médio de classificação', maiorMelhor = F))
    }
    
    df
}

camposMetricasClasse <- function(){
    # data.frame(campo = c('classAccuracy', 'classAreaUnderROC', 'classPrecision', 'classAreaUnderPRC', 
    #                      'classMatthewsCorrelationCoefficient', 'classFMeasure', 
    #                      'classTruePositiveRate', 'classFalseNegativeRate', 'classFalsePositiveRate', 'classTrueNegativeRate'),
    #            titulo = c('Acurácia', 'Area Under ROC', 'Precision', 'Area Under PRC', 
    #                       'Matthews Correlation Coefficient', 'F-Measure', 
    #                       'True Positive Rate', 'False Negative Rate', 'False Positive Rate', 'True Negative Rate'),
    #            maiorMelhor = c(T, T, T, T,
    #                            T, T, 
    #                            T, F, F, T))
    
    data.frame(campo = c('classAreaUnderROC', 'classAccuracy'),
               titulo = c('Área sob a curva ROC', 'Acurácia'),
               maiorMelhor = c(T, T))
    
}

amostraEstratificadaPorEvento <- function(df, numeroAmostrasPorEvento){
    sp <- split(seq_len(nrow(df)), df$evento)
    samples <- lapply(sp, function(indices){
        if(numeroAmostrasPorEvento >= length(indices)){
            indices
        }
        else{
            sample(indices, numeroAmostrasPorEvento)    
        }
    })
    df[unlist(samples), ]
}

estratificarPorEventoConvertendoParaArff <- function(arquivoCSVEntrada, arquivoARFFSaida, numeroAmostrasPorEvento){
    require('foreign')
    write.arff(x = amostraEstratificadaPorEvento(lerCSVDatasetProcessado(csvFile = arquivoCSVEntrada), numeroAmostrasPorEvento = numeroAmostrasPorEvento), 
               file = arquivoARFFSaida,
               relation = basename(arquivoARFFSaida))
}

numericFields <- function(df){
  sapply(1:ncol(df), function(col) is.numeric(df[, col]))
}

mapMinMax.serieTemporal <- function(serieTemporalDF, min = -1, max = 1){
  indicesCampos <- which(names(serieTemporalDF) %in% c('x', 'y', 'z'))
  
  minMaxDF <- data.frame(matrix(nrow = 2, ncol = length(indicesCampos)))
  names(minMaxDF) <- names(serieTemporalDF)[indicesCampos]
  
  for(i in seq_along(indicesCampos)){
    indiceCampo <- indicesCampos[i]
    linhas <- strsplit(x = as.character(serieTemporalDF[, indiceCampo]), split = ';', fixed = T)
    linhas <- lapply(linhas, as.numeric)
    
    todosValores <- unlist(linhas)
    dataMin <- min(todosValores)
    dataMax <- max(todosValores)
    minMaxDF[1, i] <- dataMin
    minMaxDF[2, i] <- dataMax
    
    linhasNormalizadas <- sapply(linhas, function(valoresLinha) {
      paste(mapMinMax(data = valoresLinha, dataMin = dataMin, dataMax = dataMax, min = min, max = max)$normalizedData, collapse = ';')
    })
    serieTemporalDF[indiceCampo] <- as.factor(linhasNormalizadas)
  }
  
  list(normalizedDF = serieTemporalDF, 
       minMaxDF = minMaxDF)
}

mapMinMax.vetorAtributos <- function(vetorAtributosDF, min = -1, max = 1){
  colIndexes <- which((!names(vetorAtributosDF) %in% camposTemporais()) & numericFields(df = vetorAtributosDF))
  mapMinMax.dataframe(df = vetorAtributosDF, colIndexes = colIndexes, min = min, max = max)
}

mapMinMax.dataframe <- function(df, 
                                colIndexes = which(numericFields(df = df)), 
                                dataMins = sapply(colIndexes, function(col) min(df[, col])), 
                                dataMaxes = sapply(colIndexes, function(col) max(df[, col])),
                                min = -1, 
                                max = 1){
  
  if(length(colIndexes) != length(dataMins) || length(dataMins) != length(dataMaxes)){
    stop('Os tamanhos de "colIndexes", "dataMins" e "dataMaxes" devem ser iguais.')
  }
  
  minMaxDF <- data.frame(matrix(data = c(dataMins, dataMaxes), nrow = 2, byrow = T))
  names(minMaxDF) <- names(df)[colIndexes]

  for(i in seq_along(colIndexes)){
    colIndex <- colIndexes[i]

    l <- mapMinMax(data = df[, colIndex], 
                   dataMin = dataMins[i], 
                   dataMax = dataMaxes[i],
                   min = min,
                   max = max)
    
    df[, colIndex] <- l$normalizedData
  }
  
  list(normalizedDF = df, 
       minMaxDF = minMaxDF)
}

mapMinMax <- function(data, dataMin = min(data), dataMax = max(data), min = -1, max = 1){
  dDelta <- dataMax - dataMin
  
  if(dDelta == 0){
    stop('Todos os valores de "data" são iguais. Impossível normalizar')
  }
  
  delta <- max - min
  
  if(delta == 0){
    stop('"min" == "max". Impossível normalizar')
  }
  
  normalizedData <- sapply(data, function(n) {
    ((n - dataMin) / dDelta) * delta + min
  })
  
  list(normalizedData = normalizedData,
       dataMin = dataMin,
       dataMax = dataMax)
}

removerLeiturasDuplicadas <- function(sensorDF){
  sensorDF[!duplicated(sensorDF$uptimeNanos), ]
}

# ggplot(svm3, aes(x = c, y = gamma)) + facet_grid(. ~ kernel) + geom_point(shape = 21, aes(size = acuracia, fill = acuracia, color = acuracia)) + scale_size(range = c(1,15))

# carregar dados de uma viagem em uma lista
# t9 <- carregarTodosDadosSmartphone("dados/smartphone/motorola xt1058/9") #lê os dados dos arquivos

#plota os dados dos eventos de direção nos intervalos da ground truth
#plotarTodosGraficosTodosSensoresGroundTruth(t9)

#plotarTodosGraficosTodosSensores(listaSensores, 20)

# gerar datasets processados (vetor de atributos e série temporal nos formatos CSV e ARFF, SEM datasets de treino e teste)
# dsp <- gerarDatasetsProcessados(list(c2v16,c2v17,c3v20,c3v21), numerosQuadrosNaJanela = c(4,5,6,7,8), dir = 'dados/datasetsProcessadosNormalizados', percentualDivisaoTreinoTeste = NA)

# gerar datasets processados (vetor de atributos e série temporal nos formatos CSV e ARFF, COM datasets de treino e teste separados). Os resultados dessa chamada contém os resultados da anterior (sem datasets de treino/teste).
# dividir em treino/teste ainda não está finalizado. Ver comentários da função para maiores detalhes -> 
# dsp <- gerarDatasetsProcessados(list(c2v16,c2v17,c3v20,c3v21), numerosQuadrosNaJanela = c(4,5,6,7,8), dir = 'dados/datasetsProcessadosNormalizados', percentualDivisaoTreinoTeste = 0.8)

# carregar resultados dos classificadores no novo formato
# r <- carregarNovosResultadosClassificadores('../apm/results/results.csv')
# rGerais <- extrairResultadosGerais(r)
# rPorClasse <- extrairResultadosPorClasse(r)

# gerar e salvar dataset de ensemble
# closestToMetricMeanDF <- getIdentifiersSeedsClosestToMetricMean(identificadoresMelhoresModelosDF = getIdentificadoresMelhoresModelos(), resultadosGeraisDF = resultadosGeraisDF, resultadosPorClasseDF = resultadosPorClasseDF)
# (deprecated) ensembleDF <- createEnsembleDataset(predictionsDir = 'predictions', allowNAClassification = F, classLevels = levels(r$class))
# (deprecated) ensembleQStatisticDF <- calcularQStatisticEnsemble(ensembleDF = ensembleDF)
# ensembleF6PlusDF <- createEnsembleDatasetWithExecutionSeeds(dirRaizPredicoes = '../../apm_execucao/predictions_20170712_164038', allowNAClassification = F, classLevels = levels(r$class))
# ensembleF6PlusQStatisticDF <- calcularQStatisticEnsembleF6Plus(closestToMetricMeanDF = closestToMetricMeanDF, ensembleF6PlusDF = ensembleF6PlusDF)
# diverseEnsembleDF <- createDiverseEnsembleDataset(ensembleF6PlusDF = ensembleF6PlusDF, ensembleF6PlusDFQStatistic = ensembleF6PlusQStatisticDF, closestToMetricMeanDF = closestToMetricMeanDF)
# diverseEnsembleQStatisticDF <- calcularQStatisticEnsemble(ensembleDF = diverseEnsembleDF)
# (deprecated) saveEnsembleDatasetAsARFFAndCSV(ensembleDF = ensembleDF, arquivoSemExtensao = 'ensemble')
# saveEnsembleDatasetAsARFFAndCSV(ensembleDF = diverseEnsembleDF, arquivoSemExtensao = 'diverseEnsemble')

# obter amostra estratificada por evento e salvar em arquivo ARFF
# estratificarPorEventoConvertendoParaArff('timeSeries/datasets/ts_aceleracaoLinearTerra.csv', 'timeSeries/datasets/ts_aceleracaoLinearTerra_estrat.arff', numeroAmostrasPorEvento = 1)

# carregar os resultados dos classificadores executados via Java (weka) (antigo)
#resultadosClassificadores <- carregarTodosResultadosClassificadores('../apm/results')

#resultadosClassificadores <- list('todasCoordenadas' = carregarTodosResultadosClassificadores('../apm/results2/todasCoordenadas'), 'coordenadasDispositivo' = carregarTodosResultadosClassificadores('../apm/results2/coordenadasDispositivo'), 'coordenadasTerra' = carregarTodosResultadosClassificadores('../apm/results2/coordenadasTerra'))

# gerar gráfico boxplot com os resultados
#plotResultados(resultados = resultadosClassificadores, pngFileName = 'resultados_coletas2_3.png')

#plotResultados(resultados = resultadosClassificadores$coordenadasDispositivo, pngFileName = 'resultados_coletas2_3_coordenadasDispositivo.png', numeroSeries = 5, idioma = 'en')
#plotResultados(resultados = resultadosClassificadores$coordenadasTerra, pngFileName = 'resultados_coletas2_3_coordenadasTerra.png', numeroSeries = 5, idioma = 'en')
#plotResultados(resultados = resultadosClassificadores$todasCoordenadas, pngFileName = 'resultados_coletas2_3_todasCoordenadas.png', numeroSeries = 5, idioma = 'en')
