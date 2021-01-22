# Driver Behavior machine learning evaluation

This repository contains the source-code for my masters dissertation. It contains code for data collection, preprocessing and machine learning algorithm evaluation in the driver behavior domain.

## Projects description
* The *AndroidStudioProjects* folder contains the Android app which collects sensor data;
* The *apm_R* folder contains R code for sensor data preprocessing and graphics generation;
* The *apm* folder contains Java code that performs an evaluation of several machine learning algorithms using Weka and LIBSVM;

## Raw data set preprocessing
The first step to execute the evaluation is to use the R programming language (version 3.3.3 or above) to pre-process raw sensor data sets. Preprocessing functions are located in [apm.R](apm_R/apm.R) and [apmExtracao.R](apm_R/apmExtracao.R) files. Below is a basic example of preprocessing code for the data sets in the [data directory](https://github.com/jair-jr/driverBehaviorDataset):

```R
# Load required libraries
library('caret')
library('foreign')
library('parallel')
library('plyr')
library('stringr')

# Load the code
source('apm.R')
source('apmExtracao.R')

# Load trip data. We have four trips.
trip16 <- carregarTodosDadosSmartphone("data/16")
trip17 <- carregarTodosDadosSmartphone("data/17")
trip20 <- carregarTodosDadosSmartphone("data/20")
trip21 <- carregarTodosDadosSmartphone("data/21")

# Pre-process the data sets.
processedDataSets <- gerarDatasetsProcessados(viagens = list(trip16, trip17, trip20, trip21), 
                                              tempoQuadroEmSegundos = 1, 
                                              numerosQuadrosNaJanela = c(4,5,6,7,8), 
                                              dir = 'data/processedDataSets', 
                                              percentualDivisaoTreinoTeste = NA)
```

The above code will generate an output directory named 'data/processedDataSets' which contains the processed data sets. There will be a data set file for each combination of (i) data set type (attribute vector or time series); (ii) sensor; and (iii) number of frames in the sliding window (4 to 8, in the example). The generated files are in ARFF (Weka) and CSV formats. Some examples of the generated files are `st_acelerometro_nq7.arff`, `va_aceleracaoLinearTerra_nq8.csv`, etc. The `processedDataSets` variable is a R list that holds the data sets as data frame objects.

Please, note that the entire process may take some time to complete. The following R libraries need to be installed and functional in your R environment: `caret`, `foreign`, `parallel`, `plyr` and `stringr`.
