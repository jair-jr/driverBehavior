require('ggplot2')
require('ggrepel')

require('plyr')
require('dplyr')
require('reshape2')

source('apm.R')

# Funções principais

# Plota os dados de sensores com demarcação de ground truth para todos os eventos de uma viagem
# plotarDadosSensoresGroundTruthParaPDF(v16, novaTaxaAmostragemHz = 20, arquivoPDF = 'graficos/sensores_viagem16.pdf')

# Plota todos os resultados das execuções dos algoritmos com métricas gerais e por classe
# plotarTodosResultadosParaPDF(resultadosGerais = resultadosGerais, resultadosPorClasse = resultadosPorClasse, top = 5, prefixoArquivoPDF = 'graficos/resultados')

# Plota um histograma das durações dos eventos
# plotarDuracaoEventosGroundTruthParaPDF(groundTruth = groundTruth, arquivoPDF = 'graficos/duracaoEventos.pdf')

# Plota um gráfico de dispersão com os 'top' melhores para cada métrica
# plotarGraficoDispersao(resultadosGerais = rPlusDiverseEnsembleGeraisDF, campoMetricaX = camposMetricas()[1, ], campoMetricaY = camposMetricas()[2, ], top = 10)

# plotarGraficosDispersao(resultadosGerais = rPlusDiverseEnsembleGeraisDF, top = 20)

# Armazenar em png os gráficos gerados por qualquer função
# plotarParaPNG('graficos/geral_%d.png', plotarResultadosTodasMetricas, resultadosGerais = rGerais, top = 5)
# plotarParaPNG('graficos/geralEnsemble_%d.png', plotarResultadosTodasMetricas, resultadosGerais = rPlusDiverseEnsembleGeraisDF, top = 4)
# plotarParaPNG('graficos/dispersaoEnsemble_%d.png', plotarGraficosDispersao, resultadosGerais = rPlusDiverseEnsembleGeraisDF, top = 20)

plotarParaPNG <- function(ARQUIVO_PNG, PLOT_FUNC, ...){
    dimensoes <- dimensoesPlot()
    png(filename = ARQUIVO_PNG, width = dimensoes['width'] * dimensoes['resolution'], height = dimensoes['height'] * dimensoes['resolution'], res = dimensoes['resolution'])
    print(PLOT_FUNC(...))
    dev.off()
}

adicionarTemposModelosDePrimeiroNivelAosEnsembles <- function(resultadosDF){
  filtroApenasEnsemble <- getEnsembleClassifiersFilter(resultadosDF = resultadosDF)
  if(sum(filtroApenasEnsemble) > 0){
    meanTimeByClassifierDF <- aggregate(instanceClassificationTimeInMillisAverage ~ classifier,
                                        data = resultadosDF[resultadosDF$classifier != 'IBK_DTW' & !filtroApenasEnsemble, ],
                                        FUN = mean)
    # O ensemble é composto de 21 SVMs, 7 MLP, 6 RF e 4 BN
    timeToAddToEnsembles <- 21 * meanTimeByClassifierDF$instanceClassificationTimeInMillisAverage[meanTimeByClassifierDF$classifier == 'SVM'] +
      9 * meanTimeByClassifierDF$instanceClassificationTimeInMillisAverage[meanTimeByClassifierDF$classifier == 'MULTILAYER_PERCEPTRON'] +
      6 * meanTimeByClassifierDF$instanceClassificationTimeInMillisAverage[meanTimeByClassifierDF$classifier == 'RANDOM_FOREST'] +
      4 * meanTimeByClassifierDF$instanceClassificationTimeInMillisAverage[meanTimeByClassifierDF$classifier == 'BAYES_NET']
    
    # adicionar o tempo adicional aos ensembles
    resultadosDF$instanceClassificationTimeInMillisAverage[filtroApenasEnsemble] <- resultadosDF$instanceClassificationTimeInMillisAverage[filtroApenasEnsemble] + timeToAddToEnsembles
  }
  
  resultadosDF
}

plotarGraficosDispersao <- function(resultadosGerais, top){
  metricasDF <- camposMetricas(incluirTempoClassificacao = T)
  
  print(plotarGraficoDispersao(resultadosGerais = resultadosGerais,
                               campoMetricaX = metricasDF[metricasDF$campo == 'weightedAreaUnderROC', ],
                               campoMetricaY = metricasDF[metricasDF$campo == 'correctInstancesRate', ],
                               maxXScale = 1,
                               maxYScale = 1,
                               top = top))

  resultadosGerais <- adicionarTemposModelosDePrimeiroNivelAosEnsembles(resultadosDF = resultadosGerais)

  print(plotarGraficoDispersao(resultadosGerais = resultadosGerais,
                               campoMetricaX = metricasDF[metricasDF$campo == 'instanceClassificationTimeInMillisAverage', ],
                               campoMetricaY = metricasDF[metricasDF$campo == 'weightedAreaUnderROC', ],
                               top = top,
                               textoAdicionalEixoX = '(milissegundos)',
                               incluirMelhoresCampoMetricaX = F))

  print(plotarGraficoDispersao(resultadosGerais = resultadosGerais,
                               campoMetricaX = metricasDF[metricasDF$campo == 'instanceClassificationTimeInMillisAverage', ],
                               campoMetricaY = metricasDF[metricasDF$campo == 'correctInstancesRate', ],
                               top = top,
                               textoAdicionalEixoX = '(milissegundos)',
                               incluirMelhoresCampoMetricaX = F))
}

plotarGraficoDispersao <- function(resultadosGerais, 
                                   campoMetricaX, 
                                   campoMetricaY, 
                                   top,
                                   textoAdicionalEixoX = '',
                                   textoAdicionalEixoY = '',
                                   maxXScale = NA,
                                   maxYScale = NA,
                                   incluirMelhoresCampoMetricaX = T, 
                                   incluirMelhoresCampoMetricaY = T){
    
  x <- as.character(campoMetricaX$campo)
  y <- as.character(campoMetricaY$campo)
  
  detalheX <- ifelse(campoMetricaX$maiorMelhor, "(maior é melhor)", "(menor é melhor)")
  detalheY <- ifelse(campoMetricaY$maiorMelhor, "(maior é melhor)", "(menor é melhor)")
  
  resultadosGerais$classifier <- nomesClassificadorParaFactor(resultadosGerais$classifier)

  aggregateDF <- aggregate.data.frame(x = list(x = resultadosGerais[, x], y = resultadosGerais[, y]), 
                                      by = list(identifier = resultadosGerais$identifier, 
                                                classifier = resultadosGerais$classifier,
                                                ensemble = is.na(resultadosGerais$sensor)),
                                      FUN = mean)
  
  aggregateDF$classifierEnsemble <- paste(sep = '', aggregateDF$classifier, ifelse(aggregateDF$ensemble, ' (ensemble)', ''))

  getMelhores <- function(campoAggregate, campoMetrica, incluirMelhores){
    if(incluirMelhores){
      order(aggregateDF[, campoAggregate] * ifelse(test = campoMetrica$maiorMelhor, 1, -1), decreasing = T)[1:min(top, nrow(aggregateDF))]  
    }
    else{
      c()
    }
  }
  
  melhoresX <- getMelhores(campoAggregate = 'x', campoMetrica = campoMetricaX, incluirMelhores = incluirMelhoresCampoMetricaX)
  melhoresY <- getMelhores(campoAggregate = 'y', campoMetrica = campoMetricaY, incluirMelhores = incluirMelhoresCampoMetricaY)
  
  melhoresAggregateDF <- aggregateDF[unique(c(melhoresX, melhoresY)), ]
  
  melhoresAggregateDF$classifierEnsemble <- factor(melhoresAggregateDF$classifierEnsemble,
                                                   levels = sort(unique(melhoresAggregateDF$classifierEnsemble)),
                                                   ordered = T)
  
  textSize <- 16
  
  plot <- ggplot(melhoresAggregateDF, aes(x = x, y = y, colour = classifierEnsemble)) + 
    geom_point(size = 3) +
    geom_label_repel(
      aes(label = identifier),
      size = 5,
      show.legend = F,
      fontface = 'bold', # color = 'white',
      box.padding = unit(0.35, "lines"),
      point.padding = unit(0.5, "lines"),
      segment.color = 'grey20'
    ) +
    xlab(paste(campoMetricaX$titulo, textoAdicionalEixoX, detalheX)) +
    ylab(paste(campoMetricaY$titulo, textoAdicionalEixoY, detalheY)) +
    theme_minimal() +
    theme(legend.position = 'top',
          text = element_text(size = textSize),
          axis.title.x = element_text(margin = margin(0.5, 0, 0, 0, unit = 'cm')),
          axis.title.y = element_text(margin = margin(0, 0.5, 0, 0, unit = 'cm')),
          axis.text = element_text(size = textSize),
          plot.title = element_text(face = 'bold', hjust = 0.5, margin = margin(0, 0, 1, 0, unit = 'cm'))) +
    ggtitle(paste(sep = '', 'Melhores ', top, ' modelos (', campoMetricaX$titulo, ' x ', campoMetricaY$titulo, ')')) +
    scale_color_discrete(name = 'AAM') +
    scale_shape_discrete(name = 'AAM')
  
  if(!is.na(maxXScale)){
    plot <- plot + scale_x_continuous(limits = c(min(melhoresAggregateDF$x), maxXScale))
  }
  if(!is.na(maxYScale)){
    plot <- plot + scale_y_continuous(limits = c(min(melhoresAggregateDF$y), maxYScale))
  }
  plot
}

plotarResultadosPorClassificador <- function(resultadosResumidos, campoMetrica, top, ensembleClassifiersPresent = F){
    # detalheY <- ifelse(campoMetrica$maiorMelhor, "(higher is better)", "(lower is better)")
    detalheY <- ifelse(campoMetrica$maiorMelhor, "(maior é melhor)", "(menor é melhor)")
    # titulo <- paste("Best", length(levels(resultadosResumidos$identifier)), "Assemblies -", campoMetrica$titulo, detalheY)
    titulo <- paste(sep = '', "Performance Geral dos Modelos (", top, " melhores por AAM) - ", campoMetrica$titulo, ' ', detalheY)
    
    scale <- max(resultadosResumidos[, as.character(campoMetrica$campo)]) - min(resultadosResumidos[, as.character(campoMetrica$campo)])
    
    textSize <- 16
    meanTextSize = ifelse(ensembleClassifiersPresent, 3.5, 4)
    
    mediasDF <- aggregate.data.frame(x = list(mediaMetrica = resultadosResumidos[, as.character(campoMetrica$campo)]), by = list(identifier = resultadosResumidos$identifier, classifier = resultadosResumidos$classifier), FUN = mean)
    
    plot <- ggplot(data = resultadosResumidos, aes_string(x = 'identifier', y = as.character(campoMetrica$campo), fill = 'classifier')) + 
        geom_boxplot() + 
        stat_summary(fun.y = mean, colour = "darkred", geom = "point", shape = 18, size = 3, show.legend = F) +
        geom_label_repel(
            aes(y = mediaMetrica, label = format(x = round(..y.., digits = 5), nsmall = 5, decimal.mark = ',', trim = T)),
            data = mediasDF,
            size = 6,
            fill = 'white',
            color = 'black',
            show.legend = F,
            fontface = 'bold',
            box.padding = unit(0.35, "lines"),
            point.padding = unit(0.5, "lines"),
            segment.color = 'grey20'
        ) +
        ylab(paste(campoMetrica$titulo, detalheY)) +
        xlab("Modelos") +
        ggtitle(titulo) + 
        theme_minimal() +
        theme(legend.position='top') +
        theme(text = element_text(size = textSize),
              axis.title.x = element_text(margin = margin(0.5, 0, 0, 0, unit = 'cm')),
              axis.title.y = element_text(margin = margin(0, 0.5, 0, 0, unit = 'cm')),
              axis.text = element_text(size = textSize),
              plot.title = element_text(face = 'bold', hjust = 0.5, margin = margin(0, 0, 1, 0, unit = 'cm'))) +
        guides(fill = guide_legend(title = "AAM")) +
        theme(axis.text.x= element_text(angle = 90, hjust = 1, vjust = .6))
    
    if(ensembleClassifiersPresent){
      require('RColorBrewer')
      classifiersForPairedColorsFill <- levels(resultadosResumidos$classifier)
      classifiersForPairedColorsFill <- sort(classifiersForPairedColorsFill[classifiersForPairedColorsFill != 'IBK_DTW'])
      classifiersForPairedColorsFill <- c(classifiersForPairedColorsFill, 'IBK_DTW')
      
      pairedColorsFill <- brewer.pal(n = length(classifiersForPairedColorsFill), name = 'Paired')
      names(pairedColorsFill) <- classifiersForPairedColorsFill
      
      plot <- plot + scale_fill_manual(values = pairedColorsFill)
    }
    
    plot
}

plotarTemposClassificacaoEnsemble <- function(resultadosGeraisEnsembleDF, identificadores = c('ensemble_RF4', 'nq8_Mag_IBK_DTW2', 'nq8_Gir_IBK_DTW1', 'ensemble_SVM13')){
    resultadosGeraisEnsembleDF <- adicionarTemposModelosDePrimeiroNivelAosEnsembles(resultadosDF = resultadosGeraisEnsembleDF)
    resultadosGeraisEnsembleDF$classifier <- nomesClassificadorParaFactor(resultadosGeraisEnsembleDF$classifier)
    resultadosGeraisEnsembleDF <- renameEnsembleClassifiersIfNeeded(resultadosDF = resultadosGeraisEnsembleDF)
    
    resultadosGeraisEnsembleDF$identifier <- combinacoesPara(resultadosGeraisEnsembleDF$identifier, idioma = 'pt')
    resultadosGeraisEnsembleDF <- resultadosGeraisEnsembleDF[resultadosGeraisEnsembleDF$identifier %in% identificadores, ]
    
    tempoAgregadoDF <- aggregate(formula = instanceClassificationTimeInMillisAverage ~ identifier, data = resultadosGeraisEnsembleDF, FUN = mean)
    niveisOrdenados <- as.character(tempoAgregadoDF$identifier[order(tempoAgregadoDF$instanceClassificationTimeInMillisAverage)])
    resultadosGeraisEnsembleDF$identifier <- factor(as.character(resultadosGeraisEnsembleDF$identifier), levels = niveisOrdenados, ordered = T)
    
    textSize <- 16
    
    ggplot(resultadosGeraisEnsembleDF, aes(x = identifier, y = instanceClassificationTimeInMillisAverage, fill = classifier)) +
        geom_boxplot() + 
        stat_summary(fun.y = mean, colour = "darkred", geom = "point", shape = 18, size = 3, show.legend = F) +
        geom_label_repel(
            aes(y = instanceClassificationTimeInMillisAverage, label = format(x = round(..y.., digits = 2), nsmall = 2, decimal.mark = ',', trim = T)),
            data = tempoAgregadoDF,
            size = 5,
            fill = 'white',
            color = 'black',
            show.legend = F,
            fontface = 'bold',
            box.padding = unit(0.35, "lines"),
            point.padding = unit(0.5, "lines"),
            segment.color = 'grey20'
        ) +
        ylab("Tempo médio de classificação de uma instância (milissegundos) (menor é melhor)") +
        xlab("Modelos") +
        ggtitle("Tempo de classificação (modelos ensemble vs. convencionais)") +
        theme_minimal() +
        guides(fill = guide_legend(title = "AAM")) +
        theme(text = element_text(size = textSize),
            legend.position = 'top',
            axis.title.x = element_text(margin = margin(0.5, 0, 0, 0, unit = 'cm')),
            axis.title.y = element_text(margin = margin(0, 0.5, 0, 0, unit = 'cm')),
            axis.text = element_text(size = textSize),
            plot.title = element_text(face = 'bold', hjust = 0.5, margin = margin(0, 0, 1, 0, unit = 'cm')))
    
}

filtroMaioresNumeroInstanciasTestePorClassificadorENumberoQuadros <- function(resultadosGerais){
  # obter apenas os maiores valores de número de instâncias de teste por classificador e número de quadros
  classifiersAndNumberOfFramesDF <- unique(resultadosGerais[, c('classifier', 'numberOfFrames')])
  numbersOfInstances <- unique(mapply(classifier = classifiersAndNumberOfFramesDF$classifier, 
                                      numberOfFrames = classifiersAndNumberOfFramesDF$numberOfFrames, 
                                      FUN = function(classifier, numberOfFrames) max(resultadosGerais$numberOfInstances[resultadosGerais$classifier == classifier & resultadosGerais$numberOfFrames == numberOfFrames])))
  
  resultadosGerais$numberOfInstances %in% numbersOfInstances
}

plotarTemposClassificacaoPorNumeroInstanciasTreino <- function(resultadosGerais){
    resultadosGerais$classifier <- nomesClassificadorParaFactor(resultadosGerais$classifier)
    resultadosGerais$sensor <- nomesSensoresParaFactor(nomesSensores = resultadosGerais$sensor, idioma = 'pt')
    
    resultadosGerais$classifier <- as.character(resultadosGerais$classifier)
    seriesClassificadores <- c(naoDTW = 'BN, RF, MLP, SVM', 
                               dtwUmEixo = 'IBK_DTW (um eixo; todos os sensores)', 
                               dtwDoisEixos = 'IBK_DTW (dois eixos; Mag.)', 
                               dtwTresEixos = 'IBK_DTW (três eixos; AclLin., Acel. e Gir.)')
    resultadosGerais$classifier[resultadosGerais$classifier == 'IBK_DTW' & resultadosGerais$coordinate %in% c('X', 'Y', 'Z')] <- seriesClassificadores['dtwUmEixo']
    dtwComTodosOsEixosFiltro <- resultadosGerais$classifier == 'IBK_DTW' & resultadosGerais$coordinate == 'Todos'
    resultadosGerais$classifier[dtwComTodosOsEixosFiltro & resultadosGerais$sensor == 'Magnetômetro'] <- seriesClassificadores['dtwDoisEixos']
    resultadosGerais$classifier[dtwComTodosOsEixosFiltro & resultadosGerais$sensor != 'Magnetômetro'] <- seriesClassificadores['dtwTresEixos']
    resultadosGerais$classifier[resultadosGerais$classifier %in% c('BN', 'RF', 'MLP', 'SVM')] <- seriesClassificadores['naoDTW']
    
    resultadosGerais$classifier <- factor(resultadosGerais$classifier, levels = seriesClassificadores)
    
    resultadosGerais <- resultadosGerais[filtroMaioresNumeroInstanciasTestePorClassificadorENumberoQuadros(resultadosGerais = resultadosGerais), ]
    
    ag <- do.call(data.frame, aggregate(formula = instanceClassificationTimeInMillisAverage ~ classifier + numberOfInstances,
                                        data = resultadosGerais, 
                                        FUN = function(x) c(mean = mean(x), sd = sd(x))))
    
    #numberOfInstances é o número de instâncias de teste. Devido ao 10-fold cross-validation, multiplicar por 9 para obter o número de instâncias de treino aproximado
    ag$numberOfInstances <- ag$numberOfInstances * 9
    
    textSize <- 16
    
    ggplot(ag, aes(x = numberOfInstances, y = instanceClassificationTimeInMillisAverage.mean, colour = classifier)) +
        geom_line(size = 1.5) +
        geom_errorbar(aes(ymin = instanceClassificationTimeInMillisAverage.mean - instanceClassificationTimeInMillisAverage.sd, 
                          ymax = instanceClassificationTimeInMillisAverage.mean + instanceClassificationTimeInMillisAverage.sd), 
                      width = 5,
                      size = 1.5) +
        geom_point(size = 2.5, shape = 21, fill = "white") +
        geom_label_repel(
          aes(label = format(x = round(instanceClassificationTimeInMillisAverage.mean, digits = 2), 
                             nsmall = 2, 
                             trim = T,
                             decimal.mark = ',')),
          size = 6,
          show.legend = F,
          fontface = 'bold',
          box.padding = unit(0.35, "lines"),
          point.padding = unit(0.5, "lines"),
          segment.color = 'grey20'
        ) +
        scale_color_brewer(type = 'qual', palette = 'Set1') + 
        # facet_grid(~ sensor) +
        ylab("Tempo médio de classificação de uma instância (milissegundos) (menor é melhor)") +
        xlab("Número de instâncias de treino") +
        ggtitle("Tempo médio de classificação de uma instância") + 
        theme_minimal() + 
        guides(colour = guide_legend(title = "AAM")) +
        theme(text = element_text(size = textSize),
              legend.position = 'top',
              strip.text = element_text(size = textSize),
              axis.title.x = element_text(margin = margin(0.5, 0, 0, 0, unit = 'cm')),
              axis.title.y = element_text(margin = margin(0, 0.5, 0, 0, unit = 'cm')),
              axis.text = element_text(size = textSize),
              plot.title = element_text(face = 'bold', hjust = 0.5, margin = margin(0, 0, 1, 0, unit = 'cm')))
}

plotarResultadosPorClasseTodasMetricas <- function(resultadosPorClasse, top, observacaoTitulo = NA){
    require('grid')
    require('gridExtra')
    require('stringi')
    
    resultadosPorClasse$identifier <- combinacoesPara(resultadosPorClasse$identifier, idioma = 'pt')
    resultadosPorClasse$class <- tornarEventoFactorOrdinal(resultadosPorClasse$class)
    resultadosPorClasse$class <- eventoNomeFactor(eventosFactor = resultadosPorClasse$class, idioma = 'pt')
    
    if(!is.na(observacaoTitulo)){
      observacaoTitulo = paste(sep = '', '(', observacaoTitulo, ')')
    }
    else{
      observacaoTitulo = ''
    }
    
    #Split data set into pieces
    resultadosSplitPorClasse <- split(resultadosPorClasse,resultadosPorClasse$class)
    
    textSize <- 16
    
    camposMetricasClasseD <- camposMetricasClasse()
    for(i in 1:nrow(camposMetricasClasseD)){
        campoMetricaClasse <- camposMetricasClasseD[i, ]
        metricaChar <- as.character(campoMetricaClasse$campo)
        
        maiorMelhor <- ifelse(campoMetricaClasse$maiorMelhor, 1, -1)
        
        #...and reorder the identifier variable of each one
        df_list <- lapply(resultadosSplitPorClasse, function(x){
            x$identifier <- droplevels(x$identifier)
            adf <- aggregate.data.frame(x = list(metrica = x[, metricaChar]),
                                        by = list(identifier = x$identifier, class = x$class),
                                        FUN = mean)
            
            #reordena o fator pela média de 'metrica'
            adf$identifier <- reorder(adf$identifier, maiorMelhor * adf$metrica, FUN = mean, order = T)
            
            #níveis do fator reordenado adf$identifier são usados para ordenar o fator x$identifier
            x$identifier <- factor(x$identifier, levels = levels(adf$identifier), ordered = T)
            
            #filtra pelos 'top' de acordo com o fator x$identifier
            x[x$identifier %in% tail(levels(x$identifier), top), c('identifier', 'class', metricaChar)]
        })
        
        # adicionar espaços em branco a esquerda dos identifiers 
        maxIdentifiersLength <- unlist(lapply(df_list, function(x){ 
          max(nchar(as.character(x$identifier))) 
        })) 
        maxIdentifierLength <- max(maxIdentifiersLength) 
        df_list <- lapply(df_list, function(x){ 
          levels(x$identifier) <- stri_pad_left(levels(x$identifier), width = maxIdentifierLength, pad = ' ')  
          x 
        }) 
        
        # calcular o menor e maior valor do eixo
        minMax <- unlist(lapply(df_list, function(x){
            c(min = min(x[, metricaChar]), max = max(x[, metricaChar]))
        }))
        limits <- c(min(minMax), max(minMax))
        
        # configurar o plot
        plot <- ggplot(data = resultadosPorClasse, aes_string(y = metricaChar, x = 'identifier')) +
            geom_boxplot() +
            stat_summary(fun.y = mean, colour = "darkred", geom = "point", shape = 18, size = 3, show.legend = F) +
            stat_summary(aes(label = format(x = round(..y.., digits = 5), nsmall = 5, decimal.mark = ',', trim = T)),
                         fun.y = mean,
                         position = position_nudge(x = 0.43),
                         colour = "black",
                         geom = "text",
                         show.legend = F) +
            scale_y_continuous(limits = limits) +
            facet_grid(~ class) +
            theme_gray() + 
            theme(text = element_text(size = textSize),
                  axis.text.y = element_text(family = 'mono', face = 'bold', size = textSize),
                  strip.text = element_text(size = textSize),
                  axis.title.y = element_text(size = 0),
                  axis.title.x = element_text(size = 0),
                  plot.margin = unit(c(0, 0.5, 0, 0), unit = "cm")) +
            coord_flip()
        
        # criar um novo plot a partir do original substituindo os dados por 'dfl'
        p_list <- lapply(df_list, function(dfl){
            plot %+% 
              dfl
        })
        
        # o primeiro plot precisa de uma margem superior maior por causa do título
        p_list[[1]] <- p_list[[1]] +
            theme(plot.margin = unit(c(0.5, 0.5, 0, 0), unit = "cm"))
        
        # remover o eixo Y de todos os plots, menos do último
        for(i in 1:(length(p_list) - 1)){
            p_list[[i]] <- p_list[[i]] +
                theme(axis.title.x=element_blank(),
                      axis.text.x=element_blank(),
                      axis.ticks.x=element_blank())
        }
        
        # titulo <- paste("Best", top, "Assemblies -", 
        #                 campoMetricaClasse$titulo, 
        #                 'per Event', 
        #                 ifelse(campoMetricaClasse$maiorMelhor, "(higher is better)", "(lower is better)"))
        
        titulo <- paste("Melhores", top, "Modelos", observacaoTitulo, "-", 
                        campoMetricaClasse$titulo, 
                        'por Evento', 
                        ifelse(campoMetricaClasse$maiorMelhor, "(maior é melhor)", "(menor é melhor)"))

        p_list$ncol <- 1
        p_list$top <- textGrob(titulo, gp = gpar(fontsize = textSize, fontface = 'bold'))
        
        do.call("grid.arrange", p_list)
    }
}

plotarResultados <- function(resultadosResumidos, campoMetrica){
    # detalheY <- ifelse(campoMetrica$maiorMelhor, "(higher is better)", "(lower is better)")
    detalheY <- ifelse(campoMetrica$maiorMelhor, "(maior é melhor)", "(menor é melhor)")
    # titulo <- paste("Best", length(levels(resultadosResumidos$identifier)), "Assemblies -", campoMetrica$titulo, detalheY)
    titulo <- paste("Melhores", length(levels(resultadosResumidos$identifier)), "Modelos -", campoMetrica$titulo, detalheY)
    
    textSize <- 16
    
    ggplot(data = resultadosResumidos, aes_string(x = 'identifier', y = as.character(campoMetrica$campo), fill = 'classifier')) + 
        geom_boxplot() + 
        stat_summary(fun.y = mean, colour = "darkred", geom = "point", shape = 18, size = 3, show.legend = F) +
        ylab(paste(campoMetrica$titulo, detalheY)) +
        # xlab("Assemblies") +
        xlab("Modelos") +
        ggtitle(titulo) + 
        theme_minimal() +
        guides(fill = guide_legend(title = "AAM")) +
        theme(text = element_text(size = textSize),
              legend.position='top',
              axis.title.x = element_text(margin = margin(0.5, 0, 0, 0, unit = 'cm')),
              axis.title.y = element_text(margin = margin(0, 0.5, 0, 0, unit = 'cm')),
              axis.text = element_text(size = textSize),
              plot.title = element_text(face = 'bold', hjust = 0.5, margin = margin(0, 0, 1, 0, unit = 'cm')))
}

getEnsembleClassifiersFilter <- function(resultadosDF){
  is.na(resultadosDF$sensor)
}

renameEnsembleClassifiersIfNeeded <- function(resultadosDF, ensembleClassifiersFilter = getEnsembleClassifiersFilter(resultadosDF = resultadosDF)){
  if(sum(ensembleClassifiersFilter) > 0){
    resultadosDF$classifier <- as.character(resultadosDF$classifier)
    resultadosDF$classifier[ensembleClassifiersFilter] <- paste(sep = '', resultadosDF$classifier[ensembleClassifiersFilter], ' (ensemble)')
    resultadosDF$classifier <- factor(resultadosDF$classifier)
  }
  
  resultadosDF    
}

plotarResultadosTodasMetricas <- function(resultadosGerais, top){
    camposMetricasD <- camposMetricas()
    
    resultadosGerais$identifier <- combinacoesPara(resultadosGerais$identifier, idioma = 'pt')
    
    resultadosGerais$classifier <- nomesClassificadorParaFactor(resultadosGerais$classifier)
    
    filtroEnsemble <- getEnsembleClassifiersFilter(resultadosDF = resultadosGerais)
    ensembleClassifiersPresent <- sum(filtroEnsemble) > 0
    resultadosGerais <- renameEnsembleClassifiersIfNeeded(resultadosDF = resultadosGerais, ensembleClassifiersFilter = filtroEnsemble)

    for(i in 1:nrow(camposMetricasD)){
        campoMetrica <- camposMetricasD[i, ]
        maiorMelhor <- ifelse(campoMetrica$maiorMelhor, 1, -1)
        campoMetricaChar <- as.character(campoMetrica$campo)

        df <- resultadosGerais[, c('identifier', 'classifier', campoMetricaChar)]
        df$identifier <- reorder(x = df$identifier, X = maiorMelhor * df[, campoMetricaChar], FUN = mean, order = T)
        df <- df[df$identifier %in% tail(levels(df$identifier), n = top), ]
        df$identifier <- droplevels(df$identifier)
        df$identifier <- factor(df$identifier, levels = rev(levels(df$identifier)), ordered = T)
        print(plotarResultados(resultadosResumidos = df, campoMetrica = campoMetrica))
        
        dfPorClassificador <- melhoresPorClassificador(resultadosGeraisDF = resultadosGerais, campoMetrica = campoMetrica, top = top)
        
        print(plotarResultadosPorClassificador(dfPorClassificador, campoMetrica = campoMetrica, top = top, ensembleClassifiersPresent = ensembleClassifiersPresent))
    }
}

dimensoesPlot <- function(){
    c(width = 21, height = 14, resolution = 300)
}

# plota os resultados novos em gráficos gerais e específicos por classe
plotarTodosResultadosParaPDF <- function(resultadosGerais, resultadosPorClasse, top = 5, prefixoArquivoPDF, pdf = F){
    dimensoes <- dimensoesPlot()
    width <- dimensoes['width']
    height <- dimensoes['height']
    resolution <- dimensoes['resolution']
    
    allEnsemble <- isAllEnsemble(resultadosGerais)
    
    pdfOuPng <- function(sufixoArquivoPDF, sufixoArquivoPNG){
      if(pdf){
        pdf(file = paste(prefixoArquivoPDF, sufixoArquivoPDF, sep = '_'), width = width, height = height)
      }
      else{
        png(filename = paste(prefixoArquivoPDF, sufixoArquivoPNG, sep = '_'), width = width * resolution, height = height * resolution, res = resolution)  
      }
    }
    
    pdfOuPng('geral.pdf', 'geral_%d.png')
    plotarResultadosTodasMetricas(resultadosGerais = resultadosGerais, top = top)
    dev.off()

    pdfOuPng('porClasse.pdf', 'porClasse_%d.png')
    plotarResultadosPorClasseTodasMetricas(resultadosPorClasse = resultadosPorClasse, top = top)
    dev.off()
    
    pdfOuPng('dispersao.pdf', 'dispersao_%d.png')
    plotarGraficosDispersao(resultadosGerais = resultadosGerais, top = 20)
    dev.off()

    if(!allEnsemble){
      pdfOuPng('porClasseSemDTW.pdf', 'porClasseSemDTW_%d.png')
      plotarResultadosPorClasseTodasMetricas(resultadosPorClasse = resultadosPorClasse[resultadosPorClasse$classifier != 'IBK_DTW', ], top = top, observacaoTitulo = 'sem IBk DTW')
      dev.off()

      pdfOuPng('tempoClassificacaoPorNumeroInstanciasTreino.pdf', 'tempoClassificacaoPorNumeroInstanciasTreino.png')
      print(plotarTemposClassificacaoPorNumeroInstanciasTreino(resultadosGerais = resultadosGerais))
      dev.off()
    }
}

plotarDuracaoEventosGroundTruthParaPDF <- function(groundTruth, arquivoPDF){
    plotarParaPDF(arquivoPDF = arquivoPDF, funcao = plotarDuracaoEventosGroundTruth, groundTruth = groundTruth)
}

# plota um histograma das durações dos eventos por tipo de evento
plotarDuracaoEventosGroundTruth <- function(groundTruth){
    groundTruth$evento <- tornarEventoFactorOrdinal(groundTruth$evento)
    groundTruth$evento <- eventoNomeFactor(eventosFactor = groundTruth$evento, idioma = 'pt')
    
    deltaSumarizado <- ddply(groundTruth, "evento", summarize, delta.media = mean(delta))
    
    p <- ggplot(groundTruth, aes(x = delta)) + 
        geom_histogram(binwidth = .1, colour="black", fill="white") + 
        xlab("Event duration (seconds)") +
        scale_x_continuous(
            breaks = function(limits){
                seq(from = limits[1], to = limits[2], by = 0.2)
            }) + 
        facet_wrap(~ evento, ncol = 1) +
        ylab("Event count") +
        geom_vline(data = deltaSumarizado, aes(xintercept=delta.media),
                   linetype = "dashed", size = 1, colour = "red") +
        ggtitle("Event duration histogram") + 
        theme(plot.title = element_text(face = 'bold', hjust = 0.5, margin = margin(0, 0, 0.5, 0, unit = 'cm')),
              axis.title.x = element_text(margin = margin(0.5, 0, 0, 0, unit = 'cm')),
              axis.title.y = element_text(margin = margin(0, 0.5, 0, 0, unit = 'cm')))
    
    print(p)
}

# downsample e dados são "melted" para um dataframe com 'sensor', elapsedUptimeSeconds', 'coordenada' e 'valor'
preprocessarSensor <- function(sensor, nomeSensor, novaTaxaAmostragemHz = 20){
    taxaAmostragemOriginalHz <- taxaAmostragemEmHz(sensor$elapsedUptimeNanos)
    
    if(novaTaxaAmostragemHz >= taxaAmostragemOriginalHz) {
        stop(paste('Taxa de amostragem solicitada (', novaTaxaAmostragemHz,
                   ' Hz) é maior ou igual à original (', taxaAmostragemOriginalHz,
                   ' Hz). Impossível realizar downsample.', sep = ''))
        
    }
    
    sensor$janela <- floor(sensor$elapsedUptimeNanos / segundosParaNanos(novaTaxaAmostragemHz ^ -1))
    sensor$timestamp <- NULL
    
    downsampled <- sensor %>%
        group_by(janela) %>%
        summarise_at(.funs = funs(mean), .vars = vars(x = x, y = y, z = z, elapsedUptimeNanos = elapsedUptimeNanos))
    
    downsampled$elapsedUptimeSeconds <- nanosParaSegundos(downsampled$elapsedUptimeNanos)
    downsampled$elapsedUptimeNanos <- NULL
    
    melted <- melt(downsampled, id.vars = 'elapsedUptimeSeconds', measure.vars = c('x','y','z'), variable.name = 'coordenada', value.name = 'valor')
    
    melted$sensor <- nomeSensor
    
    melted
}

plotarDadosSensorGroundTruth <- function(dadosSensores, groundTruthIndividual, titulo){
    sensorLevels <- unique(dadosSensores$sensor)
    
    anotacaoDF <- data.frame(sensor = factor(sensorLevels),
                     min_y = unlist(lapply(sensorLevels, function(sl) min(dadosSensores$valor[dadosSensores$sensor == sl]))),
                     max_y = unlist(lapply(sensorLevels, function(sl) max(dadosSensores$valor[dadosSensores$sensor == sl]))))
    
    anotacaoDF$inicio <- groundTruthIndividual$inicio
    anotacaoDF$fim <- groundTruthIndividual$fim
    anotacaoDF$evento <- groundTruthIndividual$evento
    anotacaoDF$margin_y <- abs(anotacaoDF$max_y - anotacaoDF$min_y) / 100
    
    ggplot(dadosSensores, aes(x=elapsedUptimeSeconds, y = valor, colour = coordenada)) +
        geom_rect(mapping = aes(xmin = groundTruthIndividual$inicio, 
                                xmax = groundTruthIndividual$fim, 
                                ymin = -Inf, 
                                ymax = Inf),
                  alpha = 0.05,
                  fill = "lightyellow",
                  color = "orange",
                  linetype = "dashed",
                  size = 0.25) +
        
        geom_segment(data = anotacaoDF,
                     mapping = aes(x = inicio, 
                                   y = min_y - margin_y, 
                                   xend = fim, 
                                   yend = min_y - margin_y),
                     arrow = arrow(ends = 'both'),
                     color = "orange",
                     size = 0.5,
                     linetype = "dashed") +

        geom_text(data = anotacaoDF,
                  mapping = aes(x = (inicio + fim) / 2,
                                y = min_y - 2 * margin_y,
                                label= evento,
                                vjust = 'top'),
                  size = 4,
                  color = 'black') +
        
        geom_line() +
        geom_point(size = 1) +
        scale_color_brewer(palette = 'Set1') +
        
        labs(colour = 'Eixos de sensores') +
        theme_bw() +

        facet_wrap(~ sensor, ncol = 1, scales = 'free_y') +
        
        ggtitle(titulo) + 
        theme(legend.position="top",
              legend.background = element_rect(fill = "gray90", size = 1, linetype = "dotted"),
              text = element_text(size = 20),
              axis.text = element_text(size = 20),
              axis.title.y = element_blank(),
              strip.text.x = element_text(size = 20),
              panel.grid.major = element_line(color = "gray75", linetype = 'dashed'),
              panel.grid.minor = element_line(color = "gray95", linetype = 'dashed'),
              plot.title = element_text(face = 'bold', hjust = 0.5, margin = margin(0, 0, 0.5, 0, unit = 'cm'))) + 
        xlab("Tempo de viagem (segundos)")
    
}

plotarDadosSensoresGroundTruthParaPDF <- function(viagem,
                                                  margemSegundos = 1,
                                                  novaTaxaAmostragemHz,
                                                  arquivoPDF){
    plotarParaPDF(arquivoPDF = arquivoPDF,
                  height = 17,
                  width = 18,
                  funcao = plotarDadosSensoresGroundTruth,
                  viagem = viagem,
                  margemSegundos = margemSegundos,
                  novaTaxaAmostragemHz = novaTaxaAmostragemHz)
    
}

plotarDadosSensoresGroundTruth <- function(viagem,
                                           margemSegundos,
                                           novaTaxaAmostragemHz){
    
    viagem$groundTruth$evento <- tornarEventoFactorOrdinal(viagem$groundTruth$evento)
    viagem$groundTruth$evento <- eventoNomeFactor(eventosFactor = viagem$groundTruth$evento, idioma = 'pt')
    
    plots <- lapply(1:nrow(viagem$groundTruth), function(i){
        plotarDadosSensoresGroundTruthIndividual(viagem = viagem,
                                                 margemSegundos = margemSegundos,
                                                 groundTruthIndividual = viagem$groundTruth[i, ],
                                                 novaTaxaAmostragemHz = novaTaxaAmostragemHz)
    })
    print(plots)
}

plotarDadosSensoresGroundTruthIndividual <- function(viagem,
                                                     margemSegundos,
                                                     groundTruthIndividual,
                                                     novaTaxaAmostragemHz){
    
    nomesSensores <- names(viagem)
    nomesSensores <- nomesSensores[nomesSensores != 'groundTruth']
    
    sensoresPreprocessados <- lapply(seq_along(nomesSensores), function(i){
        nomeSensor <- nomesSensores[i]
        
        sensorDF <- viagem[[nomeSensor]]
        
        segundoInicial <- max(nanosParaSegundos(head(sensorDF$elapsedUptimeNanos, n = 1)), groundTruthIndividual$inicio - margemSegundos)
        segundoFinal <- min(nanosParaSegundos(tail(sensorDF$elapsedUptimeNanos, n = 1)), groundTruthIndividual$fim + margemSegundos)

        sensorDF <- subconjunto(sensorDF, segundoInicial = segundoInicial, segundoFinal = segundoFinal)
        preprocessarSensor(sensor = sensorDF, nomeSensor = nomeSensor, novaTaxaAmostragemHz = novaTaxaAmostragemHz)
    })
    
    todosSensoresPreprocessados <- do.call(rbind, sensoresPreprocessados)
    
    todosSensoresPreprocessados$sensor <- nomesSensoresParaFactor(todosSensoresPreprocessados$sensor, idioma = 'pt')
    
    titulo <- paste(sep = "", "Evento '", groundTruthIndividual$evento, "' capturado por sensores (taxa de frequência reduzida para ", novaTaxaAmostragemHz, " Hz)")
    plotarDadosSensorGroundTruth(dadosSensores = todosSensoresPreprocessados,
                                 groundTruthIndividual = groundTruthIndividual,
                                 titulo = titulo)
    
}
