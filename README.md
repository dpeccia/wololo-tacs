# Wololo - Backend :crossed_swords:
[![Build Status](https://travis-ci.com/Tp-Tacs/TP-TACS.svg?token=4w7szGidZvKbpas7n4iA&branch=master)](https://travis-ci.com/Tp-Tacs/TP-TACS) [![codecov](https://codecov.io/gh/Tp-Tacs/TP-TACS/branch/master/graph/badge.svg)](https://codecov.io/gh/Tp-Tacs/TP-TACS) [![BCH compliance](https://bettercodehub.com/edge/badge/Tp-Tacs/TP-TACS?branch=master)](https://bettercodehub.com/)

Backend del Trabajo Práctico Grupal de la materia cuatrimestral _Tecnologías Avanzadas en la Construcción de Software_ (electiva de 4to año de Ingeniería en Sistemas de Información en la UTN)

Tecnologías usadas: Kotlin, JUnit, Mockito, Spring Boot, Docker, MongoDB, AWS EC2, AWS Fargate, Travis CI, REST, Swagger, Jacoco, Jackson, Git

## Sobre el Trabajo Práctico en sí

**Wololo** se trata de un juego por turnos, su lógica se centra en 2 o más jugadores que luchan por tener el control de una provincia de Argentina. La partida se gana cuando se toma control de todos los municipios de una provincia o un jugador se rinde.
Nos repartimos entre el grupo mitad al Frontend (React) y mitad al Backend (Kotlin). Yo participé en el Backend. Además, creamos un servidor intermedio en NodeJS en el que mediante WebSockets, todos los usuarios podían ver cualquier actualización de una partida en tiempo real sin tener que refrescar la página 

El mapa y su información la obtenemos del [siguiente GeoJson](/src/main/resources/departamentos-argentina.json). La información de las coordenadas de cada municipio la obtenemos de la [API de GeoRef](https://apis.datos.gob.ar) y la elevación de la [API de TopoData](https://www.opentopodata.org/)

## Que se puede hacer en Wololo?
* **Log In, Sign Up y Log Out:** la aplicación permite login con Google y sistema propio
* **Crear una partida:** implica seleccionar en qué provincia se quiere jugar, con qué jugadores, con cuántos municipios y el modo de dificultad (Easy/Normal/Hard)
* **Ver el listado de Partidas**: se puede ver un listado de las partidas en las que estás participando y filtar por estado (OnGoing/Finished/Canceled) y por fecha de creación
* **Jugar una partida:** cuando es tu turno de jugar, podés *atacar* de un municipio a otro que sea limítrofe de otro jugador, *mover gauchos* entre municipios tuyos (también limítrofes), setear la especialización del municipio en *Producción o Defensa*, ver el estado de cada municipio, rendirte y pasar de turno
* **Admin:** si se accede como administrador al sistema, se pueden ver estadísticas de las partidas, de usuarios y un scoreboard. Usuario Administrador: admin@wololo.com (mail), 12345678 (password)

## Como ejecutarlo
```
$ docker-compose build && docker-compose up -d
```

## Documentación API Rest - Backend
[http://localhost:8080/swagger-ui.html#/](http://localhost:8080/swagger-ui.html#/)

![](/screenshots/Swagger.PNG)

## Screenshots Wololo (Frontend + Servidor intermedio + Backend)

![](/screenshots/Listado.PNG)

![](/screenshots/Mendoza.PNG)

![](/screenshots/Game.PNG)

![](/screenshots/Game2.PNG)

![](/screenshots/Game3.PNG)

![](/screenshots/Game4.PNG)

![](/screenshots/NewGame.PNG)
