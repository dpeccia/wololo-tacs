# TP-TACS
Tp de la materia cuatrimestral Tecnicas Avanzadas en la Construccion de Software

## Como correr con Docker

```
$ docker build -t wololo . && docker run -p 8080:8080 wololo
Opcional pasarle como ultimo parametro el -d para detach
$ docker build -t wololo . && docker run -p 8080:8080 -d wololo
```

## Correr docker-compose
```
$ docker-compose up -d
```

## API

[http://localhost:8080/swagger-ui.html#/](http://localhost:8080/swagger-ui.html#/)
