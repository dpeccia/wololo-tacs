package com.grupox.wololo.model

class Stats(gamesWon: Int, gamesLost: Int) {
    var gamesWon: Int = gamesWon
        private set
    var gamesLost: Int = gamesLost
        private set

    fun increaseGamesWon(){
        this.gamesWon = this.gamesWon + 1
    }
    fun increaseGamesLost(){
        this.gamesLost = this.gamesLost + 1
    }

}