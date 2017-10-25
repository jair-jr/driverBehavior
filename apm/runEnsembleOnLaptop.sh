#!/bin/bash
java -Xmx4G -jar "apm-0.0.1-SNAPSHOT.jar" ensemble.arff resultados 1> apm.out 2> apm.err 4 &