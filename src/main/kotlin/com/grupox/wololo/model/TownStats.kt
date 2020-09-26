package com.grupox.wololo.model

class TownStats(var gauchosGeneratedByDefense: Int, var gauchosGeneratedByProduction: Int) {

    fun increaseGauchosGeneratedByDefense(gauchosAmount : Int){
        this.gauchosGeneratedByDefense = this.gauchosGeneratedByDefense + gauchosAmount
    }
    fun increaseGauchosGeneratedByProduction(gauchosAmount : Int){
        this.gauchosGeneratedByProduction = this.gauchosGeneratedByProduction + gauchosAmount
    }
}