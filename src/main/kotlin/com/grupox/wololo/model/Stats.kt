package com.grupox.wololo.model

class Stats(var gamesWon: Int, var gamesLost: Int) {

    fun increaseGamesWon(){
        this.gamesWon = this.gamesWon + 1
    }
    fun increaseGamesLost(){
        this.gamesLost = this.gamesLost + 1
    }

}