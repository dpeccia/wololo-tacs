# TP-TACS
[![Build Status](https://travis-ci.com/Tp-Tacs/TP-TACS.svg?token=4w7szGidZvKbpas7n4iA&branch=master)](https://travis-ci.com/Tp-Tacs/TP-TACS) [![codecov](https://codecov.io/gh/Tp-Tacs/TP-TACS/branch/master/graph/badge.svg)](https://codecov.io/gh/Tp-Tacs/TP-TACS) [![BCH compliance](https://bettercodehub.com/edge/badge/Tp-Tacs/TP-TACS?branch=master)](https://bettercodehub.com/)

Tp de la materia cuatrimestral Tecnicas Avanzadas en la Construccion de Software

## Como correr con Docker

```
$ docker build -t wololo . && docker run -p 8080:8080 wololo
Opcional pasarle como ultimo parametro el -d para detach
$ docker build -t wololo . && docker run -p 8080:8080 -d wololo
```

## Correr docker-compose
```
$ docker-compose build && docker-compose up -d
```

## API

[http://localhost:8080/swagger-ui.html#/](http://localhost:8080/swagger-ui.html#/)
