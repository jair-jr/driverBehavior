criarPlotter <- function(df, groundTruth = NA,
                         taxaAmostragem = as.integer(taxaAmostragemEmHz(df$elapsedUptimeNanos)),
                         abrirPNG){
    
    nAmostras_ <- nrow(df)
    
    tiposLinhas_ <- c("blank", "solid", "dashed", "dotted", "dotdash", "longdash", "twodash")
    caracteresPlotagem_ <- 0:18
    
    larguraLinha_ <- 2
    
    #valorOriginalMediaMedianaMoveis
    vommm_ <- list(colunas = c('Valor Original', 'Média Móvel', 'Mediana Móvel'),
                   cores = c('#000000', '#0033cc', '#009900', '#f5740f'),
                   tiposLinhas = tiposLinhas_[2:5],
                   caracteresPlotagem = caracteresPlotagem_[1:4])
    
    #MaxMinMediaMediana
    mmmm_ <- list(colunas = c('Máximo', 'Mínimo', 'Média', 'Mediana'),
                  cores = c('#0033cc', '#cc0000', '#009900', '#f5740f'),
                  tiposLinhas = rep(tiposLinhas_[2], 4),
                  caracteresPlotagem = caracteresPlotagem_[1:4])

    plotMediaMedianaMoveisXYZ <- function(titulo, xyzJuntos){
        if(xyzJuntos){
            plotVariosGraficosVariasLegendas(titulo, 
                                             subtitulos = c('(x, y, z)',
                                                            '(x, y, z (médias móveis))',
                                                            '(x, y, z (medianas móveis))'),
                                             sufixoArquivoPNG = 'xyzJuntos',
                                             matrizCampos = matrix(c('x', 'y', 'z',
                                                                     'mediaMovelX', 'mediaMovelY', 'mediaMovelZ',
                                                                     'medianaMovelX', 'medianaMovelY', 'medianaMovelZ'),
                                                                   ncol = 3, byrow = T),
                                             matrizLegendas = matrix(c("x (original)", "y (original)", "z (original)",
                                                                       "x (média móvel)", "y (média móvel)", "z (média móvel)",
                                                                       "x (mediana móvel)", "y (mediana móvel)", "z (mediana móvel)"),
                                                                     ncol = 3, byrow = T),
                                             ylabs = rep(NA, 3),
                                             cores = vommm_$cores,
                                             tiposLinhas = rep(tiposLinhas_[2], 3),
                                             caracteresPlotagem = vommm_$caracteresPlotagem)
        }
        else{
            plotVariosGraficosVariasLegendas(titulo = titulo, 
                                             subtitulos = '(x, y, z)',
                                             sufixoArquivoPNG = 'xyzSeparados',
                                             matrizCampos = matrix(c('x', 'mediaMovelX', 'medianaMovelX',
                                                                     'y', 'mediaMovelY', 'medianaMovelY',
                                                                     'z', 'mediaMovelZ', 'medianaMovelZ'), 
                                                                   ncol = 3, 
                                                                   byrow = T),
                                             matrizLegendas = matrix(vommm_$colunas,
                                                                     ncol = 3, byrow = T),
                                             cores = vommm_$cores,
                                             tiposLinhas = vommm_$tiposLinhas,
                                             caracteresPlotagem = vommm_$caracteresPlotagem)
        }
    }
    
    plotMediaLowPassXYZ <- function(titulo, xyzJuntos){
      plotVariosGraficosVariasLegendas(titulo, 
                                       subtitulos = c('(x, y, z (low pass))',
                                                      '(x, y, z (originais)'),
                                       sufixoArquivoPNG = 'xyzLowPass',
                                       matrizCampos = matrix(c('lowPassX', 'lowPassY', 'lowPassZ',
                                                               'x', 'y', 'z'),
                                                             ncol = 3, byrow = T),
                                       matrizLegendas = matrix(c("x (low pass)", "y (low pass)", "z (low pass)",
                                                                 "x (original)", "y (original)", "z (original)"),
                                                               ncol = 3, byrow = T),
                                       cores = vommm_$cores,
                                       tiposLinhas = rep(tiposLinhas_[2], 3),
                                       caracteresPlotagem = vommm_$caracteresPlotagem,
                                       ylabs = rep(NA, 3))
      
    }
    
    plotMediaMedianaMoveisComponentesXYZEscalar <- function(titulo, xyzJuntos){
        if(xyzJuntos){
            plotVariosGraficosVariasLegendas(titulo, 
                                             subtitulos = c('(componentes x, y, z, escalar)',
                                                            '(componentes x, y, z, escalar (médias móveis))',
                                                            '(componentes x, y, z, escalar (medianas móveis))'),
                                             sufixoArquivoPNG = 'componentesXYZEscalarJuntos',
                                             matrizCampos = matrix(c('componenteX', 'componenteY', 'componenteZ', 'componenteEscalar',
                                                                     'mediaMovelComponenteX', 'mediaMovelComponenteY', 'mediaMovelComponenteZ', 'mediaMovelComponenteEscalar',
                                                                     'medianaMovelComponenteX', 'medianaMovelComponenteY', 'medianaMovelComponenteZ', 'medianaMovelComponenteEscalar'),
                                                                   ncol = 4, byrow = T),
                                             matrizLegendas = matrix(c("Componente x (original)", "Componente y (original)", "Componente z (original)", "Componente escalar (original)",
                                                                       "Componente x (média móvel)", "Componente y (média móvel)", "Componente z (média móvel)", "Componente escalar (média móvel)",
                                                                       "Componente x (mediana móvel)", "Componente y (mediana móvel)", "Componente z (mediana móvel)", "Componente escalar (mediana móvel)"),
                                                                     ncol = 4, byrow = T),
                                             cores = vommm_$cores,
                                             tiposLinhas = rep(tiposLinhas_[2], 4),
                                             caracteresPlotagem = vommm_$caracteresPlotagem,
                                             ylabs = rep(NA, 3))
        }
        else{
            plotVariosGraficosVariasLegendas(titulo, 
                                             subtitulos = '(componentes x, y, z, escalar)',
                                             sufixoArquivoPNG = 'componentesXYZEscalarSeparados',
                                             matrizCampos = matrix(c('componenteX', 'mediaMovelComponenteX', 'medianaMovelComponenteX',
                                                                     'componenteY', 'mediaMovelComponenteY', 'medianaMovelComponenteY',
                                                                     'componenteZ', 'mediaMovelComponenteZ', 'medianaMovelComponenteZ',
                                                                     'componenteEscalar', 'mediaMovelComponenteEscalar', 'medianaMovelComponenteEscalar'), 
                                                                   ncol = 3, 
                                                                   byrow = T),
                                             matrizLegendas = matrix(vommm_$colunas,
                                                                     ncol = 3, byrow = T),
                                             cores = vommm_$cores,
                                             tiposLinhas = vommm_$tiposLinhas,
                                             caracteresPlotagem = vommm_$caracteresPlotagem)
        }
    }
    
    plotMediaMedianaMoveisDxDyDz <- function(titulo){
        plotVariosGraficosVariasLegendas(titulo, 
                                         subtitulos = '(dx, dy, dz, magnitude)',
                                         sufixoArquivoPNG = 'dxdydzmag',
                                         matrizCampos = matrix(c('dx', 'mediaMovelDx', 'medianaMovelDx',
                                                                 'dy', 'mediaMovelDy', 'medianaMovelDy',
                                                                 'dz', 'mediaMovelDz', 'medianaMovelDz',
                                                                 'magnitude', 'mediaMovelMagnitude', 'medianaMovelMagnitude'),
                                                               ncol = 3, byrow = T),
                                         matrizLegendas = matrix(vommm_$colunas,
                                                                 ncol = 3, byrow = T),
                                         cores = vommm_$cores,
                                         tiposLinhas = vommm_$tiposLinhas,
                                         caracteresPlotagem = vommm_$caracteresPlotagem,
                                         funcLayout = function(){layout(matrix(1:5, ncol=1), heights = c(5, 5, 5, 5, 1))})
        
    }
    
    plotMaxMinMediaMedianaXYZSeparados <- function(titulo){
        plotVariosGraficosVariasLegendas(titulo, 
                                         subtitulos = c('(x, y, z)'),
                                         sufixoArquivoPNG = 'xyzSeparados',
                                         matrizCampos = matrix(c('x_max', 'x_min', 'x_media', 'x_mediana',
                                                                 'y_max', 'y_min', 'y_media', 'y_mediana',
                                                                 'z_max', 'z_min', 'z_media', 'z_mediana'),
                                                               ncol = 4, byrow = T),
                                         matrizLegendas = matrix(mmmm_$colunas,
                                                                 ncol = 4, byrow = T),
                                         ylabs = c('x', 'y', 'z'),
                                         downsampled = T,
                                         cores = mmmm_$cores,
                                         tiposLinhas = mmmm_$tiposLinhas,
                                         caracteresPlotagem = mmmm_$caracteresPlotagem)
        
    }
    
    plotMaxMinMediaMedianaComponentesXYZEscalarSeparados <- function(titulo){
        plotVariosGraficosVariasLegendas(titulo, 
                                         subtitulos = c('(componentes x, y, z, escalar)'),
                                         sufixoArquivoPNG = 'componentesXYZEscalarSeparados',
                                         matrizCampos = matrix(c('componenteX_max', 'componenteX_min', 'componenteX_media', 'componenteX_mediana',
                                                                 'componenteY_max', 'componenteY_min', 'componenteY_media', 'componenteY_mediana',
                                                                 'componenteZ_max', 'componenteZ_min', 'componenteZ_media', 'componenteZ_mediana',
                                                                 'componenteEscalar_max', 'componenteEscalar_min', 'componenteEscalar_media', 'componenteEscalar_mediana'),
                                                               ncol = 4, byrow = T),
                                         matrizLegendas = matrix(mmmm_$colunas,
                                                                 ncol = 4, byrow = T),
                                         ylabs = c('componente x', 'componente y', 'componente z', 'componente escalar'),
                                         downsampled = T,
                                         cores = mmmm_$cores,
                                         tiposLinhas = mmmm_$tiposLinhas,
                                         caracteresPlotagem = mmmm_$caracteresPlotagem)
    }
    
    plotMediaMedianaComponentesXYZEscalarJuntos <- function(titulo){
        plotVariosGraficosVariasLegendas(titulo, 
                                         subtitulos = c('(componentes x, y, z, escalar (média))',
                                                        '(componentes x, y, z, escalar (mediana))'),
                                         sufixoArquivoPNG = 'componentesXYZEscalarJuntos',
                                         matrizCampos = matrix(c('componenteX_media', 'componenteY_media', 'componenteZ_media', 'componenteEscalar_media',
                                                                 'componenteX_mediana', 'componenteY_mediana', 'componenteZ_mediana', 'componenteEscalar_mediana'),
                                                               ncol = 4, byrow = T),
                                         matrizLegendas = matrix(c("componente x (média)", "componente y (média)", "componente z (média)", 'componente escalar (média)',
                                                                   "componente x (mediana)", "componente y (mediana)", "componente z (mediana)", 'componente escalar (mediana)'),
                                                                 ncol = 4, byrow = T),
                                         ylabs = rep(NA, 4),
                                         downsampled = T,
                                         cores = mmmm_$cores,
                                         tiposLinhas = rep(tiposLinhas_[2], 4),
                                         caracteresPlotagem = mmmm_$caracteresPlotagem)
    }

    plotMediaMedianaXYZJuntos <- function(titulo){
        plotVariosGraficosVariasLegendas(titulo, 
                                         subtitulos = c('(x, y, z (média))',
                                                        '(x, y, z (mediana))'),
                                         sufixoArquivoPNG = 'xyzJuntos',
                                         matrizCampos = matrix(c('x_media', 'y_media', 'z_media',
                                                                 'x_mediana', 'y_mediana', 'z_mediana'),
                                                               ncol = 3, byrow = T),
                                         matrizLegendas = matrix(c("x (média)", "y (média)", "z (média)",
                                                                   "x (mediana)", "y (mediana)", "z (mediana)"),
                                                                 ncol = 3, byrow = T),
                                         ylabs = rep(NA, 2),
                                         downsampled = T,
                                         cores = mmmm_$cores[1:3],
                                         tiposLinhas = rep(tiposLinhas_[2], 3),
                                         caracteresPlotagem = mmmm_$caracteresPlotagem[1:3])
        
    }
    
    plotMaxMinMediaMedianaDxDyDzMagnitude <- function(titulo){
        plotVariosGraficosVariasLegendas(titulo, 
                                         subtitulos = '(dx, dy, dz, magnitude)',
                                         sufixoArquivoPNG = 'dxdydzmag',
                                         matrizCampos = matrix(c('dx_max', 'dx_min', 'dx_media', 'dx_mediana',
                                                                 'dy_max', 'dy_min', 'dy_media', 'dy_mediana',
                                                                 'dz_max', 'dz_min', 'dz_media', 'dz_mediana',
                                                                 'magnitude_max', 'magnitude_min', 'magnitude_media', 'magnitude_mediana'),
                                                               ncol = 4, byrow = T),
                                         matrizLegendas = matrix(mmmm_$colunas,
                                                                 ncol = 4, byrow = T),
                                         ylabs = c('dx', 'dy', 'dz', 'magnitude'),
                                         downsampled = T,
                                         cores = mmmm_$cores,
                                         tiposLinhas = mmmm_$tiposLinhas,
                                         caracteresPlotagem = mmmm_$caracteresPlotagem,
                                         funcLayout = function(){layout(matrix(1:5, ncol=1), heights = c(5, 5, 5, 5, 1))})
        
    }
    
    plotGPS <- function(titulo = "GPS"){
        plotVariosGraficosVariasLegendas(titulo, 
                                         subtitulos = '(velocidade, aceleracao, acuracia, bearing e altitude)',
                                         sufixoArquivoPNG = 'velocidadeAceleracaoAcuraciaBearingAltitude',
                                         matrizCampos = matrix(c('velocidade', 'mediaMovelVelocidade', 'medianaMovelVelocidade',
                                                                 'aceleracao', 'mediaMovelAceleracao', 'medianaMovelAceleracao',
                                                                 'acuracia', 'mediaMovelAcuracia', 'medianaMovelAcuracia',
                                                                 'bearing', 'mediaMovelBearing', 'medianaMovelBearing',
                                                                 'altitude', 'mediaMovelAltitude', 'medianaMovelAltitude'),
                                                               ncol = 3, byrow = T),
                                         matrizLegendas = matrix(c('Valor Original', 'Média Móvel', 'Mediana Móvel'),
                                                                 ncol = 3, byrow = T),
                                         cores = c('#000000', '#0033cc', '#009900'),
                                         tiposLinhas = tiposLinhas_[2:4],
                                         caracteresPlotagem = caracteresPlotagem_[1:3],
                                         funcLayout = function() {layout(matrix(1:6, ncol=1), heights = c(5, 5, 5, 5, 5, 1))})
        
    }
    
    plotVariosGraficosVariasLegendas <- function(titulo, subtitulos = '', sufixoArquivoPNG, matrizCampos, 
                                                 funcLayout,
                                                 matrizLegendas, cores, tiposLinhas, caracteresPlotagem,
                                                 ylabs = matrizCampos[, 1],
                                                 downsampled = F) {
        nLinhas <- nrow(matrizCampos)
        nColunas <- ncol(matrizCampos)
        nSubtitulos <- length(subtitulos)
        
        nLinhasMatrizLegendas <- nrow(matrizLegendas)
        nColunasMatrizLegendas <- ncol(matrizLegendas)

        arquivoPNG <- paste(sep = '', titulo, '_', sufixoArquivoPNG, '_', taxaAmostragem, 'hz.png')
        
        arquivoPNGCompleto <- abrirPNG(nAmostras_, arquivoPNG, nLinhas)
        
        umaLegendaPorGrafico <- nLinhasMatrizLegendas == nLinhas
        
        if(!missing(funcLayout)){
            funcLayout();    
        }
        else if(umaLegendaPorGrafico){
            layout(matrix(1:(2 * nLinhas), ncol=1), heights = rep(c(10, 1), nLinhas))   
        }
        else{
            layout(matrix(1:(nLinhas + 1), ncol=1), heights = c(rep(6, nLinhas), 1))
        }
        
        margemAnterior <- NA
        for(i in 1:nLinhas){
            if(i <= nSubtitulos){
                tituloCompleto <- paste(titulo, subtitulos[i], 
                                        ifelse(downsampled,'[downsample para', '[taxa de amostragem de'), taxaAmostragem, 'Hz]')
            }
            else{
                tituloCompleto <- NA
            }
            
            par(margemAnterior)
            plotarGraficoSensor(df, matrizCampos[i, ], 
                                cores = cores, tiposLinhas = tiposLinhas, 
                                titulo = tituloCompleto,
                                ylab = ylabs[i])
            
            if(umaLegendaPorGrafico || i == nLinhas){
                margemAnterior <- par(mar=c(0, 0, 0, 0))
                plot.new()
                legend('center', y = NULL, legend = matrizLegendas[ifelse(umaLegendaPorGrafico, i, 1), ], 
                       col = cores, lty = tiposLinhas, 
                       pch = caracteresPlotagem,
                       lwd = rep(larguraLinha_, nColunasMatrizLegendas), ncol = nColunasMatrizLegendas)
            }
        }
        dev.off()
        
        arquivoPNGCompleto
    }
    
    plotarGraficoSensor <- function(df, colunas, cores, tiposLinhas, titulo = NA, ylab = colunas[1]){
        tempoSegundos <- nanosParaSegundos(df$elapsedUptimeNanos)
        plot(range(tempoSegundos), 
             multipleColumnsRange(df = df, colunas = colunas, na.rm=T), 
             type = 'n', 
             xlab = 'Tempo (segundos)', 
             ylab = ylab, 
             main = titulo)
        grid(col = 'black')
        
        for(i in seq(colunas)){
            coluna <- colunas[i]
            cor <- cores[i]
            
            #lines(df$elapsedUptimeNanos, df[,coluna], type = 'l', col = cor)
            
            #pch são códigos de símbolos de pontos no gráfico [0,18] 
            lines(tempoSegundos, df[,colunas[i]], type = 'b', 
                  col = cores[i], pch = caracteresPlotagem_[i], 
                  lty= tiposLinhas[i], lwd = larguraLinha_)
        }
        
        if(!is.na(groundTruth) && !is.null(groundTruth)){
            coordenadas <- par('usr')
            margemTextoEventoY <- abs(coordenadas[4] - coordenadas[3]) / 100
            cor <- 'maroon4'
            
            abline(v = groundTruth$inicio, lty = tiposLinhas_[4], col = cor, lwd = larguraLinha_)
            text(x = (groundTruth$inicio + groundTruth$fim) / 2, 
                 y = coordenadas[3] + 2.5 * margemTextoEventoY, 
                 labels = groundTruth$evento, 
                 cex = 1.5,
                 col = cor)
            abline(v = groundTruth$fim, lty = tiposLinhas_[4], col = cor, lwd = larguraLinha_)
            ySeta <- coordenadas[3] + 1.5 * margemTextoEventoY
            arrows(x0 = groundTruth$inicio,
                   y0 = ySeta,
                   x1 = groundTruth$fim,
                   y1 = ySeta,
                   lty = tiposLinhas_[4],
                   code = 3,
                   angle = 20,
                   length = 0.2,
                   col = cor,
                   lwd = larguraLinha_)
        }
    }
    
    list('plotMediaMedianaMoveisXYZ' = plotMediaMedianaMoveisXYZ,
         'plotMediaMedianaMoveisDxDyDz' = plotMediaMedianaMoveisDxDyDz,
         'plotMaxMinMediaMedianaXYZSeparados' = plotMaxMinMediaMedianaXYZSeparados,
         'plotMediaMedianaXYZJuntos' = plotMediaMedianaXYZJuntos,
         'plotMaxMinMediaMedianaDxDyDzMagnitude' = plotMaxMinMediaMedianaDxDyDzMagnitude,
         'plotGPS' = plotGPS,
         'plotMediaMedianaMoveisComponentesXYZEscalar' = plotMediaMedianaMoveisComponentesXYZEscalar,
         'plotMaxMinMediaMedianaComponentesXYZEscalarSeparados' = plotMaxMinMediaMedianaComponentesXYZEscalarSeparados,
         'plotMediaMedianaComponentesXYZEscalarJuntos' = plotMediaMedianaComponentesXYZEscalarJuntos,
         'plotMediaLowPassXYZ' = plotMediaLowPassXYZ
    )
}

plotResultados <- function(resultados, pngFileName = 'resultados.png', numeroSeries = 3, idioma = 'pt'){
    png(height = 5600, 
        width = 5600,
        res = 600,
        filename = pngFileName)

    intervaloSeries <- 1:min(numeroSeries, ncol(df))
    
    eventosNome <- eventoNome(names(resultados), abreviar = F, idioma = idioma)
    eventosOrdenados <- levels(eventoFactor())
    
    layoutMatrix <- matrix(c(1:length(eventosNome)), ncol = 1, byrow = T)
    layout(layoutMatrix, heights = c(15, rep(10, nrow(layoutMatrix) - 1)))
    
    for(i in seq_along(eventosOrdenados)){
        evento <- eventosOrdenados[i]
        nomeEvento <- eventosNome[evento]
        df <- resultados[[evento]]
        if(is.null(df)){
          next
        }
        if(i == 1){
            #par(mar = c(0.3, 3, 5, 7.2))
            par(mar = c(0.3, 3, 5, 10))
        }
        else{
            #par(mar = c(0.3, 3, 0, 7.2))
            par(mar = c(0.3, 3, 0, 10))
        }
        
        boxplot(df[, intervaloSeries],
                ylim = c(min(df[, intervaloSeries]), 1), horizontal = T, col = 'gray', 
                xaxt = 'n', yaxt = 'n',
                ann = F)
        #dotchart(df[, intervaloSeries],
        #        ylim = c(.96, 1)
        #)
        
        if(i == 1){
            axis(3, cex.axis = 1.2)
        }
        axis(4, at = intervaloSeries, labels = names(df)[intervaloSeries], 
             las = 2, cex.axis = 1.2)
        
        mtext(nomeEvento, side = 2, cex = 0.9, line = 0.3, font = 2)
        if(i == 1){
            mtext('Area Under the ROC Curve', side = 3, line = 3, cex = 0.9, font = 2)    
        }
    }
    dev.off()
    return(NA)
}