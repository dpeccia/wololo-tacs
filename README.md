# TP-TACS
Tp de la materia cuatrimestral Tecnicas Avanzadas en la Construccion de Software

## Como correr con Docker

```
$ gradle build
$ sudo docker build --build-arg JAR_FILE=build/libs/*.jar -t wololo .
$ sudo docker run -p 8080:8080 wololo
```