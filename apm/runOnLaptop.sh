#!/bin/bash
java -Xmx4G -jar "apm-0.0.1-SNAPSHOT.jar" ../apm_R/dados/datasetsSincronizadosProcessadosMediaMovel/datasets resultados 1> apm.out 2> apm.err 4 &