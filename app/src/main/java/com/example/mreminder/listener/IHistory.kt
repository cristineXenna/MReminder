package com.example.mreminder.listener

import com.example.mreminder.model.HistoryItemModel

interface IHistory {
    fun onLoadSucces(HistoryModelList: List<HistoryItemModel>?)
    fun onLoadFail(message:String?)
}