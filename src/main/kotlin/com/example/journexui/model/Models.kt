package com.example.journexui.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(val username:String,val nickname:String,val email:String,val password:String)
data class LoginRequest(val username:String,val password:String)
data class UpdateProfileDto(val nickname:String?=null,val profileImageUrl:String?=null,val phoneNumber:String?=null)
data class ChangePasswordDto(val oldPassword:String,val newPassword:String)
data class UserDto(val id:Long?=null,val profileImageUrl:String?=null,val nickname:String="",val username:String="",val email:String="",val phoneNumber:String?=null,val enabled:Boolean=false,val createdAt:String?=null,val updatedAt:String?=null,val lastLoginAt:String?=null,val subscriptionExpireAt:String?=null,val strategyIds:List<Long> = emptyList(),val subscriptionPlan:String?=null,val subscriptionStatus:String?=null,val role:String?=null)

data class Pagination(val page:Int=0,val size:Int=20,val sortBy:String="id",val direction:String="DESC")
data class PageMeta(val totalPages:Int=0,val totalElements:Long=0,val size:Int=0,val number:Int=0,val numberOfElements:Int=0,val last:Boolean=false,val first:Boolean=false,val empty:Boolean=true)
data class ApiPage<T>(val totalPages:Int=0,val totalElements:Long=0,val size:Int=0,val number:Int=0,val numberOfElements:Int=0,val last:Boolean=false,val first:Boolean=false,val empty:Boolean=true,val content:List<T> = emptyList())

data class StrategyDto(val id:Long?=null,val address:String="",val name:String="",val description:String?=null,val userId:Long?=null,val checklistIds:List<Long> = emptyList(),val tradeType:String?=null,val tradeMarketType:String?=null,val tradeTimeframe:String?=null,val risk:Long?=null,val reward:Long?=null,val riskPercent:Int?=null,val publicStrategy:Boolean=false,val createdAt:String?=null,val updatedAt:String?=null,val deleted:Boolean=false,val deletedAt:String?=null)

data class ChecklistDto(val id:Long?=null,val name:String="",val description:String?=null,val scope:String="PRE_TRADE",val checklistCategory:String="ENTRY_SETUP",val strategyIds:List<Long> = emptyList(),val userId:Long?=null,val itemIds:List<Long> = emptyList(),val publicChecklist:Boolean=false,val active:Boolean=true,val createdAt:String?=null,val updatedAt:String?=null)
data class ChecklistItemDto(val id:Long?=null,val value:String="",val type:String="QUESTION_BOOLEAN",val required:Boolean=false,val orderIndex:Long=0,val checklistId:Long?=null)
data class ChecklistItemAnswerDto(val itemId:Long,val answerValue:String)

data class TradeOpenRequestDto(val description:String?=null,val strategyId:Long?=null,val tradeMarketType:String,val tradeTimeframe:String,val tradeType:String,val tradePositionSide:String?=null,val symbol:String,val lotSize:Double,val leverage:Int?=null,val entryPrice:Double,val entryTime:String,val stopLoss:Double?=null,val takeProfit:Double?=null,val riskPercent:Double?=null,val balanceBeforeTrade:Double?=null,val emotionBefore:String?=null,val tags:List<String> = emptyList())
data class TradeCloseRequestDto(val exitPrice:Double,val exitTime:String,val commission:Double?=null,val swap:Double?=null,val balanceAfterTrade:Double?=null,val emotionAfter:String?=null)
data class TradeRiskUpdateDto(val stopLoss:Double?=null,val takeProfit:Double?=null)
data class TradeJournalUpdateDto(val description:String?=null,val emotionBefore:String?=null,val emotionAfter:String?=null,val tags:List<String> = emptyList())
data class TradeDto(val id:Long?=null,val description:String?=null,val strategyId:Long?=null,val tradeMarketType:String?=null,val tradeTimeframe:String?=null,val tradeType:String?=null,val tradePositionSide:String?=null,val userId:Long?=null,val symbol:String="",val lotSize:Double=0.0,val leverage:Int?=null,val entryPrice:Double=0.0,val exitPrice:Double?=null,val entryTime:String?=null,val exitTime:String?=null,val stopLoss:Double?=null,val takeProfit:Double?=null,val profitLoss:Double?=null,val profitLossPercent:Double?=null,val riskRewardRatio:Double?=null,val commission:Double?=null,val swap:Double?=null,val status:String?=null,val riskPercent:Double?=null,val balanceBeforeTrade:Double?=null,val balanceAfterTrade:Double?=null,val emotionBefore:String?=null,val emotionAfter:String?=null,val tags:List<String> = emptyList(),val createdAt:String?=null,val updatedAt:String?=null)

val tradeTypes=listOf("SCALPING","DAY_TRADING","SWING","POSITION","INVESTING","ALGORITHMIC","BOT","MULTI","OTHER")
val marketTypes=listOf("FOREX","CRYPTO","STOCKS","FUTURES","OPTIONS","MULTI","OTHER")
val timeframes=listOf("S1","S5","S10","S15","S30","M1","M2","M3","M5","M10","M15","M30","M45","H1","H2","H3","H4","H6","H8","H12","D1","W1","MN1","MN3","MN6","MN12")
val tradeStatuses=listOf("OPEN","CLOSED","PENDING","CANCELLED")
val positionSides=listOf("BUY","SELL","LONG","SHORT")
val checklistScopes=listOf("STRATEGY","PRE_TRADE","ENTRY","EXIT","POST_TRADE")
val checklistCategories=listOf("TREND","ENTRY_SETUP","EXIT_SETUP","RISK","PSYCHOLOGY","NEWS","VOLUME","STRUCTURE")
val checklistItemTypes=listOf("QUESTION_BOOLEAN","QUESTION_TEXT","ANSWER_BOOLEAN","ANSWER_TEXT")
