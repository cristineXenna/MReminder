package com.example.mreminder.listener

import com.example.mreminder.model.MedicineModel

interface Imedicine {
    fun onLoadSucces(drinkModelList: List<MedicineModel>?)
    fun onLoadFail(message:String?)
}